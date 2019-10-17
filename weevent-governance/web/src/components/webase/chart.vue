<template>
  <div class='chart' id='chart'></div>
</template>
<script>
import Highcharts from 'highcharts/highstock'
import { getLastWeek } from '../../utils/formatTime'
import API from '../../API/resource.js'
export default {
  data () {
    return {
      option: {
        title: {
          text: '关键监控指标',
          align: 'left'
        },
        subtitle: {
          text: '最近一周的交易量(笔)',
          align: 'right',
          floating: true,
          verticalAlign: 'top'
        },
        yAxis: {
          title: '',
          max: '10',
          gridLineDashStyle: 'Dot'
        },
        xAxis: {
          categories: getLastWeek()
        },
        tooltip: {
          shared: true,
          crosshairs: true,
          formatter () {
            return this.x + '<br/>交易量:' + parseInt(this.y.toFixed(2)) + '笔'
          }
        },
        series: [{
          name: '',
          data: [0, 0, 0, 0, 0, 0, 0]
        }],
        legend: {
          enabled: false
        },
        credits: {
          enabled: false
        }
      }
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
      if (localStorage.getItem('groupId')) {
        this.transDaily()
      }
    },
    groupId () {
      this.transDaily()
    }
  },
  mounted () {
    setTimeout(fun => {
      if (localStorage.getItem('groupId') && localStorage.getItem('brokerId')) {
        this.transDaily()
      }
    }, 500)
  },
  methods: {
    transDaily () {
      let vm = this
      let url = '/' + localStorage.getItem('groupId') + '?brokerId=' + localStorage.getItem('brokerId')
      API.transDaily(url).then(res => {
        if (res.status === 200) {
          let chatData = res.data.data
          let dateList = vm.option.xAxis.categories
          let max = vm.option.yAxis.max
          chatData.forEach(e => {
            if (dateList.indexOf(e.day) !== -1) {
              let index = dateList.indexOf(e.day)
              vm.option.series[0].data[index] = e.transCount
              max = max > e.transCount ? max : e.transCount
            }
          })
          vm.$set(vm.option.yAxis, 'max', max)
        }
        Highcharts.chart('chart', vm.option)
      }).catch(e => {
        Highcharts.chart('chart', vm.option)
      })
    }
  }
}
</script>
