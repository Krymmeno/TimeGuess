package at.ac.uibk.timeguess.flipflapp.topic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.RANDOM_TOPIC;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.SampleTopic;
import at.ac.uibk.timeguess.flipflapp.TimeGuessApplication;
import at.ac.uibk.timeguess.flipflapp.term.Term;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = {TimeGuessApplication.class, TestDataConfiguration.class})
@Transactional
public class TopicServiceTest {

  @Autowired
  private TopicService topicService;

  @Test
  void save() {
    Topic topic = topicService.save(new Topic("TestTopic",true));
    assertEquals(topic, topicService.getTopicIfExists(topic.getTopicId()));
  }

  @Test
  void findByName() {
    Optional<Topic> topic = topicService.findTopicByName(RANDOM_TOPIC.TOPIC_NAME);
    assertTrue(topic.isPresent());
    Optional<Topic> topicFake = topicService.findTopicByName("**TOPIC**");
    assertTrue(topicFake.isEmpty());
  }
}
