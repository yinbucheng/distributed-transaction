package cn.mst.server.base;

import cn.mst.client.constant.SystemConstant;
import cn.mst.common.WebUtils;
import cn.mst.common.ZKUtils;
import cn.mst.server.net.NetServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * 启动策略类，参与master选举和instances注册
 * @ClassName StartStrategy
 * @Author buchengyin
 * @Date 2018/12/20 10:22
 **/
public class StartStrategy {

    private Logger logger = LoggerFactory.getLogger(StartStrategy.class);

    @Value("${mst.namespace}}")
    private String namespace;
    @Value("${mst.server.port}}")
    private Integer port;
    @Autowired
    private NetServer server;

    public void beginStart(){
        for(int i=0;i<Integer.MAX_VALUE;i++) {
            registerInstance();
            masterVoteAndStart();
        }
    }

    public void registerInstance(){
        ZKUtils.createEphemeralNode(MstServerAttributeHolder.getZkClient(),"/"+SystemConstant.ROOT_PATH+"/"+namespace+"/"+SystemConstant.INSTANCES_PATH+"/"+ WebUtils.getLocalIP()+":"+port,null);
    }

    public void masterVoteAndStart(){
       boolean existFlag =  ZKUtils.exist(MstServerAttributeHolder.getZkClient(),"/"+SystemConstant.ROOT_PATH+"/"+namespace+"/master");
       if(existFlag){
           try {
               Thread.sleep(60*1000);
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
           return;
       }
        String data = WebUtils.getLocalIP()+":"+port;
        boolean flag = ZKUtils.createEphemeralNode(MstServerAttributeHolder.getZkClient(),"/"+SystemConstant.ROOT_PATH+"/"+namespace+"/master",data.getBytes());
        if(flag){
            logger.info(SystemConstant.PREV_LOG+data+" vote master success");
            //启动server内存定时清理器
            if(!MstServerAttributeClean.isStart()){
                MstServerAttributeClean.work();
            }
            server.start();
        }
    }
}
