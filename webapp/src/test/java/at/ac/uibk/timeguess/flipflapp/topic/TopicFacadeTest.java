package at.ac.uibk.timeguess.flipflapp.topic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration;
import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration.SampleTopic;
import at.ac.uibk.timeguess.flipflapp.TimeGuessApplication;
import at.ac.uibk.timeguess.flipflapp.term.Term;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
public class TopicFacadeTest {

  @Autowired
  private TopicFacade topicFacade;

  @Autowired
  private TopicService topicService;

  @Autowired
  private TopicRepository topicRepository;

  @Autowired
  EntityManager entityManager;

  @Autowired
  TopicConverter topicConverter;

  private final Topic sports = new Topic("TEST", true);

  private final Topic videoGames = new Topic("VIDEOGAMES", true);

  private final Topic biology = new Topic("BIOLOGY", false);

  @BeforeEach
  void setUp() {
    Set<Term> set = new HashSet<>();
    set.add(new Term("soccer", sports, true));
    set.add(new Term("Tennis", sports, true));
    set.add(new Term("MMA", sports, false));
    sports.setTerms(set);
    Stream.of(sports, videoGames, biology).forEach(topicRepository::save);
  }

  @Test
  void testAddTopic() {
    final TopicDto topic = topicFacade.addTopic("COMPUTER_SCIENCE");
    assertTrue(topicRepository.findByName("COMPUTER_SCIENCE").isPresent());
  }

  @Test
  void testAddTopicDuplicate() {
    assertThrows(TopicDuplicateException.class, () -> topicFacade.addTopic("TEST"));
  }


  @Test
  void testDeactivate() {
    TopicDto deactivated = topicFacade.deactivate(sports.getTopicId());
    Topic topic = topicRepository.findByName(sports.getName()).orElseThrow();
    topic.getTerms().forEach(t -> assertFalse(t.getActive()));
  }

  @Test
  void testDeactivateUnknown() {
    assertTrue(topicRepository.findById(-1L).isEmpty());
    assertThrows(TopicNotFoundException.class, () -> topicService.deactivate(-1L));
  }

  @Test
  void testUpdate() {
    TopicDto updated = topicFacade.updateTopicName(videoGames.getTopicId(), "Esports");
    Topic topic = topicRepository.findById(videoGames.getTopicId()).orElseThrow();
    assertEquals(topic.getName(), updated.getName());
  }

  @Test
  void testUpdateUnknownTopic() {
    assertTrue(topicRepository.findById(-1L).isEmpty());
    assertThrows(TopicNotFoundException.class,
        () -> topicFacade.updateTopicName(-1L, "COMPUTER_SCIENCE"));
  }

  @Test
  void getAllActiveTopics() {
    assertTrue(topicFacade.getAllActiveTopics().containsAll(Set.of(
        Objects.requireNonNull(topicConverter.convert(sports)),
        Objects.requireNonNull(topicConverter.convert(videoGames)))));
    assertTrue(
        topicRepository.findByName("BIOLOGY").isPresent() && !topicFacade.getAllActiveTopics()
            .contains(topicConverter.convert(biology)));
  }

  @Test
  void getAllAvailableTopics() {
    assertThat(topicFacade.getAllAvailableTopics())
        .doesNotContain(topicConverter.convert(sports), topicConverter.convert(videoGames),
            topicConverter.convert(biology));
    assertThat(topicFacade.getAllAvailableTopics().stream().map(TopicDto::getName))
        .contains(SampleTopic.NAME + "_30");
  }

  @Test
  void testImportWithUnknownTopic() {
    assertTrue(topicRepository.findById(-1L).isEmpty());
    assertThrows(TopicNotFoundException.class, () -> topicFacade.importTerms(-1L,
        List.of("Term1", "Term2", "Term3")));
  }

  @Test
  void testImport() {
    TopicDto topicDto = topicFacade
        .importTerms(sports.getTopicId(), List.of("Term1", "Term2","MMA"));
    assertEquals(5, topicDto.getActiveTerms().size());
  }

  @Test
  void updateDuplicate() {
    assertThrows(TopicDuplicateException.class, () -> topicFacade.updateTopicName(sports.getTopicId(),videoGames.getName()));
  }

  @Test
  void addInactive() {
    assertFalse(biology.getActive());
    TopicDto topic = topicFacade.addTopic("BIOLOGY");
    assertEquals(topic.getTopicId(),biology.getTopicId());
    assertEquals(topic.getName(),biology.getName());
  }
}
