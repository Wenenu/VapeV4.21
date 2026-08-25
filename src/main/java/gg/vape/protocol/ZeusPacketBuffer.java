package gg.vape.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** Local serializer retained for model compatibility; it has no transport dependency. */
public final class ZeusPacketBuffer {
    private final ByteArrayOutputStream output;
    private final DataOutputStream writer;
    private DataInputStream reader;

    public ZeusPacketBuffer() {
        this.output = new ByteArrayOutputStream();
        this.writer = new DataOutputStream(output);
    }

    public ZeusPacketBuffer(byte[] data) {
        this();
        this.reader = new DataInputStream(new ByteArrayInputStream(data));
    }

    public static int getVarIntSize(int value) {
        for (int size = 1; size < 5; size++) {
            if ((value & (-1 << (size * 7))) == 0) {
                return size;
            }
        }
        return 5;
    }

    public void writeDouble(double value) { write(() -> writer.writeDouble(value)); }
    public short readShort() { return read(() -> reader.readShort()); }
    public void writeBoolean(boolean value) { write(() -> writer.writeBoolean(value)); }
    public void writeUuid(UUID value) { writeLong(value.getMostSignificantBits()); writeLong(value.getLeastSignificantBits()); }
    public void writeFloat(float value) { write(() -> writer.writeFloat(value)); }
    public boolean readBoolean() { return read(() -> reader.readBoolean()); }
    public void writeVarInt(int value) {
        while ((value & 0xFFFFFF80) != 0) {
            writeByte(value & 0x7F | 0x80);
            value >>>= 7;
        }
        writeByte(value);
    }
    public void writeShort(short value) { write(() -> writer.writeShort(value)); }
    public String readString(int maxLength) {
        int length = readVarInt();
        if (length < 0 || length > maxLength * 4) throw new IllegalArgumentException("Invalid string length");
        byte[] bytes = new byte[length];
        read(() -> { reader.readFully(bytes); return null; });
        String value = new String(bytes, StandardCharsets.UTF_8);
        if (value.length() > maxLength) throw new IllegalArgumentException("String too long");
        return value;
    }
    public void writeLong(long value) { write(() -> writer.writeLong(value)); }
    public int readVarInt() {
        int result = 0;
        for (int shift = 0; shift < 35; shift += 7) {
            int value = readByte();
            result |= (value & 0x7F) << shift;
            if ((value & 0x80) == 0) return result;
        }
        throw new IllegalArgumentException("VarInt too big");
    }
    public UUID readUuid() { return new UUID(readLong(), readLong()); }
    public int readInt() { return read(() -> reader.readInt()); }
    public void writeBytes(byte[] source) { write(() -> writer.write(source)); }
    public <E extends Enum<E>> E readEnum(Class<E> enumType) { return enumType.getEnumConstants()[readVarInt()]; }
    public void writeString(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeVarInt(bytes.length);
        writeBytes(bytes);
    }
    public void writeInt(int value) { write(() -> writer.writeInt(value)); }
    public long readLong() { return read(() -> reader.readLong()); }
    public float readFloat() { return read(() -> reader.readFloat()); }
    public double readDouble() { return read(() -> reader.readDouble()); }
    public void writeEnum(Enum<?> value) { writeVarInt(value.ordinal()); }
    public byte[] toByteArray() { return output.toByteArray(); }

    private void write(IoWrite operation) {
        try { operation.run(); } catch (IOException exception) { throw new IllegalStateException(exception); }
    }
    private <T> T read(IoRead<T> operation) {
        if (reader == null) throw new IllegalStateException("Buffer is not readable");
        try { return operation.run(); } catch (IOException exception) { throw new IllegalStateException(exception); }
    }
    private void writeByte(int value) { write(() -> writer.writeByte(value)); }
    private int readByte() { return read(() -> reader.readUnsignedByte()); }

    @FunctionalInterface private interface IoWrite { void run() throws IOException; }
    @FunctionalInterface private interface IoRead<T> { T run() throws IOException; }
}
