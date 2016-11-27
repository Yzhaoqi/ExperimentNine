package com.yzq.android.experimentnine.Compoment;

/**
 * Created by YZQ on 2016/11/25.
 */

public class Index {
    private String name;
    private String value;
    private String detail;

    public Index(String name, String value, String detail) {
        this.name = name;
        this.value = value;
        this.detail = detail;
    }

    public String getName() {
        return name;
    }

    public String getDetail() {
        return detail;
    }

    public String getValue() {
        return value;
    }
}
