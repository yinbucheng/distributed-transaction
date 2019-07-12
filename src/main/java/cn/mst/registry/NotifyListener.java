package cn.mst.registry;

import cn.mst.common.URL;

import java.util.List;

public interface NotifyListener {

    void notify(List<URL> urlList);
}
