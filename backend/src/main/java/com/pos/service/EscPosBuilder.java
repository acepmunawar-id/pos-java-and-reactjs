package com.pos.service;

import java.io.ByteArrayOutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Minimal fluent builder for raw ESC/POS commands understood by common 58/80mm
 * thermal receipt printers. Kept dependency-free (no escpos-coffee etc.) since
 * the command set needed for a simple receipt is tiny.
 */
public class EscPosBuilder {

    private static final byte ESC = 0x1B;
    private static final byte GS = 0x1D;

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    public EscPosBuilder init() {
        write(ESC, 0x40); // ESC @ : initialize printer
        return this;
    }

    public EscPosBuilder alignLeft() {
        return align(0);
    }

    public EscPosBuilder alignCenter() {
        return align(1);
    }

    public EscPosBuilder alignRight() {
        return align(2);
    }

    private EscPosBuilder align(int n) {
        write(ESC, 0x61, (byte) n); // ESC a n
        return this;
    }

    public EscPosBuilder bold(boolean on) {
        write(ESC, 0x45, (byte) (on ? 1 : 0)); // ESC E n
        return this;
    }

    public EscPosBuilder doubleHeight(boolean on) {
        write(GS, 0x21, (byte) (on ? 0x11 : 0x00)); // GS ! n (width+height x2 when on)
        return this;
    }

    public EscPosBuilder line(String text) {
        text(text);
        feed(1);
        return this;
    }

    public EscPosBuilder text(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        buffer.writeBytes(bytes);
        return this;
    }

    public EscPosBuilder feed(int lines) {
        for (int i = 0; i < lines; i++) {
            buffer.write('\n');
        }
        return this;
    }

    public EscPosBuilder divider(int width) {
        return line("-".repeat(width));
    }

    /** Left-aligned label + right-aligned value on one printed line of the given width. */
    public EscPosBuilder twoColumns(String left, String right, int width) {
        int spaces = width - left.length() - right.length();
        String line = spaces > 0 ? left + " ".repeat(spaces) + right : left + " " + right;
        return line(line);
    }

    public EscPosBuilder cut() {
        feed(3);
        write(GS, 0x56, 0x01); // GS V 1 : partial cut
        return this;
    }

    public byte[] build() {
        return buffer.toByteArray();
    }

    private void write(int... bytesAsInts) {
        try {
            for (int b : bytesAsInts) {
                buffer.write(b);
            }
        } catch (Exception e) {
            throw new UncheckedIOException(new java.io.IOException(e));
        }
    }
}
