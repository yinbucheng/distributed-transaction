package cn.mst.server.base;

import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Mst服务端属性清理器
 * @ClassName MstServerAttributeClean
 * @Author buchengyin
 * @Date 2018/12/19 19:31
 **/
public class MstServerAttributeClean {
    private static Executor executor = Executors.newSingleThreadExecutor();
    private static final int size =240;
    private static LinkedBlockingQueue<String>[] tokens = new LinkedBlockingQueue[size];
    private static volatile int prev = size -1;
    private static volatile int cur = 0;
    private static volatile boolean startFlag = false;

    static {
        for(int i=0;i<size;i++){
            tokens[i]=new LinkedBlockingQueue<>();
        }
    }

    public static void addToken(String token){
        tokens[prev].add(token);
    }

    public static void work(){
        if(startFlag)
            return;
        startFlag = true;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<Integer.MAX_VALUE;i++){
                    Iterator<String> iterator = tokens[cur].iterator();
                    while(iterator.hasNext()){
                        String token = iterator.next();
                        MstServerAttributeHolder.removeChannelHandlerContext(token);
                        MstServerAttributeHolder.isRollBack(token);
                        iterator.remove();
                    }
                    prev =cur;
                    cur=cur==size-1?0:cur+1;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    public static boolean isStart(){
        return startFlag;
    }

}
