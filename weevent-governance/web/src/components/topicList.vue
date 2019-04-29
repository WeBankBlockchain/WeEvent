<template>
<div class='event-table'>
  <div class='refresh'>
    <el-button type='primary' @click='addNewOne'>新增</el-button>
    <el-button type='primary' @click='installTopic'>部署Topic Control合约</el-button>
    <el-button type="primary" icon="el-icon-refresh" @click='refresh'>刷新</el-button>
  </div>
  <el-table
    :data="tableData"
    border
    stripe
    v-loading='loading'
    element-loading-spinner='el-icon-loading'
    element-loading-text='数据加载中...'
    element-loading-background='rgba(256,256,256,0.8)'
    style="width: 100%"
    height="620"
    @expand-change='readDetial'
    >
    <el-table-column type="expand">
      <template slot-scope="props">
        <el-form label-position="left" inline class="demo-table-expand">
          <el-form-item label="名称:">
            <span>{{ props.row.detial.topicName }}</span>
          </el-form-item><br/>
          <el-form-item label="创建时间:">
            <span>{{ props.row.detial.createdTimestamp }}</span>
          </el-form-item><br/>
          <el-form-item label="地址:">
            <span>{{ props.row.detial.topicAddress }}</span>
          </el-form-item>
        </el-form>
      </template>
    </el-table-column>
    <el-table-column
      label="名称"
      prop="topicName"
      :formatter="checkName">
    </el-table-column>
    <el-table-column
      label="创建人"
      prop="creater"
      :formatter="checkCreater">
    </el-table-column>
     <el-table-column
      label="创建时间"
      prop="createdTimestamp"
      :formatter="checkTime">
    </el-table-column>
  </el-table>
  <el-pagination
    @current-change="indexChange"
    @size-change='sizeChange'
    :current-page="pageIndex"
    :page-sizes="[10, 20, 50]"
    layout="sizes,total, prev, pager, next, jumper"
    :total="total">
  </el-pagination>
  <el-dialog title="新增 topic" :visible.sync="dialogFormVisible" center width='450px' >
    <el-form :model="form" :rules="rules" ref='form'>
      <el-form-item label="名称:" prop='name'>
        <el-input v-model.trim="form.name" autocomplete="off"></el-input>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click='addTopic(form)'>确 定</el-button>
      <el-button @click="dialogFormVisible = false">取 消</el-button>
    </div>
  </el-dialog>
 </div>
</template>
<script>
import API from '../API/resource.js'
import { getDateDetial } from '../utils/formatTime'
export default {
  data () {
    return {
      loading: false,
      dialogFormVisible: false,
      tableData: [],
      pageIndex: 1,
      pageSize: 10,
      total: 0,
      form: {
        name: ''
      },
      rules: {
        name: [
          { required: true, message: '名称不能为空', trigger: 'blur' },
          {min: 1, max: 32, message: '名称长度不能超过 32 个字符', trigger: 'blur'}
        ]
      },
      creater: ''
    }
  },
  watch: {
    dialogFormVisible (nVal) {
      if (!nVal) {
        this.form.name = ''
        this.$refs.form.resetFields()
      }
    }
  },
  methods: {
    // 数据获取
    getLsitData () {
      let vm = this
      vm.loading = true
      let data = {
        pageIndex: vm.pageIndex - 1,
        pageSize: vm.pageSize
      }
      API.topicList(data).then(res => {
        if (res.status === 200) {
          vm.total = res.data.total
          let listData = res.data.topicInfoList
          let det = {
            'topicName': '',
            'createdTimestamp': '',
            'topicAddress': ''
          }
          listData.forEach(item => {
            vm.$set(item, 'detial', det)
          })
          vm.tableData = [].concat(listData)
        }
        vm.loading = false
      }).catch(e => {
        vm.loading = false
      })
    },
    refresh () {
      this.loading = true
      setTimeout(fun => {
        this.getLsitData()
      }, 1000)
    },
    readDetial (e) {
      let data = {
        topic: e.topicName
      }
      var vm = this
      API.topicState(data).then(res => {
        let time = getDateDetial(res.data.createdTimestamp)
        res.data.createdTimestamp = time
        vm.$set(e, 'detial', res.data)
      })
    },
    indexChange (e) {
      this.pageIndex = e
      this.getLsitData()
    },
    sizeChange (e) {
      this.pageSize = e
      this.getLsitData()
    },
    // 格式校验
    checkName (e) {
      if (!e.topicName) {
        return '—'
      } else {
        return e.topicName
      }
    },
    checkCreater (e) {
      if (!e.creater || (e.creater === 'unknow')) {
        return '—'
      } else {
        return e.creater
      }
    },
    checkTime (e) {
      const time = e.createdTimestamp
      return getDateDetial(time)
    },
    addNewOne () {
      this.dialogFormVisible = true
    },
    addTopic (form) {
      let vm = this
      vm.$refs.form.validate((valid) => {
        if (valid) {
          let data = {
            topic: vm.form.name,
            // creater: this.$store.state.userName
            creater: 'unknow'
          }
          API.openTopic(data).then(res => {
            if (res.status === 200) {
              if (res.data.code && (res.data.code === 100106)) {
                vm.$message({
                  type: 'error',
                  message: 'topic名称格式错误'
                })
              } else if (res.data.code === 100) {
                vm.$message({
                  type: 'error',
                  message: '新增失败'
                })
              } else {
                vm.$message({
                  type: 'success',
                  message: '添加成功'
                })
                vm.refresh()
              }
            } else {
              vm.$message({
                type: 'error',
                message: '操作失败'
              })
            }
            vm.dialogFormVisible = false
          }).catch(e => {
            vm.$message({
              type: 'error',
              message: '操作失败'
            })
          })
          vm.dialogFormVisible = false
        } else {
          return false
        }
      })
    },
    installTopic () {
      const loading = this.$loading({
        lock: true,
        text: 'loading',
        spinner: 'el-icon-loading',
        background: 'rgba(0,0,0,0.7)'
      })
      let vm = this
      API.topicControl().then(res => {
        if (res.status === 200) {
          this.$message({
            type: 'success',
            duration: 3000,
            dangerouslyUseHTMLString: true,
            message: '<span>合约地址生成成功</span> <a id="xx" style="margin-left:30px;color:blue;cursor: pointer">复制</a>'
          })
          let xx = document.getElementById('xx')
          xx.onclick = function () {
            vm.$copyText(res.data).then(e => {
              vm.$notify({
                title: '提示',
                message: '合约地址复制成功',
                type: 'success'
              })
            }, e => {
              vm.$notify({
                title: '提示',
                message: '合约地址复制失败，请稍候重试',
                type: 'warning'
              })
            })
          }
        } else {
          this.$message({
            type: 'error',
            message: '操作失败'
          })
        }
        loading.close()
      }).catch(e => {
        loading.close()
      })
    }
  },
  mounted () {
    this.getLsitData()
  }
}

</script>
