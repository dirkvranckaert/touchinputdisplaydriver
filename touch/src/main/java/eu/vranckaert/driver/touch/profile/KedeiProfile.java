package eu.vranckaert.driver.touch.profile;

import java.io.Serializable;

import eu.vranckaert.driver.touch.driver.Driver;
import eu.vranckaert.driver.touch.driver.KedeiXPT2046Driver;

/**
 * Created by dirkvranckaert on 13/10/2017.
 */

public class KedeiProfile extends SPIDriverProfile implements Serializable {
    public static final ScreenDimension DIMENSION_480_320 = new ScreenDimension(480, 320, ScreenDimension.Ratio.R_15_9);

    public static KedeiProfile INSTANCE;

    public KedeiProfile(ScreenDimension screenDimension) {
        super("Waveshare 5\" Driver", Vendor.KEDEI, screenDimension);
    }

    public static KedeiProfile getInstance(ScreenDimension screenDimension) {
        if (INSTANCE == null) {
            INSTANCE = new KedeiProfile(screenDimension);
        }
        return INSTANCE;
    }

    @Override
    public Driver getDriver() {
        return new KedeiXPT2046Driver(this);
    }
}
