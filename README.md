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
 1.mst.zk.url      zookeeper的地址比如127.0.0.1:2181
 2.mst.namespace   项目在zookeeper上面的路径
 3.mst.server.port 协调器的端口地址
 ```
 
## 等待测试及优化


