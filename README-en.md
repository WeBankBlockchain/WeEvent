[中文](README.md) | [English](README-en.md)

![image](https://github.com/WeBankFinTech/WeEvent-docs/blob/master/docs/image/weevent-logo.png)

[![CodeFactor](https://www.codefactor.io/repository/github/webankfintech/weevent/badge)](https://www.codefactor.io/repository/github/webankfintech/weevent)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/1d2141e952d84a47b0a615e51702bf6f)](https://www.codacy.com/app/WeEventAdmin/WeEvent?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=WeBankFinTech/WeEvent&amp;utm_campaign=Badge_Grade)
[![Build Status](https://travis-ci.com/WeBankFinTech/WeEvent.svg?branch=master)](https://travis-ci.com/WeBankFinTech/WeEvent)
[![codecov](https://codecov.io/gh/WeBankFinTech/WeEvent/branch/master/graph/badge.svg)](https://codecov.io/gh/WeBankFinTech/WeEvent)
[![Latest release](https://img.shields.io/github/release/WeBankFinTech/WeEvent.svg)](https://github.com/WeBankFinTech/WeEvent/releases/latest)
[![Maven Central](https://img.shields.io/maven-central/v/com.webank.weevent/weevent-client.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.webank.weevent%22%20AND%20a:%weevent-client%22)
[![Documentation Status](https://readthedocs.org/projects/weeventdoc/badge/?version=latest)](https://weeventdoc.readthedocs.io/zh_CN/latest)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## What's WeEvent?

WeEvent is a distributed event-driven architecture that implements trustworthy, reliable and efficient cross-institutional and cross-platform event notification mechanism.

WeEvent is an open source architecture developed by WeBank for peer-to-peer cooperation, smart collaboration and value sharing in the Collaborative Business Model. Aiming at higher efficiency in cross-institutional cooperation with lower costs, it connects different platforms such like applications, IOTs, cloud services and private services via cross-institutional and cross-platform event notification and processing without changing the existing programming language and network interface protocol.  

[WeEvent Official Site](http://fintech.webank.com/weevent).

## Getting Start
We can quick install WeEvent in 3 different ways：[Docker image](https://hub.docker.com/r/weevent/), [Bash Shell](https://weeventdoc.readthedocs.io/zh_CN/latest/install/quickinstall.html), [Advanced](https://weeventdoc.readthedocs.io/zh_CN/latest/install/module/index.html).
### Docker image
```shell
$ docker pull weevent/weevent:1.0.0; docker run -d -p 8080:8080 weevent/weevent:1.0.0 /root/run.sh
```

### Bash shell
[Download](https://weeventdoc.readthedocs.io/zh_CN/latest/install/download.html) and unzip the install package, like weevent-1.0.0.tar.gz. And then execute the install shell.
```shell
$ ./install-all.sh -p /usr/local/weevent/
```

### Tutorials
There are some base usecase showed via browser，like [Publish Event](http://localhost:8080/weevent-broker/rest/publish?topic=test&content=helloevent). For more examples, see the [WeEvent tutorials](https://weeventdoc.readthedocs.io/zh_CN/latest/protocal/restful.html).

## User’s Guide
[WeEvent online documents](https://weeventdoc.readthedocs.io/latest).

## Contributions
*   Develop environment  
git, gradle 4.10, java 1.8, nodejs 10.16, prefer IntelliJ IDEA.
*   [WeEvent roadmap](https://github.com/WeBankFinTech/WeEvent/wiki/Project-RoadMap)  
*   [Git workflow](https://github.com/WeBankFinTech/WeEvent/wiki/Project-WorkFlow)  

WeEvent love contributor! Please read the [CONTRIBUTING.md](https://github.com/WeBankFinTech/WeEvent/blob/master/CONTRIBUTING.md) first, and then submit a pull request.

Thank you!

## Community
*   Contacts：weevent@webank.com
