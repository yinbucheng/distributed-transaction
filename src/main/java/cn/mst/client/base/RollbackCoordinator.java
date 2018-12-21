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
 *
 * @ClassName RollbackCoordinator
 * @Author buchengyin
 * @Date 2018/12/19 17:29
 **/
public class RollbackCoordinator {
    private static Logger logger = LoggerFactory.getLogger(RollbackCoordinator.class);
    private final static int size = 120;
    private static volatile boolean start = false;
    //这里超时时间默认为120
    private static LinkedBlockingQueue<String>[] rollbackQueue = new LinkedBlockingQueue[size];
    private static Executor executor = Executors.newFixedThreadPool(10);
    private static volatile int pre = size - 1;
    private static volatile int cur = 0;
    private static Executor singleExecutor = Executors.newSingleThreadExecutor();

    static {
        for (int i = 0; i < size; i++) {
            rollbackQueue[i] = new LinkedBlockingQueue<>();
        }
    }

    public static void addConn(String token) {
        rollbackQueue[pre].add(token);
    }

    /**
     * 回滚协调器运行核心方法
     */
    public static void work() {
        if (start)
            return;
        start = true;
        singleExecutor.execute(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < Integer.MAX_VALUE; i++) {
                    LinkedBlockingQueue<String> temp = rollbackQueue[cur];
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            executeRollBack(temp);
                        }
                    });
                    pre = cur;
                    cur = cur == size - 1 ? 0 : cur + 1;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    private static void executeRollBack(LinkedBlockingQueue<String> temp) {
        if (temp.size() != 0) {
            Iterator<String> iterator = temp.iterator();
            while (iterator.hasNext()) {
                String token = iterator.next();
                try {
                    MstDbConnection connection = MstAttributeHolder.removeConn(token);
                    if (connection != null) {
                        connection.realRollbackAndClose();
                    }
                } catch (SQLException e) {
                    logger.info(SystemConstant.PREV_LOG + " rollback fail");
                    e.printStackTrace();
                }
                iterator.remove();
            }
        }
    }

    public static boolean isStart() {
        return start;
    }


}
