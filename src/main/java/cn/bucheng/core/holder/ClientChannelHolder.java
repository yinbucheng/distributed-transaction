package cn.bucheng.core.holder;


import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * @author ：yinchong
 * @create ：2019/7/11 15:29
 * @description： 远程链接持有器
 * @modified By：
 * @version:
 */
public abstract class ClientChannelHolder {
    private static Logger logger = LoggerFactory.getLogger(ClientChannelHolder.class);
    private static volatile Channel channel;
    private static volatile CountDownLatch countDownLatch;

    public static void setChannelAndCountDownLatch(Channel channel, CountDownLatch countDownLatch) {
        ClientChannelHolder.channel = channel;
        ClientChannelHolder.countDownLatch = countDownLatch;
    }

    public static Channel getChannel() {
        return channel;
    }

    public static boolean isStart() {
        return channel != null;
    }

    public static void writeAndFlush(Object message) {
        if (channel == null) {
            logger.error("channel is not ready ");
            return;
        }
        channel.pipeline().writeAndFlush(JSON.toJSONString(message));
    }

    public static void writeAndFlush(String message) {
        if (channel == null) {
            logger.error("channel is not ready ");
            return;
        }
        channel.pipeline().writeAndFlush(message);
    }

    public static void remove() {
        channel = null;
        if (null != countDownLatch) {
            countDownLatch.countDown();
        }
        countDownLatch = null;
    }


}
