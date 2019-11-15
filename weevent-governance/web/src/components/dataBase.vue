<template>
<div class='event-table topic'>
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
      prop="databaseName"
      width='200'>
    </el-table-column>
    <el-table-column
      :label="$t('rule.tableName')"
      prop="tableName"
      width='200'>
    </el-table-column>
    <el-table-column
      :label="$t('rule.ruleDataBaseId')"
      prop="databaseUrl">
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
  <el-dialog :title="title" :visible.sync="showlog" center width='450px' :close-on-click-modal='false'>
    <el-form :model="form" :rules="rules" ref='form'>
      <el-form-item :label="$t('rule.JDBCname')" prop='databaseName'>
        <el-input v-model.trim="form.databaseName" autocomplete="off"></el-input>
      </el-form-item>

      <el-form-item :label="$t('rule.ruleDataBaseId')" prop='url'>
        <el-input v-model.trim="form.url" type='textarea' :rows='4' autocomplete="off" :placeholder="$t('common.examples') + 'jdbc:mysql://127.0.0.1:3306/governance?user=root&password=123456&useUnicode=true&characterEncoding=utf-8&useSSL=false'"></el-input>
      </el-form-item>
      <el-form-item :label="$t('rule.tableName')" prop='tableName'>
        <el-input v-model.trim="form.tableName" autocomplete="off"></el-input>
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
    var url = (rule, value, callback) => {
      if (value === '') {
        callback(new Error(this.$t('rule.enterDB')))
      } else {
        callback()
      }
    }
    var databaseName = (rule, value, callback) => {
      if (value === '') {
        callback(new Error(this.$t('rule.enterJDBCname')))
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
      title: this.$t('rule.addAddress'),
      form: {
        databaseName: '',
        url: '',
        tableName: ''
      },
      rules: {
        databaseName: [
          { required: true, validator: databaseName, trigger: 'blur' }
        ],
        url: [
          { required: true, validator: url, trigger: 'blur' }
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
          databaseName: '',
          tableName: '',
          url: ''
        }
        this.form = Object.assign({}, data)
        this.type = 1
        this.title = this.$t('rule.addAddress')
        this.$refs.form.resetFields()
      }
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
          'databaseUrl': vm.form.url,
          'databaseName': vm.form.databaseName,
          'tableName': vm.form.tableName,
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
      this.form.databaseName = e.databaseName
      this.form.tableName = e.tableName
      this.form.url = e.databaseUrl
      this.title = this.$t('rule.enditAddress')
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
    }
  },
  mounted () {
    this.getDBLsit()
  }
}

</script>
