package at.ac.uibk.timeguess.flipflapp.topic;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class TopicFacade {

  private static final int MIN_TERMS_COUNT = 30;

  private final TopicService topicService;

  private final TopicConverter topicConverter;

  public TopicFacade(TopicService topicService,
      TopicConverter topicConverter) {
    this.topicService = topicService;
    this.topicConverter = topicConverter;
  }

  public TopicDto importTerms(Long topicId, List<String> terms) {
    return topicConverter.convert(topicService.importTerms(topicId, terms));
  }

  public TopicDto addTopic(final String topicName) {
    return topicConverter.convert((topicService.addTopic(topicName)));
  }

  public TopicDto updateTopicName(final Long topicId, final String topicName) {
    return topicConverter.convert(topicService.updateTopicName(topicId, topicName));
  }

  public List<TopicDto> getAllActiveTopics() {
    return topicService.getAllTopics().stream()
        .filter(Topic::getActive)
        .map(topicConverter::convert)
        .toList();
  }

  public List<TopicDto> getAllAvailableTopics() {
    return getAllActiveTopics().stream().filter(topicDto ->
        Optional.ofNullable(topicDto.getActiveTerms()).orElse(Collections.emptySet()).size()
            >= MIN_TERMS_COUNT).toList();
  }

  public TopicDto deactivate(final Long topicId) {
    return topicConverter.convert(topicService.deactivate(topicId));
  }

}
