package com.example.finalbledemo.ble;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Random;

/**
 * 
 * TODO 字节处理
 * 
 * @author fpf
 * @data: 2015年1月26日 下午3:16:42
 * @version: V1.0
 */
@SuppressLint("SimpleDateFormat")
public class BytesUtils {
	/**
	 * 将指定字符串src，以每两个字符分割转换为16进制形式 如："2B44EFD9" --> byte[]{0x2B, 0x44, 0xEF,
	 * 0xD9}
	 * 
	 * @param src
	 *            String
	 * @return byte[]
	 */
	public static byte[] HexString2Bytes(String src) {
		byte[] ret = new byte[src.length() / 2];
		byte[] tmp = src.getBytes();
		for (int i = 0; i < src.length() / 2; i++) {
			ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
		}
		return ret;
	}

	/**
	 * 将两个ASCII字符合成一个字节； 如："EF"--> 0xEF
	 * 
	 * @param src0
	 *            byte
	 * @param src1
	 *            byte
	 * @return byte
	 */
	public static byte uniteBytes(byte src0, byte src1) {
		byte _b0 = Byte.decode("0x" + new String(new byte[] { src0 })).byteValue();
		_b0 = (byte) (_b0 << 4);
		byte _b1 = Byte.decode("0x" + new String(new byte[] { src1 })).byteValue();
		byte ret = (byte) (_b0 ^ _b1);
		return ret;
	}

	/**
	 * 
	 * TODO BLE配对验证的KEY的生成
	 * 
	 * @return
	 * @throw
	 * @return String
	 */
	public static String getBleKey() {
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyMMddHH");
		String date = sDateFormat.format(new java.util.Date());
		Random random = new Random();
		int a = random.nextInt(9999);
		return date + Integer.toString(a);
	}
}
