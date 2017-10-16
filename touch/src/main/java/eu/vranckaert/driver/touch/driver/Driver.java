package eu.vranckaert.driver.touch.driver;

import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;

import com.google.android.things.userdriver.InputDriver;
import com.google.android.things.userdriver.UserDriverManager;

import java.io.Serializable;

import eu.vranckaert.driver.touch.exception.TouchDriverReadingException;
import eu.vranckaert.driver.touch.exception.UnableToOpenTouchDriverException;
import eu.vranckaert.driver.touch.profile.DriverProfile;

/**
 * Created by dirkvranckaert on 13/10/2017.
 */

public abstract class Driver implements Serializable {
    private static final String LOG_TAG = Driver.class.getSimpleName();

    private final DriverProfile driverProfile;

    private boolean mStopped;
    private InputDriver mInputDriver;
    private Thread mInputThread;

    public Driver(DriverProfile driverProfile) {
        this.driverProfile = driverProfile;
    }

    public DriverProfile getDriverProfile() {
        return driverProfile;
    }

    public final void run() {
        Log.w(LOG_TAG, "Setting up the touchscreen driver...");
        mStopped = false;
        try {
            Log.i(LOG_TAG, "Starting driver '" + driverProfile.getName() + "' (" + getVersion() + ")");
            open();
        } catch (UnableToOpenTouchDriverException e) {
            Log.e(LOG_TAG, "The driver '" + driverProfile.getName() + "' seems to be unavailable!", e);
            stop();
        }

        mInputDriver = new InputDriver.Builder(InputDevice.SOURCE_TOUCHSCREEN)
                .setName(driverProfile.getName())
                .setVersion(getVersion())
                .setAbsMax(MotionEvent.AXIS_X, driverProfile.getScreenDimension().getWidth())
                .setAbsMax(MotionEvent.AXIS_Y, driverProfile.getScreenDimension().getHeight())
                .build();
        UserDriverManager.getManager().registerInputDriver(mInputDriver);
        Log.i(LOG_TAG, "Touch Screen Driver registered!");

        Log.v(LOG_TAG, "Setting up input thread!");
        mInputThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!mInputThread.isInterrupted() && !mStopped) {
                    try {
                        TouchInput touchInput = getTouchInput();
                        mInputDriver.emit(touchInput.x, touchInput.y, touchInput.touching);
                    } catch (TouchDriverReadingException e) {
                        stop();
                    }
                }
            }
        });

        mInputThread.start();
    }

    public final void stop() {
        Log.i(LOG_TAG, "Stopping the driver '" + driverProfile.getName() + "'");
        mStopped = true;
        if (mInputThread != null) {
            mInputThread.interrupt();
        }

        close();
    }

    public abstract void open() throws UnableToOpenTouchDriverException;

    public abstract TouchInput getTouchInput() throws TouchDriverReadingException;

    public abstract void close();

    public abstract int getVersion();

    public static class TouchInput {
        private int x;
        private int y;
        private boolean touching;

        public TouchInput(int x, int y, boolean touching) {
            this.x = x;
            this.y = y;
            this.touching = touching;
        }
    }
}
