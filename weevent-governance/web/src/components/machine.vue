<template>
<div class='event-table'>
  <div class='refresh'>
    <el-button type="primary" icon="el-icon-refresh" @click=getHost>刷新</el-button>
  </div>
  <el-table
    :data="tableData"
    border
    stripe
    v-loading='loading'
    element-loading-spinner='el-icon-loading'
    element-loading-text='数据加载中...'
    element-loading-background='rgba(256,256,256,0.8)'
    style="width: 100%">
    <el-table-column
      label="设备名称"
      prop="hostName">
    </el-table-column>
        <el-table-column
      label="时间"
      prop="time"
      :formatter="checkTime">
    </el-table-column>
        <el-table-column
      label="系统"
      prop="usageSystem">
    </el-table-column>
  </el-table>
 </div>
</template>
<script>
import API from '../API/resource.js'
import { getDateDetial } from '../utils/formatTime'
export default {
  data () {
    return {
      tableData: [],
      currentPage: 1,
      loading: false
    }
  },
  methods: {
    getHost () {
      let vm = this
      vm.loading = true
      API.getHost().then(res => {
        if (res.status === 200) {
          vm.tableData = res.data
        }
        vm.loading = false
      }).catch(e => {
        vm.loading = false
      })
    },
    checkTime (e) {
      const time = e.time
      return getDateDetial(time)
    }
  },
  mounted () {
    this.getHost()
  }
}
</script>
