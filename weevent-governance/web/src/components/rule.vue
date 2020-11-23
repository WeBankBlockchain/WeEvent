<template>
  <div class='rule'>
    <div class='step'>
        <p class='rule_title'>{{$t('rule.dataGuide')}}</p>
        <p class='rule_descript'>{{$t('rule.creatRuleRemark')}}</p>
        <div class='step_list'>
            <div class='step_list_item'>
                <span class='num'>1</span>
                <span class='item_name'>{{$t('rule.creatNewRule')}}</span>
            </div>
            <span class="el-icon-right"></span>
            <div class='step_list_item'>
                <span class='num'>2</span>
                <span class="item_name">{{$t('rule.editRule')}}</span>
            </div>
            <span class="el-icon-right"></span>
            <div class='step_list_item'>
                <span class='num'>3</span>
                <span class='item_name'>{{$t('rule.addOperation')}}</span>
            </div>
            <span class="el-icon-right"></span>
            <div class='step_list_item'>
                <span class='num'>4</span>
                <span class='item_name'>{{$t('rule.startRule')}}</span>
            </div>
        </div>
    </div>
    <div class='rule_list'>
        <p class='rule_title'>
          {{$t('rule.ruleList')}}
        </p>
        <div class='control_part'>
            <el-button type='primary' size='small'  @click='createRule = !createRule'>{{$t('rule.creatRule')}}</el-button>
            <div class='search_part'>
                <el-input v-model.trim='ruleName'
                    :placeholder="$t('rule.enterRuleName')"
                    size='small'
                    clearable
                ></el-input>
                <el-button type='primary' size='small' @click='searchRule'>{{$t('common.search')}}</el-button>
            </div>
        </div>
        <el-table
            :data="ruleList"
            style="width: 100%"
            >
            <el-table-column
            prop="ruleName"
            :label="$t('rule.ruleName')"
            >
            </el-table-column>
            <el-table-column
            prop="payloadType"
            :label="$t('rule.payloadType')"
            width='100'
            :formatter="payloadType"
            >
            </el-table-column>
            <el-table-column
            prop="payloadMap"
            :label="$t('rule.payloadMap')"
            :formatter="payloadMap">
            </el-table-column>
            <el-table-column
            prop="createDateStr"
            :label="$t('tableCont.timestamp')">
            </el-table-column>
            <el-table-column
            :label="$t('tableCont.state')"
            width='110'
            >
              <template  slot-scope="scope">
                <el-popover
                  placement="top"
                  trigger="hover"
                  @show='readRuleDetial(scope.row)'
                  >
                  <div class='rule_detial_infor'>
                    <p><span>{{$t('ruleStatic.hitTimes')}} :</span>{{ruleStatic.hitTimes}}</p>
                    <p><span>{{$t('ruleStatic.notHitTimes')}} :</span>{{ruleStatic.notHitTimes}}</p>
                    <p><span>{{$t('ruleStatic.successTimes')}} :</span>{{ruleStatic.dataFlowSuccess}}</p>
                    <p><span>{{$t('ruleStatic.failTimes')}} :</span>{{ruleStatic.dataFlowFail}}</p>
                    <p><span>{{$t('ruleStatic.runningStatus')}} :</span>{{ruleStatic.status === 0 ? $t('rule.notRun') : $t('rule.run')}}</p>
                  </div>
                  <div slot="reference">
                    <span v-show="scope.row.status === 1" style='font-size:14px'><i class='isActive'></i>{{$t('rule.start')}}</span>
                    <span v-show="scope.row.status === 0" style='font-size:14px'><i class='notActive'></i>{{$t('rule.stop')}}</span>
                  </div>
                </el-popover>
              </template>
            </el-table-column>
            <el-table-column
            :label="$t('common.action')"
            width='170'>
                <template  slot-scope="scope">
                    <a v-show='scope.row.status === 0' @click='ruleStart(scope.row)'>{{$t('rule.start')}}</a>
                    <a v-show='scope.row.status === 1' @click='ruleStop(scope.row)'>{{$t('rule.stop')}}</a>
                    <a @click='readDetail(scope.row)'>{{$t('rule.read')}}</a>
                    <a v-show='scope.row.status === 0' @click='ruleDelete(scope.row)'>{{$t('rule.delete')}}</a>
                </template>
            </el-table-column>
        </el-table>
        <el-pagination
          @current-change="indexChange"
          :current-page="pageNum"
          layout="total, prev, pager, next"
          :total="total"
          >
        </el-pagination>
    </div>
    <el-dialog :title="$t('rule.creatRule')" :visible.sync="createRule" :close-on-click-modal='false'>
      <el-tooltip popper-class='rule_des' effect="light" :content="$t('rule.creatRuleRemark')" placement="bottom-start">
        <i :class="{'el-icon-warning': true, 'en_warning': lang === 'en'}"></i>
      </el-tooltip>
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
          <el-input v-model="rule.payloadMap" size='small' type='textarea' :rows='4' :placeholder="$t('rule.enterPayload')" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item :label="$t('rule.ruleDescription')  + ' :'" >
          <el-input v-model="rule.ruleDescription" size='small' type='textarea' :rows='3' autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" size="small" @click='addRule'>{{$t('rule.commit')}}</el-button>
        <el-button  size="small" @click='createRule = !createRule'>{{$t('common.cancel')}}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import API from '../API/resource'
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
    return {
      tabHeight: 0,
      createRule: false,
      ruleName: '',
      pageNum: 1,
      total: 0,
      ruleList: [],
      rule: {
        ruleName: '',
        payloadType: '1',
        payloadMap: '',
        conditionType: '1',
        ruleDescription: ''
      },
      ruleStatic: {},
      rules: {
        ruleName: [
          { required: true, validator: ruleName, trigger: 'blur' }
        ],
        payloadMap: [
          { required: true, validator: payloadMap, trigger: 'blur' }
        ]
      }
    }
  },
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
    groupId (nVal) {
      if (nVal !== '-1') {
        this.pageNum = 1
        this.ruleName = ''
        this.getRuleList()
      }
    },
    createRule (nVal) {
      if (!nVal) {
        this.rule.ruleName = ''
        this.rule.payloadMap = ''
        this.$refs.rule.resetFields()
      }
    }
  },
  mounted () {
    if (localStorage.getItem('groupId') && localStorage.getItem('brokerId')) {
      this.getRuleList()
    }
  },
  methods: {
    getRuleList () {
      const data = {
        ruleName: this.ruleName,
        brokerId: localStorage.getItem('brokerId'),
        groupId: localStorage.getItem('groupId'),
        pageNumber: this.pageNum,
        pageSize: 10
      }
      API.ruleList(data).then(res => {
        if (res.data.code === 0) {
          if (res.data.data && res.data.data.totalCount > 0) {
            this.ruleList = [].concat(res.data.data.ruleEngines)
            this.total = res.data.data.totalCount
          } else {
            this.ruleList = []
            this.total = 0
          }
        }
      })
    },
    searchRule () {
      this.pageNum = 1
      this.getRuleList()
    },
    indexChange (e) {
      this.pageNum = e
      this.getRuleList()
    },
    payloadType (e) {
      if (e.payloadType === 1) {
        return 'JSON'
      }
    },
    payloadMap (e) {
      if (JSON.stringify(e.payloadMap) === '{}') {
        return '—'
      } else {
        return JSON.stringify(e.payloadMap)
      }
    },
    ruleStart (e) {
      const data = {
        id: e.id,
        brokerId: e.brokerId
      }
      API.ruleStart(data).then(res => {
        if (res.data.code === 0) {
          this.getRuleList()
          this.$message({
            type: 'success',
            message: this.$t('rule.isStart')
          })
        } else {
          this.$store.commit('set_Msg', this.$message({
            type: 'warning',
            message: res.data.message,
            duration: 0,
            showClose: true
          }))
        }
      })
    },
    ruleStop (e) {
      const data = {
        id: e.id,
        brokerId: e.brokerId,
        status: 0
      }
      API.ruleStop(data).then(res => {
        if (res.data.code === 0) {
          this.getRuleList()
          this.$message({
            type: 'success',
            message: this.$t('rule.isStop')
          })
        } else {
          this.$store.commit('set_Msg', this.$message({
            type: 'warning',
            message: res.data.message,
            duration: 0,
            showClose: true
          }))
        }
      })
    },
    ruleDelete (e) {
      const vm = this
      vm.$confirm(vm.$t('rule.isDelete'), vm.$t('rule.deleteRule'), {
        confirmButtonText: vm.$t('common.ok'),
        cancelButtonText: vm.$t('common.cancel'),
        type: 'warning'
      }).then(() => {
        const data = {
          id: e.id,
          brokerId: e.brokerId
        }
        API.ruleDelete(data).then(res => {
          if (res.data.code === 0) {
            vm.getRuleList()
            vm.$message({
              type: 'success',
              message: vm.$t('rule.hasDelete')
            })
          } else {
            this.$store.commit('set_Msg', this.$message({
              type: 'warning',
              message: res.data.message,
              duration: 0,
              showClose: true
            }))
          }
        })
      }).catch(() => {})
    },
    readDetail (e) {
      sessionStorage.setItem('ruleId', e.id)
      this.$store.commit('set_menu', [this.$t('sideBar.engine'), this.$t('sideBar.ruleMana'), this.$t('sideBar.ruleDetail')])
      this.$router.push('./ruleDetail')
    },
    addRule () {
      const vm = this
      vm.$refs.rule.validate((valid) => {
        if (valid) {
          const data = {
            ruleName: vm.rule.ruleName,
            payloadType: vm.rule.payloadType,
            payloadMap: JSON.parse(this.rule.payloadMap),
            brokerId: localStorage.getItem('brokerId'),
            groupId: localStorage.getItem('groupId'),
            ruleDescription: vm.rule.ruleDescription
          }
          API.ruleAdd(data).then(res => {
            if (res.data.code === 0) {
              // this.getRuleList()
              this.$message({
                type: 'success',
                message: this.$t('rule.creatSuccess')
              })
              sessionStorage.setItem('ruleId', res.data.data.id)
              this.$store.commit('set_menu', [this.$t('sideBar.engine'), this.$t('sideBar.ruleMana'), this.$t('sideBar.ruleDetail')])
              this.$router.push('./ruleDetail')
            } else {
              this.$store.commit('set_Msg', this.$message({
                type: 'warning',
                message: res.data.message,
                duration: 0,
                showClose: true
              }))
            }
            vm.createRule = false
          })
        } else {
          return false
        }
      })
    },
    readRuleDetial (e) {
      const vm = this
      const id = e.id
      const str = '?idList=' + id
      API.ruleStatic(str).then(res => {
        if (res.data.errorCode === 0) {
          const list = res.data.data.statisticRuleMap
          for (const key in list) {
            if (key === String(id)) {
              vm.ruleStatic = list[key]
            }
          }
        }
      })
    }
  }
}
</script>
