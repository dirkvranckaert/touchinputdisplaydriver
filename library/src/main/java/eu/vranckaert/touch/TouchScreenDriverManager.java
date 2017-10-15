package eu.vranckaert.touch;

import android.app.Application;
import android.content.Intent;

import eu.vranckaert.touch.profile.WaveshareProfile;

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


    public void createDriver(Application application) {
        mTouchScreenDriverIntent = new Intent(application.getApplicationContext(), TouchScreenDriverService.class);
        mTouchScreenDriverIntent.putExtra(TouchScreenDriverService.PROFILE, WaveshareProfile.getInstance(WaveshareProfile.DIMENSION_800_480));
        application.startService(mTouchScreenDriverIntent);

    }

    public void stopDriver(Application application) {
        if (mTouchScreenDriverIntent != null) {
            application.stopService(mTouchScreenDriverIntent);
        }

    }
}
