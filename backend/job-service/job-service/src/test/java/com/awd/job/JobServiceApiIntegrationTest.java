package com.awd.job;

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
class JobServiceApiIntegrationTest {

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

    private long category(String name) throws Exception {
        return post("/api/categories", "{\"name\":\"" + name + "\"}");
    }

    private long job(String name, boolean available, long categoryId) throws Exception {
        return post("/api/jobs", """
                {"name":"%s","description":"d","available":%s,"date":"2026-09-01","categoryId":%d}
                """.formatted(name, available, categoryId));
    }

    @Test
    void createAndReadJobWithCategory() throws Exception {
        long cat = category("Software");
        long j = job("Java Dev", true, cat);
        mvc.perform(get("/api/jobs/" + j))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Java Dev"))
                .andExpect(jsonPath("$.category.name").value("Software"));
    }

    @Test
    void filterJobsByAvailabilityAndCategory() throws Exception {
        long c1 = category("Software");
        long c2 = category("Data");
        job("Java Dev", true, c1);
        job("Angular Dev", false, c1);
        job("Analyst", true, c2);

        mvc.perform(get("/api/jobs")).andExpect(jsonPath("$.length()").value(3));
        mvc.perform(get("/api/jobs?available=true")).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/api/jobs?categoryId=" + c1)).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/api/categories/" + c2 + "/jobs")).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/jobs?categoryId=999")).andExpect(status().isNotFound());
    }

    @Test
    void openAndCloseJob() throws Exception {
        long j = job("Java Dev", true, category("Software"));
        mvc.perform(patch("/api/jobs/" + j + "/availability?available=false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void duplicateCategoryNameIsRejected() throws Exception {
        category("Software");
        mvc.perform(post("/api/categories").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"software\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void categoryWithJobsCannotBeDeleted() throws Exception {
        long cat = category("Software");
        long j = job("Java Dev", true, cat);

        mvc.perform(delete("/api/categories/" + cat)).andExpect(status().isConflict());
        mvc.perform(delete("/api/jobs/" + j)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/categories/" + cat)).andExpect(status().isNoContent());
    }

    @Test
    void validationAndUnknownIds() throws Exception {
        mvc.perform(post("/api/jobs").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.categoryId").exists());
        mvc.perform(get("/api/jobs/999")).andExpect(status().isNotFound());
    }

    @Test
    void swaggerDocsAreExposed() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/jobs']").exists())
                .andExpect(jsonPath("$.paths['/api/categories']").exists());
    }
}
