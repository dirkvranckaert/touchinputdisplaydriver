package eu.vranckaert.driver.touch.demo;

import eu.vranckaert.driver.touch.TouchScreenDriverApplication;
import eu.vranckaert.driver.touch.profile.DriverProfile;
import eu.vranckaert.driver.touch.profile.KumanDriverProfile;
import eu.vranckaert.driver.touch.profile.WaveshareProfile;

/**
 * Created by dirkvranckaert on 13/10/2017.
 */

public class ThingsApplication extends TouchScreenDriverApplication {
    @Override
    public DriverProfile getDriverProfile() {
        return KumanDriverProfile.getInstance(KumanDriverProfile.DIMENSION_1920_1080);
    }
}
