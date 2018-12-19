package cn.mst.client.base;

import cn.mst.client.constant.SystemConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 回滚协调器
 * @ClassName RollbackCoordinator
 * @Author buchengyin
 * @Date 2018/12/19 17:29
 **/
public class RollbackCoordinator {
   private static Logger logger = LoggerFactory.getLogger(RollbackCoordinator.class);
   private  final static int size = 120;
    //这里超时时间默认为120
    private static LinkedBlockingQueue<Map<String,MstDbConnection>>[] rollbackQueue = new LinkedBlockingQueue[size];
    private static Executor executor = Executors.newFixedThreadPool(10);
    private static volatile int pre =size-1;
    private static volatile int cur = 0;
    static{
        for(int i=0;i<size;i++){
            rollbackQueue[i]=new LinkedBlockingQueue<>();
        }
    }

    public static void addConn(String token,MstDbConnection connection){
        Map<String,MstDbConnection> map = new HashMap<>();
        map.put(token,connection);
        rollbackQueue[pre].add(map);
    }

    /**
     * 回滚协调器运行核心方法
     */
    public static void work(){
        for(int i=0;i<Integer.MAX_VALUE;i++){
            LinkedBlockingQueue<Map<String,MstDbConnection>> temp=rollbackQueue[cur];
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    executeRollBack(temp);
                }
            });
            pre =cur;
            cur=cur==size-1?0:cur+1;
        }
    }

    private static void executeRollBack(LinkedBlockingQueue<Map<String,MstDbConnection>> temp) {
        if(temp.size()!=0){
            Iterator<Map<String,MstDbConnection>> iterator = temp.iterator();
            while(iterator.hasNext()){
                Map<String,MstDbConnection> next = iterator.next();
                try {
                    for(Map.Entry<String,MstDbConnection> entry:next.entrySet()){
                        String token = entry.getKey();
                        MstDbConnection connection = entry.getValue();
                        MstAttributeHolder.removeConn(token);
                        connection.realRollback();
                        connection.realClose();
                    }
                } catch (SQLException e) {
                    logger.info(SystemConstant.PREV_LOG+" rollback fail");
                    e.printStackTrace();
                }
                iterator.remove();
            }
        }
    }

}
