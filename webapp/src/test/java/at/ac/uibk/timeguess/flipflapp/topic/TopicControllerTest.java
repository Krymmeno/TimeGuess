package at.ac.uibk.timeguess.flipflapp.topic;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.RANDOM_GAMEMANAGER;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.RANDOM_PLAYER;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.SampleTopic;
import at.ac.uibk.timeguess.flipflapp.TimeGuessApplication;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest(classes = {TimeGuessApplication.class, TestDataConfiguration.class})
@AutoConfigureMockMvc
@Transactional
public class TopicControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private TopicRepository topicRepository;

  @Autowired
  private UserDetailsService userDetailsService;


  @Test
  void testAddTopic() throws Exception {
    mockMvc.perform(post("/api/topics")
        .param("topicName", "TestTopic")
        .with(user(userDetailsService
            .loadUserByUsername(TestDataConfiguration.RANDOM_GAMEMANAGER.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("TestTopic")));
  }

  @Test
  void testAddDuplicate() throws Exception {
    Assertions.assertTrue(topicRepository.findByName("SPORT").isPresent());
    mockMvc.perform(post("/api/topics")
        .param("topicName", "SPORT")
        .with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isConflict());
  }

  @Test
  void testUpdateTopic() throws Exception {
    final Topic topic = topicRepository.findByName(SampleTopic.NAME + "_10").orElseThrow();
    Assertions.assertNotEquals("NewName", topic.getName());
    mockMvc.perform(put("/api/topics/1")
        .param("topicName", "NewName")
        .with(user(userDetailsService
            .loadUserByUsername(TestDataConfiguration.RANDOM_GAMEMANAGER.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("NewName")));
  }

  @Test
  void testUpdateNotExistingTopic() throws Exception {
    final Optional<Topic> topic = topicRepository.findById(-1L);
    Assertions.assertTrue(topic.isEmpty());
    mockMvc.perform(put("/api/topics/10")
        .param("topicName", "newName")
        .with(user(userDetailsService
            .loadUserByUsername(TestDataConfiguration.RANDOM_GAMEMANAGER.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void testUpdateAsPlayer() throws Exception {
    final Topic topic = topicRepository.findByName(SampleTopic.NAME + "_10").orElseThrow();
    Assertions.assertNotEquals("NewName", topic.getName());
    mockMvc.perform(put("/api/topics/1")
        .param("topicName", "NewName")
        .with(user(
            userDetailsService.loadUserByUsername(TestDataConfiguration.RANDOM_PLAYER.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  void testDeleteNotExistingTopic() throws Exception {
    mockMvc.perform(delete("/api/topics/-1")
        .with(user(userDetailsService
            .loadUserByUsername(TestDataConfiguration.RANDOM_GAMEMANAGER.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void testDeleteTopic() throws Exception {
    final Topic topic = topicRepository.findByName(SampleTopic.NAME + "_10").orElseThrow();
    mockMvc.perform(delete("/api/topics/%d".formatted(topic.getTopicId()))
        .with(user(userDetailsService
            .loadUserByUsername(TestDataConfiguration.RANDOM_GAMEMANAGER.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(topic.getName())));
  }

  @Test
  void testDeleteTopicAsPlayer() throws Exception {
    final Topic topic = topicRepository.findByName(SampleTopic.NAME + "_10").orElseThrow();
    mockMvc.perform(delete("/api/topics/%d".formatted(topic.getTopicId()))
        .with(user(
            userDetailsService.loadUserByUsername(TestDataConfiguration.RANDOM_PLAYER.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  void testGetAllActiveTopics() throws Exception {
    mockMvc.perform(get("/api/topics")
        .with(user(userDetailsService
            .loadUserByUsername(TestDataConfiguration.RANDOM_GAMEMANAGER.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  void testGetAllAvailableTopics() throws Exception {
    final Topic topic = topicRepository.findByName(SampleTopic.NAME + "_10").orElseThrow();
    mockMvc.perform(get("/api/topics/available")
        .with(user(userDetailsService
            .loadUserByUsername(TestDataConfiguration.RANDOM_GAMEMANAGER.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string(not(containsString(topic.getName()))));
  }

  @Test
  void testImport() throws Exception {
    final Topic topic = topicRepository.findByName(SampleTopic.NAME + "_10").orElseThrow();
    mockMvc.perform(post("/api/topics/%d/terms".formatted(topic.getTopicId()))
        .content("""
            [
            "Term1",
            "Term2",
            "Term3"
            ]
            """)
        .with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Term1")))
        .andExpect(content().string(containsString("Term2")))
        .andExpect(content().string(containsString("Term3")));
  }

  @Test
  void testImportAsAdmin() throws Exception {
    final Topic topic = topicRepository.findByName(SampleTopic.NAME + "_10").orElseThrow();
    mockMvc.perform(post("/api/topics/%d/terms".formatted(topic.getTopicId()))
        .content("""
            [
            "Term1",
            "Term2",
            "Term3"
            ]
            """)
        .with(user(userDetailsService.loadUserByUsername(RANDOM_GAMEMANAGER.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Term1")))
        .andExpect(content().string(containsString("Term2")))
        .andExpect(content().string(containsString("Term3")));
  }

  @Test
  void testImportAsPlayer() throws Exception {
    final Topic topic = topicRepository.findByName(SampleTopic.NAME + "_10").orElseThrow();
    mockMvc.perform(post("/api/topics/%d/terms".formatted(topic.getTopicId()))
        .content("""
            [
            "Term1",
            "Term2",
            "Term3"
            ]
            """)
        .with(user(userDetailsService.loadUserByUsername(RANDOM_PLAYER.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }
}
