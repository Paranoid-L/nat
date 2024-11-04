package com.gitee.leo_92.nat.common.protocol;

import java.util.Map;

public class NatMessage {
    private NatMessageType type;
    private Map<String, Object> metaData;
    private byte[] data;

    public NatMessageType getType() {
        return type;
    }

    public void setType(NatMessageType type) {
        this.type = type;
    }

    public Map<String, Object> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, Object> metaData) {
        this.metaData = metaData;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
