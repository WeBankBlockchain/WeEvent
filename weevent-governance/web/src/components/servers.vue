<template>
  <div class="servers">
    <header-bar :noServer = true></header-bar>
    <div class='list_part'>
      <p class='title'>
        <span>服务管理</span>
        <i class='el-icon-plus' @click='showLog = true' ></i>
      </p>
      <ul class='block_data_list'>
        <li >
          <span>服务名称</span>
          <span >URL地址</span>
          <span>操作</span>
        </li>
        <li v-show='!server.length' style='justify-content:center'>暂无关联服务</li>
        <li v-for='(item, index) in server' :key='index'>
          <span :title='item.name'>{{item.name}}</span>
          <span :title='item.brokerUrl'>{{item.brokerUrl}}</span>
          <span>
            <i class='el-icon-edit' @click='adit(item)' title='编辑'></i>
            <i class='el-icon-delete' @click='deleteItem(item)' title='删除'></i>
            </span>
        </li>
      </ul>
    </div>
    <el-dialog :title="title" :visible.sync="showLog">
      <el-form :model="form" :rules="rules" ref='form'>
        <el-form-item label="名称:" prop='name'>
          <el-input v-model="form.name" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="Broker服务地址:" prop='brokerUrl'>
          <el-input v-model="form.brokerUrl" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="showLog = false">取 消</el-button>
        <el-button type="primary" @click="confirm">确 定</el-button>
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
    return {
      server: [],
      showLog: false,
      isEdit: false,
      title: '新增服务',
      form: {
        name: '',
        brokerUrl: ''
      },
      brokerId: '',
      rules: {
        name: [
          {required: true, message: 'IP不能为空', trigger: 'blur'}
        ],
        brokerUrl: [
          {required: true, message: '服务端口不能为空', trigger: 'blur'}
        ]
      }
    }
  },
  watch: {
    showLog (nVal) {
      if (!nVal) {
        this.title = '新增服务'
        this.$refs.form.resetFields()
        this.$set(this.form, 'name', '')
        this.$set(this.form, 'brokerUrl', '')
        this.brokerId = ''
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
      API.addServer(this.form).then(res => {
        if (res.status === 200) {
          this.$message({
            type: 'success',
            message: '新增成功'
          })
          this.getServer()
        } else {
          this.$message({
            type: 'warning',
            message: '新增失败'
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
      let data = {
        name: this.form.name,
        brokerUrl: this.form.brokerUrl,
        id: this.brokerId
      }
      API.updateServer(data).then(res => {
        if (res.status === 200) {
          this.$message({
            type: 'success',
            message: '编辑成功'
          })
          this.getServer()
        } else {
          this.$message({
            type: 'warning',
            message: '编辑失败'
          })
        }
        this.showLog = false
      })
    },
    adit (e) {
      this.title = '编辑服务信息'
      this.showLog = true
      this.isEdit = true
      this.$set(this.form, 'name', e.name)
      this.$set(this.form, 'brokerUrl', e.brokerUrl)
      this.brokerId = e.id
    },
    deleteItem (e) {
      this.$confirm('确认删除该服务？').then(_ => {
        API.deleteServer('/' + e.id).then(res => {
          if (res.status === 200) {
            this.$message({
              type: 'success',
              message: '删除成功'
            })
            this.getServer()
          } else {
            this.$message({
              type: 'warning',
              message: '删除成功'
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
