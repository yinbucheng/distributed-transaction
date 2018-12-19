package cn.mst.common;

import cn.mst.client.constant.SystemConstant;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * @ClassName ZKUtils
 * @Author buchengyin
 * @Date 2018/12/19 20:02
 **/
public class ZKUtils {
    private static Logger logger = LoggerFactory.getLogger(ZKUtils.class);

    public static ZooKeeper newZkClient(String url, int timeout) {
        try {
            return new ZooKeeper(url, timeout, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean createPersistentNode(ZooKeeper zk,String path,byte[] data){
        try {
            String result =  zk.create(path,data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            return false;
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static boolean createEphemeralNode(ZooKeeper zk,String path,byte[] data){
        try {
            zk.create(path,data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            return true;
        } catch (Exception e){
            logger.error(SystemConstant.PREV_LOG+e);
            return false;
        }
    }

    public static List<String> getChilds(ZooKeeper zk,String path){
        try {
            return zk.getChildren(path,true);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static boolean exist(ZooKeeper zk,String path){
        try {
            Stat exists = zk.exists(path, true);
            if(exists!=null)
                return true;
            return false;
        } catch (Exception e){
            logger.error(SystemConstant.PREV_LOG+e);
            return false;
        }
    }
}
