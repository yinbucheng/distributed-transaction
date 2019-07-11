package cn.mst.client.holder;

import cn.mst.model.res.TXResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author ：yinchong
 * @create ：2019/7/11 12:26
 * @description：请求结果持有器
 * @modified By：
 * @version:
 */
public abstract class RequestResultHolder {

    private static Logger logger = LoggerFactory.getLogger(RequestResultHolder.class);

    private static Map<String, Struct> resultHolder = new ConcurrentHashMap<>();

    public static TXResponse waitResult(String uuid) {
        Struct struct = new Struct();
        resultHolder.put(uuid, struct);
        try {
            struct.countDownLatch.await(20, TimeUnit.SECONDS);
            return struct.result;
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.error(e.toString());
        } finally {
            resultHolder.remove(uuid);
        }
        return null;
    }

    public static void resetResult(String uuid, TXResponse result) {
       Struct struct = resultHolder.get(uuid);
       if(null==struct){
           logger.error("not find struct by "+uuid);
           return;
       }
        struct.result = result;
        struct.countDownLatch.countDown();
    }


    static class Struct {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        volatile TXResponse result;
    }
}
