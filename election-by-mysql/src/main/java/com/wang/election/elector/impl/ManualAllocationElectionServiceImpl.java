package com.wang.election.elector.impl;

import com.wang.election.elector.ElectionService;
import com.wang.election.utils.MachineUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * 人为指定的方式选主
 *  通过配置文件指定机器作为leader
 *
 * @Author: wanglu51
 * @Date: 2023/6/15 14:09
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "election.algorithm", havingValue = "ManualAllocation", matchIfMissing = true)
public class ManualAllocationElectionServiceImpl implements ElectionService {

    @Value("${election.manual-allocation.leader}")
    private String allocationHost;


    /* 该机器是否是leader */
    private volatile boolean isLeader;

    @PostConstruct
    public void init() {
        isLeader = MachineUtil.getIpAddress().equalsIgnoreCase(allocationHost);
        if (isLeader) {
            becomeLeader();
        }
    }

    @PreDestroy
    public void destroy() {
        if (!isLeader) {
            return;
        }

        log.info("destroy by manual allocation");
    }

    @Override
    public void becomeLeader() {
        log.info("become leader by manual allocation");
    }

    @Override
    public void lostLeader() {
        log.info("lost leader by manual allocation");
        destroy();
    }

    @Override
    public boolean isLeader() {
        return isLeader;
    }
}
