package com.gitee.leo_92.nat.common.protocol;

import com.gitee.leo_92.nat.common.exception.NatException;

public enum NatMessageType {
    REGISTER(1),
    REGISTER_RESULT(2),
    CONNECTED(3),
    DISCONNECTED(4),
    DATA(5),
    KEEPALIVE(6);

    private final int code;

    NatMessageType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static NatMessageType valueOf(int code) throws NatException {
        for (NatMessageType item : NatMessageType.values()) {
            if (item.code == code) {
                return item;
            }
        }
        throw new NatException("NatMessageType code error: " + code);
    }
}
