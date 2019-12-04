<template>
<div class='event-table subcription'>
  <span>{{$t('tableCont.machine') + ":"}}</span>
  <el-select  @change='selectShow' v-model='nodes' multiple collapse-tags>
    <el-option  v-for='(item, index) in nodeList' :key='index' :label="item" :value="item"></el-option>
  </el-select>
  <el-table
    :data="tableData"
    :span-method='spanMethod'
    v-loading='loading'
    element-loading-spinner='el-icon-loading'
    :element-loading-text="$t('common.loading')"
    element-loading-background='rgba(256,256,256,0.8)'
    style="width: 100%">
    <el-table-column
      :label="$t('tableCont.machine')"
      width='150'
      prop='ip'
      >
    </el-table-column>
     <el-table-column
      label="Topic"
      width='150'
      prop='topicName'>
    </el-table-column>
    <el-table-column
      :label="$t('tableCont.subscribeId')"
      prop='subscribeId'>
      <template  slot-scope="scope">
        <a :title='scope.row.subscribeId' style='cursor:pointer;color:#006cff;text-overflow: ellipsis;white-space: nowrap' @click='checkDetail(scope.row)'>{{scope.row.subscribeId}}</a>
      </template>
    </el-table-column>
    <el-table-column
     width='120'
      :label="$t('tableCont.remoteIp')"
      prop="remoteIp">
    </el-table-column>
    <el-table-column
    width='120'
      :label="$t('tableCont.interfaceType')"
      prop="interfaceType">
    </el-table-column>
    <el-table-column
     width='120'
      :label="$t('tableCont.notifiedEventCount')"
      prop="notifiedEventCount">
    </el-table-column>
    <el-table-column
     width='120'
      :label="$t('tableCont.notifyingEventCount')"
      prop="notifyingEventCount">
    </el-table-column>
     <el-table-column
      width='150'
      :label="$t('tableCont.createTimeStamp')"
      prop="createTimeStamp">
    </el-table-column>
  </el-table>
 </div>
</template>
<script>
import API from '../API/resource.js'
import { setTimeout } from 'timers'
export default {
  data () {
    return {
      loading: false,
      nodeList: [],
      nodes: [],
      tableData: [],
      currentPage: 1,
      getData: {}
    }
  },
  methods: {
    getNodeList () {
      let url = '?brokerId=' + localStorage.getItem('brokerId')
      API.getNodes(url).then(res => {
        if (res.data.code === 0) {
          this.nodeList = [].concat(res.data.data)
          if (this.nodeList.length > 0) {
            this.nodes = []
            this.nodes.push(this.nodeList[0])
            this.subscription()
          }
        } else {
          this.$message({
            type: 'warning',
            message: this.$t('common.reqException')
          })
        }
      })
    },
    subscription () {
      let vm = this
      vm.tableData = []
      vm.loading = true
      let nodelist = ''
      nodelist = vm.nodes.join(',')
      let url = '?brokerId=' + localStorage.getItem('brokerId') + '&groupId=' + localStorage.getItem('groupId') + '&nodeIp=' + nodelist
      API.subscription(url).then(res => {
        if (res.data.code === 0) {
          let data = res.data.data
          let list = []
          for (let key in data) {
            let cont = data[key]
            if (JSON.stringify(cont) !== '{}') {
              // check if it is empty
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
                  'childs': 0,
                  'remoteIp': '—',
                  'createTimeStamp': '—'
                }
                list.push(item)
              }
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
    spanMethod ({ row, cloumn, rowIndex, columnIndex }) {
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
    },
    update () {
      this.loading = true
      setTimeout(fun => {
        this.subscription()
      }, 1000)
    },
    checkDetail (e) {
      if (e.topicName === '#') {
        sessionStorage.removeItem('topic')
      } else {
        sessionStorage.setItem('topic', e.topicName)
      }
      this.$store.commit('set_active', '2-1')
      this.$emit('selecChange', '2-1')
      this.$store.commit('set_menu', [this.$t('sideBar.topic'), this.$t('sideBar.topicList')])
      this.$router.push('./topicList')
    },
    selectShow (e) {
      if (e.length === 0) {
        this.nodes = []
      }
      this.loading = true
      setTimeout(fun => {
        this.subscription()
      }, 1000)
    }
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
      this.getNodeList()
      this.nodes = []
      this.subscription()
    },
    groupId () {
      this.getNodeList()
      this.nodes = []
      this.subscription()
    }
  },
  mounted () {
    this.getNodeList()
  },
  beforeDestroy () {
    window.clearInterval(this.getData)
  }
}
</script>
