package cn.mst.common.init;

import cn.mst.client.base.AddressStrategy;
import cn.mst.client.base.MstAttributeHolder;
import cn.mst.client.base.RollbackCoordinator;
import cn.mst.client.net.NetClient;
import cn.mst.common.InitOpertion;
import cn.mst.common.ZKUtils;
import cn.mst.server.base.MstServerAttributeHolder;
import cn.mst.server.base.StartStrategy;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 这个类控制服务端启动和客户端mst使用
 *
 * @ClassName CoreStart
 * @Author buchengyin
 * @Date 2018/12/20 11:21
 **/
@Component
public class CoreStart implements CommandLineRunner {
    @Value("${mst.namespace}")
    private String namespace;
    @Value("${mst.zk.url}")
    private String url;
    @Autowired
    private StartStrategy startStrategy;
    @Autowired
    private NetClient client;
    private Timer clientTimer = new Timer("Mst client Timer", true);
    private Timer serverTimer = new Timer("Mst server Timer", true);

    public void mstStart() {
        ZooKeeper zooKeeper = ZKUtils.newZkClient(url, 5000);
        if (zooKeeper != null) {
            MstAttributeHolder.setZkClient(zooKeeper);
            MstServerAttributeHolder.setZkClient(zooKeeper);
        }
        InitOpertion.initBasePath(zooKeeper, namespace);
        serverTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                startStrategy.beginStart();
            }
        }, 0L, 60 * 1000L);

        clientTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                invokeMstClient();
            }
        }, 6000L, 60 * 1000L);

    }

    /**
     * 启动Mst客户端
     */
    private void invokeMstClient() {
        //启动回滚事务器
        boolean flag = RollbackCoordinator.isStart();
        if (!flag) {
            RollbackCoordinator.work();
        }
        //启动客户端
        Object[] ip_port = AddressStrategy.resolveIpAndPort(namespace);
        if (ip_port != null) {
            client.startWork((String) ip_port[0], (Integer) ip_port[1]);
        }
    }

    @Override
    public void run(String... args) throws Exception {
        mstStart();
    }
}
