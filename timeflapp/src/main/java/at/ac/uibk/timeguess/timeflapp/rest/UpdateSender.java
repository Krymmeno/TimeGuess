package at.ac.uibk.timeguess.timeflapp.rest;

import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tinyb.BluetoothDevice;

/**
 * Sends updates about TimeFlip's status to the server
 */
@Component
public class UpdateSender {

  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateSender.class);

  private final RestTemplate restTemplate;
  private final String serverApiJwt;
  private final String serverApiUpdateUrl;

  public UpdateSender(final RestTemplate restTemplate,
      @Value("${timeflapp.server.api.jwt}") final String serverApiJwt,
      @Value("${timeflapp.server.api.update.url}") final String serverApiUpdateUrl) {
    this.restTemplate = restTemplate;
    this.serverApiJwt = serverApiJwt;
    this.serverApiUpdateUrl = serverApiUpdateUrl;
  }

  /**
   * Sends an update to the server.
   *
   * @param device       the device that is being reported on
   * @param connected    if true, the device is being reported as connected
   * @param batteryLevel the battery level of the device that is being reported on
   * @param facet        the facet of the device that is being reported on
   */
  public void sendUpdate(final BluetoothDevice device, final boolean connected,
      final Byte batteryLevel, final Byte facet) {
    final UpdateRequest updateRequest = new UpdateRequest(device, connected, batteryLevel, facet);
    LOGGER.debug("Sending update for {}", updateRequest);

    final HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + serverApiJwt);
    try {
      restTemplate.postForLocation(new URI(serverApiUpdateUrl),
          new HttpEntity<>(updateRequest, headers));
    } catch (final Exception e) {
      LOGGER.error("Could not send update to {}: {}", serverApiUpdateUrl, e);
    }
  }
}
