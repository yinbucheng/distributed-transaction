package cn.mst.server.net;

import cn.mst.common.MstMessageBuilder;
import cn.mst.server.base.MstServerAttributeHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @ClassName MstServerHandler
 * @Author buchengyin
 * @Date 2018/12/19 18:56
 **/
public class MstServerHandler extends SimpleChannelInboundHandler<String> {
    Executor executor = Executors.newFixedThreadPool(3);
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
          executor.execute(new Runnable() {
              @Override
              public void run() {
                  executeMsg(msg, ctx);
              }
          });
    }

    /**
     * 根据不同结果处理不同值
     * @param msg
     * @param ctx
     */
    private void executeMsg(String msg, ChannelHandlerContext ctx) {
        Map<Integer, String> map = MstMessageBuilder.resolverMessage(msg);
        for(Map.Entry<Integer,String> entry:map.entrySet()){
            Integer state = entry.getKey();
            String token = entry.getValue();
            switch (state){
                case MstMessageBuilder.REGISTER:
                    MstServerAttributeHolder.addChannelHandlerContext(token,ctx);
                    ctx.writeAndFlush(MstMessageBuilder.registerOk(token));
                    break;
                case MstMessageBuilder.ROLLBACK:
                    MstServerAttributeHolder.addRollBackFlag(token);
                    break;
                case MstMessageBuilder.FIN:
                    List<ChannelHandlerContext> ctxs = MstServerAttributeHolder.removeChannelHandlerContext(token);
                   boolean flag =  MstServerAttributeHolder.isRollBack(token);
                   if(flag){
                      for(ChannelHandlerContext channel:ctxs){
                          channel.writeAndFlush(MstMessageBuilder.sendRollback(token));
                      }
                   }else{
                       for(ChannelHandlerContext channel:ctxs){
                           channel.writeAndFlush(MstMessageBuilder.sendCommit(token));
                       }
                   }
            }
        }
    }
}
