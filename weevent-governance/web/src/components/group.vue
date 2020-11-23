<template>
  <div class="group event-table">
    <el-table
      :data='tableData'
      stripe
      v-loading='loading'
      element-loading-spinner='el-icon-loading'
      :element-loading-text="$t('common.loading')"
      element-loading-background='rgba(256,256,256,0.8)'
    >
      <el-table-column
        :label="$t('tableCont.nodeName')"
      >
        <template  slot-scope="scope">
          <span :title='scope.row.nodeId'>
            {{scope.row.nodeId}}
          </span>
        </template>
      </el-table-column>
       <el-table-column
        prop='nodeType'
        :label="$t('tableCont.nodeType')"
         width='100'
      >
         <template  slot-scope="scope">
          <span v-show="scope.row.nodeType === 'sealer'">
            {{$t('tableCont.sealer')}}
          </span>
          <span v-show="scope.row.nodeType === 'observer'">
            {{$t('tableCont.observer')}}
          </span>
        </template>
      </el-table-column>
      <el-table-column
        prop='blockNumber'
        :label="$t('tableCont.blockNumber')"
         width='100'
      ></el-table-column>
      <el-table-column
        prop='pbftView'
        label='pbftView'
         width='120'
      ></el-table-column>
      <el-table-column
      :label="$t('tableCont.state')"
        width='100'
      >
        <template  slot-scope="scope">
          <span style='color:#67c23a' v-show='scope.row.nodeActive === 1'>
            <i class='dot dot_act'></i>
              {{$t('tableCont.run')}}
          </span>
          <span style='color:#909399' v-show='scope.row.nodeActive === 0'>
            <i class='dot'></i> {{$t('tableCont.stop')}}
          </span>
        </template>
      </el-table-column>
    </el-table>
    <!-- <el-pagination
      @current-change="indexChange"
      @size-change='sizeChange'
      :current-page="pageIndex"
      :page-sizes="[10, 20, 30, 50]"
      layout="sizes,total, prev, pager, next, jumper"
      :total="total">
    </el-pagination> -->
  </div>
</template>
<script>
import API from '../API/resource.js'
export default {
  data () {
    return {
      loading: false,
      tableData: []
    }
  },
  methods: {
    update () {
      this.loading = true
      setTimeout(fun => {
        this.getNode()
      }, 1000)
    },
    getNode () {
      this.loading = true
      const url = '?groupId=' + localStorage.getItem('groupId') + '&pageNumber=1&pageSize=10&brokerId=' + localStorage.getItem('brokerId')
      API.nodeList(url).then(res => {
        if (res.status === 200 && res.data.code === 0) {
          this.tableData = res.data.data.pageData
          // this.total = res.data.totalCount
        } else {
          this.$store.commit('set_Msg', this.$message({
            type: 'warning',
            message: res.data.message,
            duration: 0,
            showClose: true
          }))
        }
      })
      this.loading = false
    }
  },
  mounted () {
    this.getNode()
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
    groupId (nVal) {
      if (String(nVal) !== '-1') {
        this.update()
      }
    }
  }
}
</script>
