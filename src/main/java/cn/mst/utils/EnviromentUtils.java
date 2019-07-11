package cn.mst.utils;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

/**
 * @author buchengyin
 * @Date 2018/12/24 22:33
 **/
public class EnviromentUtils implements EnvironmentAware {
    private static Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        EnviromentUtils.environment =environment;
    }

    public static <T> T getProperties(String key,Class<T> clazz){
       return  environment.getProperty(key,clazz);
    }

    public static String getProperties(String key){
        return environment.getProperty(key);
    }
}
