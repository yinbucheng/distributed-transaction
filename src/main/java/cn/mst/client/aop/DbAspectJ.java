package cn.mst.client.aop;

import cn.mst.client.base.MstAttributeHolder;
import cn.mst.client.base.MstDbConnection;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.sql.Connection;

/**
 * @ClassName DbAspectJ
 * @Author buchengyin
 * @Date 2018/12/19 14:34
 **/
@Aspect
public class DbAspectJ {

    @Around("execution(java.sql.Connection *..getConnection(..))")
    public Connection proxyConnection(ProceedingJoinPoint joinPoint) throws Throwable {
          String token = MstAttributeHolder.getMstToken();
          if(token==null) {
              return (Connection) joinPoint.proceed();
          }else{
              Connection connection = (Connection) joinPoint.proceed();
              connection.setAutoCommit(false);
              MstDbConnection dbConnection = new MstDbConnection(connection);
              return dbConnection;
          }
    }
}
