package at.ac.uibk.timeguess.flipflapp.term;

import at.ac.uibk.timeguess.flipflapp.topic.Topic;
import at.ac.uibk.timeguess.flipflapp.topic.TopicNotFoundException;
import at.ac.uibk.timeguess.flipflapp.topic.TopicRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class TermService {

  private final TermRepository termRepository;

  private final TopicRepository topicRepository;

  public TermService(TermRepository termRepository,
      TopicRepository topicRepository) {
    this.termRepository = termRepository;
    this.topicRepository = topicRepository;
  }

  /**
   * Deactivates a term
   *
   * @param termId Unique id to find the specified term
   * @return the deleted term
   * @throws TermNotFoundException if the termId is unknown
   */

  public Term deactivateTerm(Long termId) {
    final Term term = termRepository.findById(termId).orElseThrow(
        () -> new TermNotFoundException(termId));
    term.setActive(false);
    termRepository.save(term);
    return term;
  }

  /**
   * Updates the name of an existing term
   *
   * @param termId   unique id to find the existing term
   * @param termName the new name for term with specified termId
   * @return updated term
   * @throws TermNotFoundException if termId is unknown
   */
  public Term updateTermName(Long termId, String termName) {
    final Term term = termRepository.findById(termId).orElseThrow(
        () -> new TermNotFoundException(termId));
    if (termRepository.findByNameAndTopic(termName, term.getTopic()).isPresent()) {
      throw new TermDuplicateException(
          "Term name: %s already exists in topic: %s"
              .formatted(term.getName(), term.getTopic().getName()));
    }
    term.setName(termName);
    return termRepository.save(term);
  }

  /**
   * Creates a new Term
   *
   * @param term all information needed for creating a term (termName and topicId)
   * @return the created term
   * @throws TopicNotFoundException if the topicId to which the term should belong does not exist
   * @throws TermDuplicateException if the term Name already exists within topic
   */
  public Term addTerm(CreateTermRequest term) {
    Topic topic = topicRepository.findById(term.topicId()).orElseThrow(
        () -> new TopicNotFoundException(term.topicId()));
    Optional<Term> termOptional = termRepository.findByNameAndTopic(term.name(), topic);
    if (termOptional.isPresent()) {
      if (!termOptional.get().getActive()) {
        termOptional.get().setActive(true);
        return termRepository.save(termOptional.get());
      } else {
        throw new TermDuplicateException(
            "Term name: %s already exists in topic: %s".formatted(term.name(), topic.getName()));
      }
    }
    return termRepository
        .save(new Term(term.name(), topic, true));
  }


}
