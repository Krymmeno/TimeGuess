package at.ac.uibk.timeguess.flipflapp.term;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.RANDOM_TOPIC;
import at.ac.uibk.timeguess.flipflapp.TimeGuessApplication;
import at.ac.uibk.timeguess.flipflapp.topic.TopicRepository;
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
public class TermControllerTest {


  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserDetailsService userDetailsService;

  @Autowired
  TermRepository termRepository;

  @Autowired
  TopicRepository topicRepository;

  @Test
  void testAddTerm() throws Exception {
    mockMvc
        .perform(post("/api/terms").content("""
            {
                "name" : "TestTerm",
                "topicId" : 1
            }
            """)
            .with(user(
                userDetailsService.loadUserByUsername(TestDataConfiguration.RANDOM_ADMIN.USERNAME)))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andExpect(content().string(containsString("TestTerm")));
  }

  @Test
  void testAddTermAsPlayer() throws Exception {
    mockMvc.perform(post("/api/terms").content("""
        {
            "name" : "TestTerm",
            "topicId" : 1
        }
        """)
        .with(user(
            userDetailsService.loadUserByUsername(TestDataConfiguration.RANDOM_PLAYER.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
  }

  @Test
  void testUpdateTermName() throws Exception {
    final Term term = termRepository.findByNameAndTopic(RANDOM_TOPIC.TERM0_NAME,
        topicRepository.findByName(RANDOM_TOPIC.TOPIC_NAME).orElseThrow()).orElseThrow();
    Assertions.assertNotEquals("NewName", term.getName());
    mockMvc
        .perform(put("/api/terms/1").param("termName", "newName")
            .with(user(userDetailsService
                .loadUserByUsername(TestDataConfiguration.RANDOM_GAMEMANAGER.USERNAME)))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andExpect(content().string(containsString("newName")));
  }

  @Test
  void testUpdateTermNameAsPlayer() throws Exception {
    final Term term = termRepository.findByNameAndTopic(RANDOM_TOPIC.TERM0_NAME,
        topicRepository.findByName(RANDOM_TOPIC.TOPIC_NAME).orElseThrow()).orElseThrow();
    Assertions.assertNotEquals("NewName", term.getName());
    mockMvc.perform(put("/api/terms/1").param("termName", "newName")
        .with(user(
            userDetailsService.loadUserByUsername(TestDataConfiguration.RANDOM_PLAYER.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
  }

  @Test
  void testUpdateNotExistingTermName() throws Exception {
    mockMvc.perform(put("/api/terms/-1").param("termName", "newName")
        .with(user(userDetailsService
            .loadUserByUsername(TestDataConfiguration.RANDOM_GAMEMANAGER.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
  }

  @Test
  void testDeactivateTerm() throws Exception {
    final Term term = termRepository.findByNameAndTopic(RANDOM_TOPIC.TERM0_NAME,
        topicRepository.findByName(RANDOM_TOPIC.TOPIC_NAME).orElseThrow()).orElseThrow();
    mockMvc
        .perform(delete("/api/terms/%d".formatted(term.getTermId()))
            .with(user(userDetailsService
                .loadUserByUsername(TestDataConfiguration.RANDOM_GAMEMANAGER.USERNAME)))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andExpect(content().string(containsString(term.getName())));
  }

  @Test
  void testDeactivateNotExistingTerm() throws Exception {
    mockMvc.perform(delete("/api/terms/-1")
        .with(user(userDetailsService
            .loadUserByUsername(TestDataConfiguration.RANDOM_GAMEMANAGER.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
  }

  @Test
  void testDeactivateTermAsPlayer() throws Exception {
    final Term term = termRepository.findByNameAndTopic(RANDOM_TOPIC.TERM0_NAME,
        topicRepository.findByName(RANDOM_TOPIC.TOPIC_NAME).orElseThrow()).orElseThrow();
    mockMvc.perform(delete("/api/terms/%d".formatted(term.getTermId()))
        .with(user(
            userDetailsService.loadUserByUsername(TestDataConfiguration.RANDOM_PLAYER.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
  }

  @Test
  void testAddDuplicateTerm() throws Exception {
    mockMvc.perform(post("/api/terms").content("""
        {
            "name" : "chess",
            "topicId" : 1
        }
        """)
        .with(user(
            userDetailsService.loadUserByUsername(TestDataConfiguration.RANDOM_ADMIN.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isConflict());
  }

  @Test
  void testAddDuplicateTermOnDifferentTopics() throws Exception {
    mockMvc.perform(post("/api/terms").content("""
        {
            "name" : "chess",
            "topicId" : 2
        }
        """)
        .with(user(
            userDetailsService.loadUserByUsername(TestDataConfiguration.RANDOM_ADMIN.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
  }

}
