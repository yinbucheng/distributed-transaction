package cn.bucheng.registry;

public interface ServiceRegistry {
    //注册普通实例
    void register(ServiceInstance instance);
    //取消注册
    void deregister(ServiceInstance instance);
    //关闭
    void close();
}
