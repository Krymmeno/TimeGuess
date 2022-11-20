package at.ac.uibk.timeguess.timeflapp.bluetooth;

import static at.ac.uibk.timeguess.timeflapp.bluetooth.ConnectionManager.MAX_CONNECT_RETRIES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import at.ac.uibk.timeguess.timeflapp.event.TimeFlipConnectedEvent;
import at.ac.uibk.timeguess.timeflapp.rest.UpdateSender;
import com.ginsberg.junit.exit.ExpectSystemExitWithStatus;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import tinyb.BluetoothDevice;
import tinyb.BluetoothNotification;

@ExtendWith(MockitoExtension.class)
class ConnectionManagerTest {

  @Mock
  private BluetoothDevice device;

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  @Mock
  private UpdateSender updateSender;

  @Captor
  private ArgumentCaptor<BluetoothNotification<Boolean>> bluetoothNotificationArgumentCaptor;

  private ConnectionManager connectionManager;

  @BeforeEach
  void setUp() {
    connectionManager = new ConnectionManager(applicationEventPublisher, updateSender);
  }

  @Test
  void connect() {
    final AtomicInteger count = new AtomicInteger();
    when(device.connect())
        .thenAnswer(invocationOnMock -> count.getAndIncrement() == MAX_CONNECT_RETRIES + 1);
    connectionManager.connect(device);
    assertThat(connectionManager.getConnectedDevice()).isEqualTo(device);
    verify(device).enableConnectedNotifications(any());
    verify(applicationEventPublisher).publishEvent(any(TimeFlipConnectedEvent.class));
  }

  @Test
  @ExpectSystemExitWithStatus(-1)
  void connectFailed() {
    final AtomicInteger count = new AtomicInteger();
    when(device.connect())
        .thenAnswer(invocationOnMock -> count.getAndIncrement() > MAX_CONNECT_RETRIES + 1);
    connectionManager.connect(device);
  }

  @Test
  @ExpectSystemExitWithStatus(-1)
  void disconnectedNotification() {
    when(device.connect()).thenReturn(true);
    connectionManager.connect(device);
    verify(device).enableConnectedNotifications(bluetoothNotificationArgumentCaptor.capture());
    bluetoothNotificationArgumentCaptor.getValue().run(false);
  }

  @Test
  void destroy() {
    when(device.connect()).thenReturn(true);
    connectionManager.connect(device);
    connectionManager.destroy();
    verify(device, atLeastOnce()).disableConnectedNotifications();
    verify(device).disconnect();
    verify(updateSender).sendUpdate(device, false, null, null);
  }

  @Test
  void destroyWithDisconnectedDevice() {
    connectionManager.destroy();
    verify(device, never()).disableConnectedNotifications();
    verify(device, never()).disconnect();
    verify(updateSender, never()).sendUpdate(any(), anyBoolean(), any(), any());
  }
}