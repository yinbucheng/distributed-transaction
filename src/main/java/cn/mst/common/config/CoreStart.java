package cn.mst.common.config;

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
    private Executor executor = Executors.newFixedThreadPool(2);

    public void mstStart() {
        ZooKeeper zooKeeper = ZKUtils.newZkClient(url, 5000);
        if(zooKeeper!=null) {
            MstAttributeHolder.setZkClient(zooKeeper);
            MstServerAttributeHolder.setZkClient(zooKeeper);
        }
        InitOpertion.initBasePath(zooKeeper, namespace);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                startStrategy.beginStart();
            }
        });

        executor.execute(new Runnable() {
            @Override
            public void run() {
                invokeMstClient();
            }
        });
    }

    /**
     * 启动Mst客户端
     */
    private void invokeMstClient() {
        boolean fist = true;
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
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

            try {
                if (fist) {
                    fist = false;
                    Thread.sleep(15 * 1000);
                }else {
                    Thread.sleep(60 * 1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run(String... args) throws Exception {
        mstStart();
    }
}
