package com.reb.dsd_ble;

import com.reb.dsd_ble.util.HexStringConver;

import org.junit.Test;

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
        System.out.print(HexStringConver.String2HexStr("123,oassdd"));
    }
}