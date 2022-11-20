package at.ac.uibk.timeguess.flipflapp.security.jwt;

import static at.ac.uibk.timeguess.flipflapp.security.jwt.JwtConstants.AUTH_ENDPOINT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

public class JwtTestHelper {

  private final MockMvc mockMvc;

  private final Pattern REGEXP = Pattern.compile("\"accessToken\":\"([^\"]*)\"");

  public JwtTestHelper(final MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }

  public String getToken(final String username, final String password) throws Exception {
    final String response = getAuthenticationResponse(username, password)
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    final Matcher matcher = REGEXP.matcher(response);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return null;
  }

  public ResultActions getAuthenticationResponse(final String username, final String password)
      throws Exception {
    return mockMvc.perform(post(AUTH_ENDPOINT).content("""
        {"username": "%s", "password": "%s"}
        """.formatted(username, password))
        .contentType(MediaType.APPLICATION_JSON));
  }
}
