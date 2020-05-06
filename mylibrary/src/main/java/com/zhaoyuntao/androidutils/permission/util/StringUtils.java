package com.zhaoyuntao.androidutils.permission.util;

/**
 */
public class StringUtils {

    private static final String DIGITS_TEXT = "0123456789ABCDEF";
    private static final char[] DIGITS_ARRAY = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static String textToHex(String text) {
        StringBuilder builder = new StringBuilder();
        byte[] textBytes = text.getBytes();
        int bit;

        for (int i = 0; i < textBytes.length; i++) {
            bit = (textBytes[i] & 0x0f0) >> 4;
            builder.append(DIGITS_ARRAY[bit]);
            bit = textBytes[i] & 0x0f;
            builder.append(DIGITS_ARRAY[bit]);
        }
        return builder.toString().trim();
    }

    public static String hexToText(String hexText) {
        char[] hexArray = hexText.toCharArray();
        byte[] hexBytes = new byte[hexText.length() / 2];
        for (int i = 0; i < hexBytes.length; i++) {
            int n = DIGITS_TEXT.indexOf(hexArray[2 * i]) * 16;
            n += DIGITS_TEXT.indexOf(hexArray[2 * i + 1]);
            hexBytes[i] = (byte) (n & 0xff);
        }
        return new String(hexBytes);
    }
}
