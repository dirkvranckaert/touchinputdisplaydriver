package eu.vranckaert.touch.demo;

import eu.vranckaert.touch.TouchScreenDriverApplication;
import eu.vranckaert.touch.profile.DriverProfile;
import eu.vranckaert.touch.profile.KedeiProfile;

/**
 * Created by dirkvranckaert on 13/10/2017.
 */

public class ThingsApplication extends TouchScreenDriverApplication {
    @Override
    public DriverProfile getDriverProfile() {
        return KedeiProfile.getInstance(KedeiProfile.DIMENSION_480_320);
    }
}
