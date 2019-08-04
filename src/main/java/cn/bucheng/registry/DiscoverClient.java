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
    //获取leader
    ServiceInstance getLeader(String leaderName);

}
