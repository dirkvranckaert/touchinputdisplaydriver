package eu.vranckaert.driver.touch.driver;

import eu.vranckaert.driver.touch.profile.SPIDriverProfile;

/**
 * Created by dirkvranckaert on 13/10/2017.
 * http://duinorasp.hansotten.com/3-5-lcd-tft-touch-screen-display-on-aliexpress/
 */

public class KedeiXPT2046Driver extends XPT2046Driver {
    public KedeiXPT2046Driver(SPIDriverProfile driverProfile) {
        super(driverProfile, true, false, true, true, true);
    }

    @Override
    public boolean isPressing(byte[] buffer) {
        return buffer[4] != 127;
//        return true;
    }

    @Override
    public int getSpiChannel() {
        return 0;
    }

    @Override
    public int getVersion() {
        return 0;
    }
}
