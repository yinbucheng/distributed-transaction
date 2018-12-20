package cn.mst.common;

import cn.mst.client.constant.SystemConstant;
import cn.mst.server.base.MstServerAttributeHolder;
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
        ZKUtils.createPersistentNode(MstServerAttributeHolder.getZkClient(), "/"+ SystemConstant.ROOT_PATH,null);
        ZKUtils.createPersistentNode(MstServerAttributeHolder.getZkClient(),"/"+SystemConstant.ROOT_PATH+"/"+namespace,null);
        ZKUtils.createPersistentNode(MstServerAttributeHolder.getZkClient(),"/"+SystemConstant.ROOT_PATH+"/"+namespace+"/",null);
        ZKUtils.createPersistentNode(MstServerAttributeHolder.getZkClient(),"/"+SystemConstant.ROOT_PATH+"/"+namespace+"/"+SystemConstant.INSTANCES_PATH,null);
    }
}
