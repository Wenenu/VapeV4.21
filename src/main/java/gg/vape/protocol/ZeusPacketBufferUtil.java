package gg.vape.protocol;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class ZeusPacketBufferUtil {
    private ZeusPacketBufferUtil() {
    }

    public static int readVarInt(byte[] ignored) {
        throw new UnsupportedOperationException("Transport packet decoding is disabled");
    }

    public static void writeVarInt(byte[] ignored, int value) {
        throw new UnsupportedOperationException("Transport packet encoding is disabled");
    }

    public static <E extends Enum<E>> E readEnum(byte[] ignored, Class<E> enumType) {
        throw new UnsupportedOperationException("Transport packet decoding is disabled");
    }

    public static void writeString(byte[] ignored, String value) {
        if (value == null) throw new IllegalArgumentException("value");
    }

    public static UUID readUuid(byte[] ignored) {
        throw new UnsupportedOperationException("Transport packet decoding is disabled");
    }

    public static void writeUuid(byte[] ignored, UUID uuid) {
        if (uuid == null) throw new IllegalArgumentException("uuid");
    }

    public static String readString(byte[] ignored, int maxCharacters) {
        throw new UnsupportedOperationException("Transport packet decoding is disabled");
    }

    public static void writeEnum(byte[] ignored, Enum<?> value) {
        if (value == null) throw new IllegalArgumentException("value");
    }
}
