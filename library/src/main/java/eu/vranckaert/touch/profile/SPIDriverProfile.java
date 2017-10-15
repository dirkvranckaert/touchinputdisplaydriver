package eu.vranckaert.touch.profile;

import java.io.Serializable;

import eu.vranckaert.touch.driver.Driver;

public abstract class SPIDriverProfile extends DriverProfile implements Serializable {
    public SPIDriverProfile(String name, Vendor vendor, ScreenDimension screenDimension) {
        super(name, vendor, screenDimension);
    }

    public abstract Driver getDriver();
}