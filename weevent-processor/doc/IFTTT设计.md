# WeEvent-Processor 系统设计

[TOC]



## 架构设计

### 架构图

#### 整体架构图

![架构图](D:\14-weevent\19-IFTTT\架构图.png)



#### CEP

设置规则说明

![详细说明图](D:\14-weevent\19-IFTTT\规则引擎.png)

**说明：**

- 用户需要提前录入系统，包含产品信息`fromDestination`（设备对应的`TopicName` ）和创建流转`toDestination` (`TopicName`)
- 设置产品名称和规则描述
- 设置产品条件和产品信息
- 设置触发时间是，发送信息给`toDestination`
- 存储


**场景说明**

- Complex Event Processing Sample

```
早上7点到下午5点之间，开门的时候，根据屋内的光照度，来决定是否打开电动窗帘。还可以根据室内的温度，分辨是否需要开空调。
说明：用户设置时间范围、光照度的大小、室内的问题，是规则的触发条件
```

- Complex Event Processing and Streaming Data Analytics Sample

```
冰箱发现冷藏室里面没有水果，冰箱根据主人买水果的记录和习惯，去商店下了一个订单，并将订单信息发送信息给主人。主人点击支付，卖家会把水果送到家。
```

### 最佳实践场景

#### 场景说明

早上7点到下午5点之间，开门的时候，根据屋内的光照度，来决定是否打开电动窗帘。

- 规则名称：good_morning

- 规则条件：

  ```
  Select * from device1 where light>10
  ```

- 流转：

  ```
  toDestination:automatic_curtain
  ```

####  快速入门

- 下载项目

  ```
  $ wget http://...weevent-ifttt.gz.tar
  ```

- 配置参数和启动项目

  ```
  $ cd weevent-ifttt
  $ ./start_engine.sh
  ```

- 检查项目

  ```
  $ ./check-service.sh
  ```

  如有以下打印信息，说明正常。

  ```
  the air condition: my lady , too hot!!!
  ....
  the phone: I got it.
  ```



##  详细需求说明

#### 功能点说明

- CEP规则列表
- 首页展示
- 规则（增删改查）
- 触发条件（增删改查）
- 消息流转（mysql、topic）



#### 界面原型

![](D:\14-weevent\19-IFTTT\效果图\效果图\6规则引擎-1.png)

![](D:\14-weevent\19-IFTTT\效果图\效果图\6规则引擎-2.png)



![](D:\14-weevent\19-IFTTT\效果图\效果图\6规则引擎-31.png)

![](D:\14-weevent\19-IFTTT\效果图\效果图\6规则引擎-32.png)



![](D:\14-weevent\19-IFTTT\效果图\效果图\6规则引擎-4.png)

在线查看界面原型[页面框线图](https://org.modao.cc/app/wyogtki85xjz9xu3lcok4v4neubjp)

![img](C:\Users\CRISIT~1\AppData\Local\Temp\企业微信截图_15657075595464.png)



#### 数据结构设计

#### 规则触发器展示

```
 {
   "ruleName": "temperature-alarm",
    "type":"json",
    "createTime":"2019/08/21 03:22:49",
    "fromDestination":"aircondition",
    "payload":{
        "temperate":30,
        "humidity":0.5
     },
    "ruleContent":"temperature>26",
    "fields":["temperate"],
    "toDestination:",
    "id":"123456",
    "exParam":[""],
    "brokerUrl":"http://127.0.0.1:8090/weevent?groupId=1",
    "databaseUrl":"jdbc:mysql://127.0.0.1:3306/test_demo?root="root"&passwordword="123456"&useSSL=false&serverTimezone=UTC",
    "errorDestination": {
        "message": "error",
        "code": "0",
        "topic":"error"
    }
```

消息的payload数据以JSON格式解析，并根据SQL语句进行处理（如果消息格式不合法，将忽略此消息）

#### 详细说明

1. 创建规则

   ```
   {
       "ruleName":"alarm", 
       "type":"json",
       "payload":{
           "temperate":30,
           "humidity":0.5
        }
   }
   ```

   - ruleName: 支持英文字母、数字、下划线、连字符
   - type：改规则处理数据的格式，目前只支持JSON格式。
   - 规则的详细描述。

2. 设置触发 （**最简化的情况，是只设计触发条件**）

   JSON数据可以映射为虚拟的表，其中Key对应表的列，Value对应列值，这样就可以使用SQL处理。为便于理解，我们将数据流转的一条规则抽象为一条SQL表达（类试MySQL语法）：

    ![详细说明图](D:\14-weevent\19-IFTTT\select.png)

   例如某环境传感器用于火灾预警，可以采集温度、湿度及气压数据，上报数据内容如下：

   ```
   {
   "temperature":25.1,
   "humidity":65
   }
   ```

   ​
   假定温度大于38，湿度小于40时，需要触发报警，可以编写如下的SQL语句：

   ```
   SELECT temperature, deviceName FROM ProductA WHERE temperature > 38 and humidity < 40
   ```

   当上报的数据中，温度大于38且湿度小于40时，会触发该规则，并且解析数据中的温度、设备名称，用于进一步处理。

   - 触发条件(temperature > 38 and humidity < 40)

   - Topic:自定义和通配符 

   - MySQL 说明：

     - JSON数据格式

     SELECT语句中的字段，可以使用上报消息的payload解析结果，即JSON中的键值，也可以使用SQL内置的函数，比如`deviceName`。

     支持`*`和函数的组合。不支持子SQL查询。

     上报的JSON数据格式，可以是数组或者嵌套的JSON，SQL语句支持使用JSONPath获取其中的属性值，如对于`{a:{key1:v1, key2:v2}}`，可以通过`a.key2` 获取到值`v2`。使用变量时，需要注意单双引号区别：单引号表示常量，双引号或不加引号表示变量。如使用单引号`'a.key2'`，值为`a.key2`。

     ```
     SELECT 支持的长度？
     ```

     ​

     - FROM

     FROM 可以填写Topic。Topic中的设备名（deviceName），用于匹配需要处理的设备消息Topic。当有符合Topic规则的消息到达时，消息的payload数据以JSON格式解析，并根据SQL语句进行处理（如果消息格式不合法，将忽略此消息）。

     ​

     - WHERE

     规则触发条件，条件表达式。不支持子SQL查询。WHERE中可以使用的字段和SELECT语句一致，当接收到对应Topic的消息时，WHERE语句的结果会作为是否触发规则的判断条件。

     ```
      "WHERE temperature > 38 and humidity < 40" 表示温度大于38且湿度小于40时，才会触发该规则，执行配置。
     ```

     限制说明：

     - 可以进行单条件查询 >、<、>=、<=、<>、!=、Between、IN、LIKE

3. 设置数据流转目的地

   - 设置流向为Topic
   - 设置流向为关系型数据库

   ![详细说明图](D:\14-weevent\19-IFTTT\Topic解析过程.png)


   

   JSON格式：

   ```
     {
       "ruleName": "temperature-alarm",
       "type":"json",
       "createTime":"2019/08/21 03:22:49",
       "fromDestination":"aircondition",
       "payload":{
           "temperate":30,
           "humidity":0.5
        },
       "ruleContent":"temperature>26",
       "fields":["temperate"],
       "toDestination:",
       "id":"123456",
       "exParam":["extype"],
       "brokerUrl":"http://127.0.0.1:8090/weevent?groupId=1",
       "databaseUrl":"jdbc:mysql://127.0.0.1:3306/test_demo?root="root"&passwordword="123456"&useSSL=false&serverTimezone=UTC",
       "error_message": "error",
       "error_code": "0",
       "error_topic":"error"
       }
   ```

| 设备   | 时间         | items | 规则   |
| ---- | ---------- | ----- | ---- |
| 001  | 2019-08-02 | 65    | 30   |
| 002  | 2019-08-03 | 55    | 28   |

   

4. 设置流转失败的数据转发目的地

```
    "error_message": "error",
    "error_code": "0",
    "error_topic":"error"
```

- topic:topic name
- code:错误码
- message:错误信息


### UML

#### 完整时序图

![img](C:\Users\CRISIT~1\AppData\Local\Temp\企业微信截图_15661789995947.png)

#### 数据流转检查

![img](C:\Users\CRISIT~1\AppData\Local\Temp\企业微信截图_15659468352152.png)

#### 数据流图



![详细说明图](D:\14-weevent\19-IFTTT\存储流.png)

备注：本期只实现HTTP。



#### 类图

![详细说明图](D:\14-weevent\19-IFTTT\类图.png)

#### 

##  接口说明

### 接口基础信息

接口基础信息包含两部分，一部分是规则信息。

- 接口入参数（规则信息）

| **中文**   | **参数名**          | **类型** | **最大长度** | **必填** | 备注                                       |
| -------- | ---------------- | ------ | -------- | ------ | ---------------------------------------- |
| 规则ID     | id               | String |          | 是      | 规则Id                                     |
| 规则名称     | ruleName         | String |          | 是      | 规则名称                                     |
| 创建时间     | createTime       | String |          | 是      | 默认为当前的时间                                 |
| 来源Topic  | fromDestination  | String |          | 否      | 信息来源的Topic                               |
| 规则描述     | payload          | Json   |          | 否      | 消息的payload数据以JSON格式解析，并根据SQL语句进行处理（如果消息格式不合法，将忽略此消息） |
| 出发规则条件   | ruleConditions   | String |          | 否      | 设置条件                                     |
| 筛选条件     | fields           | Array  |          | 否      | 筛选的字段                                    |
| 流向Topic  | toDestination    | String |          | 否      | 流向的Topic                                 |
| 类型       | type             | String |          | 否      | 默认为json                                  |
|          |                  |        |          |        |                                          |
| 错误流转     | errorDestination | json   |          | 否      | 出错的情况，信息流转                               |
| broker地址 | brokerUrl        | String |          | 是      | broker信息，调用发布和订阅时需要用到                    |
| 转发流向DB   | databaseUrl      | String |          | 否      | 数据库信息，设置信息流转时，写入的数据库信息                   |
| 错误信息     | error_message    | String |          | 否      | 设置规则的时候，需要设置错误信息的流向说明                    |
| 错误码      | error_code       | String |          | 否      | 设置规则的时候，需要设置错误信息的错误码                     |
| 错误主题     | error_topic      | String |          | 否      | 设置规则的时候，需要设置错误信息的                        |

- 返回结构体信息

  ```
   {
     "ruleName": "temperature-alarm",
      "type":"json",
      "createTime":"2019/08/21 03:22:49",
      "fromDestination":"aircondition",
      "payload":{
          "temperate":30,
          "humidity":0.5
       },
      "ruleContent":"temperature>26",
      "fields":["temperate"],
      "toDestination:",
      "id":"123456",
      "exParam":[""],
      "brokerUrl":"http://127.0.0.1:8090/weevent?groupId=1",
      "databaseUrl":"jdbc:mysql://127.0.0.1:3306/test_demo?root="root"&passwordword="123456"&useSSL=false&serverTimezone=UTC",
      "error_message": "error",
      "error_code": "0",
      "error_topic":"error"
   }
  ```

-  返回结构体说明

  | **序号** | **中文** | **参数名** | **类型** | **最大长度** | **必填** | **说明**      |
  | ------ | ------ | ------- | ------ | -------- | ------ | ----------- |
  | 1      | 返回码    | code    | String |          | 是      | 返回码信息请附录1   |
  | 2      | 提示信息   | message | String |          | 是      |             |
  | 3      | 详细数据   | data    | Json   |          | 是      | 如果返回错误，则为{} |

- 结构信息

  ```
  {
    "code": 200,
    "message": "can not found the rule content",
    "data":{
      ...
    }
  }
  ```


###  接口列表

#### insert

- 接口描述

  将规则写入数据库

- 请求参数

  ```
   {
   		"id":5,
          "ruleName": "air",
          "fromDestination": "from.com.webank.weevent",
          "brokerUrl": "http://182.254.159.91:8090/weevent",
          "payload":"{\"studentName\":\"lily\",\"studentAge\":12}",
          "payloadType": 0,
          "selectField": null,
          "conditionField": null,
          "conditionType": 1,
          "toDestination": "to.com.webank.weevent",
          "databaseurl": "jdbc:mysql://182.254.159.91:3306/cep?user=root&password=WeEvent@2019",
          "createdTime": "2019-08-23T18:09:16.000+0000",
          "status": 1,
          "errorDestination": null,
          "errorCode": null,
          "errorMessage": null,
          "updatedTime": "2019-08-23T18:09:16.000+0000"
      }
  ```

  - 响应参数
    - 异常返回结果示例（错误码表）


  - 请求正常返回结果

    ```
    {
      "code":"100",
      "message":"success",
      "data":{
          id:""
      }
    }
    ```

    ​

#### getCEPById

- 接口描述

根据规则`id`可以获取规则信息信息。

- 调用方法

HTTP GET

- 请求参数

| **序号** | **中文** | **参数名** | **类型** | **最大长度** | **必填** | **说明**  |
| ------ | ------ | ------- | ------ | -------- | ------ | ------- |
| 1      | 规则Id   | id      | String |          | 是      | 规则的唯一标识 |

- 请求示例

  ```
  http://localhost:8080/weevent/processor/getCEPRuleById?id=123456
  ```

- 响应参数

  - 异常返回结果示例（错误码表）


  - 请求正常返回结果

```
  {
    "ruleName": "temperature-alarm",
    "type":"json",
    "createTime":"2019/08/21 03:22:49",
    "fromDestination":"aircondition",
    "payload":{
        "temperate":30,
        "humidity":0.5
     },
    "ruleContent":"temperature>26",
    "fields":["temperate"],
    "toDestination:",
    "id":"1",
    "exParam":["ex"],
    "brokerUrl":"http://127.0.0.1:8090/weevent?groupId=1",
    "databaseUrl":"jdbc:mysql://127.0.0.1:3306/test_demo?root="root"&passwordword="123456"&useSSL=false&serverTimezone=UTC",
     "error_message": "error",
    "error_code": "0",
    "error_topic":"error"
   }
```
#### getCEPRuleListByPage

- 接口描述

获取规则列表。

- 调用方法

HTTP GET

- 请求参数

| **序号** | **中文**    | **参数名** | **类型** | **最大长度** | **必填** |
| ------ | --------- | ------- | ------ | -------- | ------ |
| 1      | 请求数据的起始位置 | start   | int    |          | 默认为0   |
| 2      | 每页请求的条数   | page    | int    |          | 默认为0   |

- 请求示例

  ```
  http://localhost:8080/weevent/processor/getCEPRuleListByPage?currPage=1&pageSize=10
  ```

- 响应参数

  - 异常返回结果示例（错误码表）


  - 请求正常返回结果

```
  {

    list:{
        "0":{
         "ruleName": "temperature-alarm",
          "type":"json",
          "createTime":"2019/08/21 03:22:49",
          "fromDestination":"aircondition",
          "payload":{
              "temperate":30,
              "humidity":0.5
           },
          "ruleContent":"temperature>26",
          "fields":["temperate"],
          "toDestination:",
          "id":"123456",
          "exParam":["ex"],
          "brokerUrl":"http://127.0.0.1:8090/weevent?groupId=1",
          "databaseUrl":"jdbc:mysql://127.0.0.1:3306/test_demo?root="root"&passwordword="123456"&useSSL=false&serverTimezone=UTC",
           "error_message": "error",
           "error_code": "0",
           "error_topic":"error"
         }
      }
   }
```

#### getCEPRuleList

- 接口描述

根据规则名称获取规则列表。

- 调用方法

HTTP GET

- 请求参数

| **序号** | **中文**    | **参数名**  | **类型** | **最大长度** | **必填** |
| ------ | --------- | -------- | ------ | -------- | ------ |
| 1      | 规则名称      | ruleName | String |          | 是      |
| 2      | 查询的启示位置   | start    | int    |          | 默认为0   |
| 3      | 每次查询分页的长度 | page     | int    |          | 默认为10  |

- 请求示例

  ```
  http://localhost:8080/weevent/processor/getCEPListByNameruleName=airconditioner&start=1&page=10
  ```

- 响应参数

  - 异常返回结果示例（错误码表）


  - 请求正常返回结果

```
  {
    "list":{
        "0":{
         "ruleName": "temperature-alarm",
          "type":"json",
          "createTime":"2019/08/21 03:22:49",
          "fromDestination":"aircondition",
          "payload":{
              "temperate":30,
              "humidity":0.5
           },
          "ruleContent":"temperature>26",
          "fields":["temperate"],
          "toDestination:",
          "id":"123456",
          "exParam":["ex"],
          "brokerUrl":"http://127.0.0.1:8090/weevent?groupId=1",
          "databaseUrl":"jdbc:mysql://127.0.0.1:3306/test_demo?root="root"&passwordword="123456"&useSSL=false&serverTimezone=UTC",
          "error_message": "error",
          "error_code": "0",
          "error_topic":"error"
         }
      }
   }
```

#### deleteCEPRuleById

- 接口描述

根据规则`id`可以获取规则信息信息。

- 调用方法

HTTP POST

- 请求参数

| **序号** | **中文** | **参数名** | **类型** | **最大长度** | **必填** | **说明**  |
| ------ | ------ | ------- | ------ | -------- | ------ | ------- |
| 1      | 规则名称   | id      | String |          | 是      | 规则的唯一标识 |

- 请求示例

  ```
  http://localhost:8080/weevent/processor/deleteCEPRuleById?id=123456
  ```


- 响应参数

  - 异常返回结果示例（信息详情请参看附录1）


  - 请求正常返回结果

    ```
    {
      "code":"100",
      "message":"success",
      "data":{}
    }
    ```
#### startCEPRule

- 接口描述

根据规则`id`可以获取规则信息信息。

- 调用方法

HTTP POST

- 请求参数

| **序号** | **中文** | **参数名** | **类型** | **最大长度** | **必填** | **说明**  |
| ------ | ------ | ------- | ------ | -------- | ------ | ------- |
| 1      | 规则名称   | id      | String |          | 是      | 规则的唯一标识 |

- 请求示例

  ```
  http://localhost:8080/weevent/processor/startCEPRule?id=123456
  ```

  ​

- 响应参数

  - 异常返回结果示例（信息详情请参看附录1）


  - 请求正常返回结果

    ```
    {
      "code":"100",
      "message":"success",
      "data":{}
    }
    ```
    ​


#### updateCEPRuleById

有以下字段是可以被修改。`ruleName、selectField、conditionField、conditionType、toDestination、databaseUrl`。规则状态，不能通过该方法进行修改修改状态。修改状态使用其他的方法

HTTP POST

- 请求参数

| **序号** | **中文** | **参数名** | **类型** | **最大长度** | **必填** | **说明**  |
| ------ | ------ | ------- | ------ | -------- | ------ | ------- |
| 1      | 规则名称   | id      | String |          | 是      | 规则的唯一标识 |

- 请求示例

  ```
  http://localhost:8080/weevent/processor/getcepbyname?id=123456
  ```

  ​

- 响应参数

  - 异常返回结果示例（信息详情请参看附录1）


  - 请求正常返回结果

    ```
    {
      "code":"200",
      "message":"success",
      "data":{}
    }
    ```


### 错误码章节

错误码范围`280000~300000`

| 状态码    | 名称   | 备注   |
| ------ | ---- | ---- |
| 280001 |      |      |
|        |      |      |
|        |      |      |



### 其他说明



###  

