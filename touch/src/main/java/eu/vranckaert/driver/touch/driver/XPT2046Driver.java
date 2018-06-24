package eu.vranckaert.driver.touch.driver;

import android.util.Log;

import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.SpiDevice;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import eu.vranckaert.driver.touch.exception.TouchDriverReadingException;
import eu.vranckaert.driver.touch.exception.UnableToOpenTouchDriverException;
import eu.vranckaert.driver.touch.profile.SPIDriverProfile;

/**
 * Created by dirkvranckaert on 13/10/2017.
 */

public abstract class XPT2046Driver extends SpiDriver implements Serializable {
    private static final String LOG_TAG = XPT2046Driver.class.getSimpleName();

    private SpiDevice mTouchscreen;

    private boolean mIsPressing = false;
    private boolean mOutlinerDetected = false;
    private int cX = -1; // The current touching x-value
    private int cY = -1; // The current touching y-value
    private long xyTime = -1L;

    private final byte[] xRead = new byte[]{(byte) 0xd0, (byte) 0x00, (byte) 0x00};
    private final byte[] yRead = new byte[]{(byte) 0x90, (byte) 0x00, (byte) 0x00};
    private final byte[] xBuffer = new byte[3];
    private final byte[] yBuffer = new byte[3];

    private final boolean switchXY;
    private final boolean inverseX;
    private final boolean inverseY;
    private final boolean flakeynessCorrection;
    private final boolean shiverringCorrection;

    @Deprecated
    private int iMinX = 149;
    @Deprecated
    private int iMaxX = 949;
    @Deprecated
    private int iMinY = 58;
    @Deprecated
    private int iMaxY = 538;

    @Deprecated
    private int minMeasuredX = 9999;
    @Deprecated
    private int maxMeasuredX = -9999;
    @Deprecated
    private int minMeasuredY = 9999;
    @Deprecated
    private int maxMeasuredY = -9999;

    public XPT2046Driver(SPIDriverProfile driverProfile, boolean switchXY, boolean inverseX, boolean inverseY, boolean flakeynessCorrection, boolean shiverringCorrection) {
        super(driverProfile);
        this.switchXY = switchXY;
        this.inverseX = inverseX;
        this.inverseY = inverseY;
        this.flakeynessCorrection = flakeynessCorrection;
        this.shiverringCorrection = shiverringCorrection;
    }

    public abstract boolean isPressing(byte[] buffer);

    @Override
    public void open() throws UnableToOpenTouchDriverException {
        Log.i(LOG_TAG, "Opening SPI ");
        PeripheralManager peripheralManager = PeripheralManager.getInstance();
        List<String> deviceList = peripheralManager.getSpiBusList();
        if (!deviceList.isEmpty()) {
            Log.i(LOG_TAG, "List of available SPI devices: " + deviceList);
            if (deviceList.size() > 1) {
                String spiName = deviceList.get(getSpiChannel());
                try {
                    Log.i(LOG_TAG, "Opening SPI: " + spiName);
                    mTouchscreen = peripheralManager.openSpiDevice(spiName);
                    mTouchscreen.setFrequency(50000);
                    mTouchscreen.transfer(new byte[]{(byte) 0x80, (byte) 0x00, (byte) 0x000}, new byte[4], 1);
                } catch (IOException e) {
                    Log.w(LOG_TAG, "Cannot open SPI device " + spiName);
                }

            }
        }

        if (mTouchscreen == null) {
            Log.w(LOG_TAG, "Cannot get a hand on the SPI touchscreen...");
            throw new UnableToOpenTouchDriverException();
        }
    }

    @Override
    public TouchInput getTouchInput() throws TouchDriverReadingException {
        // return v4(); // Good but nearing the edges the cursor has some offset...
        // return v5(); // Good, all the shivering seems to be gone and the wrong touch inputs as well, remaining issue is the cursor offset near the edges...
        // return v6(); // Superb! Fixed the in-accuracies when moving away from the center
        return v8(); // Parameters
    }

    public byte[] concat(byte[]... arrays) {
        int length = 0;
        for (byte[] array : arrays) {
            length += array.length;
        }
        byte[] result = new byte[length];
        int destPos = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, destPos, array.length);
            destPos += array.length;
        }
        return result;
    }

    private TouchInput v1() throws TouchDriverReadingException {
        try {
            mTouchscreen.transfer(xRead, xBuffer, 3);
            mTouchscreen.transfer(yRead, yBuffer, 3);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Cannot process input readings from touch screen", e);
            Log.w(LOG_TAG, "Shutting down driver due to errors in reading!");
            throw new TouchDriverReadingException();
        }

        byte[] buffer = concat(xBuffer, yBuffer);
        boolean press = buffer[1] != 0;

        int x = (buffer[2] + (buffer[1] << 8) >> 4);
        int y = (buffer[5] + (buffer[4] << 8) >> 4);

        Log.v(LOG_TAG, "x,y=" + x + "," + y + "," + press);

        if (press && mIsPressing) {
            Log.v(LOG_TAG, String.format("HOLDING DOWN (%d, %d)", x, y));
        } else if (!press && !mIsPressing) {
            Log.v(LOG_TAG, String.format("NOT TOUCHING (%d, %d)", x, y));
        } else if (press && !mIsPressing) {
            Log.v(LOG_TAG, String.format("TOUCHING DOWN (%d, %d)", x, y));
        } else if (!press && mIsPressing) {
            Log.v(LOG_TAG, String.format("RELEASING TOUCH (%d, %d)", x, y));
        }
        mIsPressing = press;

        return new TouchInput(x, y, press);
    }

    private TouchInput v2() throws TouchDriverReadingException {
        byte[] read = new byte[]{(byte) 0xd0, (byte) 0x00, (byte) 0x00, (byte) 0x90, (byte) 0x00, (byte) 0x00};
        byte[] buffer = new byte[6];
        try {
            mTouchscreen.transfer(read, buffer, 6);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Cannot process input readings from touch screen", e);
            Log.w(LOG_TAG, "Shutting down driver due to errors in reading!");
            throw new TouchDriverReadingException();
        }

        boolean press = buffer[1] != 0;

        int x = (buffer[2] + (buffer[1] << 8) >> 4);
        int y = (buffer[5] + (buffer[4] << 8) >> 4);

        if (x > iMaxX) x = iMaxX;
        x -= iMinX;
        if (x < 0) x = 0;
        if (y > iMaxY) y = iMaxY;
        y -= iMinY;
        if (y < 0) y = 0;

        Log.v(LOG_TAG, "x,y=" + x + "," + y + "," + press);

        if (press && mIsPressing) {
            Log.v(LOG_TAG, String.format("HOLDING DOWN (%d, %d)", x, y));
        } else if (!press && !mIsPressing) {
            Log.v(LOG_TAG, String.format("NOT TOUCHING (%d, %d)", x, y));
        } else if (press && !mIsPressing) {
            Log.v(LOG_TAG, String.format("TOUCHING DOWN (%d, %d)", x, y));
        } else if (!press && mIsPressing) {
            Log.v(LOG_TAG, String.format("RELEASING TOUCH (%d, %d)", x, y));
        }
        mIsPressing = press;

        return new TouchInput(x, y, press);
    }

    private TouchInput v3() throws TouchDriverReadingException {
        try {
            mTouchscreen.transfer(xRead, xBuffer, 3);
            mTouchscreen.transfer(yRead, yBuffer, 3);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Cannot process input readings from touch screen", e);
            Log.w(LOG_TAG, "Shutting down driver due to errors in reading!");
            throw new TouchDriverReadingException();
        }

        byte[] buffer = concat(xBuffer, yBuffer);
        boolean press = buffer[1] != 0;

        int x = (buffer[2] + (buffer[1] << 8) >> 4);
        int y = (buffer[5] + (buffer[4] << 8) >> 4);
        if (press) {
            x = (int) ((x / 2018f) * 800f);
            y = (int) ((y / 2031f) * 480f);

            Log.v(LOG_TAG, "x,y=" + x + "," + y + "," + press);

//                if (x > maxMeasuredX) {
//                    maxMeasuredX = x;
//                }
//                if (y > maxMeasuredY) {
//                    maxMeasuredY = y;
//                }
//                if (x < minMeasuredX) {
//                    minMeasuredX = x;
//                }
//                if (y < minMeasuredY) {
//                    minMeasuredY = y;
//                }
        } else {
//                Log.v(LOG_TAG, "A. x,y=" + minMeasuredX + "," + minMeasuredY);
//                Log.v(LOG_TAG, "B. x,y=" + maxMeasuredX + "," + minMeasuredY);
//                Log.v(LOG_TAG, "C. x,y=" + minMeasuredX + "," + maxMeasuredY);
//                Log.v(LOG_TAG, "D. x,y=" + maxMeasuredX + "," + maxMeasuredY);
        }

//            if (press && mIsPressing) {
//                Log.v(LOG_TAG, String.format("HOLDING DOWN (%d, %d)", x, y));
//            } else if (!press && !mIsPressing) {
//                Log.v(LOG_TAG, String.format("NOT TOUCHING (%d, %d)", x, y));
//            } else if (press && !mIsPressing) {
//                Log.v(LOG_TAG, String.format("TOUCHING DOWN (%d, %d)", x, y));
//            } else if (!press && mIsPressing) {
//                Log.v(LOG_TAG, String.format("RELEASING TOUCH (%d, %d)", x, y));
//            }
        mIsPressing = press;
        return new TouchInput(x, y, press);
    }

    private TouchInput v4() throws TouchDriverReadingException {
        try {
            mTouchscreen.transfer(xRead, xBuffer, 3);
            mTouchscreen.transfer(yRead, yBuffer, 3);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Cannot process input readings from touch screen", e);
            Log.w(LOG_TAG, "Shutting down driver due to errors in reading!");
            throw new TouchDriverReadingException();
        }

        byte[] buffer = concat(xBuffer, yBuffer);
        boolean press = buffer[1] != 0;

        int x = (buffer[2] + (buffer[1] << 8) >> 4);
        int y = (buffer[5] + (buffer[4] << 8) >> 4);
        x = (int) ((x / 2031f) * 800f);
        y = (int) ((y / 2100f) * 480f);

        if (press) {
            Log.v(LOG_TAG, "x,y=" + x + "," + y);
        }
        mIsPressing = press;
        return new TouchInput(x, y, press);
    }

    private TouchInput v5() throws TouchDriverReadingException {
        try {
            mTouchscreen.transfer(xRead, xBuffer, 3);
            mTouchscreen.transfer(yRead, yBuffer, 3);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Cannot process input readings from touch screen", e);
            Log.w(LOG_TAG, "Shutting down driver due to errors in reading!");
            throw new TouchDriverReadingException();
        }

        byte[] buffer = concat(xBuffer, yBuffer);
        boolean press = buffer[1] != 0;

        int originalX = (buffer[2] + (buffer[1] << 8) >> 4);
        int originalY = (buffer[5] + (buffer[4] << 8) >> 4);
        // TODO the recalculation of x and y is ok for the center point but on edges we miss a few pixel!
        int x = (int) ((originalX / 2030f) * 800f);
        int y = (int) ((originalY / 2100f) * 480f);

        long millisSinceLastTouch = System.currentTimeMillis() - xyTime;
        boolean outlierX = false;
        boolean outlierY = false;
        boolean shiverring = false;
        boolean keepsPressing = press && mIsPressing;
        if (keepsPressing && !mOutlinerDetected) {
            boolean fastXyTracking = millisSinceLastTouch <= 50;
            if (fastXyTracking && cX != -1) {
                int xOffset = Math.abs(x - cX);
                if (xOffset > 12) {
                    outlierX = true;
                    x = cX;
                } else if (xOffset <= 12 && xOffset > 0) {
                    shiverring = true;
                    x = cX;
                }
            }
            if (fastXyTracking && cY != -1) {
                int yOffset = Math.abs(y - cY);
                if (yOffset > 12) {
                    outlierY = true;
                    y = cY;
                } else if (yOffset <= 12 && yOffset > 0) {
                    shiverring = true;
                    y = cY;
                }
            }
        }
        mOutlinerDetected = outlierX || outlierY;

        if (press) {
            Log.v(LOG_TAG, "x,y=" + originalX + "," + originalY + " | x,y=" + x + "," + y + " | cx,cy=" + cX + "," + cY + " (" + millisSinceLastTouch + "ms)" + (outlierX ? " CORRECTED-X!!!" : "") + (outlierY ? " CORRECTED-Y!!!" : "") + (shiverring ? " SHIVERRING!!!" : ""));
        } else if (mIsPressing && !press) {
            Log.v(LOG_TAG, "release");
        }
        mIsPressing = press;

        TouchInput touchInput = new TouchInput(x, y, press);

        if (press) {
            xyTime = System.currentTimeMillis();
            cX = x;
            cY = y;
        } else {
            cX = -1;
            cY = -1;
        }

        return touchInput;
    }

    private TouchInput v6() throws TouchDriverReadingException {
        try {
            mTouchscreen.transfer(xRead, xBuffer, 3);
            mTouchscreen.transfer(yRead, yBuffer, 3);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Cannot process input readings from touch screen", e);
            Log.w(LOG_TAG, "Shutting down driver due to errors in reading!");
            throw new TouchDriverReadingException();
        }

        byte[] buffer = concat(xBuffer, yBuffer);
        boolean press = buffer[1] != 0;

        int screenWidth = 800;
        int screenHeight = 480;

        int originalX = (buffer[2] + (buffer[1] << 8) >> 4);
        int originalY = (buffer[5] + (buffer[4] << 8) >> 4);
        // TODO the recalculation of x and y is ok for the center point but on edges we miss a few pixel!
        int x = (int) ((originalX / 2030f) * screenWidth);
        int y = (int) ((originalY / 2100f) * screenHeight);

        if (press) {
            if (x > maxMeasuredX) {
                maxMeasuredX = x;
            }
            if (y > maxMeasuredY) {
                maxMeasuredY = y;
            }
            if (x < minMeasuredX) {
                minMeasuredX = x;
            }
            if (y < minMeasuredY) {
                minMeasuredY = y;
            }
        } else if (mIsPressing && !press) {
            Log.v(LOG_TAG, "A. x,y=" + minMeasuredX + "," + minMeasuredY);
            Log.v(LOG_TAG, "B. x,y=" + maxMeasuredX + "," + minMeasuredY);
            Log.v(LOG_TAG, "C. x,y=" + minMeasuredX + "," + maxMeasuredY);
            Log.v(LOG_TAG, "D. x,y=" + maxMeasuredX + "," + maxMeasuredY);
        }

        float halfScreenWidth = screenWidth / 2f;
        float halfScreenHeight = screenHeight / 2f;

        int yErrorMargin = 24;
        float halfYDistance = halfScreenHeight - yErrorMargin;
        float travelledYDistance = y < halfScreenHeight ? halfYDistance - y - yErrorMargin : y - halfScreenHeight - yErrorMargin;
        int applicableYErrorMargin = (int) (((1 / halfYDistance) * travelledYDistance) * yErrorMargin);
        if (y < halfScreenHeight) {
            y = Math.max(0, y - applicableYErrorMargin);
        } else if (y > halfScreenHeight) {
            y = Math.min(screenHeight, y + applicableYErrorMargin);
        }

        int xErrorMargin = 20;
        float halfXDistance = halfScreenWidth - xErrorMargin;
        float travelledXDistance = x < halfScreenWidth ? halfXDistance - x - xErrorMargin : x - halfScreenWidth - xErrorMargin;
        int applicableXErrorMargin = (int) (((1 / halfXDistance) * travelledXDistance) * xErrorMargin);
        if (x < halfScreenWidth) {
            x = Math.max(0, x - applicableXErrorMargin);
        } else {
            x = Math.min(screenWidth, x + applicableXErrorMargin);
        }

        long millisSinceLastTouch = System.currentTimeMillis() - xyTime;
        boolean outlierX = false;
        boolean outlierY = false;
        boolean shiverring = false;
        boolean keepsPressing = press && mIsPressing;
        if (keepsPressing && !mOutlinerDetected) {
            boolean fastXyTracking = millisSinceLastTouch <= 50;
            if (fastXyTracking && cX != -1) {
                int xOffset = Math.abs(x - cX);
                if (xOffset > 12) {
                    outlierX = true;
                    x = cX;
                } else if (xOffset <= 12 && xOffset > 0) {
                    shiverring = true;
                    x = cX;
                }
            }
            if (fastXyTracking && cY != -1) {
                int yOffset = Math.abs(y - cY);
                if (yOffset > 12) {
                    outlierY = true;
                    y = cY;
                } else if (yOffset <= 12 && yOffset > 0) {
                    shiverring = true;
                    y = cY;
                }
            }
        }
        mOutlinerDetected = outlierX || outlierY;

        if (press) {
            Log.v(LOG_TAG, "x,y=" + originalX + "," + originalY + " | x,y=" + x + "," + y + " | cx,cy=" + cX + "," + cY + " | dx,dy=" + applicableXErrorMargin + "," + applicableYErrorMargin + " (" + millisSinceLastTouch + "ms)" + (outlierX ? " CORRECTED-X!!!" : "") + (outlierY ? " CORRECTED-Y!!!" : "") + (shiverring ? " SHIVERRING!!!" : ""));
        } else if (mIsPressing && !press) {
            Log.v(LOG_TAG, "release");
        }
        mIsPressing = press;

        TouchInput touchInput = new TouchInput(x, y, press);

        if (press) {
            xyTime = System.currentTimeMillis();
            cX = x;
            cY = y;
        } else {
            cX = -1;
            cY = -1;
        }

        return touchInput;
    }

    private TouchInput v7() throws TouchDriverReadingException {
        try {
            mTouchscreen.transfer(xRead, xBuffer, 3);
            mTouchscreen.transfer(yRead, yBuffer, 3);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Cannot process input readings from touch screen", e);
            Log.w(LOG_TAG, "Shutting down driver due to errors in reading!");
            throw new TouchDriverReadingException();
        }

        byte[] buffer = concat(xBuffer, yBuffer);
        boolean press = buffer[1] != 0;

        int screenWidth = getDriverProfile().getScreenDimension().getWidth();
        int screenHeight = getDriverProfile().getScreenDimension().getHeight();

        int originalX = (buffer[2] + (buffer[1] << 8) >> 4);
        int originalY = (buffer[5] + (buffer[4] << 8) >> 4);
        int x = (int) ((originalX / 2030f) * screenWidth);
        int y = (int) ((originalY / 2100f) * screenHeight);

        float halfScreenWidth = screenWidth / 2f;
        float halfScreenHeight = screenHeight / 2f;

        int yErrorMargin = 24; // TODO make parameter
        float halfYDistance = halfScreenHeight - yErrorMargin;
        float travelledYDistance = y < halfScreenHeight ? halfYDistance - y - yErrorMargin : y - halfScreenHeight - yErrorMargin;
        int applicableYErrorMargin = (int) (((1 / halfYDistance) * travelledYDistance) * yErrorMargin);
        if (y < halfScreenHeight) {
            y = Math.max(0, y - applicableYErrorMargin);
        } else if (y > halfScreenHeight) {
            y = Math.min(screenHeight, y + applicableYErrorMargin);
        }

        int xErrorMargin = 20; // TODO make parameter
        float halfXDistance = halfScreenWidth - xErrorMargin;
        float travelledXDistance = x < halfScreenWidth ? halfXDistance - x - xErrorMargin : x - halfScreenWidth - xErrorMargin;
        int applicableXErrorMargin = (int) (((1 / halfXDistance) * travelledXDistance) * xErrorMargin);
        if (x < halfScreenWidth) {
            x = Math.max(0, x - applicableXErrorMargin);
        } else {
            x = Math.min(screenWidth, x + applicableXErrorMargin);
        }

        long millisSinceLastTouch = System.currentTimeMillis() - xyTime;
        boolean outlierX = false;
        boolean outlierY = false;
        boolean shiverring = false;
        boolean keepsPressing = press && mIsPressing;
        if (keepsPressing && !mOutlinerDetected) {
            boolean fastXyTracking = millisSinceLastTouch <= 50;
            if (fastXyTracking && cX != -1) {
                int xOffset = Math.abs(x - cX);
                if (xOffset > 12) {
                    outlierX = true;
                    x = cX;
                } else if (xOffset <= 12 && xOffset > 0) {
                    shiverring = true;
                    x = cX;
                }
            }
            if (fastXyTracking && cY != -1) {
                int yOffset = Math.abs(y - cY);
                if (yOffset > 12) {
                    outlierY = true;
                    y = cY;
                } else if (yOffset <= 12 && yOffset > 0) {
                    shiverring = true;
                    y = cY;
                }
            }
        }
        mOutlinerDetected = outlierX || outlierY;

        if (press) {
            Log.v(LOG_TAG, "x,y=" + originalX + "," + originalY + " | x,y=" + x + "," + y + " | cx,cy=" + cX + "," + cY + " | dx,dy=" + applicableXErrorMargin + "," + applicableYErrorMargin + " (" + millisSinceLastTouch + "ms)" + (outlierX ? " CORRECTED-X!!!" : "") + (outlierY ? " CORRECTED-Y!!!" : "") + (shiverring ? " SHIVERRING!!!" : ""));
        } else if (mIsPressing && !press) {
            Log.v(LOG_TAG, "release");
        }
        mIsPressing = press;
        TouchInput touchInput = new TouchInput(x, y, press);

        if (press) {
            xyTime = System.currentTimeMillis();
            cX = x;
            cY = y;
        } else {
            cX = -1;
            cY = -1;
        }

        return touchInput;
    }

    private TouchInput v8() throws TouchDriverReadingException {
        try {
            mTouchscreen.transfer(xRead, xBuffer, 3);
            mTouchscreen.transfer(yRead, yBuffer, 3);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Cannot process input readings from touch screen", e);
            Log.w(LOG_TAG, "Shutting down driver due to errors in reading!");
            throw new TouchDriverReadingException();
        }

        byte[] buffer = concat(xBuffer, yBuffer);
        boolean press = isPressing(buffer);

        int screenWidth = getDriverProfile().getScreenDimension().getWidth();
        int screenHeight = getDriverProfile().getScreenDimension().getHeight();
        float halfScreenWidth = screenWidth / 2f;
        float halfScreenHeight = screenHeight / 2f;

        int originalX = (buffer[2] + (buffer[1] << 8) >> 4);
        int originalY = (buffer[5] + (buffer[4] << 8) >> 4);
        if (switchXY) {
            int temp = originalY;
            originalY = originalX;
            originalX = temp;
        }
        int x = (int) ((originalX / 2030f) * screenWidth);
        int y = (int) ((originalY / 2100f) * screenHeight);

        int yErrorMargin = 24; // TODO make parameter
        float halfYDistance = halfScreenHeight - yErrorMargin;
        float travelledYDistance = y < halfScreenHeight ? halfYDistance - y - yErrorMargin : y - halfScreenHeight - yErrorMargin;
        int applicableYErrorMargin = (int) (((1 / halfYDistance) * travelledYDistance) * yErrorMargin);
        if (y < halfScreenHeight) {
            y = Math.max(0, y - applicableYErrorMargin);
        } else if (y > halfScreenHeight) {
            y = Math.min(screenHeight, y + applicableYErrorMargin);
        }

        int xErrorMargin = 20; // TODO make parameter
        float halfXDistance = halfScreenWidth - xErrorMargin;
        float travelledXDistance = x < halfScreenWidth ? halfXDistance - x - xErrorMargin : x - halfScreenWidth - xErrorMargin;
        int applicableXErrorMargin = (int) (((1 / halfXDistance) * travelledXDistance) * xErrorMargin);
        if (x < halfScreenWidth) {
            x = Math.max(0, x - applicableXErrorMargin);
        } else {
            x = Math.min(screenWidth, x + applicableXErrorMargin);
        }

        if (inverseX) {
            x = (int) (x < halfScreenWidth ? halfScreenWidth+ (Math.abs(halfScreenWidth - x)) : halfScreenWidth - (Math.abs(halfScreenWidth - x)));
        }
        if (inverseY) {
            y = (int) (y < halfScreenHeight ? halfScreenHeight + (Math.abs(halfScreenHeight - y)) : halfScreenHeight - (Math.abs(halfScreenHeight - y)));
        }

        long millisSinceLastTouch = System.currentTimeMillis() - xyTime;
        boolean outlierX = false;
        boolean outlierY = false;
        boolean shiverring = false;
        boolean keepsPressing = press && mIsPressing;
        if (keepsPressing && !mOutlinerDetected) {
            boolean fastXyTracking = millisSinceLastTouch <= 50;
            if (fastXyTracking && cX != -1) {
                int xOffset = Math.abs(x - cX);
                if (flakeynessCorrection && xOffset > 12) {
                    outlierX = true;
                    x = cX;
                } else if (shiverringCorrection && xOffset <= 12 && xOffset > 0) {
                    shiverring = true;
                    x = cX;
                }
            }
            if (fastXyTracking && cY != -1) {
                int yOffset = Math.abs(y - cY);
                if (flakeynessCorrection && yOffset > 12) {
                    outlierY = true;
                    y = cY;
                } else if (shiverringCorrection && yOffset <= 12 && yOffset > 0) {
                    shiverring = true;
                    y = cY;
                }
            }
        }
        mOutlinerDetected = outlierX || outlierY;

        if (press) {
            Log.v(LOG_TAG, "x,y=" + originalX + "," + originalY + " | x,y=" + x + "," + y + " | cx,cy=" + cX + "," + cY + " | dx,dy=" + applicableXErrorMargin + "," + applicableYErrorMargin + " (" + millisSinceLastTouch + "ms)" + (outlierX ? " CORRECTED-X!!!" : "") + (outlierY ? " CORRECTED-Y!!!" : "") + (shiverring ? " SHIVERRING!!!" : ""));
        } else if (mIsPressing && !press) {
            Log.v(LOG_TAG, "release");
        }

        mIsPressing = press;
        TouchInput touchInput = new TouchInput(x, y, press);

        if (press) {
            xyTime = System.currentTimeMillis();
            cX = x;
            cY = y;
        } else {
            cX = -1;
            cY = -1;
        }

        return touchInput;
    }

    @Override
    public void close() {
        if (mTouchscreen != null) {
            try {
                mTouchscreen.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Cannot close the touchscreen input...");
            }
        }
    }
}
