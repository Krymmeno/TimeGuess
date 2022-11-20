package at.ac.uibk.timeguess.flipflapp;

import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  private static final String ERROR_PAGE_PATH = "/notFound";

  @Override
  public void addViewControllers(final ViewControllerRegistry registry) {
    registry.addViewController(ERROR_PAGE_PATH).setStatusCode(HttpStatus.OK)
        .setViewName("forward:/");
  }

  @Bean
  public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> webServerFactoryCustomizer() {
    return factory -> factory.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, ERROR_PAGE_PATH));
  }

  @Override
  public void addCorsMappings(final CorsRegistry registry) {
    registry.addMapping("/**").allowedMethods("*");
  }
}
