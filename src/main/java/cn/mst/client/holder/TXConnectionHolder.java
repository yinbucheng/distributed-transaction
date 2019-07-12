package cn.mst.client.holder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.mst.proxy.TXDBConnection;
import java.sql.SQLException;
import java.util.Map;
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
    private static Map<String, Struct> dbHolder = new ConcurrentHashMap<>();

    public static void putDbConnection(String uuid, TXDBConnection dbConnection) {
        Struct struct = new Struct();
        struct.startTime = System.currentTimeMillis();
        struct.txdbConnection = dbConnection;
        dbHolder.put(uuid, struct);
    }

    public static TXDBConnection getDbConnection(String uuid) {
        Struct struct = dbHolder.get(uuid);
        if(null ==struct){
            logger.error("can not find struct by "+uuid);
            return null;
        }
        return struct.txdbConnection;
    }

    public static TXDBConnection remove(String uuid) {
        Struct struct = dbHolder.remove(uuid);
        return struct.txdbConnection;
    }

    public static void commitAndRemove(String uuid) {
        Struct struct = dbHolder.remove(uuid);
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


    public static void rollbackAndRemove(String uuid) {
        Struct struct = dbHolder.remove(uuid);
        if (struct == null) {
            logger.error("get connection fail by " + uuid);
            return;
        }
        try {
            struct.txdbConnection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static class Struct{
        volatile long startTime;
        volatile TXDBConnection txdbConnection;
        //kafka改造连接

        //redis改造连接

        //mongodb改造连接

        //等等
    }
}
