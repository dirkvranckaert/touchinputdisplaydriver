package eu.vranckaert.touch.driver;

import java.io.Serializable;

import eu.vranckaert.touch.profile.SPIDriverProfile;

/**
 * Created by dirkvranckaert on 13/10/2017.
 */

public class WaveshareXPT2046Driver extends XPT2046Driver implements Serializable {
    public WaveshareXPT2046Driver(SPIDriverProfile driverProfile) {
        super(driverProfile, false, false, false, true, true);
    }

    @Override
    public int getSpiChannel() {
        return 1;
    }

    @Override
    public boolean isPressing(byte[] buffer) {
        return buffer[1] != 0;
    }

    @Override
    public int getVersion() {
        return 7;
    }
}
