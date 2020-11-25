<template>
<div class='event-table timeSch'>
  <div class='refresh'>
    <el-button type='primary' size='small' icon='el-icon-plus' @click='showlog = !showlog'>{{$t('common.add')}}</el-button>
  </div>
  <el-table
    :data="tableData"
    v-loading='loading'
    element-loading-spinner='el-icon-loading'
    :element-loading-text="$t('common.loading')"
    element-loading-background='rgba(256,256,256,0.8)'
    style="width: 100%"
    >
    <el-table-column
      :label="$t('timerSchedule.schedulerName')"
      prop="schedulerName"
      width='200'>
    </el-table-column>
    <el-table-column
      :label="$t('timerSchedule.databaseUrl')"
    >
      <template slot-scope="scope">
        <a :title='scope.row.databaseUrl'>{{scope.row.databaseUrl}}</a>
      </template>
    </el-table-column>
    <el-table-column
      :label="$t('timerSchedule.periodParams')"
      prop="periodParams"
      width='200'>
    </el-table-column>
      <el-table-column
        :label="$t('timerSchedule.parsingSql')"
      >
      <template slot-scope="scope">
        <a :title='scope.row.parsingSql'>{{scope.row.parsingSql}}</a>
      </template>
    </el-table-column>
    <el-table-column
      :label="$t('common.action')"
      width='170'>
      <template  slot-scope="scope">
        <a @click='update(scope.row)' style="cursor: pointer;margin-right:10px;color:#006cff">{{$t('common.edit')}}</a>
        <a @click='deleteItem(scope.row)' style="cursor: pointer;color:#006cff">{{$t('common.delete')}}</a>
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
  <el-dialog :title="title" :visible.sync="showlog" center width='650px' :close-on-click-modal='false'>
    <el-form :model="form" :rules="rules" ref='form'>
      <el-form-item :label="$t('timerSchedule.schedulerName') + ':'" prop='schedulerName'>
        <el-input v-model.trim="form.schedulerName" autocomplete="off"></el-input>
      </el-form-item>
      <el-form-item :label="$t('timerSchedule.databaseUrl') + ':'" prop='ruleDatabaseId'>
        <el-select name='options_dialog' v-model="form.ruleDatabaseId" v-show="dbList.length > 0">
          <el-option v-for="(item, index) in dbList" :key='index' :value="item.id" :label="item.databaseUrl"></el-option>
        </el-select>
        <div class='no_dbList' v-show="dbList.length === 0">{{$t('timerSchedule.noDbList')}} <span @click="creatDB" >{{$t('ruleDetail.setGuide')}}</span></div>
      </el-form-item>
      <el-form-item :label="$t('timerSchedule.periodParams') + ':'" prop='periodParams'>
        <el-input v-model="form.periodParams" autocomplete="off" placeholder="*/5 * * * * ?"></el-input>
      </el-form-item>
      <el-form-item :label="$t('timerSchedule.parsingSql') + ':'" prop='parsingSql'>
        <el-input v-model="form.parsingSql" autocomplete="off" type='textarea' :rows='5' placeholder="select count(1) from TIMER_SCHEDULER_JOB"></el-input>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click='addURL'>{{$t('common.ok')}}</el-button>
      <el-button @click="showlog = false">{{$t('common.cancel')}}</el-button>
    </div>
  </el-dialog>
 </div>
</template>
<script>
import API from '../API/resource.js'
export default {
  data () {
    var schedulerName = (rule, value, callback) => {
      if (value === '') {
        callback(new Error(this.$t('timerSchedule.enterSN')))
      } else {
        callback()
      }
    }
    var periodParams = (rule, value, callback) => {
      if (value === '') {
        callback(new Error(this.$t('timerSchedule.enterPP')))
      } else {
        const url = '?corn=' + value
        API.cornCheck(url).then(res => {
          if (res.data.data) {
            callback()
          } else {
            callback(new Error(this.$t('timerSchedule.errorCorn')))
          }
        })
      }
    }
    var parsingSql = (rule, value, callback) => {
      if (value === '') {
        callback(new Error(this.$t('timerSchedule.enterSQL')))
      } else {
        callback()
      }
    }
    return {
      loading: false,
      showlog: false,
      pageNum: 1,
      total: 0,
      dbList: [],
      tableData: [],
      id: '',
      type: 1,
      title: this.$t('timerSchedule.addScheduler'),
      form: {
        schedulerName: '',
        ruleDatabaseId: '',
        periodParams: '',
        parsingSql: ''
      },
      rules: {
        schedulerName: [
          { required: true, validator: schedulerName, trigger: 'blur' }
        ],
        ruleDatabaseId: [
          { required: true, message: this.$t('timerSchedule.selectDB'), trigger: 'change' }
        ],
        periodParams: [
          { required: true, validator: periodParams, trigger: 'blur' }
        ],
        parsingSql: [
          { required: true, validator: parsingSql, trigger: 'blur' }
        ]
      }
    }
  },
  watch: {
    showlog (nVal) {
      if (!nVal) {
        this.$refs.form.resetFields()
        this.form.schedulerName = ''
        this.form.ruleDatabaseId = ''
        this.form.periodParams = ''
        this.form.parsingSql = ''
        this.type = 1
        this.title = this.$t('timerSchedule.addScheduler')
      }
    }
  },
  methods: {
    timerList () {
      const data = {
        brokerId: localStorage.getItem('brokerId'),
        pageSize: 10,
        pageNumber: this.pageNum
      }
      this.tableData = []
      API.timerList(data).then(res => {
        if (res.data.code === 0 && res.data.data) {
          this.tableData = [].concat(res.data.data.timerSchedulerEntityList)
          this.total = res.data.data.totalCount
        }
      })
    },
    getDBLsit () {
      API.dbList({}).then(res => {
        if (res.data.code === 0) {
          this.dbList = [].concat(res.data.data)
        }
      })
    },
    addURL () {
      const vm = this
      vm.$refs.form.validate((valid) => {
        const data = {
          schedulerName: this.form.schedulerName,
          ruleDatabaseId: this.form.ruleDatabaseId,
          periodParams: this.form.periodParams,
          parsingSql: this.form.parsingSql
        }
        if (valid) {
          if (vm.type === 1) {
            data.brokerId = localStorage.getItem('brokerId')
            API.addTimer(data).then(res => {
              if (res.data.code === 0) {
                vm.$message({
                  type: 'success',
                  message: this.$t('rule.creatSuccess')
                })
                vm.timerList()
              } else {
                vm.$store.commit('set_Msg', vm.$message({
                  type: 'warning',
                  message: res.data.message,
                  duration: 0,
                  showClose: true
                }))
              }
              vm.showlog = false
            })
          } else {
            data.id = vm.id
            data.brokerId = localStorage.getItem('brokerId')
            API.updateTimer(data).then(res => {
              if (res.data.code === 0) {
                vm.$message({
                  type: 'success',
                  message: this.$t('common.editSuccess')
                })
                vm.timerList()
              } else {
                vm.$store.commit('set_Msg', vm.$message({
                  type: 'warning',
                  message: res.data.message,
                  duration: 0,
                  showClose: true
                }))
              }
              vm.showlog = false
            })
          }
        }
      })
    },
    update (e) {
      this.showlog = true
      this.title = this.$t('timerSchedule.editScheduler')
      this.form.schedulerName = e.schedulerName
      this.form.ruleDatabaseId = e.ruleDatabaseId
      this.form.periodParams = e.periodParams
      this.form.parsingSql = e.parsingSql
      this.id = e.id
      this.type = 2
    },
    deleteItem (e) {
      const vm = this
      vm.$confirm(vm.$t('common.isDelete'), vm.$t('rule.deleteAddress'), {
        confirmButtonText: vm.$t('common.ok'),
        cancelButtonText: vm.$t('common.cancel'),
        type: 'warning'
      }).then(() => {
        const data = {
          id: e.id,
          brokerId: localStorage.getItem('brokerId')
        }
        API.deleteTimer(data).then(res => {
          if (res.data.code === 0) {
            vm.$message({
              type: 'success',
              message: vm.$t('common.deleteSuccess')
            })
            vm.timerList()
          } else {
            vm.$store.commit('set_Msg', vm.$message({
              type: 'warning',
              message: res.data.message,
              duration: 0,
              showClose: true
            }))
          }
          vm.showlog = false
        })
      }).catch(() => {
        vm.showlog = false
      })
    },
    creatDB () {
      this.$store.commit('set_menu', [this.$t('sideBar.engine'), this.$t('sideBar.sources')])
      this.$store.commit('set_active', '4-2')
      this.$router.push('./dataBase')
    },
    indexChange (e) {
      this.pageNum = e
      this.timerList()
    }
  },
  mounted () {
    this.timerList()
    this.getDBLsit()
  }
}
</script>
