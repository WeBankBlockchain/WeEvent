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
      :label="$t('rule.tableName')"
      prop="tableName"
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
        <el-form-item :label="$t('rule.JDBCdatabaseUrl')" prop='databaseUrl'>
          <el-input v-model.trim="form.databaseUrl"  autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item :label="$t('rule.JDBCusername')" prop='username'>
          <el-input v-model.trim="form.username"  autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item :label="$t('rule.JDBCpassword')" prop='password'>
          <el-input v-model.trim="form.password"  autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item :label="$t('rule.optionalParameter')">
          <el-input v-model.trim="form.optionalParameter"  autocomplete="off"></el-input>
        </el-form-item>
      </div>
      <el-form-item :label="$t('rule.tableName')" prop='tableName'>
        <el-input v-model.trim="form.tableName" autocomplete="off"></el-input>
      </el-form-item>
    </el-form>
    <p style='color:#67c23a' class='collectWarning' v-show="connectSuccess">
      {{$t('rule.connectSuccess')}}
    </p>
    <p style='color:#F56C6C' class='collectWarning' v-show="connectFailed">
      {{$t('rule.connectFailed')}}
    </p>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click='addURL' :disabled="!JDBCCheck">{{$t('common.ok')}}</el-button>
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
    var databaseUrl = (rule, value, callback) => {
      if (value === '') {
        callback(new Error(this.$t('rule.enterJDBCDatabaseUrl')))
      } else {
        callback()
      }
    }
    var tableName = (rule, value, callback) => {
      if (value === '') {
        callback(new Error(this.$t('rule.enterTableName')))
      } else {
        callback()
      }
    }
    return {
      topicName: '',
      loading: false,
      showlog: false,
      tableData: [],
      id: '',
      type: 1,
      title: this.$t('rule.addJDBCAddress'),
      form: {
        datasourceName: '',
        databaseUrl: '',
        username: '',
        password: '',
        tableName: '',
        optionalParameter: ''
      },
      JDBCCheck: false,
      connectSuccess: false,
      connectFailed: false,
      rules: {
        datasourceName: [
          { required: true, validator: datasourceName, trigger: 'blur' }
        ],
        databaseUrl: [
          { required: true, validator: databaseUrl, trigger: 'blur' }
        ],
        username: [
          { required: true, validator: username, trigger: 'blur' }
        ],
        password: [
          { required: true, validator: password, trigger: 'blur' }
        ],
        tableName: [
          { required: true, validator: tableName, trigger: 'blur' }
        ]
      }
    }
  },
  watch: {
    showlog (nVal) {
      if (!nVal) {
        let data = {
          datasourceName: '',
          databaseUrl: '',
          ip: '',
          port: '',
          username: '',
          password: '',
          tableName: '',
          optionalParameter: ''
        }
        this.form = Object.assign({}, data)
        this.type = 1
        this.JDBCCheck = false
        this.connectSuccess = false
        this.connectFailed = false
        this.title = this.$t('rule.addJDBCAddress')
        this.$refs.form.resetFields()
      }
    },
    form: {
      handler (nVal) {
        this.JDBCCheck = false
        this.connectSuccess = false
        this.connectFailed = false
      },
      deep: true
    }
  },
  methods: {
    getDBLsit () {
      API.dbList({'userId': localStorage.getItem('userId')}).then(res => {
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
          'databaseUrl': vm.form.databaseUrl,
          'username': vm.form.username,
          'password': vm.form.password,
          'tableName': vm.form.tableName,
          'optionalParameter': vm.form.optionalParameter,
          'userId': localStorage.getItem('userId')
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
                  message: res.data.message
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
                  message: res.data.message
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
      this.form.databaseUrl = e.databaseUrl
      this.form.username = e.username
      this.form.password = e.password
      this.form.tableName = e.tableName
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
          'id': e.id,
          'userId': localStorage.getItem('userId')
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
              message: res.data.message
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
          'databaseUrl': vm.form.databaseUrl,
          'username': vm.form.username,
          'password': vm.form.password,
          'tableName': vm.form.tableName,
          'optionalParameter': vm.form.optionalParameter,
          'userId': localStorage.getItem('userId')
        }
        if (valid) {
          API.checkJDBC(data).then(res => {
            if (res.data.status === 200) {
              vm.JDBCCheck = true
              this.connectSuccess = true
              this.connectFailed = false
            } else {
              this.connectSuccess = false
              this.connectFailed = true
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
