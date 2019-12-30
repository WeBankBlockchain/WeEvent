<template>
  <div class="servers">
    <div class='top_part'>
       <header-bar :noServer = true></header-bar>
    </div>
    <div class='cont_part'>
      <P class='back' @click='back'><i class='el-icon-arrow-left'></i>{{$t('common.back')}}</P>
      <div class='ruleStatic'>
        <div class='rule_total_cont'>
          <div class='staticBox'>
            <span class='staticTitle'>{{$t('ruleStatic.systemRules')}}</span>
          </div>
        </div>
        <el-table
            :data="ruleStatic"
            style="width: 100%">
            <el-table-column
              :label="$t('ruleStatic.ruleName')"
              prop='ruleName'
              >
            </el-table-column>
            <el-table-column
              :label="$t('ruleStatic.hitTimes')"
              prop='hitTimes'
              >
            </el-table-column>
            <el-table-column
              :label="$t('ruleStatic.notHitTimes')"
              prop='notHitTimes'
              >
            </el-table-column>
            <el-table-column
              :label="$t('ruleStatic.successTimes')"
              prop='dataFlowSuccess'
              >
            </el-table-column>
            <el-table-column
              :label="$t('ruleStatic.failTimes')"
              prop='dataFlowFail'
              >
            </el-table-column>
            <el-table-column
              :label="$t('ruleStatic.destinationType')"
              >
              <template slot-scope='scope'>
                {{scope.row.destinationType === 1 ? 'Topic' : 'DB'}}
              </template>
            </el-table-column>
            <el-table-column
              :label="$t('ruleStatic.startTime')"
              :formatter="timeFormatter"
              width='180'
              >
            </el-table-column>
            <el-table-column
              :label="$t('ruleStatic.readFailRecord')"
              >
              <template slot-scope='scope'>
                <el-popover
                  placement="top-start"
                  trigger="hover"
                  v-show="scope.row.lastFailReason"
                  :content="scope.row.lastFailReason">
                    <span slot="reference" >
                      <i class='el-icon-s-order'></i>
                    </span>
                </el-popover>
                <span v-show="!scope.row.lastFailReason">
                   —
                </span>
              </template>
            </el-table-column>
          </el-table>
      </div>
    </div>
  </div>
</template>
<script>
import API from '../API/resource.js'
import headerBar from '../components/headerBar'

export default {
  components: {
    headerBar
  },
  data () {
    return {
      ruleStatic: []
    }
  },
  mounted () {
    let str = '?idList=' + this.$route.query.list
    let vm = this
    API.ruleStatic(str).then(res => {
      if (res.data.errorCode === 0) {
        vm.ruleStatic = []
        let list = res.data.data.statisticRuleMap
        for (let key in list) {
          vm.ruleStatic.push(list[key])
        }
      }
    })
  },
  methods: {
    back () {
      this.$router.go(-1)
    },
    timeFormatter (e) {
      let time = e.startTime
      let str = time.split('T')
      let ymd = str[0]
      let hms = str[1].split('.')
      return ymd + ' ' + hms[0]
    }
  }
}
</script>
