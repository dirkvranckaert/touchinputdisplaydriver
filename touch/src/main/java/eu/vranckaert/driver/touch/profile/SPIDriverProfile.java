package eu.vranckaert.driver.touch.profile;

import java.io.Serializable;

import eu.vranckaert.driver.touch.driver.Driver;

public abstract class SPIDriverProfile extends DriverProfile implements Serializable {
    public SPIDriverProfile(String name, Vendor vendor, ScreenDimension screenDimension) {
        super(name, vendor, screenDimension);
    }

    public abstract Driver getDriver();
}