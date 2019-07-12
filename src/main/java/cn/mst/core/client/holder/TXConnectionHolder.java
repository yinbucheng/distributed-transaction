package cn.mst.core.client.holder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.mst.config.proxy.TXDBConnection;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ：yinchong
 * @create ：2019/7/11 13:45
 * @description：分布式改造连接持有器
 * @modified By：
 * @version:
 */
public class TXConnectionHolder {
    private static Logger logger = LoggerFactory.getLogger(TXConnectionHolder.class);
    private static Map<String, Struct> connectionHolder = new ConcurrentHashMap<>();

    public static void addTXDB(String uuid, TXDBConnection dbConnection) {
        addConnection(uuid, connectionHolder);
    }


    private static void addConnection(String uuid, Object connection) {
        Struct struct = connectionHolder.get(uuid);
        if (struct == null) {
            synchronized (TXConnectionHolder.class) {
                if (struct == null) {
                    struct = new Struct();
                    struct.startTime = System.currentTimeMillis();
                    connectionHolder.put(uuid, struct);
                }
            }
        }
        if (connection instanceof TXDBConnection) {
            struct.txdbConnection = (TXDBConnection) connection;
        }
    }


    public static TXDBConnection getTXDB(String uuid) {
        Struct struct = connectionHolder.get(uuid);
        if (null == struct) {
            logger.error("can not find struct by " + uuid);
            return null;
        }
        return struct.txdbConnection;
    }

    //移除缓存上面的改造池
    public static TXDBConnection remove(String uuid) {
        Struct struct = connectionHolder.remove(uuid);
        return struct.txdbConnection;
    }

    //调用所有持有连接上面提交方法
    public static void commitAndRemove(String uuid) {
        Struct struct = connectionHolder.remove(uuid);
        if (struct == null) {
            logger.error("get connection fail by " + uuid);
            return;
        }
        try {
            struct.txdbConnection.realCommitAndClose();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //现在是否超时
    public static boolean isTimeoutNow(String uuid, int stepTime) {
        Struct struct = connectionHolder.get(uuid);
        if (struct == null)
            return false;
        if (struct.startTime + stepTime * 1000 < System.currentTimeMillis()) {
            return true;
        }
        return false;
    }

    public static Set<String> uuidSet() {
        return connectionHolder.keySet();
    }

    //调用所有次有链上面的回滚方法
    public static void rollbackAndRemove(String uuid) {
        Struct struct = connectionHolder.remove(uuid);
        if (struct == null) {
            logger.error("get connection fail by " + uuid);
            return;
        }
        try {
            struct.txdbConnection.realRollbackAndClose();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static class Struct {
        volatile long startTime;
        volatile TXDBConnection txdbConnection;
        //kafka改造连接

        //redis改造连接

        //mongodb改造连接

        //等等
    }
}
