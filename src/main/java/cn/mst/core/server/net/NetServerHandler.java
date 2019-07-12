package cn.mst.core.server.net;

import cn.mst.constant.TransferConstant;
import cn.mst.core.server.holder.RemoteChannelHolder;
import cn.mst.model.req.TXRequest;
import cn.mst.model.res.TXResponse;
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
            logger.info("accept client register command " + request.getUuid());
            RemoteChannelHolder.addChannel(request.getUuid(), channel);
            writeAndFlush(channel, TXResponse.ack(request.getUuid()));
        } else if (request.getType() == TransferConstant.ROLLBACK) {
            logger.info("accept client rollback command " + request.getUuid());
            RemoteChannelHolder.markRollback(request.getUuid());
            writeAndFlush(channel, TXResponse.ack(request.getUuid()));
        } else if (request.getType() == TransferConstant.COMMIT) {
            logger.info("accept client commit command " + request.getUuid());
        } else if (request.getType() == TransferConstant.FIN) {
            logger.info("accept client FIN command " + request.getUuid());
            executeCommitOrRollback(request.getUuid());
        }
    }


    private void writeAndFlush(Channel channel, TXResponse response) {
        channel.pipeline().writeAndFlush(JSON.toJSONString(response));
    }

    private void executeCommitOrRollback(String uuid) {
        boolean commit = RemoteChannelHolder.ableCommit(uuid);
        LinkedBlockingQueue<Channel> channels = RemoteChannelHolder.listChannels(uuid);
        if (channels == null) {
            logger.error("no channels find by uuid " + uuid);
            return;
        }
        //判断上面所有的连接是否为活跃状态，如果存在非活跃状态将提交标记转变为回滚
        if (commit) {
            for (Channel channel : channels) {
                if (!channel.isActive()) {
                    commit = false;
                    break;
                }
            }
        }
        if (commit) {
            for (Channel channel : channels) {
                logger.info("send commit command uuid:" + uuid);
                writeAndFlush(channel, TXResponse.commit(uuid));
            }
        } else {
            for (Channel channel : channels) {
                logger.info("send rollback command uuid:" + uuid);
                writeAndFlush(channel, TXResponse.rollback(uuid));
            }
        }
        RemoteChannelHolder.remove(uuid);
    }
}
