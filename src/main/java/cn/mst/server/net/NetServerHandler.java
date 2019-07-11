package cn.mst.server.net;

import cn.mst.constant.TransferConstant;
import cn.mst.model.req.TXRequest;
import cn.mst.model.res.TXResponse;
import cn.mst.server.holder.RemoteConnectionHolder;
import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author ：yinchong
 * @create ：2019/7/11 20:15
 * @description：
 * @modified By：
 * @version:
 */
public class NetServerHandler extends SimpleChannelInboundHandler<String> {

    private static Logger logger = LoggerFactory.getLogger(NetServerHandler.class);

    private Executor executor = Executors.newFixedThreadPool(10);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        if (TransferConstant.PING.equals(msg)) {
            logger.debug("receive ping from client ");
            return;
        }

        executor.execute(() ->
                handlerMessage(msg, ctx.channel())
        );

    }

    private void handlerMessage(String message, Channel channel) {
        TXRequest request = JSON.parseObject(message, TXRequest.class);
        if (request.getType() == TransferConstant.REGISTER) {
            RemoteConnectionHolder.addChannel(request.getUuid(), channel);
            writeAndFlush(channel, TXResponse.ack(request.getUuid()));
        } else if (request.getType() == TransferConstant.ROLLBACK) {
            RemoteConnectionHolder.markRollback(request.getUuid());
            writeAndFlush(channel, TXResponse.ack(request.getUuid()));
        } else if (request.getType() == TransferConstant.COMMIT) {
            logger.info("accept client commit command " + request.getUuid());
        } else if (request.getType() == TransferConstant.FIN) {
            logger.info("accept client fin command " + request.getUuid());
            executeCommitOrRollback(request.getUuid());
        }
    }


    private void writeAndFlush(Channel channel, TXResponse response) {
        channel.pipeline().writeAndFlush(JSON.toJSONString(response));
    }

    private void executeCommitOrRollback(String uuid) {
        boolean commit = RemoteConnectionHolder.ableCommit(uuid);
        LinkedBlockingQueue<Channel> channels = RemoteConnectionHolder.listChannels(uuid);
        if (commit) {
            for (Channel channel : channels) {
                writeAndFlush(channel, TXResponse.commit(uuid));
            }
        } else {
            for (Channel channel : channels) {
                writeAndFlush(channel, TXResponse.rollback(uuid));
            }
        }
        RemoteConnectionHolder.reomve(uuid);
    }
}
