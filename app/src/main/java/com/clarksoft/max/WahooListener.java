package com.clarksoft.max;

import com.wahoofitness.connector.HardwareConnector;
import com.wahoofitness.connector.HardwareConnectorEnums;
import com.wahoofitness.connector.HardwareConnectorTypes;
import com.wahoofitness.connector.conn.connections.SensorConnection;


public class WahooListener implements HardwareConnector.Listener {

    @Override
    public void onHardwareConnectorStateChanged(HardwareConnectorTypes.NetworkType networkType, HardwareConnectorEnums.HardwareConnectorState hardwareConnectorState) {

    }

    @Override
    public void onFirmwareUpdateRequired(SensorConnection sensorConnection, String s, String s1) {

    }
}
