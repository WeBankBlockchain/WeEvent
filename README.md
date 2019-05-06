## 什么是WeEvent？

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/b7cd883f89d740ca9fe71db35b701018)](https://app.codacy.com/app/cristicmf/WeEvent?utm_source=github.com&utm_medium=referral&utm_content=WeBankFinTech/WeEvent&utm_campaign=Badge_Grade_Settings)

WeEvent是一套分布式事件驱动架构，实现了可信、可靠、高效的跨机构、跨平台事件通知机制。

WeEvent由微众银行自主研发并完全开源，秉承分布式商业模式中对等合作、智能协同、价值共享的设计理念，致力于提升机构间合作效率，降低合作成本，同时打通应用程序、物联网、云服务和私有服务等不同平台，最终在不改变已有商业系统的开发语言、接入协议的情况下，做到跨机构、跨平台的事件通知与处理。更多内容详见[WeEvent官网](http://fintech.webank.com/weevent) 。


### 特性
- Broker服务：提供事件主题的CRUD管理，事件发布订阅Publish/Subscribe等功能；
- 适配多协议：支持RESTful、JsonRPC、STMOP、MQTT等多种接入协议；
- SDK及Demo：提供一个符合Java JMS规范JAR包，以及各种接入协议的代码样例；
- 高可用性：通过集群的负载均衡和服务主备切换实现高可用；
- 事件治理：通过Web管理台可以管理事件主题，可以查看区块链FISCO-BCOS节点、主题订阅等信息;


### 快速入门
- 依赖安装

    在体验WeEvent之前，用户需提前搭建好区块链，WeEvent现已支持FISCO-BCOS区块链底层平台，具体操作见[FISCO-BCOS文档](https://fisco-bcos-documentation.readthedocs.io/zh_CN/release-1.3/docs/tools/index.html)。
    
- 服务搭建

    快速搭建一套WeEvent服务，请参考[快速安装文档](https://weeventdoc.readthedocs.io/zh_CN/latest/install/quickinstall.html)。

- 功能体验

    通过浏览器就可以体验事件发布等基本功能，如[发布事件](http://localhost:8080/weevent/rest/publish?topic=test&content=helloevent)。 更多功能，请参见[WeEvent接入样例](https://github.com/WeBankFinTech/WeEvent/tree/master/weevent-broker/src/test/java/com/webank/weevent/sample)。


### 贡献说明
WeEvent爱贡献者！请阅读我们的贡献[文档](https://github.com/WeBankFinTech/WeEvent/blob/master/CONTRIBUTING.md)，了解如何贡献代码，并提交你的贡献。

希望在您的帮助下WeEvent继续前进。


### 社区
- 联系我们：weevent@webank.com
