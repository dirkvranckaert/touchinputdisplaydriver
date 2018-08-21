package eu.vranckaert.driver.touch.profile;

import java.io.Serializable;

import eu.vranckaert.driver.touch.driver.Driver;
import eu.vranckaert.driver.touch.driver.WaveshareXPT2046Driver;

/**
 * Created by dirkvranckaert on 13/10/2017.
 */

public class WaveshareProfile extends SPIDriverProfile implements Serializable {
    public static final ScreenDimension DIMENSION_800_480 = new ScreenDimension(800, 480, ScreenDimension.Ratio.R_4_3); // TOUCH_XPT2046

    public static WaveshareProfile INSTANCE;

    public WaveshareProfile(ScreenDimension screenDimension) {
        super(Vendor.WAVESHARE, screenDimension);
    }

    public static WaveshareProfile getInstance(ScreenDimension screenDimension) {
        if (INSTANCE == null) {
            INSTANCE = new WaveshareProfile(screenDimension);
        }
        return INSTANCE;
    }

    @Override
    public Driver getDriver() {
        return new WaveshareXPT2046Driver(this);
    }
}
