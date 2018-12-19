package cn.mst.server.base;

import io.netty.channel.ChannelHandlerContext;

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
}
