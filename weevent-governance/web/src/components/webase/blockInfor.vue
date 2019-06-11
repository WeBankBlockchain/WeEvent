<template>
  <div class="group  event-table">
    <div class='control_part'>
      <el-input placeholder="请输入交易哈希或块高" v-model='search_name'>
        <template slot='append'>
          <el-button type='primary' icon='el-icon-search' @click='search'></el-button>
        </template>
      </el-input>
    </div>
    <el-table
      :data='tableData'
    >
      <el-table-column
        prop='blockNumber'
        label='块高'
        width=150
      ></el-table-column>
      <el-table-column
        prop='transCount'
        label='交易'
        width=150
      ></el-table-column>
      <el-table-column
        label='区块哈希'
      >
      <template  slot-scope="scope">
        <i class='el-icon-copy-document' style='margin-right:5px;cursor:pointer' v-clipboard:copy='scope.row.pkHash' v-clipboard:success='onCopy'></i>
        <a :title='scope.row.logMsg' style='cursor:pointer' >{{scope.row.pkHash}}</a>
      </template>
      </el-table-column>
      <el-table-column
        prop='blockTimestamp'
        label='创建时间'
         width=300
      ></el-table-column>
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
      search_name: '',
      tableData: [],
      pageIndex: 1,
      pageSize: 10,
      total: 0
    }
  },
  methods: {
    indexChange (e) {
      this.pageIndex = e
      this.blockList()
    },
    sizeChange (e) {
      this.pageSize = e
      this.pageIndex = 1
      this.blockList()
    },
    onCopy () {
      this.$message({
        type: 'success',
        message: '复制成功'
      })
    },
    detial (e) {
      this.$alert(e, '错误信息')
    },
    blockList () {
      let url = '/' + sessionStorage.getItem('groupId') + '/' + this.pageIndex + '/' + this.pageSize + '?brokerId=' + sessionStorage.getItem('brokerId')
      if (this.search_name.length < 10 && this.search_name.length > 0) {
        url = url + '&blockNumber=' + this.search_name
      } else if (this.search_name.length >= 10) {
        url = url + '&pkHash=' + this.search_name
      }
      API.blockList(url).then(res => {
        if (res.status === 200) {
          this.tableData = res.data.data
          this.total = res.data.totalCount
        }
      })
    },
    search () {
      this.pageIndex = 1
      this.total = 0
      this.blockList()
    }
  },
  mounted () {
    this.blockList()
  }
}
</script>
