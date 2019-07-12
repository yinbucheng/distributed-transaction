package cn.mst.core.client.base;

import cn.mst.core.client.holder.TXConnectionHolder;
import cn.mst.constant.TransferConstant;
import cn.mst.utils.EnviromentUtils;
import org.springframework.boot.CommandLineRunner;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author ：yinchong
 * @create ：2019/7/12 10:23
 * @description：超时异常链接清理器
 * @modified By：
 * @version:
 */
public class TimeoutConnectionCollator implements CommandLineRunner {

    private final static int DELAY_TIME = 60;


    @Override
    public void run(String... args) throws Exception {
        startCleanTimeoutConnection();
    }

    //清理异常未关闭的远程拦截，这里会10秒扫描所有的链接将超时的链接进行回滚
    private void startCleanTimeoutConnection() {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int expireTime = EnviromentUtils.getIntValue(TransferConstant.TX_EXECUTE_TIMEOUT, TransferConstant.DEFAULT_TX_EXECUTE_TIMEOUT);
                expireTime += DELAY_TIME;
                List<String> timeoutKeys = new LinkedList<>();
                Set<String> keys = TXConnectionHolder.uuidSet();
                for (String key : keys) {
                    if (TXConnectionHolder.isTimeoutNow(key, expireTime)) {
                        timeoutKeys.add(key);
                    }
                }

                for (String key : timeoutKeys) {
                    TXConnectionHolder.rollbackAndRemove(key);
                }
            }
        });
        thread.setName("timeout_connection_collator");
        thread.setDaemon(true);
        thread.start();
    }
}
