![image](https://github.com/WeBankFinTech/WeEvent-docs/blob/master/docs/image/weevent-logo.png)

[![CodeFactor](https://www.codefactor.io/repository/github/webankfintech/weevent/badge)](https://www.codefactor.io/repository/github/webankfintech/weevent)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/1d2141e952d84a47b0a615e51702bf6f)](https://www.codacy.com/app/WeEventAdmin/WeEvent?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=WeBankFinTech/WeEvent&amp;utm_campaign=Badge_Grade)
[![Build Status](https://travis-ci.com/WeBankFinTech/WeEvent.svg?branch=master)](https://travis-ci.com/WeBankFinTech/WeEvent)
[![Latest release](https://img.shields.io/github/release/WeBankFinTech/WeEvent.svg)](https://github.com/WeBankFinTech/WeEvent/releases/latest)
[![Maven Central](https://img.shields.io/maven-central/v/com.webank.weevent/weevent-client.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.webank.weevent%22%20AND%20a:%weevent-client%22)
[![Documentation](https://img.shields.io/badge/api-reference-blue.svg)](https://weeventdoc.readthedocs.io/zh_CN/latest/protocal/index.html)
[![Documentation Status](https://readthedocs.org/projects/weeventdoc/badge/?version=latest)](https://weeventdoc.readthedocs.io/zh_CN/latest)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## 什么是WeEvent？
WeEvent是一套分布式事件驱动架构，实现了可信、可靠、高效的跨机构、跨平台事件通知机制。

WeEvent由微众银行自主研发并完全开源，秉承分布式商业模式中对等合作、智能协同、价值共享的设计理念，致力于提升机构间合作效率，降低合作成本，同时打通应用程序、物联网、云服务和私有服务等不同平台，最终在不改变已有商业系统的开发语言、接入协议的情况下，做到跨机构、跨平台的事件通知与处理。  
[WeEvent官网](http://fintech.webank.com/weevent) 。

## 快速入门
支持[Docker镜像](https://hub.docker.com/r/weevent/)、[Bash一键脚本](https://weeventdoc.readthedocs.io/zh_CN/latest/install/quickinstall.html)、[普通安装](https://weeventdoc.readthedocs.io/zh_CN/latest/install/module/index.html)三种安装方式。
#### Docker镜像安装
```
$ docker pull weevent/weevent:1.0.0; docker run -d -p 8080:8080 weevent/weevent:1.0.0 /root/run.sh
```

#### Bash一键安装
下载并且解压安装包，如weevent-1.0.0.tar.gz。在解压目录中执行安装脚本。
```
$ ./install-all.sh -p /usr/local/weevent/
```

#### 功能体验
通过浏览器即可体验事件发布等基本功能，如[发布事件](http://localhost:8080/weevent/rest/publish?topic=test&content=helloevent)。
更多功能，请参见[WeEvent接入](https://weeventdoc.readthedocs.io/zh_CN/latest/protocal/restful.html)。

## 用户文档
[WeEvent在线文档](https://weeventdoc.readthedocs.io/zh_CN/latest)。

## 项目贡献
- 开发环境  
git，gradle 4.10，java 1.8，nodejs 10.16，推荐使用IDEA。
- [项目计划](https://github.com/WeBankFinTech/WeEvent/wiki/Project-RoadMap)  
- [代码提交](https://github.com/WeBankFinTech/WeEvent/wiki/Project-WorkFlow)  

WeEvent爱贡献者！请阅读我们的[贡献文档](https://github.com/WeBankFinTech/WeEvent/blob/master/CONTRIBUTING.md)，了解如何贡献代码，并提交你的贡献。

希望在您的帮助下WeEvent继续前进。

## 社区
- 联系我们：weevent@webank.com
