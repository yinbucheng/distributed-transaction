package cn.mst.client.net;

import cn.mst.client.base.LockCondition;
import cn.mst.client.base.MstAttributeHolder;
import cn.mst.client.base.MstDbConnection;
import cn.mst.client.constant.SystemConstant;
import cn.mst.common.MstMessageBuilder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @ClassName MstNetHandler
 * @Author buchengyin
 * @Date 2018/12/19 15:45
 **/
public class MstNetHandler extends SimpleChannelInboundHandler<String> {

    private Logger logger = LoggerFactory.getLogger(MstNetHandler.class);

    private Executor executor = Executors.newFixedThreadPool(10);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                handlerResponse(msg);
            }
        });
    }

    /**
     * 处理服务端过来的信息
     *
     * @param msg
     */
    private void handlerResponse(String msg) {
        logger.debug(SystemConstant.PREV_LOG+msg);
        Map<Integer, String> result = MstMessageBuilder.resolverMessage(msg);
        for (Map.Entry<Integer, String> entry : result.entrySet()) {
            Integer state = entry.getKey();
            String token = entry.getValue();
            switch (state) {
                case MstMessageBuilder.REGISTER_OK:
                    LockCondition condition = MstAttributeHolder.removeLock(token);
                    if (condition != null) {
                        condition.single();
                    }
                    break;
                case MstMessageBuilder.ROLLBACK:
                    MstDbConnection dbConnection = MstAttributeHolder.removeConn(token);
                    try {
                        if (dbConnection != null) {
                            dbConnection.realRollbackAndClose();
                        }
                    } catch (SQLException e) {
                        logger.error(SystemConstant.PREV_LOG + token + " rollback fail");
                        e.printStackTrace();
                    }
                    break;
                case MstMessageBuilder.COMMIT:
                    MstDbConnection dbConnection2 = MstAttributeHolder.removeConn(token);
                    try {
                        if (dbConnection2 != null) {
                            dbConnection2.realCommitAndClose();
                        }
                    } catch (SQLException e) {
                        logger.error(SystemConstant.PREV_LOG + token + " commit fail");
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        NetClient.socketClient = ctx;
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NetClient.start = false;
        NetClient.socketClient = null;
        super.channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE) {
                ctx.writeAndFlush(MstMessageBuilder.ping());
            }
        }
    }
}
