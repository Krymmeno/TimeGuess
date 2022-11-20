package at.ac.uibk.timeguess.timeflapp;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class Main {

  /**
   * Bootstraps the Spring Boot application.
   * <p>
   * See {@link at.ac.uibk.timeguess.timeflapp.bluetooth.DiscoveryRunner} for the CommandLineRunner
   * that is executed immediately after startup.
   *
   * @param args the command line arguments
   */
  public static void main(final String[] args) {
    new SpringApplicationBuilder(Main.class).web(WebApplicationType.NONE).run(args);
  }
}
