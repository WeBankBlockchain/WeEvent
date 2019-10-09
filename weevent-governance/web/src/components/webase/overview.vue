<template>
  <div class='overview'>
    <div class='top_content'>
      <div class='num_block'>
        <div class="num_part">
          <div @click='toGroup'>
            <p class='title_name'>{{node}}</p>
            <p class='number'>节点个数</p>
          </div>
          <img src="../../assets/image/overview/banner-icon1.svg" alt="">
        </div>
        <div class="num_part">
          <div @click='toBlock'>
            <p class='title_name'>{{block}}</p>
            <p class='number'>区块数量</p>
          </div>
          <img src="../../assets/image/overview/banner-icon2.svg" alt="">
        </div>
        <div class="num_part">
          <div @click='toTrans'>
            <p class='title_name'>{{transaction}}</p>
            <p class='number'>交易数量</p>
          </div>
          <img src="../../assets/image/overview/banner-icon3.svg" alt="">
        </div>
      </div>
    </div>
    <chart></chart>
  </div>
</template>
<script>
import chart from './chart.vue'
import API from '../../API/resource.js'
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
      this.general()
    },
    groupId (nVal) {
      this.general()
    }
  },
  methods: {
    general () {
      let url = '/' + localStorage.getItem('groupId') + '?brokerId=' + localStorage.getItem('brokerId')
      API.general(url).then(res => {
        if (res.status === 200) {
          this.node = res.data.data.nodeCount
          this.block = res.data.data.latestBlock
          this.transaction = res.data.data.transactionCount
        }
      })
    },
    toGroup () {
      this.$store.commit('set_active', '1-3')
      this.$store.commit('set_menu', ['区块链信息', '节点列表'])
      this.$router.push('./group')
    },
    toBlock () {
      this.$store.commit('set_active', '1-2')
      this.$store.commit('set_menu', ['区块链信息', '区块'])
      this.$router.push('./blockInfor')
    },
    toTrans () {
      this.$store.commit('set_active', '1-2')
      this.$store.commit('set_menu', ['区块链信息', '区块', '交易详情'])
      this.$router.push('./transactionInfor')
    }
  },
  mounted () {
    this.general()
  }
}
</script>
