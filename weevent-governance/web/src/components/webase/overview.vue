<template>
  <div class='overview'>
    <div class='top_content'>
      <div class='num_block'>
        <div class="num_part">
          <div @click='toGroup'>
            <p class='title_name'>{{node}}</p>
            <p class='number'>{{$t('overview.nodeNum')}}</p>
          </div>
          <img src="../../assets/image/overview/banner-icon1.svg" alt="">
        </div>
        <div class="num_part">
          <div @click='toBlock'>
            <p class='title_name'>{{block}}</p>
            <p class='number'>{{$t('overview.blockNum')}}</p>
          </div>
          <img src="../../assets/image/overview/banner-icon2.svg" alt="">
        </div>
        <div class="num_part">
          <div  @click='toBlock'>
            <p class='title_name'>{{transaction}}</p>
            <p class='number'>{{$t('overview.transactionNum')}}</p>
          </div>
          <img src="../../assets/image/overview/banner-icon3.svg" alt="">
        </div>
      </div>
    </div>
    <chart></chart>
  </div>
</template>
<script>
import API from '../../API/resource.js'
import chart from './chart.vue'
export default {
  components: {
    chart
  },
  data () {
    return {
      node: 0,
      block: 0,
      transaction: 0
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
        this.general()
      }
    },
    groupId () {
      this.general()
    }
  },
  mounted () {
    setTimeout(fun => {
      if (localStorage.getItem('brokerId')) {
        this.general()
      }
    }, 500)
  },
  methods: {
    general () {
      let url = '/' + localStorage.getItem('groupId') + '?brokerId=' + localStorage.getItem('brokerId')
      API.general(url).then(res => {
        if (res.data.code === 0) {
          this.node = res.data.data.nodeCount
          this.block = res.data.data.latestBlock
          this.transaction = res.data.data.transactionCount
        }
      })
    },
    toGroup () {
      this.$store.commit('set_active', '1-3')
      this.$store.commit('set_menu', [this.$t('sideBar.blockChainInfor'), this.$t('sideBar.nodeList')])
      this.$router.push('./group')
    },
    toBlock () {
      this.$store.commit('set_active', '1-2')
      this.$store.commit('set_menu', [this.$t('sideBar.blockChainInfor'), this.$t('sideBar.transaction')])
      this.$router.push('./blockInfor')
    }
  }
}
</script>
