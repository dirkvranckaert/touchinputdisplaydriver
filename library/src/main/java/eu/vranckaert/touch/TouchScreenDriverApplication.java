package eu.vranckaert.touch;

import android.app.Application;

/**
 * Created by dirkvranckaert on 15/10/2017.
 */

public class TouchScreenDriverApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        TouchScreenDriverManager.getInstance().createDriver(this);
    }

    @Override
    public void onTerminate() {
        TouchScreenDriverManager.getInstance().stopDriver(this);
        super.onTerminate();
    }
}
