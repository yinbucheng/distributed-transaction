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
