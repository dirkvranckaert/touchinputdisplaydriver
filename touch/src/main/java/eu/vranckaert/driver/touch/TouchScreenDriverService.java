package eu.vranckaert.driver.touch;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import eu.vranckaert.driver.touch.driver.Driver;
import eu.vranckaert.driver.touch.profile.DriverProfile;

/**
 * https://github.com/machtelik/waveshare-7inch-touchscreen-driver-android/blob/master/wstouchdriver/src/main/java/web/achtelik/wstouchdriver/TouchscreenDriverService.java
 * <p>
 * https://raspberrypi.stackexchange.com/questions/68374/working-display-setting-for-android-things-on-raspberry-pi-3-with-3-5-inch-waves
 * https://github.com/bitbank2/SPI_LCD
 */
public class TouchScreenDriverService extends Service {
    private static final String LOG_TAG = TouchScreenDriverService.class.getSimpleName();

    public static final String PROFILE = "profile";

    private DriverProfile mDriverProfile;
    private Driver mDriver;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mDriverProfile = (DriverProfile) intent.getSerializableExtra(PROFILE);
        if (mDriverProfile == null) {
            throw new RuntimeException("Cannot start the TouchScreen Driver without a DriverProfile!");
        }

        mDriver = mDriverProfile.getDriver();
        mDriver.run();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //spilcdInitTouch(TOUCH_XPT2046, 1, 50000);
        // 1 = channel
        // 50000 = frequency
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mDriver != null) {
            mDriver.close();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}