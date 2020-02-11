<template>
  <div class='statisticsCharts'>
    <span class='optionTitle' style='margin-left:20px'>{{$t('tableCont.chooseTime')}}</span>
    <el-date-picker
        size="small"
        type="daterange"
        value-format="timestamp"
        v-model='selectTime'
        @change="getTime"
        :clearable='false'
        :picker-options="pickerOptions"
        range-separator="-"
        :start-placeholder="$t('tableCont.beginTime')"
        :end-placeholder="$t('tableCont.endTime')">
      </el-date-picker>
    <div class='chart' id='chart'></div>
  </div>
</template>
<script>
import Highcharts from 'highcharts/highstock'
import { getLastWeek, getTimeList } from '../../utils/formatTime'
import API from '../../API/resource.js'
require('highcharts/modules/no-data-to-display.js')(Highcharts)
export default{
  data () {
    return {
      pickerOptions: {
        disabledDate (time) {
          return time.getTime() > Date.now()
        }
      },
      topic: [],
      selectTime: [],
      option: {
        title: {
          text: this.$t('overview.essential'),
          align: 'left'
        },
        subtitle: {
          text: this.$t('overview.lastWeek'),
          align: 'right',
          verticalAlign: 'top'
        },
        yAxis: {
          title: '',
          max: '20',
          lineWidth: 2,
          labels: {
            step: 2
          }
        },
        xAxis: {
          categories: []
        },
        series: [{
          name: '',
          data: []
        }],
        lang: {
          noData: this.$t('common.noData')
        },
        noData: {
          style: {
            fontWeight: 'bold',
            fontSize: '15px',
            color: '#303030'
          }
        },
        legend: {
          enabled: false
        },
        credits: {
          enabled: false
        }
      }
    }
  },
  methods: {
    beginDate () {
      let vm = this
      let data = {
        'beginDate': vm.selectTime[0],
        'endDate': vm.selectTime[1],
        'groupId': localStorage.getItem('groupId'),
        'brokerId': localStorage.getItem('brokerId')
      }
      vm.option.series[0].data = []
      let chart = new Promise(function (resolve, reject) {
        API.eventList(data).then(res => {
          if (res.data.status === 200) {
            let max = 20
            let data = res.data.data
            let time = [].concat(vm.option.xAxis.categories)
            for (let i = 0; i < time.length; i++) {
              vm.option.series[0].data[i] = 0
              for (let x = 0; x < data.length; x++) {
                if (data[x].createDateStr === time[i]) {
                  vm.option.series[0].data[i] = data[x].eventCount
                  max = max < data[x].eventCount ? data[x].eventCount : max
                }
              }
            }
            vm.$set(vm.option.yAxis, 'max', max)
            resolve(true)
          } else {
            let max = 20
            let data = []
            let time = [].concat(vm.option.xAxis.categories)
            for (let i = 0; i < time.length; i++) {
              vm.option.series[0].data[i] = 0
              for (let x = 0; x < data.length; x++) {
                if (data[x].createDateStr === time[i]) {
                  vm.option.series[0].data[i] = data[x].eventCount
                  max = max < data[x].eventCount ? data[x].eventCount : max
                }
              }
            }
            vm.$set(vm.option.yAxis, 'max', max)
            reject(res.data)
          }
        })
      })
      chart.then(e => {
        Highcharts.setOptions({
          lang: {
            thousandsSep: ','
          }
        })
        Highcharts.chart('chart', vm.option)
      }).catch(e => {
        Highcharts.setOptions({
          lang: {
            thousandsSep: ','
          }
        })
        Highcharts.chart('chart', vm.option)
        this.$message({
          type: 'warning',
          message: e.msg,
          duration: 5000
        })
      })
    },
    getTime (e) {
      let vm = this
      if (e === null) {
        vm.getDate()
      } else {
        let timeList = getTimeList(e[0], e[1])
        vm.option.xAxis.categories = [].concat(timeList)
      }
      this.beginDate()
    },
    getDate () {
      let data = getLastWeek()
      let start = new Date(data[0]).getTime()
      let end = new Date(data[data.length - 1]).getTime()
      let vm = this
      vm.selectTime = []
      vm.selectTime.push(start)
      vm.selectTime.push(end)
      vm.option.xAxis.categories = [].concat(data)
      vm.beginDate()
    }
  },
  computed: {
    brokerId () {
      return this.$store.state.brokerId
    },
    groupId () {
      return this.$store.state.groupId
    },
    lang () {
      return this.$store.state.lang
    }
  },
  watch: {
    groupId (nVal) {
      if (nVal !== '-1') {
        this.getDate()
      }
    },
    lang (nVal) {
      this.option.lang.noData = this.$t('common.noData')
      this.option.title.text = this.$t('overview.essential')
      this.option.subtitle.text = this.$t('overview.lastWeek')
      if (this.option.series.length === 0) {
        Highcharts.chart('chart', this.option).showNoData()
      } else {
        Highcharts.chart('chart', this.option)
      }
    }
  },
  mounted () {
    this.getDate()
  }
}
</script>
