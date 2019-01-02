package cn.mst.server.base;

import cn.mst.client.constant.SystemConstant;
import cn.mst.common.EnviromentUtils;
import cn.mst.common.WebUtils;
import cn.mst.common.ZKUtils;
import cn.mst.server.net.NetServer;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 启动策略类，参与master选举和instances注册
 *
 * @ClassName StartStrategy
 * @Author buchengyin
 * @Date 2018/12/20 10:22
 **/
@Component
public class StartStrategy {

    private Logger logger = LoggerFactory.getLogger(StartStrategy.class);

    @Value("${mst.namespace}")
    private String namespace;
    @Value("${mst.server.port}")
    private Integer port;
    @Autowired
    private NetServer server;


    public void beginStart() {
        registerInstance();
        masterVoteAndStart();
    }

    public void registerInstance() {
        ZKUtils.createEphemeralNode(MstServerAttributeHolder.getZkClient(), "/" + SystemConstant.ROOT_PATH + "/" + namespace + "/" + SystemConstant.INSTANCES_PATH + "/" + getIp() + ":" + port, null);
    }

    private String getIp() {
        String data = EnviromentUtils.getProperties("mst.server.ip");
        if (data == null || data.equals("")) {
            data = WebUtils.getLocalIP();
        }
        return data;
    }


    public void masterVoteAndStart() {
        boolean existFlag = ZKUtils.exist(MstServerAttributeHolder.getZkClient(), "/" + SystemConstant.ROOT_PATH + "/" + namespace + "/master");
        if (existFlag) {
            //获取当前的值是自己吗
            String data = ZKUtils.getData(MstServerAttributeHolder.getZkClient(), "/" + SystemConstant.ROOT_PATH + "/" + namespace + "/master");
            logger.info(SystemConstant.SERVER_LOG + " get master data:" + data);
            if (!data.equals(getIp() + ":" + port)) {
                MstServerAttributeHolder.notifyCloseFutrue();
                MstServerAttributeHolder.addCloseFuture(null);
            }
            return;
        }
        boolean flag = ZKUtils.createEphemeralNode(MstServerAttributeHolder.getZkClient(), "/" + SystemConstant.ROOT_PATH + "/" + namespace + "/master", (getIp() + ":" + port).getBytes());
        if (flag) {
            logger.info(SystemConstant.PREV_LOG + getIp() + ":" + port + " vote master success!");
            //启动server内存定时清理器
            if (!MstServerAttributeClean.isStart()) {
                MstServerAttributeClean.work();
            }
            try {
                //注册事件监听保证zookeeper端口服务端也就端口
                MstServerAttributeHolder.getZkClient().exists("/" + SystemConstant.ROOT_PATH + "/" + namespace + "/master", new Watcher() {
                    @Override
                    public void process(WatchedEvent watchedEvent) {
                        switch (watchedEvent.getType()) {
                            case NodeDeleted:
                                MstServerAttributeHolder.notifyCloseFutrue();
                                MstServerAttributeHolder.addCloseFuture(null);
                                break;
                            case NodeDataChanged:
                                MstServerAttributeHolder.notifyCloseFutrue();
                                MstServerAttributeHolder.addCloseFuture(null);
                                break;
                            default:
                                MstServerAttributeHolder.getZkClient().register(this);
                                break;
                        }
                    }
                });
            } catch (Exception e) {
                logger.error(SystemConstant.SERVER_LOG + e);
            }
            server.start(port);
        }
    }
}
