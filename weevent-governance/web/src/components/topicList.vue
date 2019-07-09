<template>
<div class='event-table'>
  <div class='refresh'>
    <el-button type='primary' icon='el-icon-plus' @click='addNewOne'>新增</el-button>
    <div class='update_btn' @click='refresh'>
      <img src="../assets/image/update.png" alt=""/>
    </div>
  </div>
  <el-table
    :data="tableData"
    stripe
    v-loading='loading'
    element-loading-spinner='el-icon-loading'
    element-loading-text='数据加载中...'
    element-loading-background='rgba(256,256,256,0.8)'
    style="width: 100%"
    @expand-change='readDetial'
    >
    <el-table-column type="expand">
      <template slot-scope="props">
        <el-form label-position="left" inline class="demo-table-expand">
          <el-form-item label="Topic:">
            <span>{{ props.row.detial.topicName }}</span>
          </el-form-item><br/>
          <el-form-item label="创建时间:">
            <span>{{ props.row.detial.createdTimestamp }}</span>
          </el-form-item><br/>
          <el-form-item label="地址:">
            <span>{{ props.row.detial.topicAddress }}</span>
          </el-form-item><br/>
           <el-form-item label="已发布事件数:">
            <span>{{ props.row.detial.sequenceNumber }}</span>
          </el-form-item><br/>
           <el-form-item label="最新事件块高:">
            <span>{{ props.row.detial.blockNumber }}</span>
          </el-form-item>
        </el-form>
      </template>
    </el-table-column>
    <el-table-column
      label="Topic"
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
  <el-dialog title="新增 Topic" :visible.sync="dialogFormVisible" center width='450px' >
    <el-form :model="form" :rules="rules" ref='form'>
      <el-form-item label="名称:" prop='name'>
        <el-input v-model.trim.trim="form.name" autocomplete="off"></el-input>
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
          {min: 1, max: 64, message: '名称长度不能超过 64 个字符', trigger: 'blur'}
        ]
      },
      creater: ''
    }
  },
  methods: {
    // 数据获取
    getLsitData () {
      let vm = this
      vm.loading = true
      let data = {
        pageIndex: vm.pageIndex - 1,
        pageSize: vm.pageSize,
        brokerId: Number(localStorage.getItem('brokerId'))
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
      var vm = this
      let url = '?brokerId=' + localStorage.getItem('brokerId') + '&topic=' + e.topicName
      API.topicState(url).then(res => {
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
            creater: localStorage.getItem('user'),
            brokerId: Number(localStorage.getItem('brokerId'))
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
    }
  },
  mounted () {
    this.getLsitData()
  },
  computed: {
    brokerId () {
      return this.$store.state.brokerId
    }
  },
  watch: {
    dialogFormVisible (nVal) {
      if (!nVal) {
        this.form.name = ''
        this.$refs.form.resetFields()
      }
    },
    brokerId () {
      this.refresh()
    }
  }
}

</script>
