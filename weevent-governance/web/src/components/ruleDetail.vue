<template>
  <div class='rule_detail rule'>
    <div class='step'>
      <!-- <p class='rule_title'>{{$t('ruleDetail.guideDetail')}}</p> -->
      <p class='rule_name' style='font-size:18px'>
        {{ruleItem.ruleName}}
        <el-button type='primary' size='mini' @click="createRule = !createRule">{{$t('common.edit')}}</el-button>
      </p>
      <p class='name'><span>{{$t('rule.dataType')}} :</span>{{ruleItem.payloadType === 1 ? 'JSON' : '' }}</p>
      <p class='name'><span>{{$t('rule.payloadMap')}} :</span>{{ruleItem.payloadMap}}</p>
    </div>
    <el-dialog :title="$t('ruleDetail.editRule')" :visible.sync="createRule" :close-on-click-modal='false'>
      <div class='warning_part'>
        <img src="../assets/image/icon_tips.svg" alt="">
        <p>{{$t('rule.creatRuleRemark')}}</p>
      </div>
      <el-form :model="rule" :rules="rules" ref='rule'>
        <el-form-item :label="$t('rule.ruleName')  + ' :'" prop='ruleName'>
          <el-input v-model="rule.ruleName" size='small' autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item :label="$t('rule.dataType')  + ' :'">
          <el-radio-group v-model="rule.payloadType">
            <el-radio label="1">JSON</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('rule.payloadMap')  + ' :'" prop='payloadMap'>
          <el-input v-model="rule.payloadMap" size='small' type='textarea' :rows='5' :placeholder="$t('rule.enterPayload')" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" size="small" @click='update("rule")'>{{$t('rule.commit')}}</el-button>
        <el-button  size="small" @click="createRule = !createRule">{{$t('common.cancel')}}</el-button>
      </div>
    </el-dialog>

    <div class='step'>
      <p class='rule_title'>{{$t('ruleDetail.processData')}}</p>
      <el-button type='primary' size='mini' @click="createSQL = !createSQL">{{$t('rule.editRule')}}</el-button>
      <div class='sql_content'>
        <div class='no_sql' v-show='!fullSQL'>
          <img src="../assets/image/icon_tips.svg" alt="">
          <span>{{$t('ruleDetail.noRule')}}</span>
          <span class='creat_sql' @click="createSQL = !createSQL">{{$t('rule.editRule')}}</span>
        </div>
        <div class='sql_lession' v-show='fullSQL'>
          {{this.fullSQL}}
        </div>
      </div>
    </div>
    <el-dialog :title="$t('rule.editRule')" :visible.sync="createSQL" :close-on-click-modal='false'>
      <el-form :model="sqlOption" :rules="sqlCheck" ref='sql'>
        <el-form-item :label="$t('ruleDetail.dataCirculat')  + ' :'" prop='fromDestination'>
          <el-select  v-model='sqlOption.fromDestination'  size='mini' @visible-change='selectShow' :placeholder="$t('common.choose')">
            <el-option v-for='(item, index) in listTopic' :key='index' :label="item.topicName === '#' ? 'all': item.topicName" :value="item.topicName"></el-option>
            <el-pagination
                layout="prev, pager, next"
                small
                :current-page.sync="pageIndex"
                :total="total">
              </el-pagination>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('ruleDetail.condition') + ' :'">
          <div style='text-align:right'>
            <span class='el-icon-plus' @click='addConditionItem'></span>
          </div>
          <tree :treeData='sqlOption.conditionFieldJson' :nodeIndex='"0"' :columnList='columnName'></tree>
        </el-form-item>
        <el-form-item :label="$t('ruleDetail.letter')  + ' :'"  :placeholder="$t('common.choose')" prop="selectField">
          <el-select v-model="sqlOption.selectField" size='small' :placeholder="$t('common.choose')" multiple @change="selField">
            <div v-show='JSON.stringify(columnName) !== "{}"' class='selAll'>
              <el-checkbox v-model="selAll" @change='selChange'>{{$t('common.all')}}</el-checkbox>
            </div>
            <el-option :label="key" :value="key" v-for='(item, key, index) in columnName' :key='index'></el-option>
            <el-option label="eventId" value="eventId" v-show='JSON.stringify(columnName) !== "{}"'></el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('ruleDetail.selectOperation')  + ' :'" prop='conditionType'>
          <el-select  v-model='sqlOption.conditionType' :placeholder="$t('ruleDetail.selectGuide')" size='mini' @change="selectType">
            <el-option :label="$t('ruleDetail.toTopic')" value="1"></el-option>
            <el-option :label="$t('ruleDetail.toDB')" value="2"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="Topic :" v-show="sqlOption.conditionType === '1'" prop='toDestination'>
          <el-select  v-model="sqlOption.toDestination" :placeholder="$t('ruleDetail.errorTopic')" size='mini' @visible-change='selectShow'>
            <el-option v-for='(item, index) in listData' :key='index' :label="item.topicName" :value="item.topicName"></el-option>
            <el-pagination
                layout="prev, pager, next"
                small
                :current-page.sync="pageIndex"
                :total="total">
            </el-pagination>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('ruleDetail.db')  + ' :'" v-show="sqlOption.conditionType === '2'" prop='ruleDataBaseId' >
          <el-select  :placeholder="$t('ruleDetail.selectDB')" size='mini' name='options_dialog' v-model="sqlOption.ruleDataBaseId" v-show="dbList.length > 0">
            <el-option v-for="(item, index) in dbList" :key='index' :value="item.id" :label="item.datasourceName"></el-option>
          </el-select>
          <p class='no_dbList' v-show="dbList.length === 0">{{$t('ruleDetail.guideURL')}} <span @click="creatDB" >{{$t('ruleDetail.setGuide')}}</span></p>
        </el-form-item>
        <el-form-item :label="$t('ruleDetail.abnormalData')  + ' :'" prop='errorDestination'>
          <el-select  v-model="sqlOption.errorDestination" size='mini' :clearable="true" @visible-change='selectShow' :placeholder="$t('common.choose')">
            <el-option v-for='(item, index) in listData' :key='index' :label="item.topicName" :value="item.topicName"></el-option>
            <el-pagination
                layout="prev, pager, next"
                small
                :current-page.sync="pageIndex"
                :total="total">
            </el-pagination>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" size="small" @click='update("sql")'>{{$t('rule.commit')}}</el-button>
        <el-button  size="small" @click="createSQL = !createSQL">{{$t('common.cancel')}}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import API from '../API/resource'
import tree from './tree.vue'
import {checkRule} from '../utils/checkRule'
export default {
  data () {
    var ruleName = (rule, value, callback) => {
      if (value === '') {
        callback(new Error(this.$t('rule.enterRuleName')))
      } else {
        callback()
      }
    }
    var payloadMap = (rule, value, callback) => {
      if (value === '') {
        callback(new Error(this.$t('rule.enterPayload')))
      } else {
        if (typeof value === 'string') {
          try {
            var obj = JSON.parse(value)
            if (typeof obj === 'object' && obj) {
              callback()
            } else {
              callback(new Error(this.$t('rule.errorType')))
            }
          } catch (e) {
            callback(new Error(this.$t('rule.errorType')))
          }
        }
      }
    }
    var errorDestination = (rule, value, callback) => {
      if (!value) {
        callback()
      } else {
        if (value === this.sqlOption.fromDestination) {
          callback(new Error(this.$t('ruleDetail.cannotSame')))
        } else {
          callback()
        }
      }
    }
    var toDestination = (rule, value, callback) => {
      if (!value) {
        callback(new Error(this.$t('common.choose')))
      } else {
        if (value === this.sqlOption.fromDestination) {
          callback(new Error(this.$t('ruleDetail.cannotSame')))
        } else {
          callback()
        }
      }
    }
    return {
      selAll: false,
      createRule: false,
      createSQL: false,
      ruleItem: {
        'id': '',
        'brokerId': '',
        'ruleName': '',
        'payloadType': '1',
        'payloadMap': '',
        'selectField': '',
        'fromDestination': '',
        'toDestination': '',
        'conditionField': '',
        'conditionType': '',
        'ruleDataBaseId': '',
        'errorDestination': '',
        'conditionFieldJson': [],
        'functionArray': ''
      },
      rule: {
        'ruleName': '',
        'payloadType': '1',
        'payloadMap': ''
      },
      rules: {
        ruleName: [
          { required: true, validator: ruleName, trigger: 'blur' }
        ],
        payloadMap: [
          { required: true, validator: payloadMap, trigger: 'blur' }
        ]
      },
      sqlOption: {
        'selectField': [],
        'fromDestination': '',
        'toDestination': '',
        'conditionFieldJson': [],
        'conditionType': '',
        'ruleDataBaseId': '',
        'errorDestination': '',
        'functionArray': '',
        'conditionField': ''
      },
      sqlCheck: {
        fromDestination: [
          { required: true, message: this.$t('ruleDetail.guideAddress'), trigger: 'change' }
        ],
        conditionType: [
          { required: true, message: this.$t('common.choose'), trigger: 'blur' }
        ],
        selectField: [
          { required: true, message: this.$t('common.choose'), trigger: 'change' }
        ],
        ruleDataBaseId: [
          { required: true, message: this.$t('ruleDetail.guideURL'), trigger: 'change' }
        ],
        toDestination: [
          { required: true, validator: toDestination, trigger: 'change' }
        ],
        errorDestination: [
          { validator: errorDestination, trigger: 'change' }
        ]
      },
      dbList: [],
      condition: '',
      columnName: {},
      fullSQL: '',
      warning: '',
      pageIndex: 1,
      total: 0,
      listData: [],
      listTopic: [],
      ruleLetter: [],
      remarkLettrt: '',
      functionList: [],
      functionArray: []
    }
  },
  components: {tree},
  computed: {
    brokerId () {
      return this.$store.state.brokerId
    },
    groupId () {
      return this.$store.state.groupId
    },
    lang () {
      return this.$store.state.lang
    }
  },
  watch: {
    createRule (nVal) {
      if (!nVal) {
        let vm = this
        for (let key in vm.rule) {
          vm.rule[key] = vm.ruleItem[key]
          if (key === 'payloadMap') {
            vm.rule[key] = JSON.stringify(vm.ruleItem.payloadMap)
          }
          if (key === 'payloadType') {
            vm.rule[key] = vm.ruleItem.payloadType.toString()
          }
        }
        vm.$refs.rule.resetFields()
      }
    },
    createSQL (nVal) {
      if (!nVal) {
        let vm = this
        let nodes = document.getElementsByClassName('tree_content')
        if (nodes) {
          for (let i = 0; i < nodes.length; i++) {
            let war = nodes[i].childNodes[nodes[i].childNodes.length - 1]
            war.style.display = 'none'
          }
        }
        for (let key in vm.sqlOption) {
          if (key === 'selectField') {
            if (vm.ruleItem.selectField) {
              vm.sqlOption.selectField = [].concat(vm.ruleItem.selectField.split(','))
              let listColumnName = []
              for (let key in vm.columnName) {
                listColumnName.push(key)
              }
              if (vm.sqlOption.selectField.length === listColumnName.length + 1) {
                vm.selAll = true
              } else {
                vm.selAll = false
              }
            }
          } else {
            if (key === 'conditionFieldJson') {
              vm.sqlOption.conditionFieldJson = [].concat(vm.ruleItem.conditionFieldJson)
            } else {
              vm.sqlOption[key] = vm.ruleItem[key]
            }
          }
        }
        vm.$refs.sql.resetFields()
      }
    },
    brokerId () {
      this.$store.commit('set_menu', [this.$t('sideBar.engine'), this.$t('sideBar.ruleMana')])
      this.$store.commit('set_active', '4-1')
      this.$router.push('./rule')
    },
    groupId () {
      this.$store.commit('set_menu', [this.$t('sideBar.engine'), this.$t('sideBar.ruleMana')])
      this.$store.commit('set_active', '4-1')
      this.$router.push('./rule')
    },
    pageIndex (nVal) {
      this.getLsitData()
    },
    lang () {
      this.sqlCheck.fromDestination[0].message = this.$t('ruleDetail.guideAddress')
      this.sqlCheck.conditionType[0].message = this.$t('common.choose')
      this.sqlCheck.selectField[0].message = this.$t('common.choose')
      this.sqlCheck.ruleDataBaseId[0].message = this.$t('ruleDetail.guideURL')
      this.sqlCheck.toDestination[0].message = this.$t('common.choose')
    }
  },
  methods: {
    checkSQLData (x, y, z) {
      let vm = this
      let index = z + y
      if (!x[y].children) {
        x[y].children = []
      }
      let item = x[y].children
      if (item && item.length > 0) {
        vm.checkSQLData(item, 0, index)
      } else {
        vm.getNode(index)
      }
    },
    getNode (e) {
      let vm = this
      let list = e.split('')
      if (list.length === 1) {
        let index = Number(list[0])
        vm.sqlLetter(vm.sqlOption.conditionFieldJson[index], true)
        if (vm.sqlOption.conditionFieldJson[index + 1]) {
          vm.checkSQLData(vm.sqlOption.conditionFieldJson, index + 1, '')
        }
      } else {
        let nodeItem = vm.sqlOption.conditionFieldJson
        for (let i = 0; i < list.length - 1; i++) {
          let index = Number(list[i])
          let mid = nodeItem[index].children
          nodeItem = mid
        }
        let last = Number(list.pop())
        let newIndex = list.join('')
        vm.sqlLetter(nodeItem[last], false)
        if (nodeItem[last + 1]) {
          vm.checkSQLData(nodeItem, last + 1, newIndex)
        } else {
          vm.getNode(newIndex)
        }
      }
    },
    sqlLetter (item, e) {
      let vm = this
      let str
      if (item.functionType) {
        if (['abs', 'ceil', 'floor', 'round', 'lcase'].indexOf(item.functionType) !== -1) {
          str = item.functionType + '(' + item.columnName + ')' + item.conditionalOperator + item.sqlCondition
        }
        if (item.functionType === 'substring' || item.functionType === 'concat') {
          str = item.columnName + '.' + item.functionType + '(' + item.columnMark + ')' + item.conditionalOperator + item.sqlCondition
        }
        if (item.functionType === 'trim') {
          str = item.columnName + '.trim()' + item.conditionalOperator + item.sqlCondition
        }
        if (item.functionType === 'now' || item.functionType === 'currentDate' || item.functionType === 'currentTime') {
          str = item.columnName + item.conditionalOperator + item.functionType
        }
      } else {
        str = item.columnName + item.conditionalOperator + item.sqlCondition
      }
      if (item.children.length > 0) {
        if (item.connectionOperator) {
          str = ' ' + item.connectionOperator + ' ' + '(' + str + vm.remarkLettrt + ')'
        } else {
          str = '(' + str + vm.remarkLettrt + ')'
        }
        vm.remarkLettrt = str
      } else {
        if (item.connectionOperator) {
          str = ' ' + item.connectionOperator + ' ' + str
        }
        vm.remarkLettrt += str
      }
      if (e) {
        vm.ruleLetter.push(vm.remarkLettrt)
        vm.remarkLettrt = ''
      }
      if (item.functionType && vm.functionList.indexOf(item.functionType) === -1) {
        vm.functionList.push(item.functionType)
      }
    },
    getRuleData () {
      let vm = this
      let str = vm.ruleLetter.join('')
      vm.sqlOption.conditionField = str
      vm.functionList.forEach(e => {
        let index = 0
        index = str.indexOf(e, 0)
        while (index !== -1) {
          let i = index + 1
          let startIndex = index + e.length
          let item = []
          if (['abs', 'ceil', 'floor', 'round', 'lcase'].indexOf(e) !== -1) {
            let endIndex = index + e.length
            while (str[endIndex] !== ')') {
              endIndex++
            }
            item[0] = String(index)
            item[1] = String(endIndex + 1)
            item[2] = e
            item[3] = str.substring(startIndex + 1, endIndex)
          }
          if (e === 'substring' || e === 'concat') {
            let endIndex = index
            while (str[endIndex] !== ' ' && str[endIndex]) {
              endIndex--
            }
            let lastIndex = index + e.length
            while (str[lastIndex] !== ')') {
              lastIndex++
            }
            item[0] = String(endIndex + 1)
            item[1] = String(lastIndex + 1)
            item[2] = e
            let str1 = str
            let str2 = str
            let key = str1.substring(endIndex + 1, index - 1)
            let string = str2.substring(startIndex + 1, lastIndex)
            item[3] = key + ',' + string
          }
          if (e === 'trim') {
            let endIndex = index
            while (str[endIndex] !== ' ' && str[endIndex]) {
              endIndex--
            }
            item[0] = String(endIndex + 1)
            item[1] = String(startIndex + 2)
            item[2] = e
            item[3] = str.substring(endIndex + 1, index - 1)
          }
          if (e === 'now' || e === 'currentDate' || e === 'currentTime') {
            item[0] = String(index)
            item[1] = String(index + e.length)
            item[2] = e
            let endIndex = index - 1
            while (['>', '<', '=', '!'].indexOf(str[endIndex]) !== -1) {
              endIndex--
            }
            let start = index - 1
            while (str[start] !== ' ' && str[start]) {
              start--
            }
            item[3] = str.substring(start + 1, endIndex + 1)
          }
          vm.functionArray.push(item)
          index = str.indexOf(e, i)
        }
      })
    },
    getDetail () {
      let vm = this
      let data = {
        'userId': localStorage.getItem('userId'),
        'brokerId': localStorage.getItem('brokerId'),
        'id': sessionStorage.getItem('ruleId')
      }
      API.ruleDetail(data).then(res => {
        if (res.data.status === 200) {
          for (let key in vm.ruleItem) {
            if (res.data.data[key]) {
              vm.ruleItem[key] = res.data.data[key]
              if (key === 'conditionFieldJson') {
                if (res.data.data.conditionFieldJson) {
                  vm.ruleItem.conditionFieldJson = JSON.parse(res.data.data.conditionFieldJson)
                } else {
                  vm.ruleItem.conditionFieldJson = []
                }
              }
            }
          }
          for (let key in vm.rule) {
            vm.rule[key] = res.data.data[key]
            if (key === 'payloadMap') {
              vm.rule[key] = JSON.stringify(res.data.data.payloadMap)
            }
            if (key === 'payloadType') {
              vm.rule[key] = res.data.data.payloadType.toString()
            }
          }
          for (let key in vm.sqlOption) {
            if (key === 'selectField' || key === 'conditionType' || key === 'conditionFieldJson') {
              if (res.data.data.selectField) {
                vm.sqlOption.selectField = [].concat(res.data.data.selectField.split(','))
              }
              if (key === 'conditionType') {
                if (res.data.data.conditionType || res.data.data.conditionType === 0) {
                  vm.sqlOption.conditionType = res.data.data.conditionType.toString()
                }
              }
              if (key === 'conditionFieldJson') {
                if (res.data.data.conditionFieldJson) {
                  vm.sqlOption.conditionFieldJson = JSON.parse(res.data.data.conditionFieldJson)
                } else {
                  vm.sqlOption.conditionFieldJson = []
                }
              }
            } else {
              vm.sqlOption[key] = res.data.data[key]
            }
          }
          if (vm.sqlOption.conditionType === '1') {
            vm.condition = vm.sqlOption.toDestination
          } else {
            vm.condition = vm.sqlOption.ruleDataBaseId
          }
          vm.fullSQL = res.data.data.fullSQL
          vm.columnName = Object.assign({}, JSON.parse(res.data.data.payload))
          let listColumnName = []
          for (let key in vm.columnName) {
            listColumnName.push(key)
          }
          if (vm.sqlOption.selectField.length === listColumnName.length + 1) {
            vm.selAll = true
          } else {
            vm.selAll = false
          }
        }
      })
    },
    getDBLsit () {
      API.dbList({'userId': localStorage.getItem('userId')}).then(res => {
        if (res.data.status === 200) {
          this.dbList = [].concat(res.data.data)
        }
      })
    },
    selectType (e) {
      let vm = this
      vm.$refs.sql.clearValidate('conditionType')
      if (e === '1') {
        vm.$refs.sql.clearValidate('toDestination')
        vm.sqlOption.ruleDataBaseId = this.ruleItem.ruleDataBaseId
      } else {
        vm.$refs.sql.clearValidate('ruleDataBaseId')
        vm.sqlOption.toDestination = this.ruleItem.toDestination
      }
    },
    creatDB () {
      this.$store.commit('set_menu', [this.$t('sideBar.engine'), this.$t('sideBar.sources')])
      this.$store.commit('set_active', '4-2')
      this.$router.push('./dataBase')
    },
    update (e) {
      let vm = this
      let data = Object.assign({}, vm.ruleItem)
      vm.$refs[e].validate((valid) => {
        if (!valid) {
          if (e === 'sql') {
            if (!vm.sqlOption.fromDestination) {
              return
            }
            if (vm.sqlOption.selectField.length === 0) {
              return
            }
            if (vm.sqlOption.conditionType === '1') {
              if (!vm.sqlOption.toDestination) {
                return
              } else {
                vm.$refs.sql.clearValidate('ruleDataBaseId')
                vm.sqlOption.ruleDataBaseId = ''
              }
            } else {
              if (!vm.sqlOption.ruleDataBaseId) {
                return
              } else {
                vm.$refs.sql.clearValidate('toDestination')
                vm.sqlOption.toDestination = ''
              }
            }
          } else {
            return
          }
        }
        if (!checkRule(this.columnName)) {
          return
        }
        if (e === 'rule') {
          for (let key in vm.rule) {
            data[key] = vm.rule[key]
            if (key === 'payloadMap') {
              data.payloadMap = JSON.parse(vm.rule.payloadMap)
            }
          }
        }
        if (e === 'sql') {
          vm.ruleLetter = []
          vm.functionArray = []
          vm.sqlOption.conditionField = ''
          if (vm.sqlOption.conditionFieldJson.length > 0) {
            vm.checkSQLData(vm.sqlOption.conditionFieldJson, 0, '')
            vm.getRuleData()
            vm.sqlOption.functionArray = JSON.stringify(vm.functionArray)
          }
          if (vm.sqlOption.conditionType === '1') {
            vm.sqlOption.ruleDataBaseId = ''
          } else {
            vm.sqlOption.toDestination = ''
          }
          for (let key in vm.sqlOption) {
            if (key === 'selectField') {
              data.selectField = vm.sqlOption.selectField.join(',')
            } else if (key === 'conditionFieldJson') {
              data.conditionFieldJson = JSON.stringify(vm.sqlOption.conditionFieldJson)
            } else {
              data[key] = vm.sqlOption[key]
            }
          }
        }
        API.ruleUpdate(data).then(res => {
          if (res.data.status === 200) {
            vm.$message({
              type: 'success',
              message: vm.$t('common.editSuccess')
            })
            vm.createRule = false
            vm.createSQL = false
            vm.getDetail()
          } else {
            vm.$message({
              type: 'warning',
              message: res.data.message
            })
          }
        })
      })
    },
    addConditionItem () {
      let item = {
        'connectionOperator': '',
        'columnName': '',
        'conditionalOperator': '',
        'sqlCondition': '',
        'functionType': '',
        'columnMark': '',
        'children': []
      }
      this.sqlOption.conditionFieldJson.push(item)
    },
    getLsitData () {
      let vm = this
      let data = {
        pageIndex: vm.pageIndex - 1,
        pageSize: 10,
        brokerId: Number(localStorage.getItem('brokerId')),
        groupId: Number(localStorage.getItem('groupId'))
      }
      API.topicList(data).then(res => {
        if (res.status === 200) {
          vm.total = res.data.total
          vm.listData = [].concat(res.data.topicInfoList)
          vm.listTopic = [].concat(res.data.topicInfoList)
        }
      })
    },
    selectShow (e) {
      if (e && this.pageIndex !== 1) {
        this.pageIndex = 1
        this.getLsitData()
      }
    },
    selField (e) {
      let list = []
      for (let key in this.columnName) {
        list.push(key)
      }
      if (e.length === list.length + 1) {
        this.selAll = true
      } else {
        this.selAll = false
      }
    },
    selChange (e) {
      this.sqlOption.selectField = []
      if (e) {
        for (let key in this.columnName) {
          this.sqlOption.selectField.push(key)
        }
        this.sqlOption.selectField.push('eventId')
      }
    }
  },
  mounted () {
    this.getDetail()
    this.getDBLsit()
    this.getLsitData()
  }
}
</script>
