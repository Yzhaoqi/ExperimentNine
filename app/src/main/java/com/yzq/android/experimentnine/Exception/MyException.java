package com.yzq.android.experimentnine.Exception;

/**
 * Created by YZQ on 2016/11/27.
 */

public class MyException extends Exception {
    String errMessage;
    public MyException(String errMessager) {
        this.errMessage = errMessager;
    }

    public String getErrMessage() {
        return errMessage;
    }
}
