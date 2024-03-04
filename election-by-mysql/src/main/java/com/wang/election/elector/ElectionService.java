package com.wang.election.elector;

/**
 * 选主服务，由主节点执行定时任务
 *
 * @Author: wanglu51
 * @Date: 2023/6/15 10:56
 */
public interface ElectionService {

    /**
     * 是否是leader
     * @return true/false
     */
    boolean isLeader();

    /**
     * 成为leader
     */
    void becomeLeader() throws Exception;

    /**
     * 丢失leader
     */
    void lostLeader();

}
