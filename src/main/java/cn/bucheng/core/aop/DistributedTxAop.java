package cn.bucheng.core.aop;

import cn.bucheng.common.utils.WebUtils;
import cn.bucheng.constant.TransferConstant;
import cn.bucheng.core.base.TXDBConnectionLimit;
import cn.bucheng.core.holder.ClientChannelHolder;
import cn.bucheng.core.holder.TXConnectionHolder;
import cn.bucheng.core.holder.XIDHolder;
import cn.bucheng.model.req.TXRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * 分布式事务开始拦截器
 *
 * @ClassName MstMethodAspectJ
 * @Author buchengyin
 * @Date 2018/12/19 14:55
 **/
@Aspect
@Component
@SuppressWarnings("all")
public class DistributedTxAop extends BaseAop implements Ordered {
    private Logger logger = LoggerFactory.getLogger(DistributedTxAop.class);

    @Around("@annotation(cn.bucheng.config.annotation.BeginJTA)")
    public Object invokeMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!ClientChannelHolder.isStart()) {
            logger.error("mst client start fail,please make sure mst client start");
            throw new RuntimeException("mst client start fail,please make sure mst client start");
        }
        //先从内存中获取是否存在，比如这里同一个服务中不同方法调用,A-A
        String token = XIDHolder.getXID();
        if (token != null) {
            return joinPoint.proceed();
        }
        if (TXDBConnectionLimit.isMaxDbNumber()) {
            logger.error("mst db connection use out,please later try");
            throw new RuntimeException("mst db connection use out,please later try");
        }

        //再从请求头中获取是否存在，这里表示不同服务调用A-B中不同方法
        token = WebUtils.getRequest().getHeader(TransferConstant.XID_TOKEN);
        if (token != null) {
            return joinExistDistributeTx(joinPoint, token);
        }

        return newDistributeTx(joinPoint);
    }

    //加入到已经存在的分布式事务
    private Object joinExistDistributeTx(ProceedingJoinPoint joinPoint, String token) throws Throwable {
        notifyServer(token, TXRequest.registerTx(token));
        try {
            Object value = joinPoint.proceed();
            ClientChannelHolder.writeAndFlush(TXRequest.commitTx(token));
            return value;
        } catch (Exception e) {
            ClientChannelHolder.writeAndFlush(TXRequest.rollbackTx(token));
            logger.error("mst send rollback ,cause :" + e);
            throw new RuntimeException(e);
        } finally {
            XIDHolder.remove();
            TXConnectionHolder.remove(token);
        }
    }

    //新的分布式事务开始
    private Object newDistributeTx(ProceedingJoinPoint joinPoint) throws Throwable {
        String token;
        token = XIDHolder.createAndGetXID();
        try {
            notifyServer(token, TXRequest.registerTx(token));
            Object value = joinPoint.proceed();
            ClientChannelHolder.writeAndFlush(TXRequest.commitTx(token));
            return value;
        } catch (Exception e) {
            notifyServer(token, TXRequest.rollbackTx(token));
            logger.error("mst send rollback,cause:" + e);
            throw new RuntimeException(e);
        } finally {
            ClientChannelHolder.writeAndFlush(TXRequest.finalTx(token));
            XIDHolder.remove();
            TXConnectionHolder.remove(token);
        }
    }


    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
