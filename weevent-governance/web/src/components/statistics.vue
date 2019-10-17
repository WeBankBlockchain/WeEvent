<template>
  <div class='statistics'>
    <div class='dataContent'>
      <div class='selectOptions'>
        <span class='optionTitle'>选择主题</span>
        <el-select v-model="topic" multiple placeholder="请选择" @visible-change="selectChange" collapse-tags size="small">
          <el-option
            v-for="(item, index) in topicList"
            :key="index"
            :label="item.vaule"
            :value="item.name"
            >
          </el-option>
        </el-select>
        <span class='optionTitle' style='margin-left:20px'>选择时间</span>
        <el-date-picker
          size="small"
          type="daterange"
          value-format="timestamp"
          v-model='selectTime'
          @change="getTime"
          :picker-options="pickerOptions"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期">
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
// import API from '../API/resource.js'
export default{
  data () {
    return {
      pickerOptions: {
        disabledDate (time) {
          return time.getTime() > Date.now()
        }
      },
      topicList: [{
        'name': 'topic1',
        'value': '1'
      }, {
        'name': 'topic2',
        'value': '2'
      }, {
        'name': 'topic3',
        'value': '1'
      }, {
        'name': 'topic4',
        'value': '4'
      }],
      topic: ['topic1', 'topic2', 'topic3'],
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
          categories: getLastWeek()
        },
        tooltip: {
          shared: true,
          crosshairs: true
        },
        series: [],
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
    newData () {
      let vm = this
      let time = vm.option.xAxis.categories
      let items = vm.topic
      let start = 0
      vm.option.series = []
      for (let x = 0; x < items.length; x++) {
        let oneItem = {
          'name': items[x],
          'data': []
        }
        vm.option.series.push(oneItem)
        start++
        let data = start
        for (let y = 0; y < time.length; y++) {
          vm.option.series[x].data[y] = data
          data++
        }
      }
      Highcharts.chart('chart', vm.option)
    },
    getTime (e) {
      let timeList = getTimeList(e[0], e[1])
      let vm = this
      vm.option.xAxis.categories = [].concat(timeList)
      this.newData()
    },
    getDate () {
      let data = getLastWeek()
      let start = new Date(data[0]).getTime()
      let end = new Date(data[data.length - 1]).getTime()
      this.selectTime.push(start)
      this.selectTime.push(end)
    },
    selectChange (e) {
      if (!e) {
        this.newData()
      }
    }
  },
  mounted () {
    this.getDate()
    this.newData()
  }
}
</script>
