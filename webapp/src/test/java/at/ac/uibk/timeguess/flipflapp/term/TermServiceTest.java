package at.ac.uibk.timeguess.flipflapp.term;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration;
import at.ac.uibk.timeguess.flipflapp.TimeGuessApplication;
import at.ac.uibk.timeguess.flipflapp.topic.Topic;
import at.ac.uibk.timeguess.flipflapp.topic.TopicNotFoundException;
import at.ac.uibk.timeguess.flipflapp.topic.TopicRepository;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = {TimeGuessApplication.class, TestDataConfiguration.class})
@Transactional
public class TermServiceTest {

  @Autowired
  TermRepository termRepository;

  @Autowired
  TermService termService;

  @Autowired
  TopicRepository topicRepository;

  private final Topic topic = new Topic("SCIENCE_FICTION", true);

  private final Term starTrek = new Term("StarTrek", topic, true);

  private final Term starWars = new Term("StarWars", topic, true);

  private final Term bttf = new Term("BackToTheFuture", topic, false);

  @BeforeEach
  void setUp() {
    topicRepository.save(topic);
    Stream.of(starTrek, starWars, bttf).forEach(termRepository::save);
  }

  @Test
  void testSetUp() {
    Assertions.assertTrue(termRepository.findAll().containsAll(Set.of(starTrek, starWars, bttf)));
    Assertions.assertTrue(topicRepository.findByName(topic.getName()).isPresent());
  }

  @Test
  void testAddTerm() {
    final Topic topic = topicRepository.findByName("SCIENCE_FICTION").orElseThrow();
    final CreateTermRequest termRequest = new CreateTermRequest("E.T", topic.getTopicId());
    Term term = termService.addTerm(termRequest);
    assertTrue(term.getActive());
    assertEquals("E.T", term.getName());
    assertEquals(topic, term.getTopic());
  }

  @Test
  void testAddDuplicate() {
    final Topic topic = topicRepository.findByName("SCIENCE_FICTION").orElseThrow();
    final CreateTermRequest termRequest = new CreateTermRequest("StarWars", topic.getTopicId());
    assertThrows(TermDuplicateException.class, () -> termService.addTerm(termRequest));
  }

  @Test
  void testAddAlreadyExistingButInactive() {
    final Topic topic = topicRepository.findByName("SCIENCE_FICTION").orElseThrow();
    final Term inactive = termRepository.findByNameAndTopic("BackToTheFuture", topic).orElseThrow();
    final CreateTermRequest termRequest = new CreateTermRequest("BackToTheFuture",
        topic.getTopicId());
    assertFalse(inactive.getActive());
    Term term = termService.addTerm(termRequest);
    assertEquals(inactive.getTermId(), term.getTermId());
    assertTrue(term.getActive());
  }

  @Test
  void testAddWithoutTopic() {
    assertTrue(topicRepository.findById(-1L).isEmpty());
    final CreateTermRequest faultyReq = new CreateTermRequest("Sadge", -1L);
    assertThrows(TopicNotFoundException.class, () -> termService.addTerm(faultyReq));

  }

  @Test
  void testDeactivate() {
    final Term term = termService.deactivateTerm(starTrek.getTermId());
    assertFalse(term.getActive());
    assertFalse(termRepository.findByNameAndTopic("StarTrek", term.getTopic()).orElse(starWars)
        .getActive());
  }

  @Test
  void testDeactivateUnknown() {
    assertTrue(termRepository.findById(-1L).isEmpty());
    assertThrows(TermNotFoundException.class, () -> termService.deactivateTerm(-1L));
  }

  @Test
  void testUpdate() {
    final Term term = termService.updateTermName(starTrek.getTermId(), "traumschiff_surprise");
    assertEquals("traumschiff_surprise", term.getName());
  }

  @Test
  void testUpdateWithDuplicateName() {
    assertThrows(TermDuplicateException.class,
        () -> termService.updateTermName(starTrek.getTermId(), "StarWars"));
  }

  @Test
  void testUpdateWithUnknownTerm() {
    assertTrue(termRepository.findById(-1L).isEmpty());
    assertThrows(TermNotFoundException.class,
        () -> termService.updateTermName(-1L, "traumschiff_surprise"));
  }
}
