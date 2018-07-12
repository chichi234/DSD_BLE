package com.reb.dsd_ble;

import com.reb.dsd_ble.util.HexStringConver;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
//        System.out.print(HexStringConver.String2HexStr("123,oassdd"));
        System.out.println(new String(new byte[]{65, 84, 43, 84, 88, 80, 79, 87, 69, 82, 61, 48, 92, 114, 92, 110}));
        System.out.println(Arrays.toString("#OpenDSDAtEngine#".getBytes()));
        System.out.println(Arrays.toString("AT+TXPOWER=".getBytes()));
        System.out.println(Arrays.toString("\r\n".getBytes()));
        System.out.println(HexStringConver.String2HexStr("\r\n"));
    }
}