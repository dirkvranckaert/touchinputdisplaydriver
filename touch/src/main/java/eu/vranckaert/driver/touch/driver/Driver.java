package eu.vranckaert.driver.touch.driver;

import android.app.Service;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.google.android.things.userdriver.input.InputDriver;
import com.google.android.things.userdriver.UserDriverManager;
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
        mInputDriver = new InputDriver.Builder()

                //InputDevice.SOURCE_TOUCHSCREEN)
                .setName(driverProfile.getName())
                //.setVersion(getVersion())
                .setAxisConfiguration(MotionEvent.AXIS_X, 0, driverProfile.getScreenDimension().getWidth(),
                            10,0)
                .setAxisConfiguration(MotionEvent.AXIS_Y, 0, driverProfile.getScreenDimension().getHeight(),
                            10,0)
                .setSupportedKeys(new int[] {KeyEvent.KEYCODE_ENTER})
                //.setAbsMax(MotionEvent.AXIS_X, driverProfile.getScreenDimension().getWidth())
                //.setAbsMax(MotionEvent.AXIS_Y, driverProfile.getScreenDimension().getHeight())
                .build();

        UserDriverManager manager = UserDriverManager.getInstance();
        manager.registerInputDriver(mInputDriver);
        Log.i(LOG_TAG, "Touch Screen Driver registered!");

        Log.v(LOG_TAG, "Setting up input thread!");
        mInputThread = new Thread(new Runnable() {
            @Override
            public void run() {
                InputDriverEvent event = new InputDriverEvent();

                while (!mInputThread.isInterrupted() && !mStopped) try {
                    TouchInput touchInput = getTouchInput();
                    Thread.sleep(20);
                    event.clear();
                    event.setPosition(MotionEvent.AXIS_X, touchInput.x);
                    event.setPosition(MotionEvent.AXIS_Y, touchInput.y);
                    event.setContact(touchInput.touching);
                    mInputDriver.emit(event);


                } catch (TouchDriverReadingException e) {
                    stop();
                } catch (InterruptedException e) {
                    stop();
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
        int x;
        int y;
        private boolean touching;

        public TouchInput(int x, int y, boolean touching) {
            this.x = x;
            this.y = y;
            this.touching = touching;
        }
    }
}
