<template>
  <div class="group event-table">
    <el-table
      :data='tableData'
      stripe
      v-loading='loading'
      element-loading-spinner='el-icon-loading'
      :element-loading-text="$t('common.loading')"
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
        :label="$t('tableCont.transHash')"
      >
      <template  slot-scope="scope">
        <i class='el-icon-copy-document' style='margin-right:5px;cursor:pointer' v-clipboard:copy='scope.row.transHash' v-clipboard:success='onCopy'></i>
        <a :title='scope.row.logMsg'>{{scope.row.transHash}}</a>
      </template>
      </el-table-column>
      <el-table-column
        prop='blockNumber'
        :label="$t('tableCont.blockNumber')"
        width=150
      ></el-table-column>

      <el-table-column
        prop='blockTimestamp'
        :label="$t('tableCont.timestamp')"
         width=300
      ></el-table-column>
    </el-table>
    <el-pagination
      @current-change="indexChange"
      :current-page="pageIndex"
      layout="total, prev, pager, next, jumper"
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
      total: 0
    }
  },
  methods: {
    indexChange (e) {
      this.pageIndex = e
      this.transList()
    },
    onCopy () {
      this.$message({
        type: 'success',
        message: this.$t('tableCont.copySuccess')
      })
    },
    detail (e) {
      // this.$alert(e, '错误信息')
    },
    transList () {
      this.loading = true
      let url = '/' + localStorage.getItem('groupId') + '/' + this.pageIndex + '/10?brokerId=' + localStorage.getItem('brokerId')
      if (sessionStorage.getItem('blockHash')) {
        url = url + '&transactionHash=' + sessionStorage.getItem('blockHash')
      }
      this.tableData = []
      this.total = 0
      API.transList(url).then(res => {
        if (res.data.code === 0) {
          let tableData = res.data.data.pageData
          tableData.forEach(e => {
            this.$set(e, 'logs', { 'address': '', 'topics': [], 'hasEvent': false })
          })
          this.tableData = tableData
          this.total = res.data.data.total
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
      this.$router.push('./blockInfor')
      this.$store.commit('set_active', '1-2')
      this.$store.commit('set_menu', [this.$t('sideBar.blockChainInfor'), this.$t('sideBar.transaction')])
    },
    groupId () {
      this.$router.push('./blockInfor')
      this.$store.commit('set_active', '1-2')
      this.$store.commit('set_menu', [this.$t('sideBar.blockChainInfor'), this.$t('sideBar.transaction')])
    }
  },
  destroyed () {
    sessionStorage.removeItem('blockHash')
  }
}
</script>
