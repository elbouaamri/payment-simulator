package com.simulator.iso;

import com.simulator.engine.RuleEvaluationResult;
import com.simulator.engine.RulesEngineService;
import com.simulator.engine.Scenario;
import com.simulator.logging.PanMaskingUtil;
import com.simulator.persistence.entity.TransactionLog;
import com.simulator.persistence.repository.TransactionLogRepository;
import org.jpos.iso.*;
import org.jpos.iso.channel.NACChannel;
import org.jpos.iso.packager.GenericPackager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static com.simulator.iso.IsoFieldConstants.*;

/**
 * TCP ISO 8583 Host Server – listens on a configurable port and processes
 * incoming ISO messages using the Rules Engine.
 *
 * <p>
 * This runs as a background thread started via CommandLineRunner.
 * </p>
 */
@Component
public class TcpIsoServer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(TcpIsoServer.class);

    @Value("${simulator.tcp.port:5000}")
    private int tcpPort;

    @Value("${simulator.tcp.timeout-ms:30000}")
    private int timeoutMs;

    private final IsoMessageBuilder isoBuilder;
    private final RulesEngineService rulesEngine;
    private final TransactionLogRepository txLogRepo;

    public TcpIsoServer(IsoMessageBuilder isoBuilder,
            RulesEngineService rulesEngine,
            TransactionLogRepository txLogRepo) {
        this.isoBuilder = isoBuilder;
        this.rulesEngine = rulesEngine;
        this.txLogRepo = txLogRepo;
    }

    @Override
    public void run(String... args) {
        Thread serverThread = new Thread(this::startServer, "tcp-iso-server");
        serverThread.setDaemon(true);
        serverThread.start();
    }

    private void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(tcpPort)) {
            log.info("🔌 ISO 8583 TCP Server started on port {}", tcpPort);
            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSocket = serverSocket.accept();
                log.info("New TCP connection from {}", clientSocket.getRemoteSocketAddress());
                Thread handler = new Thread(() -> handleClient(clientSocket), "tcp-client-" + clientSocket.getPort());
                handler.setDaemon(true);
                handler.start();
            }
        } catch (IOException e) {
            log.error("TCP Server error on port {}: {}", tcpPort, e.getMessage());
        }
    }

    private void handleClient(Socket clientSocket) {
        try {
            GenericPackager packager = isoBuilder.getPackager();
            var channel = new NACChannel(packager, new byte[0]) {
                public void attachSocket(Socket socket) throws IOException {
                    super.connect(socket);
                }
            };
            channel.setTimeout(timeoutMs);
            channel.attachSocket(clientSocket);

            while (clientSocket.isConnected() && !clientSocket.isClosed()) {
                try {
                    ISOMsg request = channel.receive();
                    if (request == null)
                        break;

                    long start = System.currentTimeMillis();
                    String mti = request.getMTI();
                    String pan = request.hasField(PAN) ? request.getString(PAN) : "UNKNOWN";
                    String expiry = request.hasField(EXPIRATION) ? request.getString(EXPIRATION) : null;
                    long amount = request.hasField(AMOUNT) ? Long.parseLong(request.getString(AMOUNT)) : 0;
                    String terminalId = request.hasField(TERMINAL_ID) ? request.getString(TERMINAL_ID) : null;
                    String maskedPan = PanMaskingUtil.mask(pan);

                    log.info("TCP Received MTI={} PAN={} amount={} terminal={}", mti, maskedPan, amount, terminalId);

                    // Evaluate rules
                    RuleEvaluationResult result = rulesEngine.evaluate(pan, expiry, amount, terminalId, null);

                    // Handle TIMEOUT – simply don't respond
                    if (result.getScenario() == Scenario.TIMEOUT) {
                        log.warn("TCP TIMEOUT scenario – not sending response for MTI={}", mti);
                        persistTcpLog(request, null, maskedPan, result, System.currentTimeMillis() - start);
                        continue;
                    }

                    // Build response
                    ISOMsg response = isoBuilder.buildResponse(request, result.getResponseCode39(), false);
                    channel.send(response);

                    long duration = System.currentTimeMillis() - start;
                    log.info("TCP Sent response MTI={} RC={} duration={}ms", response.getMTI(),
                            result.getResponseCode39(), duration);

                    persistTcpLog(request, response, maskedPan, result, duration);

                } catch (ISOException e) {
                    log.error("ISO parse error: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.debug("TCP client disconnected: {}", e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (Exception ignored) {
            }
        }
    }

    private void persistTcpLog(ISOMsg request, ISOMsg response, String maskedPan,
            RuleEvaluationResult result, long duration) {
        try {
            TransactionLog txLog = new TransactionLog();
            txLog.setType("TCP");
            txLog.setVisaLike(false);
            txLog.setRequestMti(request.getMTI());
            txLog.setResponseMti(response != null ? response.getMTI() : null);
            txLog.setPanMasked(maskedPan);
            txLog.setAmount(request.hasField(AMOUNT) ? Long.parseLong(request.getString(AMOUNT)) : 0);
            txLog.setCurrency(request.hasField(CURRENCY_CODE) ? request.getString(CURRENCY_CODE) : null);
            txLog.setStan(request.hasField(STAN) ? request.getString(STAN) : null);
            txLog.setRrn(request.hasField(RRN) ? request.getString(RRN) : null);
            txLog.setTerminalId(request.hasField(TERMINAL_ID) ? request.getString(TERMINAL_ID) : null);
            txLog.setResponseCode39(result.getResponseCode39());
            txLog.setScenario(result.getScenario().name());
            txLog.setRequestIsoFields(isoBuilder.extractFields(request).toString());
            if (response != null) {
                txLog.setResponseIsoFields(isoBuilder.extractFields(response).toString());
            }
            txLog.setDurationMs(duration);
            txLog.setCorrelationId(UUID.randomUUID().toString());
            txLogRepo.save(txLog);
        } catch (Exception e) {
            log.error("Failed to persist TCP transaction log: {}", e.getMessage());
        }
    }
}
