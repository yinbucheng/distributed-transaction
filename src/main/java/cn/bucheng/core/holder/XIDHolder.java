package cn.bucheng.core.holder;

import java.util.UUID;

/**
 * @author ：yinchong
 * @create ：2019/7/11 13:35
 * @description：
 * @modified By：
 * @version:
 */
public class XIDHolder{
    //分布式事务开启是存放生成的唯一表示
    private static ThreadLocal<String> uuidHolder = new ThreadLocal<>();

    //获取唯一标示
    public static String getXID() {
        return uuidHolder.get();
    }

    //创建并返回唯一标示
    public static String createAndGetXID() {
        String uuid = UUID.randomUUID().toString();
        uuidHolder.set(uuid);
        return uuid;
    }

    public static void addXID(String uuid){
        uuidHolder.set(uuid);
    }

    //移除当前线程的唯一UUID
    public static void remove() {
        uuidHolder.remove();
    }
}
