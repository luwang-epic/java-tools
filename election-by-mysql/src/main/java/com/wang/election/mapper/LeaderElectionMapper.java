package com.wang.election.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * leader选举
 *  需要useAffectedRows设置为true，否则会导致多主情况，比如如下配置：
 *      jdbc:mysql://localhost:3306/java_tools?useAffectedRows=true
 *
 * @Author: wanglu51
 * @Date: 2023/8/16 11:23
 */
@Mapper
public interface LeaderElectionMapper {


    /**
     * 两种情况需要更新成功
     *  1. leader节点更新时间，此时其他节点不能更新成功（活跃时间在正常范围内）
     *  2. leader节点挂了，导致长时间没有更新活跃时间，这时其他节点可以更新成功
     * @param leaderId 节点id
     * @param intervalSecond 失效时间间隔，单位秒
     * @return 影响的行数
     */
    @Update("insert ignore into leader_election(service_id, leader_id, last_active_time) " +
            "values('java_tools', '${leaderId}', now()) on duplicate key update " +
            "leader_id = if(last_active_time <= now() - interval ${intervalSecond} second, values(leader_id), leader_id), " +
            "last_active_time = if(leader_id = values(leader_id), values(last_active_time), last_active_time)")
    int updateLeaderActiveTime(@Param("leaderId") String leaderId, @Param("intervalSecond") int intervalSecond);

}
