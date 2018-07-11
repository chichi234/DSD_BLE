package com.reb.dsd_ble.util;

import android.text.TextUtils;

import java.util.Locale;

/**
 * Created by Administrator on 2015/12/12 0012.
 */
public class HexStringConver {


    private final static String mHexStr = "0123456789ABCDEF";
    /**
     * 检查16进制字符串是否有效
     * @param sHex String 16进制字符串
     * @return boolean
     */
    public static boolean checkHexStr(String sHex){
        String sTmp = getRuledHexString(sHex);
        int iLen = sTmp.length();
        if (iLen > 1 && iLen%2 == 0){
            for(int i=0; i<iLen; i++)
                if (!mHexStr.contains(sTmp.substring(i, i+1)))
                    return false;
            return true;
        }
        else
            return false;
    }

    /**
     * 将字符串规范化
     * @param s
     * @return
     */
    public static String getRuledHexString(String s){
        String sTmp = s.toString().trim().replace(" ", "").toUpperCase(Locale.US);
        if(sTmp.startsWith("0X")){
            sTmp = sTmp.substring(2);
        }
        if(TextUtils.isEmpty(sTmp)){
            return null;
        }
        int iLen = sTmp.length();
        if(iLen % 2 != 0){
            sTmp = "0" + sTmp;
        }
        return sTmp;
    }

    public static byte[] convertHexString2Bytes(String hexString){
        String ruledStr = getRuledHexString(hexString);
        if (checkHexStr(ruledStr)){
            int iLen = ruledStr.length();
            byte[]  bs = new byte[iLen / 2];
            for (int i = 0; i < bs.length; i++) {
                int d = Integer.valueOf(ruledStr.substring(i*2 , i*2 + 2) , 16);
                bs[i] = (byte)d;
            }
            return bs;
        }

        return null;
    }


    public static String getPrintHexString(String str){
        String ruledStr = getRuledHexString(str);
        StringBuffer sb = new StringBuffer("[ ");
        if (checkHexStr(ruledStr)){
            for (int i = 0; i < str.length() / 2; i++) {
                sb.append(ruledStr.substring(i*2 , i*2 + 2) + " ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public static int[] bytes2ints(byte[] datas){
        int[] d = new int[datas.length];
        for (int i = 0; i < datas.length; i++) {
            d[i] = datas[i] & 0xFF;
        }
        return d;
    }

    public static String bytes2HexStr(byte[] datas){
        StringBuffer sb = new StringBuffer("[");
        for (int i = 0; i < datas.length; i++) {
            sb.append(String.format("%02x", datas[i] & 0xFF) + ", ");
        }
        sb.replace(sb.length() - 2, sb.length(), "]");
        return sb.toString();
    }

    public static String String2HexStr(String string) {
        StringBuilder hex = new StringBuilder();
        if (string != null && !string.isEmpty()) {
            char[] chars = string.toCharArray();
            for (char aChar : chars) {
                hex.append(String.format(Locale.getDefault(), "%02X", (int) aChar));
            }
        }
        return hex.toString();
    }

}
