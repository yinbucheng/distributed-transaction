package cn.bucheng.registry;

public interface ServiceRegistry {
    //注册普通实例
    void register(ServiceInstance instance);
    //取消注册
    void deregister(ServiceInstance instance);
    //关闭
    void close();

    //参与选举leader，如果成功选择为voteLeader返回true，失败返回false
    boolean voteLeader(ServiceInstance instance);

    //监控leader
    void monitorLeader(ServiceInstance instance);
}
