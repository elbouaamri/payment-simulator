package com.simulator.engine;

import com.simulator.api.dto.TransactionRequest;
import com.simulator.api.dto.TransactionResponse;
import com.simulator.iso.IsoFieldConstants;
import com.simulator.iso.IsoMessageBuilder;
import com.simulator.logging.PanMaskingUtil;
import com.simulator.persistence.entity.SimulationProfile;
import com.simulator.persistence.entity.TransactionLog;
import com.simulator.persistence.repository.SimulationProfileRepository;
import com.simulator.persistence.repository.TransactionLogRepository;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Core transaction processing service.
 * Orchestrates: ISO message build → rules engine → response build → log persist.
 */
@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final IsoMessageBuilder isoBuilder;
    private final RulesEngineService rulesEngine;
    private final VisaLikeService visaLikeService;
    private final SimulationProfileRepository profileRepo;
    private final TransactionLogRepository txLogRepo;

    public TransactionService(IsoMessageBuilder isoBuilder,
                              RulesEngineService rulesEngine,
                              VisaLikeService visaLikeService,
                              SimulationProfileRepository profileRepo,
                              TransactionLogRepository txLogRepo) {
        this.isoBuilder = isoBuilder;
        this.rulesEngine = rulesEngine;
        this.visaLikeService = visaLikeService;
        this.profileRepo = profileRepo;
        this.txLogRepo = txLogRepo;
    }

    /**
     * Process a transaction request end-to-end.
     */
    public TransactionResponse process(TransactionRequest request) throws ISOException, InterruptedException {
        long startTime = System.currentTimeMillis();
        String correlationId = MDC.get("correlationId");
        if (correlationId == null) correlationId = UUID.randomUUID().toString();

        String txType = request.getType().toUpperCase();
        String maskedPan = PanMaskingUtil.mask(request.getPan());
        log.info("Processing {} transaction | PAN={} | amount={} | terminal={}",
                txType, maskedPan, request.getAmount(), request.getTerminalId());

        // 1. Load simulation profile
        SimulationProfile profile = null;
        if (request.getProfileId() != null) {
            profile = profileRepo.findById(request.getProfileId()).orElse(null);
        }
        if (profile == null) {
            profile = profileRepo.findByName("FAST_ACCEPT").orElse(null);
        }

        // 2. Determine processing code based on tx type
        String processingCode = mapProcessingCode(txType);

        // 3. Build ISO request message
        ISOMsg requestMsg;
        if ("REVERSAL".equals(txType)) {
            requestMsg = isoBuilder.buildReversalRequest(
                    request.getPan(), request.getAmount(), request.getExpiry(),
                    request.getTerminalId(), request.getMerchantId(),
                    request.getStan(), request.getRrn(), request.getCurrency(),
                    request.isVisaLike());
        } else {
            requestMsg = isoBuilder.buildAuthorizationRequest(
                    request.getPan(), processingCode, request.getAmount(),
                    request.getExpiry(), request.getTerminalId(), request.getMerchantId(),
                    request.getStan(), request.getRrn(), request.getCurrency(),
                    request.isVisaLike());
        }

        // 3b. If Visa-like, add fake EMV data
        String fakeCryptogram = null;
        if (request.isVisaLike()) {
            fakeCryptogram = visaLikeService.generateFakeCryptogram(request.getPan(), request.getAmount());
            String emvData = visaLikeService.generateFakeEmvData(request.getPan(), request.getAmount());
            requestMsg.set(IsoFieldConstants.EMV_DATA, emvData);
        }

        Map<String, String> reqFields = isoBuilder.extractFields(requestMsg);
        String requestHex = isoBuilder.packToHex(requestMsg);

        // 4. Evaluate rules
        RuleEvaluationResult ruleResult = rulesEngine.evaluate(
                request.getPan(), request.getExpiry(), request.getAmount(),
                request.getTerminalId(), profile);

        // 5. Simulate latency from profile
        if (profile != null && profile.getLatencyMs() > 0) {
            log.debug("Simulating {}ms latency (profile={})", profile.getLatencyMs(), profile.getName());
            Thread.sleep(profile.getLatencyMs());
        }

        // 6. Handle TIMEOUT scenario
        if (ruleResult.getScenario() == Scenario.TIMEOUT) {
            log.warn("TIMEOUT scenario – no response sent");
            long duration = System.currentTimeMillis() - startTime;
            TransactionLog txLog = buildTxLog(request, txType, requestMsg.getMTI(), null,
                    maskedPan, null, ruleResult, reqFields, null, requestHex, null,
                    duration, correlationId);
            txLogRepo.save(txLog);

            TransactionResponse resp = new TransactionResponse();
            resp.setTransactionId(txLog.getId());
            resp.setType(txType);
            resp.setRequestMti(requestMsg.getMTI());
            resp.setScenario("TIMEOUT");
            resp.setScenarioDescription("Timeout – No Response Sent");
            resp.setPanMasked(maskedPan);
            resp.setAmount(request.getAmount());
            resp.setCurrency(request.getCurrency());
            resp.setTerminalId(request.getTerminalId());
            resp.setVisaLike(request.isVisaLike());
            resp.setRequestIsoFields(reqFields);
            resp.setDurationMs(duration);
            resp.setCreatedAt(LocalDateTime.now());
            resp.setCorrelationId(correlationId);
            return resp;
        }

        // 7. Build ISO response message
        ISOMsg responseMsg = isoBuilder.buildResponse(requestMsg, ruleResult.getResponseCode39(), request.isVisaLike());
        Map<String, String> respFields = isoBuilder.extractFields(responseMsg);
        String responseHex = isoBuilder.packToHex(responseMsg);

        long duration = System.currentTimeMillis() - startTime;

        // 8. Persist transaction log
        TransactionLog txLog = buildTxLog(request, txType, requestMsg.getMTI(), responseMsg.getMTI(),
                maskedPan, ruleResult.getResponseCode39(), ruleResult, reqFields, respFields,
                requestHex, responseHex, duration, correlationId);
        txLogRepo.save(txLog);

        log.info("Transaction {} completed | scenario={} | rc39={} | duration={}ms",
                txLog.getId(), ruleResult.getScenario(), ruleResult.getResponseCode39(), duration);

        // 9. Build response DTO
        TransactionResponse resp = new TransactionResponse();
        resp.setTransactionId(txLog.getId());
        resp.setType(txType);
        resp.setRequestMti(requestMsg.getMTI());
        resp.setResponseMti(responseMsg.getMTI());
        resp.setResponseCode39(ruleResult.getResponseCode39());
        resp.setScenario(ruleResult.getScenario().name());
        resp.setScenarioDescription(ruleResult.getScenario().getDescription());
        resp.setPanMasked(maskedPan);
        resp.setAmount(request.getAmount());
        resp.setCurrency(request.getCurrency());
        resp.setStan(reqFields.get("F11"));
        resp.setRrn(reqFields.get("F37"));
        resp.setTerminalId(request.getTerminalId());
        resp.setVisaLike(request.isVisaLike());
        resp.setFakeCryptogram(fakeCryptogram);
        resp.setRequestIsoFields(reqFields);
        resp.setResponseIsoFields(respFields);
        resp.setDurationMs(duration);
        resp.setCreatedAt(txLog.getCreatedAt());
        resp.setCorrelationId(correlationId);

        return resp;
    }

    private String mapProcessingCode(String txType) {
        return switch (txType) {
            case "AUTHORIZE" -> IsoFieldConstants.PC_PURCHASE;
            case "REFUND"    -> IsoFieldConstants.PC_REFUND;
            case "CANCEL"    -> IsoFieldConstants.PC_VOID;
            case "REVERSAL"  -> IsoFieldConstants.PC_PURCHASE;
            default          -> IsoFieldConstants.PC_PURCHASE;
        };
    }

    private TransactionLog buildTxLog(TransactionRequest request, String txType,
                                       String requestMti, String responseMti,
                                       String maskedPan, String responseCode39,
                                       RuleEvaluationResult ruleResult,
                                       Map<String, String> reqFields, Map<String, String> respFields,
                                       String requestHex, String responseHex,
                                       long duration, String correlationId) {
        TransactionLog txLog = new TransactionLog();
        txLog.setType(txType);
        txLog.setVisaLike(request.isVisaLike());
        txLog.setRequestMti(requestMti);
        txLog.setResponseMti(responseMti);
        txLog.setPanMasked(maskedPan);
        txLog.setAmount(request.getAmount());
        txLog.setCurrency(request.getCurrency());
        txLog.setStan(reqFields != null ? reqFields.get("F11") : null);
        txLog.setRrn(reqFields != null ? reqFields.get("F37") : null);
        txLog.setTerminalId(request.getTerminalId());
        txLog.setResponseCode39(responseCode39);
        txLog.setScenario(ruleResult.getScenario().name());
        txLog.setRequestIsoFields(reqFields != null ? reqFields.toString() : null);
        txLog.setResponseIsoFields(respFields != null ? respFields.toString() : null);
        txLog.setRequestIsoRaw(requestHex);
        txLog.setResponseIsoRaw(responseHex);
        txLog.setDurationMs(duration);
        txLog.setCorrelationId(correlationId);
        return txLog;
    }
}
