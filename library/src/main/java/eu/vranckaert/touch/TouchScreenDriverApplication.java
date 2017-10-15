package eu.vranckaert.touch;

import android.app.Application;

import eu.vranckaert.touch.profile.DriverProfile;

/**
 * Created by dirkvranckaert on 15/10/2017.
 */

public abstract class TouchScreenDriverApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        TouchScreenDriverManager.getInstance().createDriver(this, new TouchScreenDriverManager.TouchScreenDriverProfileHandler() {
            @Override
            public DriverProfile getDriverProfile() {
                return TouchScreenDriverApplication.this.getDriverProfile();
            }
        });
    }

    public abstract DriverProfile getDriverProfile();

    @Override
    public void onTerminate() {
        TouchScreenDriverManager.getInstance().unload(this);
        super.onTerminate();
    }
}
