package at.ac.uibk.timeguess.timeflapp.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import tinyb.BluetoothDevice;

@SpringBootTest
@ActiveProfiles("test")
class UpdateSenderTest {

  private static final String DEVICE_ADDRESS = "0123456789";
  private static final String DEVICE_NAME = "MockTimeFlip";
  private static final boolean CONNECTED = true;
  private static final Byte BATTERY_LEVEL = 95;
  private static final Byte FACET = 3;

  private final RestTemplate restTemplate;
  private final UpdateSender updateSender;

  @Value("${timeflapp.server.api.jwt}")
  private String serverApiJwt;

  @Value("${timeflapp.server.api.update.url}")
  private String serverApiUpdateUrl;

  private BluetoothDevice device;
  private MockRestServiceServer mockServer;

  @Autowired
  UpdateSenderTest(final RestTemplate restTemplate,
      final UpdateSender updateSender) {
    this.restTemplate = restTemplate;
    this.updateSender = updateSender;
  }

  @BeforeEach
  void setUp() {
    device = mock(BluetoothDevice.class);
    when(device.getAddress()).thenReturn(DEVICE_ADDRESS);
    when(device.getName()).thenReturn(DEVICE_NAME);
    mockServer = MockRestServiceServer.createServer(restTemplate);
  }

  @Test
  void sendUpdate() throws URISyntaxException {
    assertThat(serverApiJwt).isNotEmpty();
    assertThat(serverApiUpdateUrl).isNotEmpty();
    mockServer
        .expect(once(), requestTo(new URI(serverApiUpdateUrl)))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header(HttpHeaders.CONTENT_TYPE, "application/json"))
        .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + serverApiJwt))
        .andExpect(jsonPath("deviceAddress").value(DEVICE_ADDRESS))
        .andExpect(jsonPath("deviceName").value(DEVICE_NAME))
        .andExpect(jsonPath("connected").value(CONNECTED))
        .andExpect(jsonPath("batteryLevel").value(BATTERY_LEVEL.toString()))
        .andExpect(jsonPath("facet").value(FACET.toString()))
        .andRespond(withStatus(HttpStatus.OK));
    updateSender.sendUpdate(device, CONNECTED, BATTERY_LEVEL, FACET);
    mockServer.verify();
  }

  @Test
  void sendUpdateFailed() {
    final RestTemplate mockRestTemplate = mock(RestTemplate.class);
    when(mockRestTemplate.postForLocation(any(URI.class), any(HttpEntity.class)))
        .thenThrow(new RuntimeException());
    final UpdateSender mockUpdateSender = new UpdateSender(mockRestTemplate,
        serverApiJwt, serverApiUpdateUrl);
    assertThatNoException()
        .isThrownBy(() -> mockUpdateSender.sendUpdate(device, CONNECTED, BATTERY_LEVEL, FACET));
  }
}