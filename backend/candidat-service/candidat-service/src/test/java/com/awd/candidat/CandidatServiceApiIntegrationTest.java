package com.awd.candidat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Runs against in-memory H2 (see src/test/resources/application.properties), no MySQL or Eureka needed. */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CandidatServiceApiIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private long post(String url, String body) throws Exception {
        String json = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(json).get("id").asLong();
    }

    private long candidate(String email) throws Exception {
        return post("/api/candidates", """
                {"firstname":"Badia","lastname":"Abouhdid","email":"%s",
                 "address":{"street":"Avenue Habib Bourguiba","houseNumber":"12","zipCode":"1001"}}
                """.formatted(email));
    }

    @Test
    void createAndReadCandidateWithAddress() throws Exception {
        long c = candidate("a@example.com");
        mvc.perform(get("/api/candidates/" + c))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("a@example.com"))
                .andExpect(jsonPath("$.address.zipCode").value("1001"));
        mvc.perform(get("/api/candidates")).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void duplicateEmailIsRejected() throws Exception {
        candidate("a@example.com");
        mvc.perform(post("/api/candidates").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstname\":\"X\",\"lastname\":\"Y\",\"email\":\"a@example.com\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void validationAndUnknownIds() throws Exception {
        mvc.perform(post("/api/candidates").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.firstname").exists())
                .andExpect(jsonPath("$.errors.email").exists());
        mvc.perform(get("/api/candidates/999")).andExpect(status().isNotFound());
    }

    @Test
    void updateAndDeleteCandidate() throws Exception {
        long c = candidate("a@example.com");
        mvc.perform(put("/api/candidates/" + c).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstname\":\"New\",\"lastname\":\"Name\",\"email\":\"a@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname").value("New"));
        mvc.perform(delete("/api/candidates/" + c)).andExpect(status().isNoContent());
        mvc.perform(get("/api/candidates/" + c)).andExpect(status().isNotFound());
    }

    @Test
    void assignAndRemoveAddress() throws Exception {
        long c = post("/api/candidates",
                "{\"firstname\":\"A\",\"lastname\":\"B\",\"email\":\"b@example.com\"}");
        long addr = post("/api/addresses", "{\"street\":\"Rue X\",\"houseNumber\":\"1\",\"zipCode\":\"1000\"}");

        mvc.perform(put("/api/candidates/" + c + "/address/" + addr))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address.id").value(addr));
        mvc.perform(delete("/api/candidates/" + c + "/address"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").doesNotExist());
        mvc.perform(get("/api/addresses/" + addr)).andExpect(status().isOk());
    }

    @Test
    void swaggerDocsAreExposed() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/candidates']").exists())
                .andExpect(jsonPath("$.paths['/api/addresses']").exists());
    }
}
