package com.wang.election.elector.impl;

import com.wang.election.elector.ElectionService;
import com.wang.election.mapper.LeaderElectionMapper;
import com.wang.election.utils.MachineUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/**
 * 通过mysql选主
 *
 * @Author: wanglu51
 * @Date: 2023/8/16 14:27
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "election.algorithm", havingValue = "Mysql")
public class MysqlElectionServiceImpl implements ElectionService {
    private static final int LEADER_INTERVAL_SECOND = 100;

    @Resource
    private LeaderElectionMapper leaderElectionMapper;


    /* 是否已经初始化 */
    private volatile boolean isInitialized;
    /* 该机器是否是leader */
    private volatile boolean isLeader;

    @PostConstruct
    public void init() throws Exception {
        // 异常会启动失败
        confirmRole();
        isInitialized = true;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    @Scheduled(fixedRate = 30 * 1000)
    public void confirmRoleTask() throws Exception {
        // 没有初始化不需要尝试成为主节点
        if (!isInitialized) {
            return;
        }
        confirmRole();
    }

    /**
     * 尝试获取leader，如果成功择变成leader，否则变成follower
     */
    void confirmRole() throws Exception {
        String host = MachineUtil.getIpAddress();
        // 某一次异常不会继续执行，此时leader保持原来的
        int rowCount = leaderElectionMapper.updateLeaderActiveTime(host, LEADER_INTERVAL_SECOND);
        if (rowCount > 0) {
            if (!isLeader) {
                // 异常不会修改状态，下次如果成为leader继续执行
                becomeLeader();
            }
            isLeader = true;
        } else {
            if (isLeader) {
                lostLeader();
            }
            isLeader = false;
        }
        log.info("try acquire leader by mysql with result: {}", isLeader);
    }

    @Override
    public void becomeLeader() throws Exception {
        log.info("become leader by mysql");
    }

    @Override
    public void lostLeader() {
        log.info("lost leader by mysql");
        destroy();
    }

    @Override
    public boolean isLeader() {
        return isLeader;
    }


    @PreDestroy
    public void destroy() {
        log.info("destroy by mysql");
    }
}
