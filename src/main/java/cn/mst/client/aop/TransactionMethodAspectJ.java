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

/**
 * 这里拦截用来获取事务id并和服务端进行通信
 *
 * @ClassName TransactionMethodAspectJ
 * @Author buchengyin
 * @Date 2018/12/19 14:54
 **/
@Aspect
@Component
public class TransactionMethodAspectJ implements Ordered {

    private Logger logger = LoggerFactory.getLogger(TransactionMethodAspectJ.class);

    @Around("@annotation(org.springframework.transaction.annotation.Transactional)")
    public Object invokeMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        //这里表示分布式服务方法同一个服务不同方法调用
        if (prevCheck()) return joinPoint.proceed();
        String token;
        //这里表示不同服务不同方法调用
        token = (String) WebUtils.getRequest().getHeader(SystemConstant.MST_TOKEN);
        if (token == null) {
            return joinPoint.proceed();
        }
        checkMstOK();
        return nextMstInvoke(joinPoint, token);
    }

    /**
     * 执行下一个分布式事务
     * @param joinPoint
     * @param token
     * @return
     * @throws Throwable
     */
    private Object nextMstInvoke(ProceedingJoinPoint joinPoint, String token) throws Throwable {
        try {
            return MstMethodAspectJ.registerAndInvokeMethod(joinPoint, token);
        } catch (Exception e) {
            notifyAndWait(token, MstMessageBuilder.sendRollback(token));
            throw new RuntimeException(e);
        } finally {
            MstAttributeHolder.removeMstToken();
            MstAttributeHolder.removeLock(token);
        }
    }

    /**
     * 校验分布式事务传递是否ok
     */
    private void checkMstOK() {
        if (!NetClient.start || NetClient.socketClient == null || !NetClient.socketClient.channel().isActive()) {
            throw new RuntimeException("netclient start fail,please make sure netclient start");
        }
        if (MstDbConnectionLimit.isMaxDbNumber()) {
            throw new RuntimeException("mst db connection user out,please later try");
        }
    }

    /**
     * 判断当前方法是否正在分布式事务中执行
     * @return
     * @throws Throwable
     */
    private boolean prevCheck() throws Throwable {
        String token = MstAttributeHolder.getMstToken();
        if (token != null) {
            return true;
        }
        return false;
    }

    //通知服务器并等待回复
    private void notifyAndWait(String token, String msg) {
        NetClient.socketClient.writeAndFlush(msg);
        LockCondition condition = new LockCondition();
        MstAttributeHolder.putTokenAndLock(token, condition);
        condition.await(60);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;
    }
}
