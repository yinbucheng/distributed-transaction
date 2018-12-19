package cn.mst.common;

import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * @ClassName ZKUtils
 * @Author buchengyin
 * @Date 2018/12/19 20:02
 **/
public class ZKUtils {

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
            String result =  zk.create(path,data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            return false;
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static List<String> getChilds(ZooKeeper zk,String path){
        try {
            return zk.getChildren(path,true);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
