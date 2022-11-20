package at.ac.uibk.timeguess.flipflapp.timeflip;

import java.io.Serial;
import org.springframework.context.ApplicationEvent;

public class TimeFlipFacetChangeEvent extends ApplicationEvent {
  @Serial
  private static final long serialVersionUID = 1L;

  private final transient TimeFlip timeFlip;
  private final Byte facet;

  public TimeFlipFacetChangeEvent(Object source, TimeFlip timeFlip, Byte facet) {
    super(source);
    this.timeFlip = timeFlip;
    this.facet = facet;
  }

  public TimeFlip getTimeFlip() {
    return this.timeFlip;
  }

  public Byte getFacet() {
    return this.facet;
  }

}
