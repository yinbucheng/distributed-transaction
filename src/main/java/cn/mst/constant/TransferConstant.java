package cn.mst.constant;

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
    public final static String MST_TOKEN = "Microservice_Transaction";
    public final static String ROOT_PATH = "microservice_transaction";
}
