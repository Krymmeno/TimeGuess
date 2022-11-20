package at.ac.uibk.timeguess.timeflapp.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import at.ac.uibk.timeguess.timeflapp.bluetooth.CharacteristicsManager;
import at.ac.uibk.timeguess.timeflapp.bluetooth.ConnectionManager;
import at.ac.uibk.timeguess.timeflapp.event.TimeFlipConfiguredEvent;
import at.ac.uibk.timeguess.timeflapp.event.TimeFlipValueNotificationEvent;
import at.ac.uibk.timeguess.timeflapp.event.TimeFlipValueNotificationEvent.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tinyb.BluetoothDevice;

class UpdateHandlerTest {

  private static final Byte BATTERY_LEVEL = 95;
  private static final Byte FACET = 3;

  private UpdateSender updateSender;
  private ConnectionManager connectionManager;
  private UpdateHandler updateHandler;

  @BeforeEach
  void setUp() {
    final CharacteristicsManager characteristicsManager = mock(CharacteristicsManager.class);
    when(characteristicsManager.getBatteryLevel()).thenReturn(BATTERY_LEVEL);
    when(characteristicsManager.getFacet()).thenReturn(FACET);

    updateSender = mock(UpdateSender.class);
    connectionManager = mock(ConnectionManager.class);
    updateHandler = new UpdateHandler(updateSender, connectionManager, characteristicsManager);
  }

  @Test
  void periodicUpdateWithDisconnectedDevice() {
    when(connectionManager.getConnectedDevice()).thenReturn(null);
    updateHandler.periodicUpdate();
    verify(updateSender, never()).sendUpdate(any(), anyBoolean(), any(), any());
  }

  @Test
  void periodicUpdate() {
    final BluetoothDevice device = mock(BluetoothDevice.class);
    when(connectionManager.getConnectedDevice()).thenReturn(device);
    updateHandler.periodicUpdate();
    verify(updateSender).sendUpdate(device, true, BATTERY_LEVEL, FACET);
  }

  @Test
  void handleValueNotification() {
    final BluetoothDevice device = mock(BluetoothDevice.class);
    when(connectionManager.getConnectedDevice()).thenReturn(device);
    updateHandler
        .handleValueNotification(new TimeFlipValueNotificationEvent(updateHandler, Type.FACET));
    verify(updateSender).sendUpdate(device, true, BATTERY_LEVEL, FACET);
  }

  @Test
  void handleConfigured() {
    final BluetoothDevice device = mock(BluetoothDevice.class);
    updateHandler.handleConfigured(new TimeFlipConfiguredEvent(updateHandler, device));
    verify(updateSender).sendUpdate(device, true, BATTERY_LEVEL, FACET);
  }
}