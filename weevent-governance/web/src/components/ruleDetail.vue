<template>
  <div class='rule_detail rule'>
    <div class='step'>
      <p class='rule_title'>{{$t('ruleDetail.guideDetail')}}</p>
      <el-button type='primary' size='mini' @click="createRule = !createRule">{{$t('common.edit')}}</el-button>
      <p class='rule_name'>{{ruleItem.ruleName}}</p>
      <p class='name'><span>{{$t('rule.dataType')}} :</span>{{ruleItem.payloadType === 1 ? 'JSON' : '' }}</p>
      <p class='name'><span>{{$t('rule.payloadMap')}} :</span>{{ruleItem.payloadMap}}</p>
    </div>
    <el-dialog :title="$t('ruleDetail.editRule')" :visible.sync="createRule">
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
      <el-button size='mini'>{{$t('ruleDetail.sqlDescription')}}</el-button>
      <div class='sql_content'>
        <div class='no_sql' v-show='!fullSQL'>
          <img src="../assets/image/icon_tips.svg" alt="">
          <span>{{$t('ruleDetail.noRule')}}</span>
          <span class='creat_sql' @click="createSQL = !createSQL">{{$t('rule.editRule')}}</span>
        </div>
        <div class='sql_lession' v-show='fullSQL'>
          {{this.fullSQL}}
        </div>
        <div class='sql'></div>
      </div>
    </div>

    <el-dialog :title="$t('rule.editRule')" :visible.sync="createSQL">
      <div class='warning_part sql_part'>
        <p>
          <span>{{$t('ruleDetail.ruleSearchLetter')}}</span>
          <!-- <span>复制语句</span> -->
        </p>
        <p>{{$t('ruleDetail.ruleSearchWarning')}}</p>
      </div>
      <el-form :model="sqlOption" :rules="sqlCheck" ref='sql'>
        <el-form-item :label="$t('ruleDetail.letter')  + ' :'">
          <el-select v-model="sqlOption.selectField" size='small'>
            <el-option :label="key" :value="key" v-for='(item, key, index) in columnName' :key='index'></el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('ruleDetail.dataCirculat')  + ' :'" prop='fromDestination'>
          <el-input v-model="sqlOption.fromDestination" size='small' autocomplete="off" :placeholder="$t('common.examples') + 'TopicName'"></el-input>
        </el-form-item>
        <el-form-item :label="$t('ruleDetail.abnormalData')  + ' :'" prop='toDestination'>
          <el-input v-model="sqlOption.toDestination" size='small' autocomplete="off" :placeholder="$t('common.examples') + 'TopicName'"></el-input>
        </el-form-item>
        <el-form-item :label="$t('ruleDetail.condition')">
          <div style='text-align:right'>
            <span class='el-icon-plus' @click='addConditionItem'></span>
          </div>
            <div class='conditionItem' v-for="(item, index) in sqlOption.ruleEngineConditionList" :key='index'>
              <el-select v-model="item.connectionOperator" size='small'>
                  <el-option label="and" value="and"></el-option>
                  <el-option label="or" value="or"></el-option>
                </el-select>
                <span class='line'>-</span>
                <el-select v-model="item.columnName" size='small'>
                  <el-option :label="key" :value="key" v-for='(item, key, index) in columnName' :key='index'></el-option>
                </el-select>
                <span class='line'>-</span>
                <el-select v-model="item.conditionalOperator" size='small'>
                  <el-option label=">" value=">"></el-option>
                  <el-option label=">=" value=">="></el-option>
                  <el-option label="<" value="<"></el-option>
                  <el-option label="<=" value="<="></el-option>
                  <el-option label="!=" value=">="></el-option>
                  <el-option label="=" value=">="></el-option>
                </el-select>
                <span class='line'>-</span>
                <el-input size='small' v-model.trim="item.sqlCondition" autocomplete="off"></el-input>
                <span class='el-icon-remove-outline' @click='remove(index)'></span>
                <p class='conditionaWarning'>{{$t('ruleDetail.completeLetter')}}</p>
            </div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" size="small" @click='update("sql")'>{{$t('rule.commit')}}</el-button>
        <el-button  size="small" @click="createSQL = !createSQL">{{$t('common.cancel')}}</el-button>
      </div>
    </el-dialog>

    <div class='step'>
      <p class='rule_title'>{{$t('ruleDetail.forwardData')}}</p>
      <el-button type='primary' size='mini' @click='option = !option'>{{$t('ruleDetail.forwardOption')}}</el-button>
      <ul class='foward_list'>
        <li class='foward_list_title'>{{$t('ruleDetail.dataDestination')}}</li>
        <li class='no_content' v-show='!condition'>{{$t('common.noData')}}</li>
        <li v-show='condition' :title='condition'>{{condition}}</li>
      </ul>
    </div>

    <el-dialog :title="$t('rule.addOperation')" :visible.sync="option">
      <el-form :model="options" :rules="optionsCheck" ref='options'>
        <el-form-item :label="$t('ruleDetail.selectOperation')  + ' :'" prop='conditionType'>
          <el-select  v-model='options.conditionType' :placeholder="$t('ruleDetail.selectGuide')" size='mini' @change="selectType">
            <el-option :label="$t('ruleDetail.toTopic')" value="1"></el-option>
            <el-option :label="$t('ruleDetail.toDB')" value="2"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="Topic :" v-show="options.conditionType === '1'" prop='errorDestination'>
          <el-input v-model="options.errorDestination" size='small' autocomplete="off" :placeholder="$t('ruleDetail.errorTopic')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('ruleDetail.db')  + ' :'" v-show="options.conditionType === '2'" prop='databaseUrl'>
          <el-select  :placeholder="$t('ruleDetail.selectDB')" size='mini' name='options_dialog' v-model="options.databaseUrl" v-show="dbList.length > 0">
            <el-option v-for="(item, index) in dbList" :key='index' :value="item.databaseUrl" :label="item.databaseUrl"></el-option>
          </el-select>
          <p class='no_dbList' v-show="dbList.length === 0">{{$t('ruleDetail.guideURL')}} <span @click="creatDB" >{{$t('ruleDetail.setGuide')}}</span></p>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" size="small" @click='update("options")'>{{$t('rule.commit')}}</el-button>
        <el-button  size="small" @click='option = !option'>{{$t('common.cancel')}}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import API from '../API/resource'
export default{
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
    return {
      createRule: false,
      createSQL: false,
      option: false,
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
        'databaseUrl': '',
        'errorDestination': '',
        'ruleEngineConditionList': []
      },
      rule: {
        'ruleName': '',
        'payloadType': '1',
        'payloadMap': ''
      },
      rules: {
        ruleName: [
          { validator: ruleName, trigger: 'blur' }
        ],
        payloadMap: [
          { validator: payloadMap, trigger: 'blur' }
        ]
      },
      sqlOption: {
        'selectField': '',
        'fromDestination': '',
        'toDestination': '',
        'ruleEngineConditionList': []
      },
      sqlCheck: {
        fromDestination: [
          { required: true, message: this.$t('ruleDetail.guideAddress'), trigger: 'blur' }
        ],
        toDestination: [
          { required: true, message: this.$t('ruleDetail.abnormalAddress'), trigger: 'blur' }
        ]
      },
      options: {
        'conditionType': '',
        'databaseUrl': '',
        'errorDestination': ''
      },
      optionsCheck: {
        conditionType: [
          { required: true, message: this.$t('common.choose'), trigger: 'blur' }
        ],
        databaseUrl: [
          { required: true, message: this.$t('common.choose'), trigger: 'blur' }
        ],
        errorDestination: [
          { required: true, message: this.$t('common.enter'), trigger: 'blur' }
        ]
      },
      dbList: [],
      condition: '',
      columnName: {},
      fullSQL: '',
      conditionList: [{
        'connectionOperator': '',
        'columnName': '',
        'conditionalOperator': '',
        'sqlCondition': ''
      }]
    }
  },
  computed: {
    brokerId () {
      return this.$store.state.brokerId
    },
    groupId () {
      return this.$store.state.groupId
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
        for (let key in vm.sqlOption) {
          vm.sqlOption[key] = vm.ruleItem[key]
        }
        let war = document.getElementsByClassName('conditionaWarning')
        for (let i = 0; i < war.length; i++) {
          war[i].style.display = 'none'
        }
        vm.$refs.sql.resetFields()
      }
    },
    option (nVal) {
      if (!nVal) {
        let vm = this
        for (let key in vm.options) {
          vm.options[key] = vm.ruleItem[key]
          if (key === 'conditionType') {
            vm.options[key] = vm.ruleItem[key].toString()
          }
        }
        vm.$refs.options.resetFields()
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
    }
  },
  methods: {
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
            vm.sqlOption[key] = res.data.data[key]
          }
          for (let key in vm.options) {
            vm.options[key] = res.data.data[key]
            if (key === 'conditionType') {
              if (res.data.data.conditionType || res.data.data.conditionType === 0) {
                vm.options.conditionType = res.data.data.conditionType.toString()
              }
            }
          }
          if (vm.options.conditionType === '1') {
            vm.condition = vm.options.errorDestination
          } else {
            vm.condition = vm.options.databaseUrl
          }
          this.fullSQL = res.data.data.fullSQL
          this.columnName = Object.assign({}, JSON.parse(res.data.data.payload))
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
      if (e === '1') {
        vm.options.databaseUrl = this.ruleItem.databaseUrl
      } else {
        vm.options.errorDestination = this.ruleItem.errorDestination
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
          if (e === 'options') {
            if (vm.options.conditionType === '1') {
              if (vm.options.errorDestination === '') {
                return
              }
            } else {
              if (vm.options.databaseUrl === '') {
                return
              }
            }
          } else {
            return
          }
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
          let hasEmpty = false
          let list = vm.sqlOption.ruleEngineConditionList
          if (list.length > 0) {
            for (let i = 0; i < list.length; i++) {
              for (var key in list[i]) {
                let item = list[i]
                if (item[key] === '') {
                  hasEmpty = true
                  let war = document.getElementsByClassName('conditionaWarning')
                  war[i].style.display = 'block'
                }
              }
            }
          }
          if (hasEmpty) {
            return
          } else {
            let war = document.getElementsByClassName('conditionaWarning')
            for (let i = 0; i < war.length; i++) {
              war[i].style.display = 'none'
            }
          }

          for (let key in vm.sqlOption) {
            data[key] = vm.sqlOption[key]
          }
        }
        if (e === 'options') {
          for (let key in vm.options) {
            data[key] = vm.options[key]
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
            vm.option = false
            vm.getDetail()
          } else {
            vm.$message({
              type: 'warning',
              message: vm.$t('common.operFail')
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
        'sqlCondition': ''
      }
      if (this.sqlOption.ruleEngineConditionList.length < 3) {
        this.sqlOption.ruleEngineConditionList.push(item)
      }
    },
    remove (e) {
      this.sqlOption.ruleEngineConditionList.splice(e, 1)
    }
  },
  mounted () {
    this.getDetail()
    this.getDBLsit()
  }
}
</script>
