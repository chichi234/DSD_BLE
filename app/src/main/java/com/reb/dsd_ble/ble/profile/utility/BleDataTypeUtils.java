package com.reb.dsd_ble.ble.profile.utility;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Administrator on 2015/6/28.
 */
public class BleDataTypeUtils {

	/**
	 * 
	 * @title:
	 * @description:
	 * @param:@param src
	 * @param:@return
	 * @return:String
	 * @throws
	 */
	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	/**
	 * 将字符串转成16进制字符串
	 * @param s
	 * @return
	 */
	public static String toStringHex1(String s) {
		byte[] baKeyword = new byte[s.length() / 2];
		for (int i = 0; i < baKeyword.length; i++) {
			try {
				baKeyword[i] = (byte) (0xff & Integer.parseInt(
						s.substring(i * 2, i * 2 + 2), 16));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			s = new String(baKeyword, "utf-8");// UTF-16le:Not
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return s;
	}

	/**
	 * 将字符串转成16进制数组
	 * @param str
	 * @param charsetName  编码方式
	 * @return
	 */
	public static byte[] toStringHex(String str, String charsetName) {
		byte[] baKeyword = new byte[0];

		try {
			baKeyword = str.getBytes(charsetName);
			return baKeyword;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return baKeyword;

	}

	/**
	 * 将16进制数组转成字符串字符串
	 * 
	 * @param data
	 * @param charsetName
	 *            编码方式
	 * @return
	 */
	public static String HextoString(byte[] data, String charsetName) {
		String baKeyword = "defual";

		try {
			baKeyword = new String(data, charsetName);
			return baKeyword;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return baKeyword;

	}

	public static byte Int2Byte(int data) {
		byte b = (byte) (data & 0xff);
		return b;
	}

	/**
	 * 将int数值转换为占四个字节的byte数组，本方法适用于(低位在前，高位在后)的顺序。
	 * 
	 * @param value
	 *            要转换的int值
	 * @return byte数组
	 */
	public static byte[] intToBytes(int value, int length) {
		byte[] src = new byte[length];
		for (int i = 0; i < length; i++) {
			src[i] = (byte) ((value >> (8 * i)) & 0xFF);
		}
		return src;
	}

	/**
	 * 将int数值转换为byte数组，本方法适用于(低位在后，高位在前)的顺序
	 * 
	 * @param value
	 *            要转换的int值
	 * @return byte数组
	 */
	public static byte[] intToBytes2(int value, int length) {
		byte[] src = new byte[length];
		for (int i = 0; i < length; i++) {
			src[i] = (byte) ((value >> (8 * (length - i - 1))) & 0xFF);
		}
		return src;
	}

	/**
	 * byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序，和和intToBytes（）配套使用
	 * 
	 * @param src
	 *            byte数组
	 * @param offset
	 *            从数组的第offset位开始
	 * @param length
	 *            字节长度
	 * @return int数值
	 */
	public static int bytesToInt(byte[] src, int offset, int length) {
		int value = ((src[offset] & 0xFF));
		for (int i = 1; i < length; i++) {
			value = (int) (value | (src[offset + i] & 0xFF) << 8 * i);
		 }
//		String str = String.valueOf(value);
//		if(str.length()==1){
//			value = Integer.valueOf("0"+ str);
//		}
		return value;
	}

	/**
	 * 
	 * @param src
	 * @return
	 */
	public static int byteToInt(byte src) {
		int value = 0;
		value = src & 0XFF;
		return value;
	}

	/**
	 * byte数组中取int数值，本方法适用于(低位在后，高位在前)的顺序
	 */
	public static int bytesToInt2(byte[] src, int offset, int length) {
		int value = ((src[offset] & 0xFF) << 8 * length);
		for (int i = 1; i < length; i++) {
			value = (int) (value | (src[offset + i] & 0xFF) << 8 * (length - i));
		}
		return value;
	}

	/**
	 * 
	 * @title:
	 * @description:
	 * @param:@param data
	 * @param:@return
	 * @return:byte[]
	 * @throws
	 */
	public static byte[] IntDate2Byte(int data) {
		byte[] bytes = new byte[3];
		bytes[2] = (byte) (data & 0xff);
		bytes[1] = (byte) ((data >> 8) & 0xff);
		bytes[0] = (byte) ((data >> 16) & 0xff);
		return bytes;
	}

	/**
	 * 
	 * @title:
	 * @description:
	 * @param:@param b
	 * @param:@return
	 * @return:int
	 * @throws
	 */
	public static int byte2IntDate(byte[] b) {
		int date = 0;
		date = ((b[0] << 16) & 0xff0000);
		date |= (b[1] << 8) & 0xff00;
		date |= (b[2]) & 0xff;
		return date;
	}

	/**
	 * 根据开始时间和结束时间返回时间段内的时间集合
	 * 
	 * @param s
	 * @param e
	 * @return List
	 * @throws ParseException
	 */
	public static List<String> getDatesBetweenTwoDate(int s, int e) {
		int type = s < e ? 1 : -1;
		List<String> strDateList = new ArrayList<String>();
		String start = String.valueOf(s);
		String end = String.valueOf(e);
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd", Locale.getDefault());
		Date beginDate = null;
		Date endDate = null;
		try {
			beginDate = sdf.parse(start);
			endDate = sdf.parse(end);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		List<Date> lDate = new ArrayList<Date>();
		lDate.add(beginDate);// 把开始时间加入集合
		Calendar cal = Calendar.getInstance();
		// 使用给定的 Date 设置此 Calendar 的时间
		cal.setTime(beginDate);
		boolean bContinue = true;
		while (bContinue) {
			if (type == 1) {
				// 根据日历的规则，为给定的日历字段添加或减去指定的时间量
				cal.add(Calendar.DAY_OF_MONTH, 1);
				// 测试此日期是否在指定日期之后
				if (endDate.after(cal.getTime())) {
					lDate.add(cal.getTime());
					strDateList.add(sdf.format(cal.getTime()));
				} else {
					break;
				}
			} else if (type == -1) {
				// 根据日历的规则，为给定的日历字段添加或减去指定的时间量
				cal.add(Calendar.DAY_OF_MONTH, -1);
				// 测试此日期是否在指定日期之后
				if (endDate.before(cal.getTime())) {
					lDate.add(cal.getTime());
					strDateList.add(sdf.format(cal.getTime()));
				} else {
					break;
				}
			}

		}
		lDate.add(endDate);// 把结束时间加入集合
		strDateList.add(sdf.format(endDate));
		return strDateList;
	}

	/**
	 * Byte转Bit
	 */
	public static String byteToBit(byte b) {
		return "" + (byte) ((b >> 7) & 0x1) + (byte) ((b >> 6) & 0x1)
				+ (byte) ((b >> 5) & 0x1) + (byte) ((b >> 4) & 0x1)
				+ (byte) ((b >> 3) & 0x1) + (byte) ((b >> 2) & 0x1)
				+ (byte) ((b >> 1) & 0x1) + (byte) ((b >> 0) & 0x1);
	}

	/**
	 * Bit转Byte
	 */
	public static byte BitToByte(String byteStr) {
		int re, len;
		if (null == byteStr) {
			return 0;
		}
		len = byteStr.length();
		if (len != 4 && len != 8) {
			return 0;
		}
		if (len == 8) {// 8 bit处理
			if (byteStr.charAt(0) == '0') {// 正数
				re = Integer.parseInt(byteStr, 2);
			} else {// 负数
				re = Integer.parseInt(byteStr, 2) - 256;
			}
		} else {// 4 bit处理
			re = Integer.parseInt(byteStr, 2);
		}
		return (byte) re;
	}
	/**
	 * Bit转Byte
	 */
	public static int BitToDec(String byteStr) {
		if (null == byteStr) {
			return 0;
		}
		BigInteger src1 = new BigInteger(byteStr, 2);
		return Integer.parseInt(src1.toString());
	}
	/**
	 * Byte[] 返回指定长度的 Byte[]
	 */
	public static byte[] bytesCut(byte[] src, int offset, int length) {
		byte[] value = new byte[length];
		if(offset>src.length||(offset+length)>src.length){
			return value;
		}
		
		for (int i = 0; i < length; i++) {
			value[i] = src[offset+i];
		}
		return value;
	}

	public static int minustoint(byte x) {
		int result = 0;
		byte n = 0;
		if (((x >> 7) & 0x1) == 0) {
			result = BleDataTypeUtils.byteToInt(x);

		} else {
			n = (byte) (x - 1);
			result = BleDataTypeUtils.byteToInt((byte) (n ^ 255)) * (-1);
		}
		return result;
	}
	
	/**
	 * Bit转Byte
	 */
	public static byte BitToByteforWeek(String byteStr) {
		int re, len;
		if (null == byteStr) {
			return 0;
		}
		len = byteStr.length();
		if (len != 4 && len != 8) {
			return 0;
		}
		if (len == 8) {// 8 bit处理
			if (byteStr.charAt(0) == '0') {// 正数
				re = Integer.parseInt(byteStr, 2);
			} else {// 负数
				re = Integer.parseInt(byteStr, 2) - 256;
			}
		} else {// 4 bit处理
			re = Integer.parseInt(byteStr, 2);
		}
		return (byte) re;
	}

}
