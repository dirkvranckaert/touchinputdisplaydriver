package eu.vranckaert.touch;

import android.app.Application;
import android.content.Intent;

import eu.vranckaert.touch.profile.DriverProfile;

/**
 * Created by dirkvranckaert on 15/10/2017.
 */

public class TouchScreenDriverManager {
    private static TouchScreenDriverManager INSTANCE;

    private Intent mTouchScreenDriverIntent;

    public static TouchScreenDriverManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TouchScreenDriverManager();
        }

        return INSTANCE;
    }


    public void createDriver(Application application, TouchScreenDriverProfileHandler profileHandler) {
        mTouchScreenDriverIntent = new Intent(application.getApplicationContext(), TouchScreenDriverService.class);
        mTouchScreenDriverIntent.putExtra(TouchScreenDriverService.PROFILE, profileHandler.getDriverProfile());
        application.startService(mTouchScreenDriverIntent);

    }

    public void unload(Application application) {
        if (mTouchScreenDriverIntent != null) {
            application.stopService(mTouchScreenDriverIntent);
        }

    }

    public interface TouchScreenDriverProfileHandler {
        DriverProfile getDriverProfile();
    }
}
