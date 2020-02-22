<template>
<div class='event-table topic dataBase'>
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
      :label="$t('rule.JDBCname')"
      prop="datasourceName"
      width='200'>
    </el-table-column>
    <el-table-column
      :label="$t('rule.ruleDataBaseId')"
    >
      <template slot-scope="scope">
        <a :title='scope.row.databaseUrl'>{{scope.row.databaseUrl}}</a>
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
  <el-dialog :title="title" :visible.sync="showlog" center width='650px' :close-on-click-modal='false'>
    <el-form :model="form" :rules="rules" ref='form'>
      <el-form-item :label="$t('rule.JDBCname')" prop='datasourceName'>
        <el-input v-model.trim="form.datasourceName" autocomplete="off"></el-input>
      </el-form-item>
      <div class='JDBCinfor'>
        <div class='JDBCTitle'><i>*</i> {{$t('rule.JDBCinfor')}}
         <el-button type='primary' size='small' @click='checkJDBC'>{{$t('rule.checkJDBC')}}</el-button>
        </div>
        <el-form-item :label="$t('rule.databaseType')  + ' :'">
          <el-radio-group v-model="form.databaseType">
            <el-radio label="2">Mysql</el-radio>
            <el-radio label="1">H2</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="ip" prop='databaseIp'>
          <el-input v-model.trim="form.databaseIp"  autocomplete="off" placeholder="127.0.0.1"></el-input>
        </el-form-item>
        <el-form-item label="port" prop='databasePort'>
          <el-input v-model.trim="form.databasePort"  autocomplete="off" :placeholder="3306"></el-input>
        </el-form-item>
        <el-form-item :label="$t('rule.JDBCDatabaseName')" prop='databaseName'>
          <el-input v-model.trim="form.databaseName"  autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item :label="$t('rule.JDBCusername')" prop='username'>
          <el-input v-model.trim="form.username"  autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item :label="$t('rule.JDBCpassword')" prop='password'>
          <el-input v-model.trim="form.password"  type='password'  autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item :label="$t('rule.optionalParameter')">
          <el-input v-model.trim="form.optionalParameter"  autocomplete="off"></el-input>
        </el-form-item>
      </div>
    </el-form>
    <p style='color:#67c23a' class='collectWarning' v-show="connectSuccess">
      {{$t('rule.connectSuccess')}}
    </p>
    <p style='color:#F56C6C' class='collectWarning' v-show="connectFailed">
      {{$t('rule.connectFailed')}}
    </p>
    <p style='color:#F56C6C' class='collectWarning' v-show="connectTimeOut">
      {{$t('rule.connectTimeOut')}}
    </p>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click='addURL' :disabled="!connectSuccess">{{$t('common.ok')}}</el-button>
      <el-button @click="showlog = false">{{$t('common.cancel')}}</el-button>
    </div>
  </el-dialog>
 </div>
</template>
<script>
import API from '../API/resource.js'
export default {
  data () {
    var datasourceName = (rule, value, callback) => {
      if (value === '') {
        callback(new Error(this.$t('rule.enterJDBCname')))
      } else {
        callback()
      }
    }
    var username = (rule, value, callback) => {
      if (value === '') {
        callback(new Error(this.$t('rule.enterJDBCusername')))
      } else {
        callback()
      }
    }
    var password = (rule, value, callback) => {
      if (value === '') {
        callback(new Error(this.$t('rule.enterJDBCpassword')))
      } else {
        callback()
      }
    }
    var databaseIp = (rule, value, callback) => {
      if (value === '') {
        callback(new Error(this.$t('rule.enterJDBCIP')))
      } else {
        callback()
      }
    }
    var databasePort = (rule, value, callback) => {
      if (value === '') {
        callback(new Error(this.$t('rule.enterJDBCport')))
      } else {
        callback()
      }
    }
    var databaseName = (rule, value, callback) => {
      if (!value) {
        callback(new Error(this.$t('rule.enterJDBCDatabaseName')))
      } else {
        callback()
      }
    }
    return {
      topicName: '',
      loading: false,
      showlog: false,
      connectSuccess: false,
      connectFailed: false,
      connectTimeOut: false,
      tableData: [],
      id: '',
      type: 1,
      title: this.$t('rule.addJDBCAddress'),
      form: {
        datasourceName: '',
        databaseType: '2',
        databaseIp: '',
        databasePort: '',
        databaseName: '',
        username: '',
        password: '',
        optionalParameter: ''
      },
      rules: {
        datasourceName: [
          { required: true, validator: datasourceName, trigger: 'blur' }
        ],
        username: [
          { required: true, validator: username, trigger: 'blur' }
        ],
        password: [
          { required: true, validator: password, trigger: 'blur' }
        ],
        databaseIp: [
          { required: true, validator: databaseIp, trigger: 'blur' }
        ],
        databasePort: [
          { required: true, validator: databasePort, trigger: 'blur' }
        ],
        databaseName: [
          { required: true, validator: databaseName, trigger: 'blur' }
        ]
      }
    }
  },
  watch: {
    showlog (nVal) {
      if (!nVal) {
        let data = {
          datasourceName: '',
          databaseType: '2',
          databaseIp: '',
          databasePort: '',
          databaseName: '',
          username: '',
          password: '',
          optionalParameter: ''
        }
        this.form = Object.assign({}, data)
        this.type = 1
        this.connectSuccess = false
        this.connectFailed = false
        this.connectTimeOut = false
        this.title = this.$t('rule.addJDBCAddress')
        this.$refs.form.resetFields()
      }
    },
    form: {
      handler (nVal) {
        this.connectSuccess = false
        this.connectFailed = false
        this.connectTimeOut = false
      },
      deep: true
    }
  },
  methods: {
    getDBLsit () {
      API.dbList({}).then(res => {
        if (res.data.status === 200) {
          this.tableData = [].concat(res.data.data)
        }
      })
    },
    addURL () {
      let vm = this
      vm.$refs.form.validate((valid) => {
        let data = {
          'datasourceName': vm.form.datasourceName,
          'databaseType': vm.form.databaseType,
          'username': vm.form.username,
          'password': vm.form.password,
          'optionalParameter': vm.form.optionalParameter,
          'databaseIp': vm.form.databaseIp,
          'databasePort': vm.form.databasePort,
          'databaseName': vm.form.databaseName
        }
        if (valid) {
          if (vm.type === 1) {
            data.brokerId = localStorage.getItem('brokerId')
            API.dbAdd(data).then(res => {
              if (res.data.status === 200) {
                vm.$message({
                  type: 'success',
                  message: this.$t('rule.creatSuccess')
                })
                vm.getDBLsit()
              } else {
                vm.$message({
                  type: 'warning',
                  message: res.data.message,
                  duration: 5000
                })
              }
              vm.showlog = false
            })
          } else {
            data.id = vm.id
            API.dbUpdate(data).then(res => {
              if (res.data.status === 200) {
                vm.$message({
                  type: 'success',
                  message: this.$t('common.editSuccess')
                })
                vm.getDBLsit()
              } else {
                vm.$message({
                  type: 'warning',
                  message: res.data.message,
                  duration: 5000
                })
              }
              vm.showlog = false
            })
          }
        }
      })
    },
    update (e) {
      this.showlog = true
      this.id = e.id
      this.form.datasourceName = e.datasourceName
      this.form.databaseType = e.databaseType
      this.form.databaseIp = e.databaseIp
      this.form.databasePort = e.databasePort
      this.form.databaseName = e.databaseName
      this.form.username = e.username
      this.form.password = e.password
      this.form.optionalParameter = e.optionalParameter
      this.title = this.$t('rule.editJDBCAddress')
      this.type = 2
    },
    deleteItem (e) {
      let vm = this
      vm.$confirm(vm.$t('common.isDelete'), vm.$t('rule.deleteAddress'), {
        confirmButtonText: vm.$t('common.ok'),
        cancelButtonText: vm.$t('common.cancel'),
        type: 'warning'
      }).then(() => {
        let data = {
          'id': e.id
        }
        API.dbDelete(data).then(res => {
          if (res.data.status === 200) {
            vm.$message({
              type: 'success',
              message: vm.$t('common.deleteSuccess')
            })
            vm.getDBLsit()
          } else {
            vm.$message({
              type: 'warning',
              message: res.data.message,
              duration: 5000
            })
          }
          vm.showlog = false
        })
      }).catch(() => {
        vm.showlog = false
      })
    },
    checkJDBC () {
      let vm = this
      vm.$refs.form.validate((valid) => {
        let data = {
          'datasourceName': vm.form.datasourceName,
          'databaseType': vm.form.databaseType,
          'username': vm.form.username,
          'password': vm.form.password,
          'databaseIp': vm.form.databaseIp,
          'databasePort': vm.form.databasePort,
          'databaseName': vm.form.databaseName,
          'optionalParameter': vm.form.optionalParameter,
          'checkType': 1
        }
        if (valid) {
          API.checkJDBC(data).then(res => {
            try {
              if (res.data.status === 200) {
                this.connectSuccess = true
                this.connectFailed = false
              } else {
                this.connectSuccess = false
                this.connectFailed = true
              }
            } catch (e) {
              if (res.message.includes('timeout')) {
                vm.connectTimeOut = true
              }
            }
          })
        }
      })
    }
  },
  mounted () {
    this.getDBLsit()
  }
}

</script>
