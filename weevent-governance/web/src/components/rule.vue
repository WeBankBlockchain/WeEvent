<template>
  <div class='rule'>
    <div class='step'>
        <p class='rule_title'>数据流新手引导</p>
        <p class='rule_descript'>数据流转类型的规则可以对设备上报的数据进行简单处理,并将处理后的数据流转到其他Topic,支持JSON数据格式。<span>使用文档</span></p>
        <div class='step_list'>
            <div class='step_list_item'>
                <span class='num'>1</span>
                <span class='item_name'>新建规则</span>
                <span class='creat_rule' @click='createRule = !createRule'>立即创建</span>
            </div>
            <span class="el-icon-right"></span>
            <div class='step_list_item'>
                <span class='num'>2</span>
                <span class="item_name">编写SQL</span>
            </div>
            <span class="el-icon-right"></span>
            <div class='step_list_item'>
                <span class='num'>3</span>
                <span class='item_name'>添加操作</span>
            </div>
            <span class="el-icon-right"></span>
            <div class='step_list_item'>
                <span class='num'>4</span>
                <span class='item_name'>验证规则</span>
            </div>
            <span class="el-icon-right"></span>
            <div class='step_list_item'>
                <span class='num'>5</span>
                <span class='item_name'>启动规则</span>
            </div>
        </div>
    </div>
    <div class='rule_list'>
        <p class='rule_title'>
            数据流转换表
        </p>
        <div class='control_part'>
            <el-button type='primary' size='small'  @click='createRule = !createRule'>创建规则</el-button>
            <div class='search_part'>
                <el-input v-model.trim='ruleName'
                    placeholder="请输入规则名称"
                    size='small'
                    clearable
                ></el-input>
                <el-button type='primary' size='small' @click='searchRule'>搜索</el-button>
            </div>
        </div>
        <el-table
            :data="ruleList"
            style="width: 100%"
            height='400'
            >
            <el-table-column
            prop="ruleName"
            label="规则名称"
            >
            </el-table-column>
            <el-table-column
            prop="payloadType"
            label="规则格式"
            width='100'
            :formatter="payloadType"
            >
            </el-table-column>
            <el-table-column
            prop="payloadMap"
            label="规则描述"
            :formatter="payloadMap">
            </el-table-column>
            <el-table-column
            prop="createDateStr"
            label="创建时间">
            </el-table-column>
            <el-table-column
            label="状态"
            width='110'
            >
              <template  slot-scope="scope">
                <span v-show="scope.row.status === 1"><i class='isActive'></i>运行中</span>
                <span v-show="scope.row.status === 0"><i class='notActive'></i>未启动</span>
              </template>
            </el-table-column>
            <el-table-column
            label="操作"
            width='170'>
                <template  slot-scope="scope">
                    <a v-show='scope.row.status === 0' @click='ruleStart(scope.row)'>启动</a>
                    <a v-show='scope.row.status === 1' @click='ruleStop(scope.row)'>停止</a>
                    <a @click='readDetail(scope.row)'>查看</a>
                    <a v-show='scope.row.status === 0' @click='ruleDelete(scope.row)'>删除</a>
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
    <el-dialog title="创建规则" :visible.sync="createRule">
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
        <el-button type="primary" size="small" @click='addRule'>提交</el-button>
        <el-button  size="small" @click='createRule = !createRule'>取 消</el-button>
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
      tabHeight: 0,
      createRule: false,
      ruleName: '',
      pageNum: 1,
      total: 0,
      ruleList: [],
      rule: {
        'ruleName': '',
        'payloadType': '1',
        'payloadMap': '',
        'conditionType': '1'
      },
      rules: {
        ruleName: [
          { validator: ruleName, trigger: 'blur' }
        ],
        payloadMap: [
          { validator: payloadMap, trigger: 'blur' }
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
    }
  },
  watch: {
    brokerId () {
      if (localStorage.getItem('groupId')) {
        this.getRuleList()
      }
    },
    groupId () {
      this.getRuleList()
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
      let data = {
        'ruleName': this.ruleName,
        'userId': localStorage.getItem('userId'),
        'brokerId': localStorage.getItem('brokerId'),
        'groupId': localStorage.getItem('groupId'),
        'pageNumber': this.pageNum,
        'pageSize': 10
      }
      API.ruleList(data).then(res => {
        if (res.data.status === 200) {
          if (res.data.data) {
            this.ruleList = [].concat(res.data.data)
            this.total = res.data.totalCount
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
      let data = {
        'id': e.id,
        'userId': e.userId,
        'brokerId': e.brokerId
      }
      API.ruleStart(data).then(res => {
        if (res.data.status === 200) {
          this.getRuleList()
          this.$message({
            type: 'success',
            message: '已启动'
          })
        } else {
          this.$message({
            type: 'warning',
            message: '启动失败'
          })
        }
      })
    },
    ruleStop (e) {
      let data = {
        'id': e.id,
        'userId': e.userId,
        'brokerId': e.brokerId,
        'status': 0
      }
      API.ruleStop(data).then(res => {
        if (res.data.status === 200) {
          this.getRuleList()
          this.$message({
            type: 'success',
            message: '已停止'
          })
        } else {
          this.$message({
            type: 'warning',
            message: '操作失败'
          })
        }
      })
    },
    ruleDelete (e) {
      this.$confirm('确认删除？', '删除规则', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        let data = {
          'id': e.id,
          'userId': e.userId,
          'brokerId': e.brokerId
        }
        API.ruleDelete(data).then(res => {
          if (res.data.status === 200) {
            this.getRuleList()
            this.$message({
              type: 'success',
              message: '已删除'
            })
          } else {
            this.$message({
              type: 'warning',
              message: '操作失败'
            })
          }
        })
      }).catch(() => {})
    },
    readDetail (e) {
      sessionStorage.setItem('ruleId', e.id)
      this.$store.commit('set_menu', ['规则引擎', '规则管理', '规则详情'])
      this.$router.push('./ruleDetail')
    },
    addRule () {
      let vm = this
      vm.$refs.rule.validate((valid) => {
        if (valid) {
          let data = {
            'ruleName': vm.rule.ruleName,
            'payloadType': vm.rule.payloadType,
            'payloadMap': JSON.parse(this.rule.payloadMap),
            'userId': localStorage.getItem('userId'),
            'brokerId': localStorage.getItem('brokerId'),
            'groupId': localStorage.getItem('groupId')
          }
          API.ruleAdd(data).then(res => {
            if (res.data.status === 200) {
              this.getRuleList()
              this.$message({
                type: 'success',
                message: '创建成功'
              })
            } else {
              this.$message({
                type: 'warning',
                message: '创建失败'
              })
            }
            vm.createRule = false
          })
        } else {
          return false
        }
      })
    }
  }
}
</script>
