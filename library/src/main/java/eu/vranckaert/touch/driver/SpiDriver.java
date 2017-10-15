package eu.vranckaert.touch.driver;

import eu.vranckaert.touch.profile.SPIDriverProfile;

/**
 * Created by dirkvranckaert on 13/10/2017.
 */

public abstract class SpiDriver extends Driver {
    public SpiDriver(SPIDriverProfile driverProfile) {
        super(driverProfile);
    }

    public abstract int getSpiChannel();
}
