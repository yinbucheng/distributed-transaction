package cn.mst.client.base;

import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * mst中常用数据存放工具
 *
 * @ClassName MstAttributeHolder
 * @Author buchengyin
 * @Date 2018/12/19 15:05
 **/
public class MstAttributeHolder {
    //分布式事务开启是存放生成的唯一表示
    private static ThreadLocal<String> uniqueMst = new ThreadLocal<>();
    //事务唯一标示和数据链接对应关系
    private static ConcurrentMap<String, MstDbConnection> token_conn = new ConcurrentHashMap<>();
    private static ConcurrentMap<String, LockCondition> token_lock = new ConcurrentHashMap<>();
    private static volatile ZooKeeper zkClient;

    public static void putMstToken(String mstToken) {
            uniqueMst.set(mstToken);
    }

    public static String getMstToken() {
        return uniqueMst.get();
    }

    public static void removeMstToken() {
        uniqueMst.remove();
    }

    public static void putTokenAndCon(String token, MstDbConnection connection) {
        token_conn.put(token, connection);
    }

    public static MstDbConnection getConnByToken(String token) {
        return token_conn.get(token);
    }

    public static MstDbConnection removeConn(String token) {
        return token_conn.remove(token);
    }

    public static void putTokenAndLock(String token, LockCondition lockCondition) {
        token_lock.put(token, lockCondition);
    }

    public static LockCondition getLockByToken(String token) {
        return token_lock.get(token);
    }

    public static LockCondition removeLock(String token) {
        return token_lock.remove(token);
    }

    public static void setZkClient(ZooKeeper zkClient) {
        MstAttributeHolder.zkClient = zkClient;
    }

    public static ZooKeeper getZkClient() {
        return zkClient;
    }
}
