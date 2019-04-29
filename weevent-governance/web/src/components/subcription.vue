<template>
<div class='event-table'>
  <div class='refresh'>
    <el-button type="primary" icon="el-icon-refresh" @click='subscription'>刷新</el-button>
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
      label="机器地址"
      prop='ip'
      >
    </el-table-column>
     <el-table-column
      label="Topic名称"
      prop='topicName'>
    </el-table-column>
    <el-table-column
      label="订阅ID"
      width='400'
      prop='subscribeId'>
    </el-table-column>
    <el-table-column
      label="订阅方式"
      prop="interfaceType">
    </el-table-column>
    <el-table-column
      label="已通知事件"
      prop="notifiedEventCount">
    </el-table-column>
    <el-table-column
      label="待通知事件"
      prop="notifyingEventCount">
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
      currentPage: 1,
      getData: {}
    }
  },
  methods: {
    subscription () {
      let vm = this
      vm.tableData = []
      vm.loading = true
      API.subscription().then(res => {
        if (res.status === 200) {
          let data = res.data
          let list = []
          for (let key in data) {
            let cont = data[key]
            // 判断是否是一个空对象
            let arr = Object.keys(cont)
            if (arr.length) {
              for (let x in cont) {
                vm.$set(cont[x], 'ip', key)
                vm.$set(cont[x], 'childs', arr.length)
                list.push(cont[x])
              }
            } else {
              const item = {
                'ip': key,
                'interfaceType': '—',
                'notifyingEventCount': '—',
                'notifyTimeStamp': '—',
                'subscribeId': '—',
                'topicName': '—',
                'notifiedEventCount': '—',
                'childs': 0
              }
              list.push(item)
            }
          }
          vm.tableData = [].concat(list)
        } else {
          window.clearInterval(vm.getData)
        }
        vm.loading = false
      }).catch(e => {
        vm.loading = false
        window.clearInterval(vm.getData)
      })
    },
    spanMethod ({row, cloumn, rowIndex, columnIndex}) {
      let table = this.tableData
      if (columnIndex === 0) {
        // 先判断是第一行否存在并行 (根据row.childs个数确定该IP下有多少个topic)
        if (rowIndex === 0) {
          if (row.childs === 0) {
            return [1, 1]
          } else {
            return [row.childs, 1]
          }
        } else {
          // // 非第一行的情况下判断上一行row.childs是否大于2
          // // 如果小于则代表没有合并行
          if (row.childs < 2) {
            return [1, 1]
          }
          // 根据IP判断 该行是否需要与上一行合并，如果相同则需要与合并，如果不同则是新起的行
          if (table[rowIndex - 1].ip !== table[rowIndex].ip) {
            return [row.childs, 1]
          } else {
            return [0, 0]
          }
        }
      }
    }
  },
  mounted () {
    let time = 60 * 1000
    this.subscription()
    this.getData = setInterval(this.subscription, time)
  },
  beforeDestroy () {
    window.clearInterval(this.getData)
  }
}
</script>
