package cn.mst.client.aop;

import cn.mst.client.base.MstAttributeHolder;
import cn.mst.client.base.MstDbConnection;
import cn.mst.client.base.MstDbConnectionLimit;
import cn.mst.client.base.RollbackCoordinator;
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
public class DbAspectJ implements Ordered{

    @Around("execution(java.sql.Connection *..getConnection(..))")
    public Connection proxyConnection(ProceedingJoinPoint joinPoint) throws Throwable {
          String token = MstAttributeHolder.getMstToken();
          if(token==null) {
              return (Connection) joinPoint.proceed();
          }else{
              if(MstDbConnectionLimit.isMaxDbNumber()){
                  throw new RuntimeException("mst db connection user out,please later try");
              }
              Connection connection = (Connection) joinPoint.proceed();
              connection.setAutoCommit(false);
              MstDbConnection dbConnection = new MstDbConnection(connection);
              MstAttributeHolder.putTokenAndCon(token,dbConnection);
              RollbackCoordinator.addConn(token);
              MstDbConnectionLimit.incrementDbNumber();
              return dbConnection;
          }
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
