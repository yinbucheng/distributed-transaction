package cn.mst.registry;


import cn.mst.common.URL;

/**
 * @author ：yinchong
 * @create ：2019/7/12 16:26
 * @description：
 * @modified By：
 * @version:
 */
public interface RegistryService {
    //进行注册
    void register(URL url);

    //取消注册
    void unregister(URL url);

    //添加监听
    void subscribe(URL url, NotifyListener listener);

    //取消监听
    void unsubscribe(URL url, NotifyListener listener);

}
