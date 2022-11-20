package at.ac.uibk.timeguess.flipflapp;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@OpenAPIDefinition(
    info = @Info(
        title = "TimeGuess backend",
        version = "v1.2.0",
        description = "Backend application for TimeGuess game"
    )
)
@SecurityScheme(
    name = "JWT",
    type = SecuritySchemeType.HTTP,
    in = SecuritySchemeIn.HEADER,
    scheme = "bearer",
    bearerFormat = "JWT"
)
@SpringBootApplication
public class TimeGuessApplication extends SpringBootServletInitializer {

  public static void main(final String[] args) {
    SpringApplication.run(TimeGuessApplication.class, args);
  }
}
