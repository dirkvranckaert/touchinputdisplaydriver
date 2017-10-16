package eu.vranckaert.driver.touch.profile;

import java.io.Serializable;

import eu.vranckaert.driver.touch.driver.Driver;

public abstract class DriverProfile implements Serializable {
    private final String name;
    private final Vendor vendor;
    private final ScreenDimension screenDimension;

    public DriverProfile(String name, Vendor vendor, ScreenDimension screenDimension) {
        this.name = name;
        this.vendor = vendor;
        this.screenDimension = screenDimension;
    }

    public abstract Driver getDriver();

    public String getName() {
        return name;
    }

    public ScreenDimension getScreenDimension() {
        return screenDimension;
    }

    public Vendor getVendor() {
        return vendor;
    }
}