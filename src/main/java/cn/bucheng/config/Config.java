package cn.bucheng.config;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author buchengyin
 * @create 2019/7/13 8:40
 * @describe
 */
@Configuration
public class Config {


    @Bean
    CuratorFramework curatorFramework(ClientProperties properties) {
        String zookeeperURL = properties.getZk().getHost();
        int sessionTimeOut = properties.getZk().getSessionTimeout();
        int connectionTimeOut = properties.getZk().getConnectionTimeout();
        int baseSleepTimeMS = properties.getZk().getSleepTime();
        int maxRetires = properties.getZk().getMaxRetries();
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(baseSleepTimeMS, maxRetires);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(zookeeperURL)
                .sessionTimeoutMs(sessionTimeOut)
                .connectionTimeoutMs(connectionTimeOut)
                .retryPolicy(retryPolicy)
                .build();
        client.start();
        return client;
    }
}
