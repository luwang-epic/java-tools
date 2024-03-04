
通过mysql选主的工具

运行
    1. 需要准备mysql环境，在本地或者远程启动mysql，修改配置文件中的mysql连接
    2. 创建对应的库名和表名，库名和连接字符串一致，表名为leader_election，具体什么见下面
    3. 修改配置文件election.algorithm为ManualAllocation，启动应用，观察是否出现 become leader by manual allocation 等字样的日志，说明通过人工指定的方式选主成功
    4. 切换配置项election.algorithm为Mysql，再次启动应用，观察是否出现 become leader by mysql 和 try acquire leader by mysql with result: true 等字样的日志，说明通过mysql选主成功
    5. 注意，由于选主是通过ip进行的，所以不能在本机启动多个项目来验证，如果需要这种方式，可以在启动过程中生成一个唯一的id，用这个唯一的服务id来代替ip进行选主


目标
1. 具有选主功能，且当前主节点异常后其他节点可以重新选主
2. 允许短暂的时间存于无主状态，但是不允许同一时间存在双主
3. 主节点需要加载和运行任务，不要频繁的变动
4. 最好不要引入额外组件依赖，增加维护的成本

方案
1 选主抽象化
为了方便兼容现有的方案（指定机器为主），将选主抽象为接口，定义如下方法：
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
分别提供两种实现，一种为手动指定：ManualAllocationElectionServiceImpl类实现，一种为mysql实现：MysqlElectionServiceImpl类实现；然后通过配置as.election.algorithm来决定加载哪个类，从而实现无缝的切换，如果有问题也可以回滚到之前版本；同时也可以方便以后扩展，比如：通过etcd或者DC服务来选主等

2 Mysql实现
mysql的更新操作在分布式下是安全的，只有一个可以更新成功，因此当更新成功的机器为leader，之后leader通过定时更新这条记录来续约，使其在正常情况下可以一直持有该锁，需要注意几点：
* 为了防止网络抖动等情况导致的更新失败，需要设置一个可以容忍的间隔时间
* 不能立马感知主节点已经失效，有一定的滞后性，因此可能在某一小段时间是没有主的

创建主节点选举表：
CREATE TABLE leader_election (
    service_id varchar(128) NOT NULL COMMENT '服务名称，如：AS',
    leader_id varchar(128) NOT NULL COMMENT '主节点ID，可以用hostname代替',
    last_active_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '活跃时间，一般使用mysql函数now()生成',
    PRIMARY KEY (service_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='select master node';

mysql支持on duplicate key update语法，当有某个key是更新，如果没有插入，语句如下：

insert ignore into leader_election(service_id, leader_id, last_active_time) values('AutoScaling', @{leaderId}, now()) on duplicate key update leader_id = if(last_active_time <= now() - interval @{intervalSecond} second, values(leader_id), leader_id), last_active_time = if(leader_id = values(leader_id), values(last_active_time), last_active_time)
各个节点执行上面sql，如果成功，返回的影响行数大于0，此时为主节点，如果失败，返回影响行数为0，此时不是主节点
* 当时间在允许的误差范围之内，只有主节点的leader_id和记录的一样，因此只有主节点才可以更新成功
* 当主节点挂了，不会更新记录，那么时间会超过指定的范围，此时都可以更新成功，多个节点开始抢锁，mysql可以保证只有一个成功，其它失败，此时重新选出一个主节点

指定主节点的方式：
只需要通过sql更新记录，将leader_id该为指定的机器即可，此时原来的主将会失效，指定的机器为新的主节点

