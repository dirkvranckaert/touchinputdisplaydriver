package eu.vranckaert.driver.touch.profile;

import java.io.Serializable;

/**
 * Created by dirkvranckaert on 13/10/2017.
 */

public class ScreenDimension implements Serializable {
    private int width;
    private int height;
    private Ratio ratio;

    public ScreenDimension(int width, int height, Ratio ratio) {
        this.width = width;
        this.height = height;
        this.ratio = ratio;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Ratio getRatio() {
        return ratio;
    }

    public enum Ratio {
        R_4_3,
        R_14_9,
        R_16_9,
        R_5_4,
        R_16_10,
        R_15_9,
        UNKNOWN;
    }
}
