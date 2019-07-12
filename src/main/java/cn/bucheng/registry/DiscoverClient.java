package cn.bucheng.registry;


import java.util.List;

/**
 * @author ：yinchong
 * @create ：2019/7/12 19:24
 * @description：
 * @modified By：
 * @version:
 */
public interface DiscoverClient {
    //获取所有服务
    List<String> getServices();
    //通过服务id获取所有的实例
    List<ServiceInstance> getInstances(String serviceId);
    //获取leader
    ServiceInstance getLeader();

}
