<template>
  <div class="group event-table">
    <div class='refresh' >
      <div class='update_btn' @click='update'>
        <img src="../../assets/image/update.png" alt="">
      </div>
    </div>
    <el-table
      :data='tableData'
      stripe
      v-loading='loading'
      element-loading-spinner='el-icon-loading'
      element-loading-text='数据加载中...'
      element-loading-background='rgba(256,256,256,0.8)'
    >
      <el-table-column
        prop='nodeName'
        label='节点列表'
      ></el-table-column>
      <el-table-column
        prop='blockNumber'
        label='块高'
         width='300'
      ></el-table-column>
      <el-table-column
        prop='pbftView'
        label='pbftView'
         width='300'
      ></el-table-column>
      <el-table-column
        label='状态'
        width='100'
      >
        <template  slot-scope="scope">
          <span style='color:#67c23a' v-show='scope.row.nodeActive === 1'>
            <i class='dot dot_act'></i>
              运行
          </span>
          <span class='dot dot_act' style='color:#909399' v-show='scope.row.nodeActive === 0'>
            <i class='dot'></i> 停止
          </span>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      @current-change="indexChange"
      @size-change='sizeChange'
      :current-page="pageIndex"
      :page-sizes="[10, 20, 30, 50]"
      layout="sizes,total, prev, pager, next, jumper"
      :total="total">
    </el-pagination>
  </div>
</template>
<script>
import API from '../../API/resource.js'
export default {
  data () {
    return {
      loading: false,
      node_name: '',
      tableData: [],
      pageIndex: 1,
      pageSize: 10,
      total: 0
    }
  },
  methods: {
    indexChange (e) {
      this.pageIndex = e
      this.getNode()
    },
    sizeChange (e) {
      this.pageSize = e
      this.pageIndex = 1
      this.getNode()
    },
    update () {
      this.loading = true
      this.pageSize = 10
      this.pageIndex = 1
      setTimeout(fun => {
        this.getNode()
      }, 1000)
    },
    getNode () {
      this.loading = true
      let url = '/' + localStorage.getItem('groupId') + '/' + this.pageIndex + '/' + this.pageSize + '?brokerId=' + localStorage.getItem('brokerId')
      API.nodeList(url).then(res => {
        if (res.status === 200) {
          this.tableData = res.data.data
          this.total = res.data.totalCount
        } else {
          this.$message({
            type: 'warning',
            message: '数据请求出错'
          })
        }
      })
      this.loading = false
    }
  },
  mounted () {
    this.getNode()
  },
  computed: {
    brokerId () {
      return this.$store.state.brokerId
    }
  },
  watch: {
    brokerId () {
      this.update()
    }
  }
}
</script>
