<template>
  <div class="group block event-table">
    <el-table
      :data='tableData'
      stripe
      v-loading='loading'
      element-loading-spinner='el-icon-loading'
      element-loading-text='数据加载中...'
      element-loading-background='rgba(256,256,256,0.8)'
    >
      <el-table-column
        prop='blockNumber'
        label='块高'
        width=150
      ></el-table-column>
      <el-table-column
        prop='transCount'
        label='交易'
        width=100
      ></el-table-column>
      <el-table-column
        prop='blockTimestamp'
        label='创建时间'
        width=230
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
        label='操作'
        width=200
      >
      <template  slot-scope="scope">
        <span class='link_option' @click="linkToTrans(scope.row.pkHash)">查看交易详情</span>
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
    detail (e) {
      this.$alert(e, '错误信息')
    },
    blockList () {
      this.loading = true
      let url = '/' + localStorage.getItem('groupId') + '/' + this.pageIndex + '/' + this.pageSize + '?brokerId=' + localStorage.getItem('brokerId')
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
      this.loading = false
    },
    linkToTrans (e) {
      sessionStorage.setItem('blockHash', e)
      this.$store.commit('set_active', '1-2')
      this.$store.commit('set_menu', ['区块链信息', '区块', '交易详情'])
      this.$router.push('./transactionInfor')
    },
    search () {
      this.pageIndex = 1
      this.total = 0
      this.blockList()
    }
  },
  mounted () {
    this.blockList()
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
    brokerId () {
      this.loading = true
      setTimeout(fun => {
        this.blockList()
      }, 1000)
    },
    groupId (nVal) {
      this.loading = true
      setTimeout(fun => {
        this.blockList()
      }, 1000)
    }
  }
}
</script>
