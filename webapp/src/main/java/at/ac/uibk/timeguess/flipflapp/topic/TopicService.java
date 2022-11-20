package at.ac.uibk.timeguess.flipflapp.topic;

import at.ac.uibk.timeguess.flipflapp.term.Term;
import at.ac.uibk.timeguess.flipflapp.term.TermRepository;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import org.springframework.stereotype.Service;

@Service
public class TopicService {

  private final TopicRepository topicRepository;

  private final TermRepository termRepository;

  private final EntityManager manager;

  public TopicService(TopicRepository topicRepository, TermRepository termRepository,
      EntityManager manager) {
    this.topicRepository = topicRepository;
    this.termRepository = termRepository;
    this.manager = manager;
  }

  /**
   * Creates a new Topic
   *
   * @param topicName name of the new Topic
   * @return created topic
   */
  public Topic addTopic(final String topicName) {
    Optional<Topic> topic = topicRepository.findByName(topicName);
    if (topic.isPresent()) {
      if (topic.get().getActive()) {
        throw new TopicDuplicateException(topicName);
      }
      topic.get().setActive(true);
      return topicRepository.save(topic.get());
    }
    return topicRepository.save(new Topic(topicName, true));
  }

  /**
   * Updates name of an existing topic
   *
   * @param topicId   unique id to find a topic
   * @param topicName new name
   * @return updated topic
   * @throws TopicNotFoundException if the topicId is unknown
   */
  public Topic updateTopicName(final Long topicId, final String topicName) {
    final Topic topic = getTopicIfExists(topicId);
    if (topicRepository.findByName(topicName).isEmpty()) {
      topic.setName(topicName);
    } else {
      throw new TopicDuplicateException(topicName);
    }
    return topicRepository.save(topic);
  }

  public List<Topic> getAllTopics() {
    return topicRepository.findAll();
  }

  public Topic save(Topic topic) {
    return topicRepository.save(topic);
  }


  /**
   * @return topic with the given name
   */
  public Optional<Topic> findTopicByName(final String name) {
    return topicRepository.findByName(name);
  }

  /**
   * @return topic with the given id
   * @throws TopicNotFoundException if the topic with the given id could not be found
   */
  public Topic getTopicIfExists(final Long id) {
    return topicRepository.findById(id).orElseThrow(() -> new TopicNotFoundException(id));
  }

  /**
   * Deactivates a topic
   *
   * @param topicId unique id to find topic
   * @return deleted topic
   * @throws TopicNotFoundException if the topicId is unknown
   */
  public Topic deactivate(final Long topicId) {
    final Topic topic = getTopicIfExists(topicId);
    topic.setActive(false);
    for (Term term : topic.getTerms()) {
      term.setActive(false);
    }
    return topicRepository.save(topic);
  }

  /**
   * Import a list of terms to a specific topic
   *
   * @param topicId the id of the topic where the terms will be added
   * @param terms a list of strings representing the terms to import
   * @return the topic where the terms have been added
   */
  public Topic importTerms(Long topicId, List<String> terms) {
    final Topic topic = getTopicIfExists(topicId);
    for (String name : terms) {
      Optional<Term> termOptional = termRepository.findByNameAndTopic(name, topic);
      if (termOptional.isPresent() && !termOptional.get().getActive()) {
        termOptional.get().setActive(true);
        termRepository.save(termOptional.get());
        manager.flush();
      }
      if (termOptional.isEmpty()) {
        termRepository.save(new Term(name, topic, true));
      }
    }
    manager.refresh(topic);
    return topic;
  }
}
