package cn.mst.client.aop;

import cn.mst.client.base.LockCondition;
import cn.mst.client.base.MstAttributeHolder;
import cn.mst.client.base.MstDbConnectionLimit;
import cn.mst.client.constant.SystemConstant;
import cn.mst.client.net.NetClient;
import cn.mst.common.MstMessageBuilder;
import cn.mst.common.WebUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 这里用来拦截设置事务唯一id
 *
 * @ClassName MstMethodAspectJ
 * @Author buchengyin
 * @Date 2018/12/19 14:55
 **/
@Aspect
@Component
public class MstMethodAspectJ implements Ordered {
    private Logger logger = LoggerFactory.getLogger(MstMethodAspectJ.class);

    @Around("@annotation(cn.mst.client.annotation.BeginMst)")
    public Object invokeMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        if (prevCheck()) return joinPoint.proceed();
       String  token = (String) WebUtils.getRequest().getHeader(SystemConstant.MST_TOKEN);
        if (token != null) {
            return nextMstInvoke(joinPoint, token);
        }
        return beginMstInvoke(joinPoint);
    }

    /**
     * 分布式事务传递执行
     * @param joinPoint
     * @param token
     * @return
     * @throws Throwable
     */
    private Object nextMstInvoke(ProceedingJoinPoint joinPoint, String token) throws Throwable {
        try {
            return registerAndInvokeMethod(joinPoint, token);
        } catch (Exception e) {
            notifyAndWait(token, MstMessageBuilder.sendRollback(token));
            throw new RuntimeException(e);
        } finally {
            MstAttributeHolder.removeMstToken();
            MstAttributeHolder.removeLock(token);
        }
    }


    /**
     * 开始分布式事务执行
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    private Object beginMstInvoke(ProceedingJoinPoint joinPoint) throws Throwable {
        String token = System.nanoTime() + UUID.randomUUID().toString();
        try {
            return registerAndInvokeMethod(joinPoint, token);
        } catch (Exception e) {
            notifyAndWait(token, MstMessageBuilder.sendRollback(token));
            throw new RuntimeException(e);
        } finally {
            NetClient.socketClient.writeAndFlush(MstMessageBuilder.sendFIN(token));
            MstAttributeHolder.removeMstToken();
            MstAttributeHolder.removeLock(token);
        }
    }

    /**
     * 校验是已经在分布式事务中执行
     * @return
     * @throws Throwable
     */
    private boolean prevCheck() throws Throwable {
        if (!NetClient.start || NetClient.socketClient == null || !NetClient.socketClient.channel().isActive()) {
            throw new RuntimeException("netclient start fail,please make sure netclient start");
        }
        //先从内存中获取是否存在，比如这里同一个服务中不同方法调用
        String token = MstAttributeHolder.getMstToken();
        if (token != null) {
            return true;
        }
        if (MstDbConnectionLimit.isMaxDbNumber()) {
            throw new RuntimeException("mst db connection user out,please later try");
        }
        return false;
    }

    /**
     * 注册token到本地和协调器并执行方法，并发送提交命令
     * @param joinPoint
     * @param token
     * @return
     * @throws Throwable
     */
    public static Object registerAndInvokeMethod(ProceedingJoinPoint joinPoint, String token) throws Throwable {
        MstAttributeHolder.putMstToken(token);
//        notifyAndWait(token, MstMessageBuilder.sendRegister(token));
        Object value = joinPoint.proceed();
        NetClient.socketClient.writeAndFlush(MstMessageBuilder.sendCommit(token));
        return value;
    }

    /**
     * 进行阻塞等待协调器唤醒
     * @param token
     * @param msg
     */
    public static void notifyAndWait(String token, String msg) {
        NetClient.socketClient.writeAndFlush(msg);
        LockCondition condition = new LockCondition();
        MstAttributeHolder.putTokenAndLock(token, condition);
        condition.await(60);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
