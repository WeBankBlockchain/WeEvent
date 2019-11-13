<template>
  <div class="servers">
    <div class='top_part'>
       <header-bar :noServer = true></header-bar>
    </div>
    <div class='cont_part'>
      <P class='back' @click='back'><i class='el-icon-arrow-left'></i>{{$t('common.back')}}</P>
      <div class='table_part'>
        <p class='title'>
          <span>{{$t('serverSet.serverMana')}}</span>
          <i class='el-icon-plus' @click="showLog = true" ></i>
        </p>
        <el-table
          :data="server"
          style="width: 100%">
          <el-table-column
            :label="$t('serverSet.serverName')"
            prop='name'
            width='120'
            >
          </el-table-column>
          <el-table-column
            :label="$t('serverSet.brokerURLAddress')"
            prop='brokerUrl'>
          </el-table-column>
          <el-table-column
            :label="$t('serverSet.webaseURLAddress')"
            prop='webaseUrl'>
          </el-table-column>
          <el-table-column
            width='100'
           :label="$t('common.action')">
            <template slot-scope='scope'>
              <i class='el-icon-edit table_icon' @click='adit(scope.row)' :title="$t('common.edit')"></i>
              <i class='el-icon-delete table_icon' @click='deleteItem(scope.row)' :title="$t('common.delete')"></i>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    <el-dialog :title="title" :visible.sync="showLog" :close-on-click-modal='false'>
      <el-form :model="form" :rules="rules" ref='form'>
        <el-form-item :label="$t('common.name') + ' :'" prop='name'>
          <el-input v-model.trim="form.name" autocomplete="off" :placeholder="$t('serverSet.namePlaceholder')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('serverSet.brokerURLAddress') + ' :'" prop='brokerUrl'>
          <el-input v-model.trim="form.brokerUrl" autocomplete="off"  :placeholder="$t('serverSet.borkerPlaceholder')"></el-input>
          <p class='version' v-show="version" v-html="version"></p>
        </el-form-item>
        <el-form-item :label="$t('serverSet.webaseURLAddress') + ' :'" prop='webaseUrl'>
          <el-input v-model.trim="form.webaseUrl" autocomplete="off"  :placeholder="$t('serverSet.webasePlaceholder')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('serverSet.authorized') + ' :'" v-show='showAccount'>
          <el-select
            v-model="form.userIdList"
            multiple
            :placeholder="$t('serverSet.authorizedPlaceholder')">
            <el-option
              v-for="item in accountList"
              :key="item.id"
              :label="item.username"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="showLog = false">{{$t('common.cancel')}}</el-button>
        <el-button type="primary" @click="confirm">{{$t('common.ok')}}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import API from '../API/resource.js'
import headerBar from '../components/headerBar'
export default {
  components: {
    headerBar
  },
  data () {
    var checkName = (rule, value, callback) => {
      if (value === '') {
        callback(new Error(this.$t('serverSet.noServerName')))
      } else {
        let regex = /^[0-9A-Za-z]{1,20}$/
        if (regex.exec(value)) {
          callback()
        } else {
          callback(new Error(this.$t('serverSet.errorServer')))
        }
      }
    }
    var checkBroker = (rule, value, callback) => {
      if (value === '') {
        callback(new Error(this.$t('serverSet.emptyPort')))
      } else {
        let url = '?brokerUrl=' + value
        API.getVersion(url).then(res => {
          if (res.data.code === 0) {
            let data = res.data.data
            let str = this.$t('header.version') + ': '
            for (var key in data) {
              str += data[key] + '&nbsp&nbsp'
            }
            this.version = str
            callback()
          } else {
            callback(new Error(this.$t('serverSet.errorAddress')))
          }
        }).catch(e => {
          callback(new Error(this.$t('serverSet.errorAddress')))
        })
      }
    }
    var checkWeBase = (rule, value, callback) => {
      if (value === '') {
        callback()
      } else {
        // let data = {
        //   'userId': parseInt(localStorage.getItem('userId')),
        //   'id': parseInt(localStorage.getItem('brokerId')),
        //   'weBaseUrl': value
        // }
        // API.checkBrokerServer(data).then(res => {
        //   if (res.data === 'SUCCESS') {
        //     callback()
        //   } else {
        //     callback('error')
        //   }
        // })
        callback()
      }
    }
    return {
      version: '',
      showVersion: false,
      server: [],
      showLog: false,
      isEdit: false,
      title: '',
      accountList: [],
      showAccount: true,
      form: {
        name: '',
        brokerUrl: '',
        webaseUrl: '',
        userIdList: []
      },
      brokerId: '',
      rules: {
        name: [
          { required: true, validator: checkName, trigger: 'blur' }
        ],
        brokerUrl: [
          { required: true, validator: checkBroker, trigger: 'blur' }
        ],
        webaseUrl: [
          { validator: checkWeBase, trigger: 'blur' }
        ]
      }
    }
  },
  watch: {
    showLog (nVal) {
      if (!nVal) {
        this.$refs.form.resetFields()
        this.$set(this.form, 'name', '')
        this.$set(this.form, 'brokerUrl', '')
        this.$set(this.form, 'webaseUrl', '')
        this.$set(this.form, 'userIdList', [])
        this.version = ''
        this.brokerId = ''
        this.showAccount = true
        this.isEdit = false
      } else {
        if (!this.isEdit) {
          this.title = this.$t('serverSet.addServer')
        }
        API.accountList('').then(res => {
          if (res.data.status === 200) {
            this.accountList = [].concat(res.data.data)
          }
        })
      }
    }
  },
  mounted () {
    this.getServer()
  },
  methods: {
    confirm () {
      let vm = this
      vm.$refs.form.validate((valid) => {
        if (valid) {
          if (vm.isEdit) {
            vm.editServer()
          } else {
            vm.addServer()
          }
        } else {
          return false
        }
      })
    },
    addServer () {
      let data = {
        name: this.form.name,
        brokerUrl: this.form.brokerUrl,
        webaseUrl: this.form.webaseUrl,
        userId: Number(localStorage.getItem('userId'))
      }
      data.userIdList = [].concat(this.form.userIdList)
      API.addServer(data).then(res => {
        if (res.status === 200) {
          if (res.data.status === 200) {
            this.$message({
              type: 'success',
              message: this.$t('common.addSuccess')
            })
            this.getServer()
          } else if (res.data.status === 100108) {
            this.$message({
              type: 'warning',
              message: this.$t('serverSet.exitBrokerURL')
            })
          } else {
            this.$message({
              type: 'warning',
              message: this.$t('common.addFail')
            })
          }
        } else {
          this.$message({
            type: 'warning',
            message: this.$t('common.addFail')
          })
        }
        this.showLog = false
      })
    },
    getServer () {
      API.getServer('').then(res => {
        if (res.status === 200) {
          this.server = [].concat(res.data)
        }
      })
    },
    editServer () {
      this.isEdit = false
      let data = {
        name: this.form.name,
        brokerUrl: this.form.brokerUrl,
        webaseUrl: this.form.webaseUrl,
        id: this.brokerId,
        userId: Number(localStorage.getItem('userId'))
      }
      data.userIdList = [].concat(this.form.userIdList)
      API.updateServer(data).then(res => {
        if (res.status === 200) {
          if (res.data.status === 200) {
            this.$message({
              type: 'success',
              message: this.$t('common.editSuccess')
            })
            this.getServer()
          } else if (res.data.status === 100108) {
            this.$message({
              type: 'warning',
              message: this.$t('serverSet.exitBrokerURL')
            })
          } else {
            this.$message({
              type: 'warning',
              message: this.$t('common.editFail')
            })
          }
        } else {
          this.$message({
            type: 'warning',
            message: this.$t('common.editFail')
          })
        }
        this.showLog = false
      })
    },
    adit (e) {
      let vm = this
      vm.$set(this.form, 'name', e.name)
      vm.$set(this.form, 'brokerUrl', e.brokerUrl)
      vm.$set(this.form, 'webaseUrl', e.webaseUrl)
      vm.$set(this.form, 'userIdList', [])
      vm.brokerId = e.id
      if (e.isCreator === '1') {
        let data = {
          brokerId: e.id
        }
        API.permissionList(data).then(res => {
          if (res.data.status === 200) {
            res.data.data.forEach(e => {
              vm.form.userIdList.push(e.userId)
            })
          }
        })
      } else {
        this.showAccount = false
      }
      this.title = '编辑信息'
      this.showLog = true
      this.isEdit = true
      this.title = this.$t('serverSet.editServer')
    },
    deleteItem (e) {
      var vm = this
      vm.$confirm(vm.$t('common.isDelete')).then(_ => {
        let data = {
          'id': e.id
        }
        API.deleteServer(data).then(res => {
          if (res.data.status === 200) {
            vm.$message({
              type: 'success',
              message: vm.$t('common.deleteSuccess')
            })
            vm.getServer()
            if (e.id === parseInt(localStorage.getItem('brokerId'))) {
              if (vm.server.length > 0) {
                let newId = vm.server[0].id
                localStorage.setItem('brokerId', newId)
              } else {
                localStorage.removeItem('brokerId')
              }
            }
          } else {
            this.$message({
              type: 'warning',
              message: vm.$t('common.deleteFail')
            })
          }
        })
      })
    },
    back () {
      this.$router.go(-1)
    }
  }
}
</script>
