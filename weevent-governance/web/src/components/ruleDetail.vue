<template>
  <div class='rule_detail rule'>
    <div class='step'>
      <p class='rule_title'>数据流转详情</p>
      <el-button type='primary' size='mini' @click="createRule = !createRule">编辑</el-button>
      <p class='rule_name'>{{ruleItem.ruleName}}</p>
      <p class='name'><span>数据格式:</span>{{ruleItem.payloadType === 1 ? 'JSON' : '' }}</p>
      <p class='name'><span>规则描述:</span>{{ruleItem.payloadMap}}</p>
    </div>
    <el-dialog title="编辑规则" :visible.sync="createRule">
      <div class='warning_part'>
        <img src="../assets/image/icon_tips.svg" alt="">
        <p>数据流转类型的规则可以对设备上报的数据进行简单处理,并将处理后的数据流转到其他Topic,支持JSON数据格式。</p>
      </div>
      <el-form :model="rule" :rules="rules" ref='rule'>
        <el-form-item label="规则名称" prop='ruleName'>
          <el-input v-model="rule.ruleName" size='small' autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="数据格式">
          <el-radio-group v-model="rule.payloadType">
            <el-radio label="1">JSON</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="规则描述" prop='payloadMap'>
          <el-input v-model="rule.payloadMap" size='small' type='textarea' :rows='5' placeholder="请输入规则描述" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" size="small" @click='update("rule")'>提交</el-button>
        <el-button  size="small" @click="createRule = !createRule">取 消</el-button>
      </div>
    </el-dialog>

    <div class='step'>
      <p class='rule_title'>数据流转详情</p>
      <el-button type='primary' size='mini' @click="createSQL = !createSQL">编写SQL</el-button>
      <el-button size='mini'>SQL语法说明</el-button>
      <div class='sql_content'>
        <div class='no_sql' v-show='!fullSQL'>
          <img src="../assets/image/icon_tips.svg" alt="">
          <span>您还没有编写SQL语句处理数据,</span>
          <span class='creat_sql' @click="createSQL = !createSQL">编写SQL</span>
        </div>
        <div class='sql_lession' v-show='fullSQL'>
          {{this.fullSQL}}
        </div>
        <div class='sql'></div>
      </div>
    </div>

    <el-dialog title="编写SQL" :visible.sync="createSQL">
      <div class='warning_part sql_part'>
        <p>
          <span>规则查询语言:</span>
          <!-- <span>复制语句</span> -->
        </p>
        <p>选择下方选项后,语句将自动生成</p>
      </div>
      <el-form :model="sqlOption" :rules="sqlCheck" ref='sql'>
        <el-form-item label="字段">
          <el-select v-model="sqlOption.selectField" size='small'>
            <el-option :label="key" :value="key" v-for='(item, key, index) in columnName' :key='index'></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="数据流转" prop='fromDestination'>
          <el-input v-model="sqlOption.fromDestination" size='small' autocomplete="off" placeholder="例如: TopicName"></el-input>
        </el-form-item>
        <el-form-item label="异常流转" prop='toDestination'>
          <el-input v-model="sqlOption.toDestination" size='small' autocomplete="off" placeholder="例如: TopicName"></el-input>
        </el-form-item>
        <el-form-item label="条件(选填)">
          <div style='text-align:right'>
            <span class='el-icon-plus' @click='addConditionItem'></span>
          </div>
          <!-- <el-input v-model="sqlOption.conditionField" size='small' autocomplete="off"></el-input> -->
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
                <p class='conditionaWarning'>请填写完整的语句</p>
            </div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" size="small" @click='update("sql")'>提交</el-button>
        <el-button  size="small" @click="createSQL = !createSQL">取 消</el-button>
      </div>
    </el-dialog>

    <div class='step'>
      <p class='rule_title'>转发数据</p>
      <el-button type='primary' size='mini' @click='option = !option'>转发操作</el-button>
      <ul class='foward_list'>
        <li class='foward_list_title'>数据目的地</li>
        <li class='no_content' v-show='!condition'>暂无数据</li>
        <li v-show='condition' :title='condition'>{{condition}}</li>
      </ul>
    </div>

    <el-dialog title="添加操作" :visible.sync="option">
      <el-form :model="options" :rules="optionsCheck" ref='options'>
        <el-form-item label="选择操作" prop='conditionType'>
          <el-select  v-model='options.conditionType' placeholder="请选择数据流转方式" size='mini' @change="selectType">
            <el-option label="发布到一个Topic" value="1"></el-option>
            <el-option label="发布到一个DB" value="2"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="Topic" v-show="options.conditionType === '1'" prop='errorDestination'>
          <el-input v-model="options.errorDestination" size='small' autocomplete="off" placeholder="流转错误时的topic"></el-input>
        </el-form-item>
        <el-form-item label="数据库" v-show="options.conditionType === '2'" prop='databaseUrl'>
          <el-select  placeholder="请选择数据库" size='mini' name='options_dialog' v-model="options.databaseUrl" v-show="dbList.length > 0">
            <el-option v-for="(item, index) in dbList" :key='index' :value="item.databaseUrl" :label="item.databaseUrl"></el-option>
          </el-select>
          <p class='no_dbList' v-show="dbList.length === 0">还未配置数据流转路径, <span @click="creatDB" >前往配置</span></p>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" size="small" @click='update("options")'>提交</el-button>
        <el-button  size="small" @click='option = !option'>取 消</el-button>
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
        callback(new Error('请输入规则名称'))
      } else {
        callback()
      }
    }
    var payloadMap = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入规则描述'))
      } else {
        if (typeof value === 'string') {
          try {
            var obj = JSON.parse(value)
            if (typeof obj === 'object' && obj) {
              callback()
            } else {
              callback(new Error('格式错误'))
            }
          } catch (e) {
            callback(new Error('格式错误'))
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
          { required: true, message: '数据流转地址必须填写', trigger: 'blur' }
        ],
        toDestination: [
          { required: true, message: '异常流转地址必须填写', trigger: 'blur' }
        ]
      },
      options: {
        'conditionType': '',
        'databaseUrl': '',
        'errorDestination': ''
      },
      optionsCheck: {
        conditionType: [
          { required: true, message: '请选择', trigger: 'blur' }
        ],
        databaseUrl: [
          { required: true, message: '请选择', trigger: 'blur' }
        ],
        errorDestination: [
          { required: true, message: '请填写', trigger: 'blur' }
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
      this.$store.commit('set_menu', ['规则引擎', '规则管理'])
      this.$store.commit('set_active', '4-1')
      this.$router.push('./rule')
    },
    groupId () {
      this.$store.commit('set_menu', ['规则引擎', '规则管理'])
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
              vm.options.conditionType = res.data.data.conditionType.toString()
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
      this.$store.commit('set_menu', ['规则引擎', '数据源设置'])
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
              message: '编辑成功'
            })
            vm.createRule = false
            vm.createSQL = false
            vm.option = false
            vm.getDetail()
          } else {
            vm.$message({
              type: 'warning',
              message: '操作失败'
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
