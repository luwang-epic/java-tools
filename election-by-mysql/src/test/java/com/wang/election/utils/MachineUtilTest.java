package com.wang.election.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @Author: wanglu51
 * @Date: 2024/3/4 10:30
 */
public class MachineUtilTest {

    @Test
    public void testGetIpAddress() {
        String ip = MachineUtil.getIpAddress();
        Assertions.assertEquals("127.0.0.1", ip);
    }

}
