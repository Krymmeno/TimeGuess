package at.ac.uibk.timeguess.flipflapp.timeflip;

import static at.ac.uibk.timeguess.flipflapp.security.jwt.JwtConstants.AUTHORIZATION_VALUE_PREFIX;
import static at.ac.uibk.timeguess.flipflapp.security.jwt.JwtConstants.SECRET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration;
import at.ac.uibk.timeguess.flipflapp.TimeGuessApplication;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = {TimeGuessApplication.class, TestDataConfiguration.class})
@AutoConfigureMockMvc
@Transactional
class TimeFlipControllerTest {

  public static final String REQUEST_BODY = """
      {
          "deviceAddress": "0C:61:CF:C7:95:88",
          "deviceName": "TimeFlip",
          "connected": true,
          "batteryLevel": 92,
          "facet": 10
      }
      """;

  public static final String CALIBRATE_TIME_FLIP_BODY = """
      {
          "1": {
              "activity": "RHYME",
              "roundPoints": "TWO",
              "time": "ONE"
          },
          "2": {
              "activity": "RHYME",
              "roundPoints": "TWO",
              "time": "ONE"
          },
          "3": {
              "activity": "RHYME",
              "roundPoints": "TWO",
              "time": "ONE"
          },
          "4": {
              "activity": "RHYME",
              "roundPoints": "TWO",
              "time": "ONE"
          },
          "5": {
              "activity": "RHYME",
              "roundPoints": "TWO",
              "time": "ONE"
          },
          "6": {
              "activity": "RHYME",
              "roundPoints": "TWO",
              "time": "ONE"
          },
          "7": {
              "activity": "RHYME",
              "roundPoints": "TWO",
              "time": "ONE"
          },
          "8": {
              "activity": "RHYME",
              "roundPoints": "TWO",
              "time": "ONE"
          },
          "9": {
              "activity": "RHYME",
              "roundPoints": "TWO",
              "time": "ONE"
          },
          "10": {
              "activity": "RHYME",
              "roundPoints": "TWO",
              "time": "ONE"
          },
          "11": {
              "activity": "RHYME",
              "roundPoints": "TWO",
              "time": "ONE"
          },
          "12": {
              "activity": "RHYME",
              "roundPoints": "TWO",
              "time": "TWO"
          }
      }
      """;

  private static final String RANDOM_TIME_FLIP_ADDRESS = "RANDOM_ADDRESS";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserDetailsService userDetailsService;

  @Autowired
  private TimeFlipRepository timeFlipRepository;

  @BeforeEach
  public void setup() {
    timeFlipRepository.save(new TimeFlip(RANDOM_TIME_FLIP_ADDRESS, "deviceName"));
  }

  @Test
  void update() throws Exception {
    mockMvc.perform(post("/api/timeflip/update").content(REQUEST_BODY)
        .header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_VALUE_PREFIX + getJwt("TIMEFLIP"))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  void updateMissingDeviceBatteryLevelAndFacet() throws Exception {
    mockMvc.perform(post("/api/timeflip/update").content("""
        {
            "deviceAddress": "0C:61:CF:C7:95:88",
            "deviceName": "TimeFlip",
            "connected": false
        }
        """)
        .header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_VALUE_PREFIX + getJwt("TIMEFLIP"))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  void updateMissingDeviceAddress() throws Exception {
    mockMvc.perform(post("/api/timeflip/update").content("""
        {
            "deviceName": "TimeFlip",
            "connected": true,
            "batteryLevel": 92,
            "facet": 10
        }
        """)
        .header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_VALUE_PREFIX + getJwt("TIMEFLIP"))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  void updateWithoutJwt() throws Exception {
    mockMvc.perform(post("/api/timeflip/update").content(REQUEST_BODY)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  void updateWithMalformedJwt() throws Exception {
    mockMvc.perform(post("/api/timeflip/update").content(REQUEST_BODY)
        .header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_VALUE_PREFIX + getJwt("TIMEFLIP") + "abc")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void updateWithWrongAuthority() throws Exception {
    mockMvc.perform(post("/api/timeflip/update").content(REQUEST_BODY)
        .header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_VALUE_PREFIX + getJwt("WRONGAUTHORITY"))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  void updateWithRegularUser() throws Exception {
    mockMvc.perform(post("/api/timeflip/update").content(REQUEST_BODY)
        .with(user(
            userDetailsService.loadUserByUsername(TestDataConfiguration.RANDOM_ADMIN.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  void getAllTimeFlips() throws Exception {
    mockMvc.perform(get("/api/timeflip")
        .with(user(
            userDetailsService.loadUserByUsername(TestDataConfiguration.RANDOM_ADMIN.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  void getAllAvailableTimeFlips() throws Exception {
    mockMvc.perform(get("/api/timeflip/available")
        .with(user(
            userDetailsService.loadUserByUsername(TestDataConfiguration.RANDOM_ADMIN.USERNAME)))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  void getAllAvailableTimeFlipsUnauthenticated() throws Exception {
    mockMvc.perform(get("/api/timeflip/available")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  void getAllTimeFlipsUnauthenticated() throws Exception {
    mockMvc.perform(get("/api/timeflip")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  void calibrateTimeFlip() throws Exception {
    final TimeFlip timeFlip = timeFlipRepository.findByDeviceAddress(RANDOM_TIME_FLIP_ADDRESS)
        .orElseThrow();
    mockMvc.perform(post("/api/timeflip/%d".formatted(timeFlip.getTimeFlipId()))
        .with(user(
            userDetailsService.loadUserByUsername(TestDataConfiguration.RANDOM_ADMIN.USERNAME)))
        .content(CALIBRATE_TIME_FLIP_BODY)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  void calibrateTimeFlipForNotExistingTimeFlip() throws Exception {
    assertThat(timeFlipRepository.findById(-1L)).isEmpty();
    mockMvc.perform(post("/api/timeflip/%d".formatted(-1))
        .with(user(
            userDetailsService.loadUserByUsername(TestDataConfiguration.RANDOM_ADMIN.USERNAME)))
        .content(CALIBRATE_TIME_FLIP_BODY)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void calibrateTimeFlipAsPlayer() throws Exception {
    final TimeFlip timeFlip = timeFlipRepository.findByDeviceAddress(RANDOM_TIME_FLIP_ADDRESS)
        .orElseThrow();
    mockMvc.perform(post("/api/timeflip/%d".formatted(timeFlip.getTimeFlipId()))
        .with(user(
            userDetailsService.loadUserByUsername(TestDataConfiguration.RANDOM_PLAYER.USERNAME)))
        .content(CALIBRATE_TIME_FLIP_BODY)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  void calibrateTimeFlipWithMissingFacets() throws Exception {
    final TimeFlip timeFlip = timeFlipRepository.findByDeviceAddress(RANDOM_TIME_FLIP_ADDRESS)
        .orElseThrow();
    mockMvc.perform(post("/api/timeflip/%d".formatted(timeFlip.getTimeFlipId()))
        .with(user(
            userDetailsService
                .loadUserByUsername(TestDataConfiguration.RANDOM_GAMEMANAGER.USERNAME)))
        .content("""
            {
              "1": {
                  "activity": "RHYME",
                  "roundPoints": "TWO",
                  "time": "ONE"
              }
            }
              """)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isConflict());
  }

  @Test
  void calibrateTimeFlipMalformed() throws Exception {
    final TimeFlip timeFlip = timeFlipRepository.findByDeviceAddress(RANDOM_TIME_FLIP_ADDRESS)
        .orElseThrow();
    mockMvc.perform(post("/api/timeflip/%d".formatted(timeFlip.getTimeFlipId()))
        .with(user(
            userDetailsService
                .loadUserByUsername(TestDataConfiguration.RANDOM_GAMEMANAGER.USERNAME)))
        .content("""
            {
              "1": {
                  "activity": "RHYME",
                  "roundPoints": "TWO",
                  "time
              }
            }
              """)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  private String getJwt(String authority) {
    return JWT.create()
        .withSubject("TestFlapp")
        .withClaim("is_user", false)
        .withArrayClaim("authorities", new String[]{authority})
        .sign(Algorithm.HMAC256(SECRET.getBytes(StandardCharsets.UTF_8)));
  }
}