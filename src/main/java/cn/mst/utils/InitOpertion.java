package cn.mst.utils;

import cn.mst.constant.ClientConstant;
import org.apache.zookeeper.ZooKeeper;

/**
 * @ClassName InitOpertion
 * @Author buchengyin
 * @Date 2018/12/20 11:02
 **/
public class InitOpertion {

    /**
     * 初始化zookeeper上面最基本路径
     * @param zooKeeper
     * @param namespace
     */
    public static void initBasePath(ZooKeeper zooKeeper,String namespace){
        ZKUtils.createPersistentNode(MstServerAttributeHolder.getZkClient(), "/"+ ClientConstant.ROOT_PATH,null);
        ZKUtils.createPersistentNode(MstServerAttributeHolder.getZkClient(),"/"+ ClientConstant.ROOT_PATH+"/"+namespace,null);
        ZKUtils.createPersistentNode(MstServerAttributeHolder.getZkClient(),"/"+ ClientConstant.ROOT_PATH+"/"+namespace+"/"+ ClientConstant.INSTANCES_PATH,null);
    }
}
