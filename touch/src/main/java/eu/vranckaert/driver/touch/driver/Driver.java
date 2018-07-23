package eu.vranckaert.driver.touch.driver;

import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;

import com.google.android.things.userdriver.UserDriverManager;
import com.google.android.things.userdriver.input.InputDriver;
import com.google.android.things.userdriver.input.InputDriverEvent;

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
    private InputDriverEvent mEvent;
    private TouchInput mPrevTouchInput = new TouchInput(0,0,mStopped);

    public Driver(DriverProfile driverProfile) {
        this.driverProfile = driverProfile;
    }

    public DriverProfile getDriverProfile() {
        return driverProfile;
    }

    public final InputDriver run() {
        mEvent = new InputDriverEvent();
        Log.w(LOG_TAG, "Setting up the touchscreen driver...");
        mStopped = false;
        try {
            Log.i(LOG_TAG, "Starting driver '" + driverProfile.getName() + "' (" + getVersion() + ")");
            open();
        } catch (UnableToOpenTouchDriverException e) {
            Log.e(LOG_TAG, "The driver '" + driverProfile.getName() + "' seems to be unavailable!", e);
            stop();
        }

        mInputDriver = new InputDriver.Builder()
                .setName(driverProfile.getName() + " - " + getVersion())
                .setAxisConfiguration(MotionEvent.AXIS_X, 0, driverProfile.getScreenDimension().getWidth(), 0, 0)
                .setAxisConfiguration(MotionEvent.AXIS_Y, 0, driverProfile.getScreenDimension().getHeight(), 0, 0)
                .build();
        UserDriverManager.getInstance().registerInputDriver(mInputDriver);
        Log.i(LOG_TAG, "Touch Screen Driver registered!");

        Log.v(LOG_TAG, "Setting up input thread!");
        mInputThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!mInputThread.isInterrupted() && !mStopped) {
                    try {
                        TouchInput touchInput = getTouchInput();
                        if (touchInput != null && (mPrevTouchInput == null || mPrevTouchInput.x != touchInput.x || mPrevTouchInput.y != touchInput.y || mPrevTouchInput.touching != touchInput.touching)) {
                            mEvent.clear();
                            mEvent.setPosition(MotionEvent.AXIS_X, touchInput.touching ? touchInput.x : mPrevTouchInput.x);
                            mEvent.setPosition(MotionEvent.AXIS_Y, touchInput.touching ? touchInput.y : mPrevTouchInput.y);
                            mEvent.setContact(touchInput.touching);
                            Log.d(LOG_TAG, "Emitting event: " + touchInput.x + "," + touchInput.y + " - Touching? " + (touchInput.touching ? "YES" : "NO"));
                            mInputDriver.emit(mEvent);
                            mPrevTouchInput = touchInput;
                        }
                    } catch (TouchDriverReadingException e) {
                        stop();
                    }
                }
            }
        });

        mInputThread.start();
        return mInputDriver;
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
