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
public class MstMethodAspectJ implements Ordered{
    private Logger logger = LoggerFactory.getLogger(MstMethodAspectJ.class);

    @Around("@annotation(cn.mst.client.annotation.BeginMst)")
    public Object invokeMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!NetClient.start || NetClient.socketClient == null) {
            throw new RuntimeException("netclient start fail,please make sure netclient start");
        }
        //先从内存中获取是否存在，比如这里同一个服务中不同方法调用
        String token = MstAttributeHolder.getMstToken();
        if (token != null) {
            return joinPoint.proceed();
        }
        if(MstDbConnectionLimit.isMaxDbNumber()){
            throw new RuntimeException("mst db connection user out,please later try");
        }

        //再从请求头中获取是否存在，这里表示不同服务调用A-B中不同方法
        token = (String) WebUtils.getRequest().getHeader(SystemConstant.MST_TOKEN);
        MstAttributeHolder.putMstToken(token);
        if (token != null) {
            notifyAndWait(token, MstMessageBuilder.sendRegister(token));
            try {
                Object value = joinPoint.proceed();
                NetClient.socketClient.writeAndFlush(MstMessageBuilder.sendCommit(token));
                return value;
            } catch (Exception e) {
                NetClient.socketClient.writeAndFlush(MstMessageBuilder.sendRollback(token));
                throw new RuntimeException(e);
            } finally {
                MstAttributeHolder.removeMstToken();
                MstAttributeHolder.removeLock(token);
            }
        }

        token = System.nanoTime() + UUID.randomUUID().toString();
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
            NetClient.socketClient.writeAndFlush(MstMessageBuilder.sendFIN(token));
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
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
