<template>
  <div class='statistics'>
    <div class='dataContent'>
      <div class='selectOptions'>
        <span class='optionTitle'>{{$t('tableCont.chooseTopic')}}</span>
        <el-select v-model="topic" multiple :placeholder="$t('common.choose')" @visible-change="selectChange" collapse-tags size="small">
          <el-option
            v-for="(item, index) in topicList"
            :key="index"
            :label="item"
            :value="item"
            >
          </el-option>
        </el-select>
        <span class='optionTitle' style='margin-left:20px'>{{$t('tableCont.chooseTime')}}</span>
        <el-date-picker
          size="small"
          type="daterange"
          value-format="timestamp"
          v-model='selectTime'
          @change="getTime"
          :picker-options="pickerOptions"
          range-separator="-"
          :start-placeholder="$t('tableCont.beginTime')"
          :end-placeholder="$t('tableCont.endTime')">
        </el-date-picker>
      </div>
      <div class='statisticsCharts'>
        <div class='chart' id='chart'></div>
      </div>
    </div>
  </div>
</template>
<script>
import Highcharts from 'highcharts/highstock'
import { getLastWeek, getTimeList } from '../utils/formatTime'
import API from '../API/resource.js'
require('highcharts/modules/no-data-to-display.js')(Highcharts)
export default{
  data () {
    return {
      pickerOptions: {
        disabledDate (time) {
          return time.getTime() > Date.now()
        }
      },
      topicList: [],
      topic: [],
      selectTime: [],
      option: {
        title: {
          text: ''
        },
        yAxis: {
          title: '',
          max: '10',
          lineWidth: 2
        },
        xAxis: {
          categories: []
        },
        tooltip: {
          shared: true,
          crosshairs: true
        },
        series: [],
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
          align: 'center',
          verticalAlign: 'top',
          y: 20
        },
        credits: {
          enabled: false
        }
      }
    }
  },
  methods: {
    beginDate (e) {
      let vm = this
      let data = {
        'beginDate': vm.selectTime[0],
        'endDate': vm.selectTime[1],
        'topicList': vm.topic,
        'groupId': localStorage.getItem('groupId'),
        'brokerId': localStorage.getItem('brokerId'),
        'userId': localStorage.getItem('userId')
      }
      API.historicalData(data).then(res => {
        if (res.data.status === 200) {
          let resData = res.data.data
          if (!resData || resData.length === 0) {
            Highcharts.chart('chart', vm.option).showNoData()
            return
          }
          let topic = []
          vm.option.series = []
          if (e) {
            vm.topicList = []
          }
          for (var key in resData) {
            if (e) {
              vm.topicList.push(key)
            }
            topic.push(key)
            let item = {
              'name': key,
              'data': resData[key]
            }
            vm.option.series.push(item)
          }
          let max = 10
          vm.option.series.forEach(x => {
            x.data.forEach(y => {
              max = y > max ? y : max
            })
          })
          vm.option.yAxis.max = max
          vm.topic = [].concat(topic)
          Highcharts.chart('chart', vm.option)
        } else {
          vm.$message({
            type: 'warning',
            message: this.$t('tableCont.getDataError')
          })
          Highcharts.chart('chart', vm.option).showNoData()
        }
      })
    },
    getTime (e) {
      let vm = this
      if (!e) {
        vm.getDate()
      } else {
        let timeList = getTimeList(e[0], e[1])
        vm.option.xAxis.categories = [].concat(timeList)
      }
      this.beginDate(true)
    },
    getDate () {
      let data = getLastWeek()
      let start = new Date(data[0]).getTime()
      let end = new Date(data[data.length - 1]).getTime()
      this.selectTime.push(start)
      this.selectTime.push(end)
      this.option.xAxis.categories = [].concat(data)
      this.beginDate(true)
    },
    selectChange (e) {
      if (!e) {
        this.beginDate(false)
      }
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
    brokerId () {
      this.beginDate(true)
    },
    groupId () {
      this.beginDate(true)
    },
    lang () {
      if (this.option.series.length === 0) {
        this.option.lang.noData = this.$t('common.noData')
        Highcharts.chart('chart', this.option).showNoData()
      } else {
        this.option.lang.noData = this.$t('common.noData')
        Highcharts.chart('chart', this.option)
      }
    }
  },
  mounted () {
    this.getDate()
  }
}
</script>
