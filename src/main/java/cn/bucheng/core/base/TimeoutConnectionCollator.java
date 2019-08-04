package cn.bucheng.core.base;

import cn.bucheng.core.holder.TXConnectionHolder;
import cn.bucheng.common.constant.TransferConstant;
import cn.bucheng.common.utils.EnvironmentUtils;
import org.springframework.boot.CommandLineRunner;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author ：yinchong
 * @create ：2019/7/12 10:23
 * @description：超时异常链接清理器
 * @modified By：
 * @version:
 */
public class TimeoutConnectionCollator implements CommandLineRunner {

    private final static int DELAY_TIME = 60;

    private ScheduledExecutorService threadPool  = Executors.newScheduledThreadPool(1);


    @Override
    public void run(String... args) throws Exception {
        startCleanTimeoutConnection();
    }

    //清理异常未关闭的远程拦截，这里会10秒扫描所有的链接将超时的链接进行回滚
    private void startCleanTimeoutConnection() {
        threadPool.scheduleWithFixedDelay(()->{
            int expireTime = EnvironmentUtils.getIntValue(TransferConstant.TX_EXECUTE_TIMEOUT, TransferConstant.DEFAULT_TX_EXECUTE_TIMEOUT);
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
        },30,10, TimeUnit.SECONDS);
    }
}
