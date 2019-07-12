package cn.mst.registry.support;

import cn.mst.common.URL;
import cn.mst.registry.NotifyListener;
import cn.mst.registry.RegistryService;

/**
 * @author ：yinchong
 * @create ：2019/7/12 16:44
 * @description：
 * @modified By：
 * @version:
 */
public abstract class AbstractRegistryService implements RegistryService {
    @Override
    public void register(URL url) {

    }

    public abstract boolean createPath(String path);

    public abstract void doRegister();

    @Override
    public void unregister(URL url) {

        doUnregister();
    }

    public abstract void doUnregister();

    @Override
    public void subscribe(URL url, NotifyListener listener) {
        doSubscribe();
    }

    public abstract void doSubscribe();

    @Override
    public void unsubscribe(URL url, NotifyListener listener) {
        doUnsubsribe();
    }

    public abstract void doUnsubsribe();
}
