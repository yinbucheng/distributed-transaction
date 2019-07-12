package cn.mst.core.server.base;

import cn.mst.core.server.holder.RemoteChannelHolder;
import org.springframework.boot.CommandLineRunner;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author ：yinchong
 * @create ：2019/7/12 10:38
 * @description：服务端超时连接器清理器
 * @modified By：
 * @version:
 */
public class RemoteTimeoutChannelCollator implements CommandLineRunner {

    public static int EXPIRE_TIME = 60 * 10;

    @Override
    public void run(String... args) throws Exception {
        startCleanTimeoutChannel();
    }

    private void startCleanTimeoutChannel() {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Set<String> keys = RemoteChannelHolder.keys();
                if (keys == null)
                    continue;
                List<String> expireKeys = new LinkedList<>();
                for (String key : keys) {
                    if (RemoteChannelHolder.isTimeoutNow(key, EXPIRE_TIME)) {
                        expireKeys.add(key);
                    }
                }

                for (String key : expireKeys) {
                    RemoteChannelHolder.remove(key);
                }
            }
        });
        thread.setDaemon(true);
        thread.setName("remote_timeout_channel_collator");
        thread.start();
    }
}
