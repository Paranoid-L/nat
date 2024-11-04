package com.gitee.leo_92.nat.common.codec;

import com.gitee.leo_92.nat.common.protocol.NatMessage;
import com.gitee.leo_92.nat.common.protocol.NatMessageType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.CharsetUtil;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class NatMessageDecoder extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {

        int type = msg.readInt();
        NatMessageType natMessageType = NatMessageType.valueOf(type);

        int metaDataLength = msg.readInt();
        CharSequence metaDataString = msg.readCharSequence(metaDataLength, CharsetUtil.UTF_8);
        JSONObject jsonObject = new JSONObject(metaDataString.toString());
        Map<String, Object> metaData = jsonObject.toMap();

        byte[] data = null;
        if (msg.isReadable()) {
            data = ByteBufUtil.getBytes(msg);
        }

        NatMessage natMessage = new NatMessage();
        natMessage.setType(natMessageType);
        natMessage.setMetaData(metaData);
        natMessage.setData(data);

        out.add(natMessage);
    }
}
