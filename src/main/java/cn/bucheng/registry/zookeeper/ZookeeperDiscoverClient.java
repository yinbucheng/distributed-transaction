package cn.bucheng.registry.zookeeper;

import cn.bucheng.common.constant.TransferConstant;
import cn.bucheng.registry.DefaultServiceInstance;
import cn.bucheng.registry.DiscoverClient;
import cn.bucheng.registry.ServiceInstance;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author buchengyin
 * @create 2019/7/14 10:46
 * @describe
 */
public class ZookeeperDiscoverClient implements DiscoverClient {
    @Autowired
    private CuratorFramework client;

    private static Logger logger = LoggerFactory.getLogger(ZookeeperDiscoverClient.class);

    @Override
    public ServiceInstance getLeader(String leaderName) {
        String path = "/" + leaderName + "/" + TransferConstant.LEADER_NAME;
        try {
            byte[] bytes = client.getData().forPath(path);
            if (null == bytes || bytes.length == 0) {
                return null;
            }
            String content = new String(bytes);
            String[] split = content.split(":");
            ServiceInstance instance = new DefaultServiceInstance(null, split[0], Integer.parseInt(split[1]), null, -1, leaderName);
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
        }
        return null;
    }
}
