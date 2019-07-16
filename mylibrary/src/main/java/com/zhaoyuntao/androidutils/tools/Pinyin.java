package com.zhaoyuntao.androidutils.tools;


import com.zhaoyuntao.androidutils.pinyin.PinyinHelper;
import com.zhaoyuntao.androidutils.pinyin.format.HanyuPinyinCaseType;
import com.zhaoyuntao.androidutils.pinyin.format.HanyuPinyinOutputFormat;
import com.zhaoyuntao.androidutils.pinyin.format.HanyuPinyinToneType;
import com.zhaoyuntao.androidutils.pinyin.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * Created by zhaoyuntao on 2018-11-14.
 */

public class Pinyin {
    /**
     * 汉字转换为拼音
     *
     * @param chinese
     * @return
     */
    public static String toPinyin(String chinese) throws NullPointerException {
        String pinyinStr = "";
        char[] newChar = chinese.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < newChar.length; i++) {
            if (newChar[i] > 128) {
                try {
                    pinyinStr += PinyinHelper.toHanyuPinyinStringArray(newChar[i], defaultFormat)[0];
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else {
                pinyinStr += newChar[i];
            }
        }
        return pinyinStr;
    }
}
