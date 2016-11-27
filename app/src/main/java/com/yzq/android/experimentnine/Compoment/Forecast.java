package com.yzq.android.experimentnine.Compoment;

/**
 * Created by YZQ on 2016/11/25.
 */

public class Forecast {
    private String date;
    private String high;
    private String low;
    private String type;

    public Forecast(String date, String high, String low, String type) {
        this.date = date;
        this.high = high;
        this.low = low;
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public String getHigh() {
        return high;
    }

    public String getLow() {
        return low;
    }

    public String getType() {
        return type;
    }
}
