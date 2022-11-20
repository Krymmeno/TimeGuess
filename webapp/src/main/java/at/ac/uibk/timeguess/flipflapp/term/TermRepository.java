package at.ac.uibk.timeguess.flipflapp.term;

import at.ac.uibk.timeguess.flipflapp.topic.Topic;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermRepository extends JpaRepository<Term, Long> {

  Optional<Term> findByNameAndTopic(String name, Topic topic);
}
