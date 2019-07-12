package cn.bucheng.core.base;

import cn.bucheng.common.utils.EnvironmentUtils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName TXDBConnectionLimit
 * @Author buchengyin
 * @Date 2018/12/19 19:40
 **/
public class TXDBConnectionLimit {
    //最大连接数量限制
    private static int max =50;
    private static AtomicInteger dbCount = new AtomicInteger(0);

    public static void incrementDbNumber(){
        dbCount.incrementAndGet();
    }

    public static int getCurrentDbNumber(){
        return dbCount.get();
    }

    public static void decrementDbNumber(){
        dbCount.decrementAndGet();
    }

    public static boolean isMaxDbNumber(){
        Integer tempMax = EnvironmentUtils.getProperties("mst.max.connection",Integer.class);
        if(tempMax!=null){
            max = tempMax;
        }
        return getCurrentDbNumber()>max;
    }
}
