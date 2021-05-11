package us.myles.ViaVersion.api.type.types;

import io.netty.buffer.ByteBuf;

public class VarIntType {
    public static int readPrimitive(ByteBuf buffer) {
        int out = 0;
        int bytes = 0;
        byte in;
        do {
            in = buffer.readByte();

            out |= (in & 0x7F) << (bytes++ * 7);

            if (bytes > 5) { // 5 is maxBytes
                throw new RuntimeException("VarInt too big");
            }

        } while ((in & 0x80) == 0x80);
        return out;
    }

    public static void writePrimitive(ByteBuf buffer, int object) {
        int part;
        do {
            part = object & 0x7F;

            object >>>= 7;
            if (object != 0) {
                part |= 0x80;
            }

            buffer.writeByte(part);

        } while (object != 0);
    }
}