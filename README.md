# hrpc 
# Overview
基于netty的rpc框架。轻量 易用
# Features
* 基于netty 4.X。client采用长连接的方式。client内置连接池。
* 服务发现基于zookeeper。
* 支持序列化框架：protostuff。
* 支持动态代理框架：cglib。
# Quick Start
    <dependency>
        <groupId>com.pairs.arch</groupId>
        <artifactId>hrpc</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
## 1、定义公共接口

```
public interface HelloServer {
    public String getName(String name);
}
```
## 2、实现接口
> @HrpcServer 发现服务的注解    
> value 需要指定实现的接口

```
@HrpcServer(value = HelloServer.class)
public class HelloServerImpl implements HelloServer {
    @Override
    public String getName(String name) {
        System.out.println(121212+name);
        return name+"call success";
    }
}

```
## 3、启动服务端。
> 需要手动指定提供服务的包路径

```
public class ServerTest {
    public static void main(String[] args) {
        HrpcServerConfig hrpcServerConfig=HrpcServerConfig.getInstance(Lists.newArrayList("com.pairs.arch.rpc.demo"));
    }
}
```
## 4、客户端消费服务
```
public class ClientTest {

    public static void main(String[] args) {
        HelloServer helloServer= HrpcProxy.getInstance(ServerDiscovery.getInstance()).getBean(HelloServer.class);
        System.out.println(helloServer.getName("aaa"));
    }
}
```

