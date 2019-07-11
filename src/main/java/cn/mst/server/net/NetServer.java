package cn.mst.server.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 分布式事务服务端网络通信
 *
 * @ClassName NetServer
 * @Author buchengyin
 * @Date 2018/12/19 18:44
 **/
public class NetServer {

    private Logger logger = LoggerFactory.getLogger(NetServer.class);

    public void  start(int port){
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        try{
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup,workGroup);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE,true);
            bootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ch.pipeline().addLast("serverLengthDecode",new LengthFieldBasedFrameDecoder(1024,0,4,0,4));
                    ch.pipeline().addLast("serverStringDecode",new StringDecoder());
                    ch.pipeline().addFirst("serverLengthEncode",new LengthFieldPrepender(4));
                    ch.pipeline().addFirst("serverStringEncode",new StringEncoder());
                    ch.pipeline().addLast("serverHandler",new NetServerHandler());
                }
            });

            ChannelFuture future = bootstrap.bind(port);
            future.addListener((param)->{
                if (param.isSuccess()){
                    logger.info("start server ack in port "+port);
                }else{
                    logger.error("start server fail in port "+port);
                }
            });
            future.sync();
        }catch (Exception e){
             logger.error(e.toString());
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}
