package cn.mst.client.net;

import cn.mst.client.constant.SystemConstant;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.LineEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 这个类负责服务之间通信
 *
 * @ClassName NetClient
 * @Author buchengyin
 * @Date 2018/12/19 15:33
 **/
@Component
public class NetClient {
    private Logger logger = LoggerFactory.getLogger(NetClient.class);
    //此标记用来表示网络通信是否启动成功
    public static volatile boolean start = false;
    //用于通信的套接字
    public static volatile ChannelHandlerContext socketClient;

    public void startWork(String ip,int port) {
        if (start)
            return;
        start = true;
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ch.pipeline().addLast("ping_pong", new IdleStateHandler(0, 5, 0,TimeUnit.SECONDS));
                    ch.pipeline().addLast("decode1", new LengthFieldBasedFrameDecoder(1024,0,4,0,4));
                    ch.pipeline().addLast("decode2", new StringDecoder());
                    ch.pipeline().addLast("myDecode", new MstNetHandler());
                    ch.pipeline().addFirst("encode1", new StringEncoder());
                    ch.pipeline().addFirst("encode2", new LengthFieldPrepender(4));
                }
            });
            ChannelFuture future = bootstrap.connect(ip, port).sync();
            future.addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    if (future.isSuccess()) {
                        logger.info(SystemConstant.PREV_LOG+" net client start success");
                    } else {
                        start = false;
                    }
                }
            }).channel().closeFuture().sync();

        } catch (Exception e) {
            start = false;
            logger.error(SystemConstant.PREV_LOG + e);
        } finally {
            start=false;
            workGroup.shutdownGracefully();
            logger.info(SystemConstant.PREV_LOG+" net client close success");
        }
    }
}
