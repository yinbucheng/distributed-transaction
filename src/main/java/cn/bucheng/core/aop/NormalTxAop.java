package cn.bucheng.core.aop;

import cn.bucheng.common.utils.WebUtils;
import cn.bucheng.common.constant.TransferConstant;
import cn.bucheng.core.base.TXDBConnectionLimit;
import cn.bucheng.core.holder.TXConnectionHolder;
import cn.bucheng.core.holder.ClientChannelHolder;
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
 * 普通是否注解开启拦截器
 *
 * @ClassName TransactionMethodAspectJ
 * @Author buchengyin
 * @Date 2018/12/19 14:54
 **/
@Aspect
@Component
public class NormalTxAop extends BaseAop implements Ordered {

    private Logger logger = LoggerFactory.getLogger(NormalTxAop.class);

    @SuppressWarnings("all")
    @Around("@annotation(org.springframework.transaction.annotation.Transactional)")
    public Object invokeMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        //这里表示分布式服务方法同一个服务不同方法调用
        String token = XIDHolder.getXID();
        if (token != null) {
            return joinPoint.proceed();
        }
        //这里表示不同服务不同方法调用
        token = WebUtils.getRequest().getHeader(TransferConstant.XID_TOKEN);
        logger.debug("get token from request head, tx_xid_token:{}" ,token);
        if (token == null) {
            return joinPoint.proceed();
        }
        if (!ClientChannelHolder.isStart()) {
            logger.error("net client start fail,please make sure net client start");
            throw new RuntimeException("net client start fail,please make sure net client start");
        }
        if (TXDBConnectionLimit.isMaxDbNumber()) {
            logger.error("distribute tx db connection user out,please later try");
            throw new RuntimeException("distribute tx db connection user out,please later try");
        }
        XIDHolder.addXID(token);
        try {
            notifyServer(token, TXRequest.registerTx(token));
            Object value = joinPoint.proceed();
            ClientChannelHolder.writeAndFlush(TXRequest.commitTx(token));
            return value;
        } catch (Exception e) {
            logger.error("tx client send rollback ,cause:" + e);
            notifyServer(token, TXRequest.rollbackTx(token));
            throw new RuntimeException(e);
        } finally {
            XIDHolder.remove();
            TXConnectionHolder.remove(token);
        }
    }


    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;
    }
}
