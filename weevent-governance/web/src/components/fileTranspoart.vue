<template>
<div class='event-table topic fileTranspoart'>
  <div class='refresh top_part'>
    <el-button type='primary' size='small' icon='el-icon-plus' @click='addNewOne'>{{$t('common.add')}}</el-button>
  </div>
  <el-table
    :data="tableData"
    v-loading='loading'
    element-loading-spinner='el-icon-loading'
    :element-loading-text="$t('common.loading')"
    element-loading-background='rgba(256,256,256,0.8)'
    style="width: 100%"
    @row-dblclick='rowClick'
    @expand-change='readDetail'
    ref='table'
    >
    <el-table-column type="expand">
      <template slot-scope="props">
        <h3>{{$t('file.fileuploadList')}}</h3>
        <el-table
          :data="props.row.detail"
          >
          <el-table-column
            label="File Name"
            prop="fileName"
          >
          </el-table-column>
          <el-table-column
            label="File Md5"
          >
            <template slot-scope="scope">
              <span :title='scope.row.fileMd5'>{{scope.row.fileMd5}}</span>
            </template>
          </el-table-column>
          <el-table-column
            label="File Size"
            prop="fileSize"
            :formatter="checkFileSize"
          >
          </el-table-column>
          <el-table-column
            label="Speed"
            prop="speed"
            :formatter="checkSpeed"
          >
          </el-table-column>
          <el-table-column
            label="Process"
            prop="process"
          >
          </el-table-column>
        </el-table>
      </template>
    </el-table-column>
    <el-table-column
      label="Topic"
      prop="topicName"
      >
    </el-table-column>
    <el-table-column
      :label="$t('file.creater')"
      prop="creater"
    >
    </el-table-column>
     <el-table-column
      :label="$t('file.creatTime')"
      prop="createTime"
    >
    </el-table-column>
    <el-table-column
      :label="$t('file.roles')"
      prop="role"
      :formatter="checkRoles"
      >
    </el-table-column>
    <el-table-column
      :label="$t('file.certification')"
      >
      <template  slot-scope="scope">
        <el-tooltip v-show="scope.row.verified" class="item" effect="dark" :content="scope.row.role === '1'? scope.row.publicKey : scope.row.privateKey " placement="bottom">
          <span style='cursor: pointer'>{{$t('file.certified')}}</span>
        </el-tooltip>
        <span v-show="!scope.row.verified">{{$t('file.uncertified')}}</span>
      </template>
    </el-table-column>
    <el-table-column
      :label="$t('file.options')"
      width='200'
      >
      <template  slot-scope="scope">
        <el-button size='mini' type='primary' @click.stop='fileOption(scope.row)' v-show="scope.row.role || scope.row.role === '1'">{{scope.row.role === '1' ? $t('file.upload') : $t('file.download')}}</el-button>
        <a class='el-icon-delete-solid' style='font-size:18px;margin-left:10px;cursor:pointer' @click.stop='deleteItem(scope.row)'></a>
      </template>
    </el-table-column>
  </el-table>
  <el-dialog :title="$t('tableCont.addTopic')" :visible.sync="dialogFormVisible" center width='450px' :close-on-click-modal='false'>
    <el-form :model="form" :rules="rules" ref='form' label-position="top">
      <el-form-item :label="$t('common.name') + ':'" prop='name'>
        <el-select v-model="form.name" @visible-change='selectShow'>
          <el-option v-for="(item, index) in listTopic" :key="index" :label="item.topicName" :value="item.topicName"></el-option>
          <el-pagination
            layout="prev, pager, next"
            small
            :current-page.sync="pageIndex"
            :total="total">
          </el-pagination>
        </el-select>
      </el-form-item >
      <el-form-item :label="$t('file.roles') + ':'" prop='roles'>
        <el-select v-model="form.roles">
          <el-option :label="$t('file.sender')" value="1"></el-option>
          <el-option :label="$t('file.receiver')" value="0"></el-option>
        </el-select>
      </el-form-item >
      <el-form-item :label="$t('file.publicKey') + ' :'" v-show='form.roles === "1"'>
        <el-input v-model="form.publicKey" type='textarea' :rows='3' autocomplete="off"></el-input>
      </el-form-item>
      <el-form-item :label="$t('file.privateKey') + ' :'" v-show='form.roles === "0"'>
        <el-input v-model="form.privateKey" type='textarea' :rows='3' autocomplete="off"></el-input>
      </el-form-item>
      <el-form-item :label="$t('file.overwrite')" v-show="form.roles === '1'">
        <el-radio-group v-model="form.overwrite">
          <el-radio label="1">Yes</el-radio>
          <el-radio label="0">No</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click='addTopic(form)'>{{$t('common.ok')}}</el-button>
      <el-button @click="dialogFormVisible = false">{{$t('common.cancel')}}</el-button>
    </div>
  </el-dialog>
  <el-dialog :title="$t('file.downloadList')" :visible.sync="showDownList" center :close-on-click-modal='false'>
    <el-table
    :data="downLoadList"
    style="width: 100%"
    >
      <el-table-column
        :label="$t('file.fileName')"
        prop="fileName"
      >
      </el-table-column>
      <el-table-column
        :label="$t('file.options')"
        width='150'
      >
        <template  slot-scope="scope">
          <el-button size='mini' type='primary' @click='dFile(scope.row)'>{{$t('file.downloadFile')}}</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>
 </div>
</template>
<script>
import API from '../API/resource.js'
import Bus from './tool/js/bus'
// import axios from 'axios'
const con = require('../../config/config.js')
export default {
  data () {
    var name = (rule, value, callback) => {
      if (value === '') {
        callback(new Error(this.$t('tableCont.noName')))
      } else {
        callback()
      }
    }
    var roles = (rule, value, callback) => {
      if (value === '') {
        callback(new Error(this.$t('common.choose')))
      } else {
        callback()
      }
    }
    return {
      topicName: '',
      showDownList: false,
      loading: false,
      dialogFormVisible: false,
      tableData: [],
      fileList: [],
      listTopic: [],
      downLoadList: [],
      form: {
        name: '',
        roles: '',
        overwrite: '0',
        publicKey: '',
        privateKey: ''
      },
      rules: {
        name: [
          { required: true, validator: name, trigger: 'blur' }
        ],
        roles: [
          { required: true, validator: roles, trigger: 'change' }
        ]
      },
      pageIndex: 0,
      total: 0
    }
  },
  methods: {
    checkName (e) {
      if (!e.topicName) {
        return '—'
      } else {
        return e.topicName
      }
    },
    checkRoles (e) {
      if (e.role === '1') {
        return this.$t('file.sender')
      } else if (e.role === '0') {
        return this.$t('file.receiver')
      } else {
        return '-'
      }
    },
    addNewOne () {
      this.dialogFormVisible = true
    },
    getData () {
      let vm = this
      vm.loading = true
      setTimeout(fun => {
        const url = '?brokerId=' + localStorage.getItem('brokerId') + '&groupId=' + localStorage.getItem('groupId')
        API.listTransport(url).then(res => {
          if (res.data.status === 200) {
            vm.tableData = [].concat(res.data.data)
            let det = []
            vm.tableData.forEach(item => {
              vm.$set(item, 'detail', det)
            })
          } else {
            vm.$store.commit('set_Msg', this.$message({
              type: 'warning',
              message: this.$t('tableCont.getDataError'),
              duration: 0,
              showClose: true
            }))
          }
          vm.loading = false
        }).catch(e => {
          vm.loading = false
        })
      }, 500)
    },
    addTopic (form) {
      let vm = this
      vm.$refs.form.validate((valid) => {
        if (valid) {
          let data = {
            topicName: vm.form.name,
            brokerId: Number(localStorage.getItem('brokerId')),
            groupId: localStorage.getItem('groupId'),
            role: vm.form.roles,
            creater: localStorage.getItem('user'),
            overWrite: vm.form.overwrite,
            publicKey: '',
            privateKey: ''
          }
          if (data.role === '1') {
            this.$nextTick(fun => {
              this.form.privateKey = ''
              data.publicKey = this.form.publicKey
            })
          } else {
            this.$nextTick(fun => {
              this.form.publicKey = ''
              data.privateKey = this.form.privateKey
            })
          }
          API.openTransport(data).then(res => {
            if (res.data.status === 200) {
              vm.$message({
                type: 'success',
                message: this.$t('common.addSuccess')
              })
              vm.getData()
            } else {
              vm.$store.commit('set_Msg', vm.$message({
                type: 'warning',
                message: res.data.message,
                duration: 0,
                showClose: true
              }))
            }
            vm.dialogFormVisible = false
          }).catch(e => {
            vm.$store.commit('set_Msg', vm.$message({
              type: 'warning',
              message: this.$t('common.addFail'),
              duration: 0,
              showClose: true
            }))
          })
          vm.dialogFormVisible = false
        } else {
          return false
        }
      })
    },
    editItem (e) {
    },
    deleteItem (e) {
      let vm = this
      vm.$confirm(vm.$t('file.deleteTopic'), vm.$t('file.isDelete'), {
        confirmButtonText: vm.$t('common.ok'),
        cancelButtonText: vm.$t('common.cancel'),
        type: 'warning'
      }).then(() => {
        let data = {
          topicName: e.topicName,
          id: e.id,
          role: e.role,
          brokerId: Number(localStorage.getItem('brokerId')),
          groupId: localStorage.getItem('groupId')
        }
        API.deleteTransport(data).then(res => {
          if (res.data.status === 200) {
            vm.$message({
              type: 'success',
              message: vm.$t('rule.hasDelete')
            })
            vm.getData()
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
    rowClick (e) {
      this.$refs.table.toggleRowExpansion(e)
    },
    readDetail (e) {
      let vm = this
      const url = '?brokerId=' + localStorage.getItem('brokerId') + '&groupId=' + localStorage.getItem('groupId') + '&topicName=' + e.topicName + '&role=' + e.role
      API.fileStatus(url).then(res => {
        if (res.data.status === 200) {
          let list = res.data.data
          let newList = []
          list.forEach(item => {
            let e = {
              'fileName': item.file.fileName,
              'fileSize': item.file.fileSize,
              'fileMd5': item.file.fileMd5,
              'process': item.process,
              'speed': item.speed
            }
            newList.push(e)
          })
          vm.$set(e, 'detail', newList)
        } else {
          vm.$store.commit('set_Msg', vm.$message({
            type: 'warning',
            message: vm.$t('tableCont.getDataError'),
            duration: 0,
            showClose: true
          }))
        }
      })
    },
    checkFileSize (e) {
      const b = 1024
      const mb = 1024 * 1024
      const g = 1024 * 1024 * 1024
      const size = e.fileSize
      if (size < b) {
        return (size / b).toFixed(1) + ' KB'
      }
      if (size >= b && size < g) {
        return (size / mb).toFixed(1) + ' MB'
      }
      if (size >= g) {
        return (size / g).toFixed(1) + ' G'
      }
    },
    checkSpeed (e) {
      let nu = e.speed.split('B/s')
      let size = Number(nu[0])
      const b = 1024
      const mb = 1024 * 1024
      if (size < b) {
        return (size / b).toFixed(1) + 'KB / s'
      }
      if (size >= b) {
        return (size / mb).toFixed(1) + ' MB / s'
      }
    },
    fileOption (e) {
      if (e.role === '1') {
        // upload file
        sessionStorage.setItem('uploadName', e.topicName)
        Bus.$emit('openUploader', {
          topicName: e.topicName,
          brokerId: localStorage.getItem('brokerId'),
          groupId: localStorage.getItem('groupId')
        })
      } else {
        // download file
        const url = '?brokerId=' + localStorage.getItem('brokerId') + '&groupId=' + localStorage.getItem('groupId') + '&topicName=' + e.topicName
        API.listFile(url).then(res => {
          if (res.data.status === 200) {
            this.downLoadList = [].concat(res.data.data)
          } else {
            this.$store.commit('set_Msg', this.$message({
              type: 'warning',
              message: this.$t('tableCont.getDataError'),
              duration: 0,
              showClose: true
            }))
          }
        })
        this.showDownList = true
      }
    },
    chooseFile () {
      let input = document.createElement('input')
      input.attributes.type = 'file'
      input.focus()
    },
    selectShow (e) {
      if (e && this.pageIndex !== 1) {
        this.pageIndex = 1
        this.getLsitData()
      }
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
          vm.listTopic = [].concat(res.data.topicInfoList)
        }
      })
    },
    dFile (e) {
      const url = con.ROOT + 'file/download?groupId=' + localStorage.getItem('groupId') + '&brokerId=' + localStorage.getItem('brokerId') + '&topicName=' + e.topic + '&fileId=' + e.fileId
      var xhr = new XMLHttpRequest()
      var formData = new FormData()
      xhr.open('get', url)
      xhr.setRequestHeader('Authorization', localStorage.getItem('token'))
      xhr.responseType = 'blob'
      xhr.onload = function (e) {
        if (this.status === 200) {
          let blob = this.response
          const filename = this.getResponseHeader('filename')
          if (window.navigator.msSaveOrOpenBlob) {
            navigator.msSaveBlob(blob, filename)
          } else {
            var a = document.createElement('a')
            var url = window.URL.createObjectURL(blob)
            a.href = url
            a.download = filename
            document.body.appendChild(a)
            a.click()
            window.URL.revokeObjectURL(url)
          }
        }
      }
      xhr.send(formData)
    }
  },
  mounted () {
    // if the data is exit so it means click form subscribtion list
    this.getData()
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
    dialogFormVisible (nVal) {
      if (!nVal) {
        this.form.name = ''
        this.form.roles = ''
        this.form.publicKey = ''
        this.form.privateKey = ''
        this.form.overwrite = '0'
        this.$refs.form.resetFields()
      }
    },
    pageIndex (nVal) {
      this.getLsitData()
    },
    groupId (nVal) {
      if (nVal !== '-1') {
      }
    }
  }
}
</script>
