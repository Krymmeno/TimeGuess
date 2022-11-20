package at.ac.uibk.timeguess.flipflapp.topic;

import at.ac.uibk.timeguess.flipflapp.term.Term;
import java.util.HashSet;
import java.util.stream.Collectors;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class TopicConverter implements Converter<Topic, TopicDto> {

  @Override
  public TopicDto convert(Topic topic) {
    TopicDto topicDto = new TopicDto(topic.getTopicId(), topic.getName(), new HashSet<>());
    if (topic.getTerms() != null) {
      topicDto.setActiveTerms((topic.getTerms().stream().filter(Term::getActive)
          .collect(Collectors.toSet())));
    }
    return topicDto;
  }
}
