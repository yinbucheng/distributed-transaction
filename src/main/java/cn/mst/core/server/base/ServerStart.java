package cn.mst.core.server.base;

import cn.mst.constant.TransferConstant;
import cn.mst.core.server.net.NetServer;
import cn.mst.common.utils.EnviromentUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;

/**
 * @author ：yinchong
 * @create ：2019/7/12 11:11
 * @description：
 * @modified By：
 * @version:
 */
public class ServerStart implements CommandLineRunner {

    @Autowired
    private NetServer netServer;

    @Value("${server.port}")
    private Integer port;


    @Override
    public void run(String... args) throws Exception {
        startServer();
    }

    //端口是先从配置文件中获取，如果获取失败再将server.port的端口加上指定大小启动
    private void startServer() {
        Thread thread = new Thread(() -> {
            while (true) {
                Integer tempPort = EnviromentUtils.getIntValue(TransferConstant.MST_SERVER_PORT, 0);
                if (tempPort == 0) {
                    tempPort = port + TransferConstant.STEP_PORT;
                }
                netServer.start(tempPort);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.setName("server_run");
        thread.setDaemon(true);
        thread.start();
    }
}
