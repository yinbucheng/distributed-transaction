package cn.mst.core.client.net;

import cn.mst.core.client.holder.ClientChannelHolder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author ：yinchong
 * @create ：2019/7/11 17:11
 * @description：
 * @modified By：
 * @version:
 */
public class NetClient {

    private static Logger logger = LoggerFactory.getLogger(NetClient.class);

    public static void connect(String ip, int port) {
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ch.pipeline().addLast("pingIdleHandler", new IdleStateHandler(10, -1, -1, TimeUnit.SECONDS));
                    ch.pipeline().addLast("clientLengthDecode", new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4));
                    ch.pipeline().addLast("clientStringDecode", new StringDecoder());
                    ch.pipeline().addFirst("clientStringEncode", new StringEncoder());
                    ch.pipeline().addFirst("clientLengthEncode", new LengthFieldPrepender(4));
                    ch.pipeline().addLast("clientHandler", new NetClientHandler());
                }
            });
            ChannelFuture future = bootstrap.connect(ip, port);
            future.addListener((param) -> {
                if (param.isSuccess()) {
                    logger.info("connect server " + ip + ":" + port + " ack");
                    ClientChannelHolder.setChannelAndCountDownLatch(future.channel(), countDownLatch);
                } else {
                    logger.info("connect server " + ip + ":" + port + " fail");
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
        } catch (Exception e) {
            logger.error(e.toString());
        } finally {
            workGroup.shutdownGracefully();
        }

    }
}
