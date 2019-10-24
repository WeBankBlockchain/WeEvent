<template>
  <div class="group event-table">
    <el-table
      :data='tableData'
      stripe
      v-loading='loading'
      element-loading-spinner='el-icon-loading'
      element-loading-text='数据加载中...'
      element-loading-background='rgba(256,256,256,0.8)'
      @expand-change='readDetail'
    >
      <el-table-column type='expand'>
        <template slot-scope='props'>
            <el-tabs type="border-card">
              <el-tab-pane label="input">
                <ul class='trans_detail'>
                  <li>
                    <span>Block Height:</span>
                    <span>{{props.row.blockNumber}}</span>
                  </li>
                  <li>
                    <span>From:</span>
                    <span>{{props.row.transFrom}}</span>
                  </li>
                  <li>
                    <span>To:</span>
                    <span>{{props.row.transTo ? props.row.transTo : '0x0000000000000000000000000000000000000000'}}</span>
                  </li>
                  <li>
                    <span>Timestamp:</span>
                    <span>{{props.row.blockTimestamp}}</span>
                  </li>
                </ul>
              </el-tab-pane>

              <el-tab-pane label="event" :disabled="!props.row.logs.hasEvent">
                <ul class='trans_detail'>
                  <li>
                    <span>Address:</span>
                    <span>{{props.row.logs.address}}</span>
                  </li>
                  <li>
                    <span>Topics:</span>
                    <div>
                      <p v-for="(item, index) in props.row.logs.topics" :key='index'>{{item}}</p>
                    </div>
                  </li>
                </ul>
              </el-tab-pane>
            </el-tabs>
        </template>
      </el-table-column>
      <el-table-column
        label='交易哈希'
      >
      <template  slot-scope="scope">
        <i class='el-icon-copy-document' style='margin-right:5px;cursor:pointer' v-clipboard:copy='scope.row.transHash' v-clipboard:success='onCopy'></i>
        <a :title='scope.row.logMsg'>{{scope.row.transHash}}</a>
      </template>
      </el-table-column>
      <el-table-column
        prop='blockNumber'
        label='块高'
        width=150
      ></el-table-column>

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
      this.transList()
    },
    sizeChange (e) {
      this.pageSize = e
      this.pageIndex = 1
      this.transList()
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
    transList () {
      this.loading = true
      let url = '/' + localStorage.getItem('groupId') + '/' + this.pageIndex + '/' + this.pageSize + '?brokerId=' + localStorage.getItem('brokerId')
      if (sessionStorage.getItem('blockHash')) {
        url = url + '&transactionHash=' + sessionStorage.getItem('blockHash')
      }
      API.transList(url).then(res => {
        if (res.status === 200) {
          let tableData = res.data.data
          tableData.forEach(e => {
            this.$set(e, 'logs', { 'address': '', 'topics': [], 'hasEvent': false })
          })
          this.tableData = tableData
          this.total = res.data.totalCount
        }
      })
      this.loading = false
    },
    readDetail (e) {
      let url = '/' + localStorage.getItem('groupId') + '/' + e.blockNumber + '?brokerId=' + localStorage.getItem('brokerId')
      let index = this.tableData.indexOf(e)
      API.blockByNumber(url).then(res => {
        if (res.status === 200) {
          let hash = res.data.data.transactions[0].hash
          this.getEvent(hash, index)
        }
      })
    },
    getEvent (e, i) {
      let url = '/' + localStorage.getItem('groupId') + '/' + e + '?brokerId=' + localStorage.getItem('brokerId')
      API.getEvent(url).then(res => {
        if (res.status === 200 && res.status.code === 0) {
          this.tableData[i].logs.address = res.data.data.logs[0].address
          this.tableData[i].logs.topics = [].concat(res.data.data.logs[0].topics)
          this.tableData[i].logs.hasEvent = true
        }
      })
    }
  },
  mounted () {
    this.transList()
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
        this.transList()
      }, 1000)
    },
    groupId () {
      this.loading = true
      setTimeout(fun => {
        this.transList()
      }, 1000)
    }
  },
  destroyed () {
    sessionStorage.removeItem('blockHash')
  }
}
</script>
