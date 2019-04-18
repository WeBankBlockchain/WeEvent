## WeEvent 介绍
`WeEvent`是一个基于区块链实现的事件中间件，为业务提供事件发布/订阅`Pub/Sub`功能。发布到`WeEvent`上的事件，永久存储和不可篡改，支持事后跟踪和审计。
支持`Restful`、`RPC`、`JsonRPC`、`STOMP`等多种接入方式，


## 特性
- Broker服务：提供主题`Topic`的`CRUD`管理、事件发布/订阅`Pub/Sub`等功能；
- 适配多协议：支持`Restful`、`JsonRPC`、`STMOP`、`MQTT `等多种接入协议；
- Java SDK：提供一个符合`JMS`规范的Jar包，封装了`Broker`服务提供的各种能力；
- 高可用性：完善的主备服务切换和负载均衡能力；
- 丰富样例：各种接入方式的使用代码样例。


## 快速入门
- 安装前置依赖

区块链是`WeEvent`的前置依赖，用户需要提前安装，具体操作见[FISCO-BCOS文档](https://fisco-bcos-documentation.readthedocs.io/zh_CN/release-1.3/docs/tools/index.html)。

- 搭建服务

快速搭建一套`WeEvent`的服务，请参考[文档](http://)。通过一键部署的`WeEvent`的服务，用户可以快速体验和开发。

- 体验订阅

用户可以下载[Client](http://)，体验创建主题`Topic`，发布/订阅事件`Event`。

## 贡献说明
WeEvent爱贡献者！请阅读我们的贡献[文档](http://)，了解如何贡献代码，并提交你的贡献。

希望在您的帮助下WeEvent继续前进。


## 社区
- 联系我们：weevent@webank.com
