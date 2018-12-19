package cn.mst.client.net;

import cn.mst.common.MstMessageBuilder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @ClassName MstNetHandler
 * @Author buchengyin
 * @Date 2018/12/19 15:45
 **/
public class MstNetHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        NetClient.socketClient = ctx;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NetClient.start=false;
        NetClient.socketClient = null;
        super.channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if(evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent) evt;
            if(event.state()== IdleState.WRITER_IDLE){
                  ctx.writeAndFlush(MstMessageBuilder.ping());
            }
        }
    }
}
