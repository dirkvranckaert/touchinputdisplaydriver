package eu.vranckaert.driver.touch.profile;

import eu.vranckaert.driver.touch.driver.Driver;
import eu.vranckaert.driver.touch.driver.KumanDriver;

public class KumanDriverProfile extends SPIDriverProfile {

    public static final ScreenDimension DIMENSION_1920_1080 = new ScreenDimension(1920, 1080, ScreenDimension.Ratio.R_16_10);

    private static KumanDriverProfile INSTANCE;

    private KumanDriverProfile(ScreenDimension screenDimension) {
        super(Vendor.KUMAN, screenDimension);
    }

    public static KumanDriverProfile getInstance(ScreenDimension screenDimension) {
        if(INSTANCE == null) {
            INSTANCE = new KumanDriverProfile(screenDimension);
        }
        return INSTANCE;
    }

    @Override
    public Driver getDriver() {
        return new KumanDriver(this);
    }
}
