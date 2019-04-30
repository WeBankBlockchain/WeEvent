<template>
<div class='event-table'>
  <div class='refresh'>
    <el-button type="primary" icon="el-icon-refresh" @click='blockchaininfo'>刷新</el-button>
  </div>
  <el-table
    :data="tableData"
    border
    stripe
    :span-method='spanMethod'
    v-loading='loading'
    element-loading-spinner='el-icon-loading'
    element-loading-text='数据加载中...'
    element-loading-background='rgba(256,256,256,0.8)'
    style="width: 100%">
    <el-table-column
      label="ip地址"
      >
        <template  slot-scope="scope">
          <a >{{scope.row.ip}}</a>
        </template>
    </el-table-column>
        <el-table-column
      label="端口"
      prop="host">
    </el-table-column>
    <el-table-column
      label="id名称">
      <template  slot-scope="scope">
        <a class='id-scope' :title="scope.row.id">{{scope.row.id}}</a>
      </template>
    </el-table-column>
    <el-table-column
      label="块高"
      prop="blockNumber">
    </el-table-column>
  </el-table>
 </div>
</template>
<script>
import API from '../API/resource.js'
export default {
  data () {
    return {
      loading: false,
      tableData: [],
      currentPage: 1
    }
  },
  methods: {
    blockchaininfo () {
      let vm = this
      vm.tableData = []
      vm.loading = true
      API.blockchaininfo().then(res => {
        if (res.status === 200) {
          let ipList = res.data.nodeIpList
          let idList = res.data.nodeIdList
          let blockNumber = res.data.blockNumber
          for (let i = 0; i < idList.length; i++) {
            let data = {
              ip: '',
              host: '',
              id: '',
              blockNumber: blockNumber
            }
            let IP = ipList[i]
            IP = IP.substring(1, IP.length - 1).split(':')
            let ID = idList[i]
            data.ip = IP[0]
            data.host = IP[1]
            data.id = ID
            vm.tableData.push(data)
          }
        }
        vm.loading = false
      }).catch(e => {
        vm.loading = false
      })
    },
    spanMethod ({row, cloumn, rowIndex, columnIndex}) {
      if (columnIndex === 3) {
        return [this.tableData.length, 1]
      }
    }
  },
  mounted () {
    this.blockchaininfo()
  }
}
</script>
