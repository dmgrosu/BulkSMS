package com.emotion.ecm.smpp;

@FunctionalInterface
public interface ReceivedListener<T> {
    void onReceived(T pduMessage);
}
