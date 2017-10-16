package eu.vranckaert.driver.touch.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 * <p>
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class MainActivity extends Activity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private CountDownTimer debugTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        updateDebugInfo();

        findViewById(R.id.launch_touch_debug).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Press 10 times quickly to quite touch test mode!", Toast.LENGTH_LONG).show();
                findViewById(R.id.touch_debug).setVisibility(View.VISIBLE);
            }
        });
        findViewById(R.id.hello).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText helloTextInput = findViewById(R.id.hello_text);
                if (helloTextInput.getVisibility() == View.GONE) {
                    helloTextInput.setVisibility(View.VISIBLE);
                    return;
                }

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                String helloText = helloTextInput.getText().toString();

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Hello Android Things")
                        .setMessage(helloText)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    private void updateDebugInfo() {
        NetworkInterface networkInterface = getActiveNetwork();
        TextView ip = findViewById(R.id.debug_ip);
        TextView wifi = findViewById(R.id.debug_wifi);
        if (networkInterface != null) {
            ip.setText(getIpAddress(networkInterface, true));
            if (networkInterface.getDisplayName().contains("wlan")) {
                wifi.setText(getCurrentSsid());
            } else {
                wifi.setText(networkInterface.getDisplayName());
            }
        } else {
            wifi.setText("Waiting for known WiFi...");
        }
    }

    private void restartDebugTimer() {
        Log.d(LOG_TAG, "Restarting the debug timer...");
        cancelDebugTimer();
        debugTimer = new CountDownTimer(2000, 100) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                Log.d(LOG_TAG, "Updating the debug info...");
                updateDebugInfo();
                restartDebugTimer();
            }
        };
        debugTimer.start();
    }

    private void cancelDebugTimer() {
        if (debugTimer != null) {
            debugTimer.cancel();
            debugTimer = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        restartDebugTimer();
    }

    @Override
    protected void onPause() {
        cancelDebugTimer();
        super.onPause();
    }

    private String getIpAddress(NetworkInterface networkInterface, boolean useIPv4) {
        List<InetAddress> addrs = Collections.list(networkInterface.getInetAddresses());
        for (InetAddress addr : addrs) {
            if (!addr.isLoopbackAddress()) {
                String sAddr = addr.getHostAddress();
                //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                boolean isIPv4 = sAddr.indexOf(':') < 0;

                if (useIPv4) {
                    if (isIPv4)
                        return sAddr;
                } else {
                    if (!isIPv4) {
                        int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                        return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                    }
                }
            }
        }
        return null;
    }

    private static NetworkInterface getActiveNetwork() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            if (interfaces != null && !interfaces.isEmpty()) {
                int count = interfaces.size();
                for (int i = 0; i < count; i++) {
                    NetworkInterface networkInterface = interfaces.get(i);
                    if (networkInterface.isUp() && (networkInterface.getName().contains("wlan") || networkInterface.getName().contains("eth"))) {
                        return networkInterface;
                    }
                }
            }
        } catch (Exception ex) {
        } // for now eat exceptions
        return null;
    }

    public String getCurrentSsid() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.getSSID())) {
                return connectionInfo.getSSID().replace("\"", "");
            }
        }
        return null;
    }
}
