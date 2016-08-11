package com.guster.skydb;

/**
 * Created by Gusterwoei on 8/11/16.
 */
public class SkyDbException extends RuntimeException {
    public SkyDbException() {}

    public SkyDbException(String detailMessage) {
        super(detailMessage);
    }

    public SkyDbException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public SkyDbException(Throwable throwable) {
        super(throwable);
    }
}
