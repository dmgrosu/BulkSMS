package com.emotion.ecm.enums;

public enum MessageStatus {
    READY(false),
    PENDING(false),
    SENT(true),
    DELIVERED(true),
    EXPIRED(true),
    BLACKLISTED(false),
    REJECTED(true);

    private final boolean sent;

    MessageStatus(boolean sent) {
        this.sent = sent;
    }

    public boolean isSent() {
        return sent;
    }
}
