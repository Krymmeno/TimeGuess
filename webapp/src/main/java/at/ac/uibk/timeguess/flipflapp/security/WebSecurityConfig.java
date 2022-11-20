package at.ac.uibk.timeguess.flipflapp.security;

import at.ac.uibk.timeguess.flipflapp.security.jwt.JwtAuthorizationFilter;
import at.ac.uibk.timeguess.flipflapp.security.jwt.JwtConstants;
import at.ac.uibk.timeguess.flipflapp.user.UserRepository;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Based on at.ac.uibk.heidi.security.WebSecurityConfig
 * (https://git.uibk.ac.at/csaw3618/ws2020_ps_swa_6_2)
 */
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  private final DataSource dataSource;
  private final UserRepository userRepository;

  public WebSecurityConfig(final DataSource dataSource, final UserRepository userRepository) {
    this.dataSource = dataSource;
    this.userRepository = userRepository;
  }

  @Override
  protected void configure(final HttpSecurity http) throws Exception {
    http.cors().and().csrf().disable().sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
        .authorizeRequests()
        .antMatchers(JwtConstants.AUTH_ENDPOINT).permitAll()
        .antMatchers("/api/timeflip/update").permitAll()
        .antMatchers("/api/**").authenticated()
        .anyRequest().permitAll().and()
        .addFilterAfter(jwtAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class)
        .headers().frameOptions().disable();
  }

  @Override
  public void configure(final AuthenticationManagerBuilder auth) throws Exception {
    auth.jdbcAuthentication().dataSource(dataSource).passwordEncoder(passwordEncoder())
        .usersByUsernameQuery("SELECT username, password, active FROM user WHERE username = ?")
        .authoritiesByUsernameQuery("SELECT username, role FROM user WHERE username = ?");
  }

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }

  @Bean
  @Override
  public UserDetailsService userDetailsServiceBean() {
    return new TimeGuessUserDetailsService(userRepository);
  }

  @Bean
  public JwtAuthorizationFilter jwtAuthorizationFilter() {
    return new JwtAuthorizationFilter(userDetailsServiceBean(), userRepository);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
