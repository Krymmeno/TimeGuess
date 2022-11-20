package at.ac.uibk.timeguess.timeflapp.bluetooth;

import static at.ac.uibk.timeguess.timeflapp.bluetooth.CharacteristicsManager.BATTERY_LEVEL_CHARACTERISTIC_UUID;
import static at.ac.uibk.timeguess.timeflapp.bluetooth.CharacteristicsManager.BATTERY_SERVICE_UUID;
import static at.ac.uibk.timeguess.timeflapp.bluetooth.CharacteristicsManager.FACETS_CHARACTERISTIC_UUID;
import static at.ac.uibk.timeguess.timeflapp.bluetooth.CharacteristicsManager.PASSWORD_CHARACTERISTIC_UUID;
import static at.ac.uibk.timeguess.timeflapp.bluetooth.CharacteristicsManager.TIME_FLIP_SERVICE_UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import at.ac.uibk.timeguess.timeflapp.event.TimeFlipConfiguredEvent;
import at.ac.uibk.timeguess.timeflapp.event.TimeFlipConnectedEvent;
import at.ac.uibk.timeguess.timeflapp.event.TimeFlipValueNotificationEvent;
import at.ac.uibk.timeguess.timeflapp.event.TimeFlipValueNotificationEvent.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import tinyb.BluetoothDevice;
import tinyb.BluetoothGattCharacteristic;
import tinyb.BluetoothGattService;
import tinyb.BluetoothNotification;

@ExtendWith(MockitoExtension.class)
class CharacteristicsManagerTest {

  private static final Byte BATTERY_LEVEL = 95;
  private static final Byte FACET = 3;

  @Mock
  private BluetoothDevice device;

  @Mock
  private BluetoothGattService batteryService;

  @Mock
  private BluetoothGattService timeFlipService;

  @Mock
  private BluetoothGattCharacteristic batteryLevelCharacteristic;

  @Mock
  private BluetoothGattCharacteristic passwordCharacteristic;

  @Mock
  private BluetoothGattCharacteristic facetsCharacteristic;

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  @Captor
  private ArgumentCaptor<BluetoothNotification<byte[]>> bluetoothNotificationArgumentCaptor;

  @Captor
  private ArgumentCaptor<TimeFlipValueNotificationEvent> valueNotificationEventArgumentCaptor;

  private CharacteristicsManager characteristicsManager;

  @BeforeEach
  void setUp() {
    when(device.find(BATTERY_SERVICE_UUID)).thenReturn(batteryService);
    when(device.find(TIME_FLIP_SERVICE_UUID)).thenReturn(timeFlipService);

    when(batteryService.find(BATTERY_LEVEL_CHARACTERISTIC_UUID))
        .thenReturn(batteryLevelCharacteristic);
    when(timeFlipService.find(FACETS_CHARACTERISTIC_UUID)).thenReturn(facetsCharacteristic);
    when(timeFlipService.find(PASSWORD_CHARACTERISTIC_UUID)).thenReturn(passwordCharacteristic);

    when(batteryLevelCharacteristic.readValue()).thenReturn(new byte[]{BATTERY_LEVEL});
    when(facetsCharacteristic.readValue()).thenReturn(new byte[]{FACET});

    characteristicsManager = new CharacteristicsManager(applicationEventPublisher, "000000");
  }

  @Test
  void handleConnected() {
    characteristicsManager.handleConnected(new TimeFlipConnectedEvent(this, device));

    verify(passwordCharacteristic).writeValue(new byte[]{0x30, 0x30, 0x30, 0x30, 0x30, 0x30});
    assertThat(characteristicsManager.getBatteryLevel()).isEqualTo(BATTERY_LEVEL);
    assertThat(characteristicsManager.getFacet()).isEqualTo(FACET);
    verify(batteryLevelCharacteristic).enableValueNotifications(any());
    verify(facetsCharacteristic).enableValueNotifications(any());
    verify(applicationEventPublisher).publishEvent(any(TimeFlipConfiguredEvent.class));
  }

  @Test
  void batteryLevelValueNotification() {
    characteristicsManager.handleConnected(new TimeFlipConnectedEvent(this, device));
    verify(batteryLevelCharacteristic)
        .enableValueNotifications(bluetoothNotificationArgumentCaptor.capture());

    bluetoothNotificationArgumentCaptor.getValue().run(new byte[]{(byte) (BATTERY_LEVEL - 1)});

    assertThat(characteristicsManager.getBatteryLevel()).isEqualTo((byte) (BATTERY_LEVEL - 1));
    verify(applicationEventPublisher, atLeastOnce())
        .publishEvent(valueNotificationEventArgumentCaptor.capture());
    assertThat(valueNotificationEventArgumentCaptor.getValue().getType())
        .isEqualTo(Type.BATTERY_LEVEL);
  }

  @Test
  void facetValueNotification() {
    characteristicsManager.handleConnected(new TimeFlipConnectedEvent(this, device));
    verify(facetsCharacteristic)
        .enableValueNotifications(bluetoothNotificationArgumentCaptor.capture());

    bluetoothNotificationArgumentCaptor.getValue().run(new byte[]{(byte) (FACET + 1)});

    assertThat(characteristicsManager.getFacet()).isEqualTo((byte) (FACET + 1));
    verify(applicationEventPublisher, atLeastOnce())
        .publishEvent(valueNotificationEventArgumentCaptor.capture());
    assertThat(valueNotificationEventArgumentCaptor.getValue().getType()).isEqualTo(Type.FACET);
  }
}