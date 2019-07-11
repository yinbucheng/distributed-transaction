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
 * @description：mst改造后的数据库持有者
 * @modified By：
 * @version:
 */
public class TXDBHolder {
    private static Logger logger = LoggerFactory.getLogger(TXDBHolder.class);
    private static Map<String, TXDBConnection> dbHolder = new ConcurrentHashMap<>();

    public static void putDbConnection(String uuid, TXDBConnection dbConnection) {
        dbHolder.put(uuid, dbConnection);
    }

    public static TXDBConnection getDbConnection(String uuid) {
        return dbHolder.get(uuid);
    }

    public static TXDBConnection remove(String uuid) {
        return dbHolder.remove(uuid);
    }

    public static void commitAndRemove(String uuid) {
        TXDBConnection connection = dbHolder.remove(uuid);
        if (connection == null) {
            logger.error("get connection fail by " + uuid);
            return;
        }
        try {
            connection.realCommitAndClose();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void rollbackAndRemove(String uuid) {
        TXDBConnection connection = dbHolder.remove(uuid);
        if (connection == null) {
            logger.error("get connection fail by " + uuid);
            return;
        }
        try {
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
