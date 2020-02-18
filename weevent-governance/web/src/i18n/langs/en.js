module.exports = {
  header: {
    'broker': 'Broker',
    'group': 'Group',
    'lang': 'English',
    'version': 'Version',
    'lastCommit': 'Last git commit',
    'weEventVersion': "WeEvent's version",
    'serverSetting': 'Broker setting',
    'userSetting': 'User setting',
    'login': 'Sign In',
    'loginOut': 'Sign Out'
  },
  common: {
    'back': 'Back',
    'edit': 'Edit',
    'delete': 'Delete',
    'action': 'Action',
    'ok': 'Ok',
    'cancel': 'Cancel',
    'name': 'Name',
    'yes': 'Yes',
    'no': 'No',
    'addSuccess': 'Add Success',
    'addFail': 'Add Failed',
    'editSuccess': 'Edit Success',
    'editFail': 'Edit Failed',
    'deleteSuccess': 'Delete Success',
    'deleteFail': 'Delete Failed',
    'isDelete': 'Sure to delete it?',
    'loading': 'loading ...',
    'add': 'Add',
    'search': 'Search',
    'detail': 'Description',
    'choose': 'Choose',
    'operFail': 'Failed',
    'examples': 'Examples: ',
    'noData': 'Empty data',
    'enter': 'Please input',
    'reqException': 'Request exception',
    'timeOut': 'Request timeout',
    'all': 'all',
    'noServer': 'Please input the servers'
  },
  serverSet: {
    'serverMana': 'Server Management',
    'serverName': 'Server Name',
    'addServer': 'Add Server',
    'editServer': 'Edit Server',
    'brokerURLAddress': 'WeEvent Broker URL',
    'webaseURLAddress': 'WeBASE URL',
    'authorized': 'Authorized Account',
    'ruleEngine': 'Eule Engine',
    'namePlaceholder': 'Please input server name(1-20character,number,underline)',
    'borkerPlaceholder': 'eg: "http://127.0.0.1:8080/weevent"',
    'webasePlaceholder': 'eg: "http://127.0.0.1:8080/webase"',
    'authorizedPlaceholder': 'Select Account',
    'errorServer': 'Invalid server name',
    'noServerName': 'Empty server name',
    'emptyAddress': 'Invalid server URL',
    'errorAddress': 'Can not connect to server',
    'exitBrokerURL': 'Broker server is already existed, please apply for authorization from another user'
  },
  userSet: {
    'userName': 'Account',
    'passWord': 'Password',
    'enterUserName': 'Please input account',
    'quickRegistered': 'Sign Up',
    'confirmPassWord': 'Confirm',
    'newPassWord': 'New password',
    'oldPassWord': 'Old password',
    'enterPassWord': 'Please input password',
    'enterAgain': 'Input Again',
    'modify': 'Modify',
    'hasAccount': 'Exist account, Sign In',
    'registered': 'Register',
    'regSuccess': 'Register success',
    'regFail': 'Register failed',
    'emptyUserName': 'Empty account name',
    'errorUserName': '6~20 characters contains alpha and digital',
    'exitUserName': 'Account exist',
    'enterPassWordAgain': 'Please input confirm password',
    'passWordInconsistent': 'Not same password',
    'errorOldPassWord': 'Invalid old password',
    'passWordModifySuccess': 'Change password success',
    'mail': 'Email',
    'emptyEail': 'Empty email',
    'errorEail': 'Invalid email format',
    'loginFail': 'Sign in failed',
    'errorLogin': 'Account/Password is wrong，input again',
    'forgetPassWord': 'Forget password?',
    'login': 'Sign In',
    'resetPassWord': 'Reset password',
    'mailWarning': 'Notice: Reset password will send a confirm mail to the binding email',
    'noUser': 'Account not exist',
    'sendMailFail': 'Send Email failed',
    'sendMailSuccess': 'Send Email success'
  },
  sideBar: {
    'blockChainInfor': 'BlockChain',
    'overview': 'Overview',
    'transaction': 'Transactions',
    'transactionDetial': 'Transaction List',
    'nodeList': 'Nodes',
    'topic': 'Topic',
    'topicList': 'Topics',
    'statistics': 'Event History',
    'subcription': 'Current Subscription',
    'engine': 'Streaming',
    'ruleMana': 'Rule Engine',
    'ruleDetail': 'Rule Detail',
    'sources': 'DataBase Resources'
  },
  overview: {
    'nodeNum': 'Nodes',
    'blockNum': 'Blocks',
    'transactionNum': 'Transactions',
    'lastWeek': 'History published Events',
    'essential': 'Monitor'
  },
  tableCont: {
    'blockNumber': 'Block Height',
    'transCount': 'Transaction',
    'timestamp': 'Create Time',
    'pkHash': 'Block Hash',
    'transDetial': 'Transactions',
    'copySuccess': 'Copy Success',
    'transHash': 'Transaction Hash',
    'nodeName': 'Node',
    'state': 'Status',
    'run': 'Run',
    'stop': 'Stop',
    'getDataError': 'Request failed',
    'searchTopic': 'Please input topic',
    'exitTopic': 'Topic is already existed',
    'sequenceNumber': 'Published Events',
    'newBlockNumber': 'Last Event’s Block',
    'address': 'Topic Contract Address',
    'creater': 'Create Account',
    'addTopic': 'New topic',
    'noName': 'Empty topic name',
    'noMore': 'Topic name exceed 64 byte',
    'errorTopicName': 'Invalid topic name',
    'chooseTopic': 'Topic',
    'chooseTime': 'Date',
    'beginTime': 'Start timestamp',
    'endTime': 'End timestamp',
    'machine': 'Broker IP',
    'subscribeId': 'SubscriptionID',
    'remoteIp': 'Remote IP',
    'interfaceType': 'Protocol',
    'notifiedEventCount': 'Notified events',
    'notifyingEventCount': 'Notifying events',
    'createTimeStamp': 'Create timestamp',
    'nodeType': 'Node Type',
    'sealer': 'sealer',
    'observer': 'observer',
    'lastTimestamp': 'Last Update Time'
  },
  rule: {
    'dataGuide': 'User Guide',
    'creatRuleRemark': 'Streaming processor support Topic event in JSON format',
    'useText': 'Tutorials',
    'creatNewRule': 'New Rule',
    'creatRule': 'Create Rule',
    'editRule': 'Edit Rule',
    'addOperation': 'Add Operation',
    'checkRule': 'Check Rule',
    'startRule': 'Start Rule',
    'ruleList': 'Rule List',
    'creatNow': 'Create Now',
    'enterRuleName': 'Please input rule name',
    'ruleName': 'Rule Name',
    'payloadType': 'Format',
    'payloadMap': 'Event Payload Samples',
    'ruleDescription': 'Rule Description',
    'run': 'Running',
    'notRun': 'Init',
    'start': 'Start',
    'stop': 'Stop',
    'read': 'View',
    'delete': 'Delete',
    'dataType': 'Data type',
    'enterPayload': 'Please input event payload samples',
    'commit': 'Commit',
    'errorType': 'Invalid format',
    'isStart': 'Started',
    'startFail': 'Start failed',
    'isStop': 'Stopped',
    'isDelete': 'Sure to delete?',
    'deleteRule': 'Delete Rule',
    'hasDelete': 'Deleted',
    'creatSuccess': 'Create success',
    'creatFail': 'Create fail',
    'ruleDataBaseId': 'JDBC Resources',
    'addAddress': 'New Database',
    'addJDBCAddress': 'Add JDBC',
    'editJDBCAddress': 'Edit JDBC',
    'JDBCinfor': 'JDBC Information',
    'enterDB': 'Please input database JDBC URL',
    'JDBCname': 'JDBC Name',
    'JDBCIP': 'JDBC IP',
    'JDBCport': 'JDBC Port',
    'JDBCDatabaseName': 'Database',
    'JDBCdatabaseUrl': 'DatabaseUrl',
    'JDBCusername': 'Username',
    'JDBCpassword': 'Password',
    'tableName': 'Table Name',
    'errorTable': 'Can not connect to the table in selected baseData',
    'inputTableName': 'Please input the table Name in selected baseData',
    'optionalParameter': 'Optional',
    'enterJDBCname': 'Please input database JDBC Name',
    'enterJDBCIP': 'Please input database JDBC IP',
    'enterJDBCport': 'Please input database JDBC Port',
    'enterJDBCDatabaseName': 'Please input Database Name',
    'enterJDBCDatabaseUrl': 'Please input Database Url',
    'enterJDBCusername': 'Please input database Username',
    'enterJDBCpassword': 'Please input database Password',
    'enterTableName': 'Please input database Table Name',
    'enditAddress': 'Edit Database',
    'deleteAddress': 'Delete database?',
    'checkJDBC': 'Check JDBC',
    'connectSuccess': 'JDBC Connect Success',
    'connectFailed': 'JDBC Connect Failed',
    'databaseType': 'Databae Type'
  },
  ruleDetail: {
    'guideDetail': 'Data Guide Detail',
    'ruleInfo': 'Rule Info',
    'editRule': 'Edit Rule',
    'processData': 'Rule Detail',
    'sqlDescription': 'Grammar instructions',
    'ruleDetail': 'Rule Detail',
    'noRule': 'Empty rule detail',
    'ruleSearchLetter': 'Rule expression',
    'ruleSearchWarning': 'Rule Search Warning',
    'letter': 'Fields',
    'dataCirculat': 'Streaming Target',
    'abnormalData': 'Exception',
    'condition': 'Filter Condition',
    'nocondition': 'please set filter condition',
    'completeLetter': 'Unfinished',
    'forwardData': 'Target',
    'forwardOption': 'Forward while exception',
    'dataDestination': 'Destination',
    'selectOperation': 'Select Operation',
    'selectGuide': 'Please select a destination',
    'toTopic': 'Target To Topic',
    'toDB': 'Target To DB',
    'errorTopic': 'Select Topic',
    'db': 'Database',
    'selectDB': 'Select Database',
    'guideURL': 'No Target',
    'setGuide': 'Setting',
    'guideAddress': 'Empty database',
    'abnormalAddress': 'Empty error database',
    'cannotSame': 'Cannot same with Streaming Target'
  },
  ruleStatic: {
    'readRule': 'Read Rules',
    'readInsideRule': 'System Rules',
    'systemRules': 'System Rules',
    'ruleName': 'Rule Name',
    'hitTimes': 'Hit Times',
    'notHitTimes': 'Hit Failed Times',
    'successTimes': 'Successful Times',
    'failTimes': 'Failed Times',
    'startTime': 'Start Time',
    'readFailRecord': 'Read Record',
    'lastRecord': 'Last Time',
    'runningStatus': 'Running Status',
    'destinationType': 'Destination Type'
  },
  ruleCheck: {
    'inputRule': 'please input completed rule',
    'notNumber': 'data type error - not a number',
    'inputInteger': 'data type error - not an integer',
    'bigger': 'please input the value bigger then 0',
    'inputString': 'please warp the string in double quotes',
    'errorOperator': 'operator is error please use != or ==',
    'errorParameter': 'function error - parameter is error',
    'errorIndex': 'function error - index is not a integer',
    'typeErrorString': 'Type error - the type of key is String different with input value',
    'typeErrorNumber': 'Type error - the type of key is Number different with input value',
    'typeTimeError': 'Use this function, the value of key should be a time(hh:mm:ss type)',
    'typeDateError': 'Use this function, the value of key should be a time(YYYY-MM-DD type)',
    'errorTime': 'Use this function, the value of key should be a time("YYYY-MM-DD hh:mm:ss" type)',
    'columnMarkError': 'Use this function, the selected datas should be string type',
    'notAdate': 'The value of selected data is a error date',
    'notAtime': 'The value of selected data is a error time'
  }
}
