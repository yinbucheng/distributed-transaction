package cn.mst.client.net;

import cn.mst.client.holder.ClientChannelHolder;
import cn.mst.client.holder.RequestResultHolder;
import cn.mst.client.holder.TXDBHolder;
import cn.mst.constant.TransferConstant;
import cn.mst.model.res.TXResponse;
import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author ：yinchong
 * @create ：2019/7/11 17:11
 * @description：
 * @modified By：
 * @version:
 */
public class NetClientHandler extends SimpleChannelInboundHandler<String> {
    private static Logger logger = LoggerFactory.getLogger(NetClientHandler.class);

    private Executor executor = Executors.newFixedThreadPool(3);

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
            RequestResultHolder.resetResult(uuid, response);
        } else if (response.getStatus() == TransferConstant.COMMIT) {
            logger.info("accept server commit command");
            TXDBHolder.commitAndRemove(response.getUuid());
        } else if (response.getStatus() == TransferConstant.ROLLBACK) {
            TXDBHolder.rollbackAndRemove(response.getUuid());
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
