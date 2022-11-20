package at.ac.uibk.timeguess.flipflapp.timeflip;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import at.ac.uibk.timeguess.flipflapp.TestDataConfiguration;
import at.ac.uibk.timeguess.flipflapp.TimeGuessApplication;
import at.ac.uibk.timeguess.flipflapp.game.room.GameRoomService;
import at.ac.uibk.timeguess.flipflapp.game.round.Activity;
import at.ac.uibk.timeguess.flipflapp.game.round.RoundPoints;
import at.ac.uibk.timeguess.flipflapp.game.round.Time;
import at.ac.uibk.timeguess.flipflapp.topic.TopicRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = {TimeGuessApplication.class, TestDataConfiguration.class})
@AutoConfigureMockMvc
@Transactional
public class TimeFlipServiceTest {

  @Autowired
  private TimeFlipService timeFlipService;

  @Autowired
  private TimeFlipRepository timeFlipRepository;

  @Autowired
  private GameRoomService gameRoomService;

  @Autowired
  private TopicRepository topicRepository;

  private final TimeFlip t1 = new TimeFlip("0C:61:CF:C7:95:88", "t1");

  private final TimeFlip t2 = new TimeFlip("0C:61:CF:C7:95:89", "t2");

  private final TimeFlip t3 = new TimeFlip("0C:61:CF:C7:95:90", "t2");

  @BeforeEach
  void setUp() {
    Stream.of(t1, t2, t3).forEach(timeFlip -> {
      timeFlip.getTimeFlipFacetMap().put((byte) 0, new TimeFlipFacet(
          Activity.DRAW, RoundPoints.ONE, Time.TWO));
      timeFlipRepository.save(timeFlip);
    });
    byte b = 10;
    timeFlipService
        .recognize(new TimeFlipUpdate(t1.getDeviceAddress(), t1.getDeviceName(), true, b, b));
    timeFlipService
        .recognize(new TimeFlipUpdate(t2.getDeviceAddress(), t2.getDeviceName(), false, b, b));
  }

  @Test
  void recognizeAsNewAndActive() {
    assertFalse(timeFlipRepository.findByDeviceAddress("5C:61:CF:C7:95:90").isPresent());
    byte battery = 10;
    byte facet = 1;
    timeFlipService
        .recognize(new TimeFlipUpdate("5C:61:CF:C7:95:90", "newFlip", true, battery, facet));
    assertTrue(timeFlipRepository.findByDeviceAddress("5C:61:CF:C7:95:90").isPresent());
  }

  @Test
  void batteryLevel() {
    assertThat(timeFlipRepository.findByDeviceAddress("5C:61:CF:C7:95:90")).isNotPresent();
    byte facet = 1;
    timeFlipService
        .recognize(new TimeFlipUpdate("5C:61:CF:C7:95:90", "newFlip", true, (byte) 75, facet));
    timeFlipService
        .recognize(new TimeFlipUpdate("5C:61:CF:C7:95:90", "newFlip", true, (byte) 65, facet));
    final TimeFlip timeFlip = timeFlipService.getAllTimeFlips().stream()
        .filter(tf -> tf.getDeviceAddress().equals("5C:61:CF:C7:95:90")).findAny().get();
    assertThat(timeFlip.getBatteryLevel()).isEqualTo((byte) 65);
  }

  @Test
  void recognizeAsActive() {
    byte battery = 10;
    byte facet = 1;
    final Long id = timeFlipRepository.findById(t2.getTimeFlipId()).orElseThrow().getTimeFlipId();
    assertFalse(timeFlipService.getAvailableTimeFlips().contains(t2));
    TimeFlip t = timeFlipService.recognize(
        new TimeFlipUpdate(t2.getDeviceAddress(), t2.getDeviceName(), true, battery, facet));
    assertTrue(timeFlipService.getAvailableTimeFlips().contains(t2));
    assertEquals(id, timeFlipRepository.findById(t.getTimeFlipId()).orElseThrow().getTimeFlipId());
  }

  @Test
  void recognizeDisconnect() {
    byte battery = 10;
    byte facet = 1;
    assertTrue(timeFlipService.getAvailableTimeFlips().contains(t1));
    timeFlipService.recognize(
        new TimeFlipUpdate(t1.getDeviceAddress(), t1.getDeviceName(), false, battery, facet));
    assertFalse(timeFlipService.getAvailableTimeFlips().contains(t1));
  }

  @Test
  void getAllAvailable() {
    List<TimeFlip> list = timeFlipService.getAvailableTimeFlips();
    assertTrue(list.contains(t1));
    assertEquals(1, list.size());
  }

  @Test
  void getAllTimeFlips() {
    assertTrue(timeFlipService.getAllTimeFlips().containsAll(List.of(t1, t2, t3)));
  }

  @Test
  void calibrateTimeFlip() {
    final TimeFlip timeFlip = timeFlipService
        .calibrateTimeFlip(t1.getTimeFlipId(), getTestConfiguration());
    final Byte key = 12;
    assertThat(timeFlip.getTimeFlipFacetMap()).containsKey(key);
  }

  @Test
  void calibrateTimeFlipWithWrongConfiguration() {
    final Map<Byte, TimeFlipFacet> testConfiguration = getTestConfiguration();
    final Byte key = 1;
    testConfiguration.remove(key);
    assertThatExceptionOfType(TimeFlipConfigurationException.class)
        .isThrownBy(() -> timeFlipService.calibrateTimeFlip(t1.getTimeFlipId(), testConfiguration));
  }

  @Test
  void calibrateForNotExistingTimeFlip() {
    assertThat(timeFlipRepository.findById(-1L)).isEmpty();
    assertThatExceptionOfType(TimeFlipNotFoundException.class)
        .isThrownBy(() -> timeFlipService.calibrateTimeFlip(-1L, getTestConfiguration()));
  }

  @Test
  void calibrateNotExistingFacet() {
    final Map<Byte, TimeFlipFacet> testConfiguration = getWrongTestConfiguration();
    assertThatExceptionOfType(TimeFlipConfigurationException.class)
        .isThrownBy(() -> timeFlipService.calibrateTimeFlip(t1.getTimeFlipId(), testConfiguration));
  }

  private Map<Byte, TimeFlipFacet> getTestConfiguration() {
    final Map<Byte, TimeFlipFacet> config = new HashMap<>();
    for (byte i = 1; i < 13; i++) {
      config.put(i, new TimeFlipFacet(Activity.DRAW, RoundPoints.ONE, Time.TWO));
    }
    return config;
  }

  private Map<Byte, TimeFlipFacet> getWrongTestConfiguration() {
    final Map<Byte, TimeFlipFacet> config = new HashMap<>();
    for (byte i = 5; i < 15; i++) {
      config.put(i, new TimeFlipFacet(Activity.DRAW, RoundPoints.ONE, Time.TWO));
    }
    return config;
  }

  @Test
  public void equalsContract() {
    EqualsVerifier.simple().forClass(TimeFlip.class)
        .withIgnoredFields("deviceName", "timeFlipFacetMap").verify();
  }
}
