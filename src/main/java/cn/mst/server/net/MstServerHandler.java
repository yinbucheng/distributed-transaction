package cn.mst.server.net;

import cn.mst.client.constant.SystemConstant;
import cn.mst.common.MstMessageBuilder;
import cn.mst.server.base.MstServerAttributeClean;
import cn.mst.server.base.MstServerAttributeHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private Logger logger = LoggerFactory.getLogger(MstServerHandler.class);
    Executor executor = Executors.newFixedThreadPool(3);
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        logger.debug(SystemConstant.PREV_LOG+msg);
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
            if(state!=MstMessageBuilder.PING){
                logger.debug(SystemConstant.SERVER_LOG+msg);
            }
            switch (state){
                case MstMessageBuilder.REGISTER:
                    MstServerAttributeHolder.addChannelHandlerContext(token,ctx);
                    MstServerAttributeClean.addToken(token);
                    //这里通知客户端取消阻塞
                    ctx.writeAndFlush(MstMessageBuilder.registerOk(token));
                    break;
                case MstMessageBuilder.ROLLBACK:
                    MstServerAttributeHolder.addRollBackFlag(token);
                    //通知客户端取消阻塞
                    ctx.writeAndFlush(MstMessageBuilder.registerOk(token));
                    break;
                case MstMessageBuilder.FIN:
                    List<ChannelHandlerContext> ctxs = MstServerAttributeHolder.removeChannelHandlerContext(token);
                   boolean rollBackFlag =  MstServerAttributeHolder.isRollBack(token);
                   if(!rollBackFlag){
                       rollBackFlag = !allChannelActive(ctxs);
                   }
                   if(rollBackFlag){
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

    //发送提交命令时判断下当前客户端是否全部都正常，否则回滚
    public boolean allChannelActive(List<ChannelHandlerContext> ctxs){
        for(ChannelHandlerContext ctx:ctxs){
            if(!ctx.channel().isActive())
                return false;
        }
        return true;
    }
}
