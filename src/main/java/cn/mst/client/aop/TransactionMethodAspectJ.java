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
public class TransactionMethodAspectJ implements Ordered{

    private Logger logger = LoggerFactory.getLogger(TransactionMethodAspectJ.class);

    @Around("@annotation(org.springframework.transaction.annotation.Transactional)")
    public Object invokeMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        //这里表示分布式服务方法同一个服务不同方法调用
        String token = MstAttributeHolder.getMstToken();
        if (token != null) {
            return joinPoint.proceed();
        }
        //这里表示不同服务不同方法调用
        token = (String) WebUtils.getRequest().getHeader(SystemConstant.MST_TOKEN);
        logger.debug(SystemConstant.PREV_LOG+" get token :"+token);
        if (token == null) {
            return joinPoint.proceed();
        }
        if (!NetClient.start || NetClient.socketClient == null||NetClient.socketClient.channel().isActive()) {
            throw new RuntimeException("netclient start fail,please make sure netclient start");
        }
        if(MstDbConnectionLimit.isMaxDbNumber()){
            throw new RuntimeException("mst db connection user out,please later try");
        }
        MstAttributeHolder.putMstToken(token);
        try {
            notifyAndWait(token, MstMessageBuilder.sendRegister(token));
            Object value = joinPoint.proceed();
            NetClient.socketClient.writeAndFlush(MstMessageBuilder.sendCommit(token));
            return value;
        } catch (Exception e) {
            notifyAndWait(token, MstMessageBuilder.sendRollback(token));
            throw new RuntimeException(e);
        } finally {
            MstAttributeHolder.removeMstToken();
            MstAttributeHolder.removeLock(token);
        }
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
        return Ordered.LOWEST_PRECEDENCE-1;
    }
}
