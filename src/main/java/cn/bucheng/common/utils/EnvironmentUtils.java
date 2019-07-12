package cn.bucheng.common.utils;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

/**
 * @author buchengyin
 * @Date 2018/12/24 22:33
 **/
public class EnvironmentUtils implements EnvironmentAware {
    private static Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        EnvironmentUtils.environment =environment;
    }

    public static <T> T getProperties(String key,Class<T> clazz){
       return  environment.getProperty(key,clazz);
    }

    public static String getProperty(String key){
        return environment.getProperty(key);
    }

    public static int getIntValue(String key,int defaultValue){
        String value = environment.getProperty(key);
        if(null==value){
            return defaultValue;
        }

        return Integer.parseInt(value);
    }
}
