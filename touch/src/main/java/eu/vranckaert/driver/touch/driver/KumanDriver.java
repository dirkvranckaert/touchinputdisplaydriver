package eu.vranckaert.driver.touch.driver;

import eu.vranckaert.driver.touch.profile.SPIDriverProfile;

public class KumanDriver extends XPT2046Driver {

    public KumanDriver(SPIDriverProfile driverProfile) {
        super(driverProfile, true, true, true, true, true);
    }

    @Override
    public boolean isPressing(byte[] buffer) {
        return buffer[1] != 0;
    }

    @Override
    public int getSpiChannel() {
        return 1;
    }

    @Override
    public int getVersion() {
        return 1;
    }
}
