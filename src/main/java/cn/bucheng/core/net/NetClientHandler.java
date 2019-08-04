package cn.bucheng.core.net;

import cn.bucheng.core.holder.ClientChannelHolder;
import cn.bucheng.core.holder.RequestResultHolder;
import cn.bucheng.core.holder.TXConnectionHolder;
import cn.bucheng.common.constant.TransferConstant;
import cn.bucheng.model.res.TXResponse;
import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * @author ：yinchong
 * @create ：2019/7/11 17:11
 * @description：
 * @modified By：
 * @version:
 */
public class NetClientHandler extends SimpleChannelInboundHandler<String> {
    private static Logger logger = LoggerFactory.getLogger(NetClientHandler.class);

    private static Executor executor = new ThreadPoolExecutor(10, 50, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(200), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("distribute tx client handler thread");
            return thread;
        }
    });

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        executor.execute(() ->
                handleMessage(msg)
        );
    }

    private void handleMessage(String msg) {
        TXResponse response = JSON.parseObject(msg, TXResponse.class);
        String uuid = response.getUuid();
        if (response.getStatus() == TransferConstant.ACK) {
            logger.info("accept server ack command uuid:" + response.getUuid());
            RequestResultHolder.resetResult(uuid, response);
        } else if (response.getStatus() == TransferConstant.COMMIT) {
            logger.info("accept server commit command uuid:" + response.getUuid());
            TXConnectionHolder.commitAndRemove(response.getUuid());
        } else if (response.getStatus() == TransferConstant.ROLLBACK) {
            logger.info("accept server rollback command uuid:" + response.getUuid());
            TXConnectionHolder.rollbackAndRemove(response.getUuid());
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent stateEvent = (IdleStateEvent) evt;
            if (stateEvent.state() == IdleState.READER_IDLE) {
                ctx.pipeline().writeAndFlush(TransferConstant.PING);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.toString());
        ClientChannelHolder.remove();
    }
}
