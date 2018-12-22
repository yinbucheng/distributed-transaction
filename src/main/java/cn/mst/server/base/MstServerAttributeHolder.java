package cn.mst.server.base;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Signal;
import io.netty.util.concurrent.DefaultPromise;
import org.apache.zookeeper.ZooKeeper;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @ClassName MstServerAttributeHolder
 * @Author buchengyin
 * @Date 2018/12/19 19:04
 **/
public class MstServerAttributeHolder {
    private static LinkedBlockingQueue<String> rollBack = new LinkedBlockingQueue<>();
    private static ConcurrentHashMap<String,List<ChannelHandlerContext>> token_channels = new ConcurrentHashMap<>();
    private static volatile ZooKeeper zkClient;

    private static volatile ChannelFuture closeFuture;

    public static void addCloseFuture(ChannelFuture future){
        closeFuture = future;
    }

    public static void notifyCloseFutrue(){
        if(closeFuture==null)
            return;
        synchronized (closeFuture){
            try {
                Field field = DefaultPromise.class.getDeclaredField("result");
                field.setAccessible(true);
                field.set(closeFuture, Signal.valueOf(DefaultPromise.class, "SUCCESS"));
                closeFuture.notifyAll();
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }
    }

    public static void addRollBackFlag(String token){
        rollBack.add(token);
    }

    public static boolean isRollBack(String token){
        boolean flag = rollBack.remove(token);
        return flag;
    }

    public static void addChannelHandlerContext(String token,ChannelHandlerContext context){
       List<ChannelHandlerContext> channels =  token_channels.get(token);
       if(channels==null){
           channels = new LinkedList<>();
           token_channels.put(token,channels);
       }
       channels.add(context);
    }

    public static List<ChannelHandlerContext> removeChannelHandlerContext(String token){
        return token_channels.remove(token);
    }

    public static void setZkClient(ZooKeeper zkClient){
        MstServerAttributeHolder.zkClient = zkClient;
    }

    public static ZooKeeper getZkClient(){
        return zkClient;
    }
}
