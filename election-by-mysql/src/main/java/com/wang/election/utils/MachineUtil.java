package com.wang.election.utils;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 机器工具类
 *
 * @Author: wanglu51
 * @Date: 2024/3/2 14:16
 */
@Slf4j
public class MachineUtil {
    /**
     * 获取系统的IP地址
     * @return ip address
     */
    public static String getIpAddress() {
        StringBuffer ipAddr = new StringBuffer();
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            byte[] addr = inetAddress.getAddress();
            for (int i = 0; i < addr.length; i++) {
                if (i > 0) {
                    ipAddr.append(".");
                }
                ipAddr.append(addr[i] & 0XFF);
            }

        } catch (UnknownHostException e) {
            log.error("host not found", e);
            return "127.0.0.1";
        }
        return ipAddr.toString();
    }
}
