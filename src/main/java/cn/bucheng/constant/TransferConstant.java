package cn.bucheng.constant;

/**
 * @author ：yinchong
 * @create ：2019/7/11 12:36
 * @description：
 * @modified By：
 * @version:
 */
public class TransferConstant {
    public static final String PING = "PING";
    //成功
    public static final int ACK = 200;
    //注册事件
    public static final int REGISTER = 1;
    //提交事件
    public static final int COMMIT = 2;
    //回滚事件
    public static final int ROLLBACK = 3;
    //最终事件FIN由分布式事务开始服务发起
    public static final int FIN = 4;
    //进行分布式事务管控的实例
    public final static String INSTANCE_NAME = "instances";
    //充当分布式事务中的协调器实例
    public final static String LEADER_NAME = "leader";
    //执行超时时间
    public final static String TX_EXECUTE_TIMEOUT = "mst.tx.execute.timeout";
    //默认为3分钟
    public final static int DEFAULT_TX_EXECUTE_TIMEOUT = 180;
    //默认启动的端口步长
    public static int STEP_PORT = 33;

    public static final int EPHEMERAL = 0;
    public static final int PERSISTENT = 1;

    public static final String VERSION = "v1.0.0";

    public static final String XID_TOKEN ="tx_xid_token";
}
