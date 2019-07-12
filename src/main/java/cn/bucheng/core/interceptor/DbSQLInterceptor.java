package cn.bucheng.core.interceptor;

import com.mysql.jdbc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Properties;

/**
 * @author buchengyin
 * @create 2019/6/22 10:15
 * @describe 拦截sql语句非select操作
 */
public class DbSQLInterceptor implements StatementInterceptorV2 {

    private Logger logger = LoggerFactory.getLogger(DbSQLInterceptor.class);

    @Override
    public void init(Connection connection, Properties properties) throws SQLException {

    }

    @Override
    public ResultSetInternalMethods preProcess(String s, Statement statement, Connection connection) throws SQLException {
        if (statement instanceof PreparedStatement) {
            PreparedStatement preparedStatement = (PreparedStatement) statement;
            s = preparedStatement.asSql();
            if (!s.trim().toLowerCase().startsWith("select"))
                logger.info(s);
        }
        return null;
    }

    @Override
    public boolean executeTopLevelOnly() {
        return false;
    }

    @Override
    public void destroy() {

    }

    @Override
    public ResultSetInternalMethods postProcess(String s, Statement statement, ResultSetInternalMethods resultSetInternalMethods, Connection connection, int i, boolean b, boolean b1, SQLException e) throws SQLException {
        return resultSetInternalMethods;
    }
}
