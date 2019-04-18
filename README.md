.. image:: https://weeventdoc.readthedocs.io/en/latest/images/weevent-logo.png
   :target: https://weeventdoc.readthedocs.io/en/latest/images/weevent-logo.png
   :alt: weevent-logo.png

## WeEvent 介绍
`WeEvent`是一个基于区块链实现的事件中间件，为业务提供事件发布订阅`Pub/Sub`功能。发布到`WeEvent`上的事件，永久存储和不可篡改，支持事后跟踪和审计。
支持`Restful`、`RPC`、`JsonRPC`、`STOMP`等多种接入方式，


## 特性
- Broker服务：提供事件发布订阅`Pub/Sub`，事件主题的`CRUD`管理等功能；
- 适配多协议：支持`Restful`、`JsonRPC`、`STMOP`、`MQTT `等多种接入协议；
- SDK及Demo：提供一个符合`JMS`规范的Jar包，各种接入协议的代码样例；
- 高可用性：完善的主备服务切换和负载均衡能力；


## 快速入门
- 安装前置依赖

区块链是`WeEvent`的前置依赖，用户需要提前安装，具体操作见[FISCO-BCOS文档](https://fisco-bcos-documentation.readthedocs.io/zh_CN/release-1.3/docs/tools/index.html)。

- 搭建服务

快速搭建一套`WeEvent`服务，请参考[文档](https://weeventdoc.readthedocs.io/en/latest/install/quickinstall.html)。通过一键部署的`WeEvent`服务，用户可以快速体验和开发。

- 体验订阅

参见[更多代码样例](https://github.com/WeBankFinTech/WeEvent/tree/master/src/test/java/com/webank/weevent/sample)。


## 贡献说明
WeEvent爱贡献者！请阅读我们的贡献[文档](http://)，了解如何贡献代码，并提交你的贡献。

希望在您的帮助下WeEvent继续前进。


## 社区
- 联系我们：weevent@webank.com
