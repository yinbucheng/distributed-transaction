package cn.bucheng.core.aop;

import cn.bucheng.core.holder.TXConnectionHolder;
import cn.bucheng.config.proxy.TXDBConnection;
import cn.bucheng.core.base.TXDBConnectionLimit;
import cn.bucheng.core.holder.UUIDHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.sql.Connection;

/**
 * @ClassName DbAspectJ
 * @Author buchengyin
 * @Date 2018/12/19 14:34
 **/
@Aspect
@Component
public class TxDBAop implements Ordered {

    @Around("execution(java.sql.Connection *..getConnection(..))")
    public Connection proxyConnection(ProceedingJoinPoint joinPoint) throws Throwable {
        String token = UUIDHolder.getUUID();
        if (token == null) {
            return (Connection) joinPoint.proceed();
        }
        if (TXDBConnectionLimit.isMaxDbNumber()) {
            throw new RuntimeException("tx db connection use out,please later try");
        }
        TXDBConnection dbConnection = TXConnectionHolder.getTXDB(token);
        if (null != dbConnection) {
            return dbConnection;
        }
        TXDBConnectionLimit.incrementDbNumber();
        Connection connection = (Connection) joinPoint.proceed();
        connection.setAutoCommit(false);
        dbConnection = new TXDBConnection(connection);
        TXConnectionHolder.addTXDB(token, dbConnection);
        return dbConnection;
    }


    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
