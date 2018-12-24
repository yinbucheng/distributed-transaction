# 第一个开源项目MST分布式事务解决方案。配置更少不需再部署任何其他服务

## 第一步现在项目，采用maven install 安装到本地仓库中（下面适用于springboot版本1cloudDalston.SR4）
```
<dependency>
	<groupId>cn.mst</groupId>
	<artifactId>mst</artifactId>
	<version>1.5.6</version>
</dependency>
```
## 第二步配置添加
```
 1. mst.zk.url      zookeeper的地址比如127.0.0.1:2181 (相同事务控制的微服务配置要同一个zookeeper地址)
 2. mst.namespace   项目在zookeeper上面的路径(相同事务控制的微服务配置要相同)
 3. mst.server.port 协调器的端口地址(只要端口不冲突就可以了)
 4. mst.server.ip   如果为部署在docker上面需要配置docker部署上面真实ip地址直接jar包运行linux或window下面可以不用配置
 
 添加如下配置防止接口被多次重复调用保证接口幂等性
 4. ribbon.okToRetryOnAllOperations = false
 5. ribbon.MaxAutoRetriesNextServer = 0
 ```
 
##  实例代码


