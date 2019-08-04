package cn.bucheng.registry.zookeeper;

import cn.bucheng.common.constant.TransferConstant;
import cn.bucheng.registry.ServiceInstance;
import cn.bucheng.registry.ServiceRegistry;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author buchengyin
 * @create 2019/7/13 10:07
 * @describe
 */
public class ZookeeperServiceRegistry implements ServiceRegistry {
    private static Logger logger = LoggerFactory.getLogger(ZookeeperServiceRegistry.class);

    @Autowired
    private CuratorFramework client;

    @Override
    public void register(ServiceInstance instance) {
        CreateMode createMode = CreateMode.EPHEMERAL;
        if (instance.getState() == TransferConstant.PERSISTENT) {
            createMode = CreateMode.PERSISTENT;
        }
        String path = "/" + instance.getLeaderName() + "/" + TransferConstant.INSTANCE_NAME + "/" + instance.getServiceId();
        while (true) {
            try {
                Stat stat = client.checkExists().forPath(path);
                if (null != stat) {
                    byte[] bytes = client.getData().forPath(path);
                    StringBuilder sb = new StringBuilder();
                    if (null != bytes) {
                        sb.append(new String(bytes));
                    }
                    sb.append(instance.getHost() + "_" + instance.getVersion()).append("\n");
                    stat = client.setData().forPath(path, sb.toString().getBytes());
                    if (stat == null) {
                        Thread.sleep(1000);
                        logger.error("update data to zookeeper fail,begin try again");
                        continue;
                    }
                } else {
                    String content = client.create().creatingParentsIfNeeded().withMode(createMode).forPath(path, (instance.getHost() + "_" + instance.getVersion()).getBytes());
                    if (content == null || "".equals(content)) {
                        Thread.sleep(1000);
                        logger.error("create path and set data to zookeeper fail,begin try again");
                        continue;
                    }
                }
                return;
            } catch (Exception e) {
                logger.error(e.toString());
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void deregister(ServiceInstance instance) {
        try {
            client.delete().guaranteed().deletingChildrenIfNeeded().forPath(instance.getServiceId() + "/" + TransferConstant.INSTANCE_NAME);
        } catch (Exception e) {
            logger.error(e.toString());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {

    }

}
