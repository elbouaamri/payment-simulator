package com.simulator.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simulator.api.dto.TransactionRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private TransactionRequest validRequest() {
        TransactionRequest req = new TransactionRequest();
        req.setPan("4111111111111111");
        req.setExpiry("2812");
        req.setAmount(10000L);
        req.setCurrency("504");
        req.setTerminalId("TERM0001");
        req.setProfileId(1L);
        return req;
    }

    @Test
    @DisplayName("POST /authorize – returns 200 with transaction response")
    void testAuthorize() throws Exception {
        mockMvc.perform(post("/api/v1/transactions/authorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").isNotEmpty())
                .andExpect(jsonPath("$.requestMti").value("0200"))
                .andExpect(jsonPath("$.responseMti").value("0210"))
                .andExpect(jsonPath("$.responseCode39").value("00"))
                .andExpect(jsonPath("$.scenario").value("ACCEPT"));
    }

    @Test
    @DisplayName("POST /refund – returns 200")
    void testRefund() throws Exception {
        mockMvc.perform(post("/api/v1/transactions/refund")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("REFUND"));
    }

    @Test
    @DisplayName("POST /cancel – returns 200")
    void testCancel() throws Exception {
        mockMvc.perform(post("/api/v1/transactions/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("CANCEL"));
    }

    @Test
    @DisplayName("POST /reversal – returns 200 with MTI 0400/0410")
    void testReversal() throws Exception {
        mockMvc.perform(post("/api/v1/transactions/reversal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestMti").value("0400"))
                .andExpect(jsonPath("$.responseMti").value("0410"));
    }

    @Test
    @DisplayName("POST /authorize – invalid request returns 400")
    void testValidationError() throws Exception {
        TransactionRequest bad = new TransactionRequest();
        bad.setPan(""); // invalid
        bad.setAmount(-1L); // invalid

        mockMvc.perform(post("/api/v1/transactions/authorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /transactions – returns list")
    void testGetTransactions() throws Exception {
        // First create one
        mockMvc.perform(post("/api/v1/transactions/authorize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest())));

        mockMvc.perform(get("/api/v1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /transactions/{id} – not found returns 404")
    void testGetNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/transactions/nonexistent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /profiles – returns seed profiles")
    void testGetProfiles() throws Exception {
        mockMvc.perform(get("/api/v1/profiles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("FAST_ACCEPT"));
    }

    @Test
    @DisplayName("GET /rules – returns seed rules")
    void testGetRules() throws Exception {
        mockMvc.perform(get("/api/v1/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /dashboard – returns KPIs")
    void testDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTransactions").isNumber());
    }
}
