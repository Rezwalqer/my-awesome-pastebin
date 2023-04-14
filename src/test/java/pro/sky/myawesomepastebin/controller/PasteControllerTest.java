package pro.sky.myawesomepastebin.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.junit.jupiter.Testcontainers;
import pro.sky.myawesomepastebin.config.DockerConfig;
import pro.sky.myawesomepastebin.dto.PasteCreateDTO;
import pro.sky.myawesomepastebin.model.Paste;
import pro.sky.myawesomepastebin.model.enums.PasteStatus;
import pro.sky.myawesomepastebin.model.enums.ExpirationTime;
import pro.sky.myawesomepastebin.repository.PasteRepository;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class PasteControllerTest extends DockerConfig {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    PasteRepository pasteRepository;
    @Autowired
    ObjectMapper objectMapper;

    Paste paste;
    PasteStatus pasteStatus = PasteStatus.PUBLIC;
    ExpirationTime expirationTime = ExpirationTime.TEN_MIN;
    private JSONObject jsonObject;

    @BeforeEach
    void setUp() {
        paste = new Paste();
        paste.setLink(UUID.randomUUID().toString());
        paste.setTitle("Title");
        paste.setBody("Body");
        paste.setStatus(pasteStatus);
        paste.setCreationTime(Instant.now());
        paste.setExpiredTime(Instant.now().plus(expirationTime.getDuration()));
        pasteRepository.save(paste);
    }

    @AfterEach
    void tearDown() {
        pasteRepository.deleteAll();
    }
    @Test
    void createPaste() throws Exception {
        mockMvc.perform(post("/my-awesome-pastebin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(PasteCreateDTO.fromPaste(paste)))
                        .param("expirationTime", expirationTime.toString())
                        .param("pasteStatus", pasteStatus.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isString());
    }

    @Test
    void findByLink() throws Exception {
        mockMvc.perform(get("/my-awesome-pastebin/" + paste.getLink())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(PasteCreateDTO.fromPaste(paste))))
                .andExpect(status().isOk());

    }

    @Test
    void findAllPublic() throws Exception {
        mockMvc.perform(get("/my-awesome-pastebin/last-ten"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void findByTitleOrBody() throws Exception {
        mockMvc.perform(get("/my-awesome-pastebin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

}
