<template>
<div class='event-table topic'>
  <div class='refresh'>
    <el-button type='primary' size='small' icon='el-icon-plus' @click='showlog = !showlog'>新增</el-button>
  </div>
  <el-table
    :data="tableData"
    v-loading='loading'
    element-loading-spinner='el-icon-loading'
    element-loading-text='数据加载中...'
    element-loading-background='rgba(256,256,256,0.8)'
    style="width: 100%"
    >
    <el-table-column
      label="数据库地址"
      prop="databaseUrl">
    </el-table-column>
    <el-table-column
      label="操作"
      width='170'>
      <template  slot-scope="scope">
        <a @click='update(scope.row)' style="cursor: pointer;margin-right:10px">编辑</a>
        <a @click='deleteItem(scope.row)' style="cursor: pointer">删除</a>
      </template>
    </el-table-column>
  </el-table>
  <el-dialog :title="title" :visible.sync="showlog" center width='450px' >
    <el-form :model="form" :rules="rules" ref='form'>
      <el-form-item label="数据库地址:" prop='url'>
        <el-input v-model.trim="form.url" type='textarea' :rows='4' autocomplete="off" placeholder="例如:jdbc:mysql://127.0.0.1:3306/governance?root=root&password=123456&useUnicode=true&characterEncoding=utf-8&useSSL=false&tableName=tableName "></el-input>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click='addURL'>确 定</el-button>
      <el-button @click="showlog = false">取 消</el-button>
    </div>
  </el-dialog>
 </div>
</template>
<script>
import API from '../API/resource.js'
export default {
  data () {
    return {
      topicName: '',
      loading: false,
      showlog: false,
      tableData: [],
      id: '',
      type: 1,
      title: '新增地址',
      form: {
        url: ''
      },
      rules: {
        url: [
          { required: true, message: '请填写DB地址', trigger: 'blur' }
        ]
      }
    }
  },
  watch: {
    showlog (nVal) {
      if (!nVal) {
        this.form.url = ''
        this.type = 1
        this.title = '新增地址'
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
          'userId': localStorage.getItem('userId')
        }
        if (valid) {
          if (vm.type === 1) {
            data.brokerId = localStorage.getItem('brokerId')
            API.dbAdd(data).then(res => {
              if (res.data.status === 200) {
                vm.$message({
                  type: 'success',
                  message: '添加成功'
                })
                vm.getDBLsit()
              } else {
                vm.$message({
                  type: 'warning',
                  message: '操作失败'
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
                  message: '编辑成功'
                })
                vm.getDBLsit()
              } else {
                vm.$message({
                  type: 'warning',
                  message: '操作失败'
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
      this.form.url = e.databaseUrl
      this.title = '修改地址'
      this.type = 2
    },
    deleteItem (e) {
      let vm = this
      vm.$confirm('确认删除？', '删除该地址', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
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
              message: '删除成功'
            })
            vm.getDBLsit()
          } else {
            vm.$message({
              type: 'warning',
              message: '操作失败'
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
