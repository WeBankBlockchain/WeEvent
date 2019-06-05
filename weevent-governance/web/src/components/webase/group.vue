<template>
  <div class="group event-table">
    <div class='refresh'>
      <el-button type="primary" icon="el-icon-refresh" @click='update'>刷新</el-button>
    </div>
    <el-table
      :data='tableData'
    >
      <el-table-column
        prop='nodeName'
        label='节点名称'
      ></el-table-column>
      <el-table-column
        prop='blockNumber'
        label='块高'
      ></el-table-column>
      <el-table-column
        prop='pbftView'
        label='pbftView'
      ></el-table-column>
      <el-table-column
        prop='nodeIp'
        label='IP'
      ></el-table-column>
      <el-table-column
        prop='p2pPort'
        label='p2p端口'
      ></el-table-column>
      <el-table-column
        label='状态'
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
      this.pageSize = 1
      this.pageIndex = 1
      this.getNode()
    },
    getNode () {
      let url = '/' + sessionStorage.getItem('groupId') + '/' + this.pageIndex + '/' + this.pageSize + '?brokerId=' + sessionStorage.getItem('brokerId')
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
    }
  },
  mounted () {
    this.getNode()
  }
}
</script>
