# 第一个开源项目MST分布式事务解决方案。配置更少不需再部署任何其他服务

## 第一步选择合适的分支
```
springboot版本1的使用下面：
https://github.com/yinbucheng/mst/tree/v1.5.6

springboot版本2的使用下面:
https://github.com/yinbucheng/mst/tree/v2.0.1
```
## 第二步选择好合适分支导入后进行maven install
```
根据不同分支上面的readme进行不同操作
 ```
## 

## 注意点
```
1.需要将service层上面错误抛出了
2.使用分布式事务已入相应的包
3.在需要使用分布式事务的服务开始service上面添加@BeginMst注解
4.fegin的failback方法直接抛出异常
```

###  内部原理
```
第一步会进行协调者的选举（协调者就是用来保证各个服务事务正常运行的核心主件）
通过zookeeper上面可以体现出来：打开zookeeper客户端进入到microservice_transaction
在进入到你配置的namespace中会看到instance目录，里面记录是所有参与选举的服务
master中则表示选举成功的服务，内容就是当前正在运行的协调者ip和端口
```
