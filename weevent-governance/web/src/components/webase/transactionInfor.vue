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
       @expand-change='readDetial'
    >
      <el-table-column type='expand'>
        <template slot-scope='props'>
            <el-tabs type="border-card">
              <el-tab-pane label="input">
                <ul class='trans_detial'>
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

              <el-tab-pane :label="props.row.logs.hasEvent?'event':''">
                <ul class='trans_detial'>
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
        <a :title='scope.row.logMsg' style='cursor:pointer' >{{scope.row.transHash}}</a>
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
    detial (e) {
      this.$alert(e, '错误信息')
    },
    transList () {
      let url = '/' + sessionStorage.getItem('groupId') + '/' + this.pageIndex + '/' + this.pageSize + '?brokerId=' + sessionStorage.getItem('userId')
      if (this.search_name.length < 10 && this.search_name.length > 0) {
        url = url + '&blockNumber=' + this.search_name
      } else if (this.search_name.length >= 10) {
        url = url + '&pkHash=' + this.search_name
      }
      API.transList(url).then(res => {
        if (res.status === 200) {
          let tableData = res.data.data
          tableData.forEach(e => {
            this.$set(e, 'logs', {'address': '', 'topics': [], 'hasEvent': false})
          })
          this.tableData = tableData
          this.total = res.data.totalCount
        }
      })
    },
    readDetial (e) {
      let url = '/' + sessionStorage.getItem('groupId') + '/' + e.blockNumber + '?brokerId=' + sessionStorage.getItem('userId')
      let index = this.tableData.indexOf(e)
      API.blockByNumber(url).then(res => {
        if (res.status === 200) {
          let hash = res.data.data.transactions[0].hash
          this.getEvent(hash, index)
        }
      })
    },
    getEvent (e, i) {
      let url = '/' + sessionStorage.getItem('groupId') + '/' + e + '?brokerId=' + sessionStorage.getItem('userId')
      API.getEvent(url).then(res => {
        if (res.status === 200 && res.status.code === 0) {
          this.tableData[i].logs.address = res.data.data.logs[0].address
          this.tableData[i].logs.topics = [].concat(res.data.data.logs[0].topics)
          this.tableData[i].logs.hasEvent = true
        }
      })
    },
    search () {
      this.pageIndex = 1
      this.total = 0
      this.transList()
    }
  },
  mounted () {
    this.transList()
  }
}
</script>
