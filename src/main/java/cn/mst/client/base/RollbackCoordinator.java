package cn.mst.client.base;

import cn.mst.constant.ClientConstant;
import cn.mst.client.holder.TXDBHolder;
import cn.mst.proxy.TXDBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 回滚事务器，保证将长时间未提交的事件进行提交
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
        Thread thread = new Thread(() -> {
            for (; ; ) {
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
        );
        thread.setDaemon(true);
        thread.setName("rollback_coordinator");
        thread.start();
    }

    private static void executeRollBack(LinkedBlockingQueue<String> temp) {
        if (temp == null || temp.size() == 0)
            return;
        Iterator<String> iterator = temp.iterator();
        while (iterator.hasNext()) {
            String token = iterator.next();
            try {
                TXDBConnection connection = TXDBHolder.remove(token);
                if (connection != null) {
                    connection.realRollbackAndClose();
                }
            } catch (SQLException e) {
                logger.info(ClientConstant.PREV_LOG + " rollback fail");
                e.printStackTrace();
            }
            iterator.remove();
        }
    }

    public static boolean isStart() {
        return start;
    }


}
