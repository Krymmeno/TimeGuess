package at.ac.uibk.timeguess.timeflapp.event;

import org.springframework.context.ApplicationEvent;

public class TimeFlipValueNotificationEvent extends ApplicationEvent {

  private static final long serialVersionUID = 1L;

  public enum Type {
    BATTERY_LEVEL, FACET
  }

  private final Type type;

  public TimeFlipValueNotificationEvent(final Object source, final Type type) {
    super(source);
    this.type = type;
  }

  public Type getType() {
    return type;
  }

  @Override
  public String toString() {
    return String.format("Notification: %s", type);
  }
}
