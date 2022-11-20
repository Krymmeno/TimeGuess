package at.ac.uibk.timeguess.timeflapp.event;

import static org.assertj.core.api.Assertions.assertThat;

import at.ac.uibk.timeguess.timeflapp.event.TimeFlipValueNotificationEvent.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TimeFlipValueNotificationEventTest {

  private TimeFlipValueNotificationEvent timeFlipValueNotificationEvent;

  @BeforeEach
  void setUp() {
    timeFlipValueNotificationEvent = new TimeFlipValueNotificationEvent(this, Type.FACET);
  }

  @Test
  void getType() {
    assertThat(timeFlipValueNotificationEvent.getType()).isEqualTo(Type.FACET);
  }

  @Test
  void testToString() {
    assertThat(timeFlipValueNotificationEvent.toString()).contains(Type.FACET.name());
  }
}