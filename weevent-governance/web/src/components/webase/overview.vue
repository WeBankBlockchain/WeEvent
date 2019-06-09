<template>
  <div class='overview'>
    <div class='top_content'>
      <div class='num_block'>
        <div class="num_part">
          <span>节点个数</span><span>{{node}}</span>
        </div>
        <div class="num_part">
          <span>区块数量</span><span>{{block}}</span>
        </div>
        <div class="num_part">
          <span>交易数量</span><span>{{transaction}}</span>
        </div>
      </div>
     <chart></chart>
    </div>
    <div class='bottom_content'>
      <div class='block'>
        <p class='title'>
          <span>区块</span>
          <span class='more_date'>
            <router-link to='./blockInfor'>更多</router-link>
          </span>
        </p>
        <ul class='block_data_list'>
          <li  style='justify-content: center;color:#8993a2' v-show='!b_data.length'>暂无数据</li>
          <li v-for='(item, index) in b_data' :key='index'>
            <div>
              <p>块高: {{item.blockNumber}}</p>
              <p>{{item.blockTimestamp}}</p>
            </div>
            <div class='right_part'>
               <p :title='item.sealer'>
                出块者: {{item.sealer}}
              </p>
              <span>
                {{item.transCount}}txns
              </span>
            </div>
          </li>
        </ul>
      </div>
      <div class='transaction'>
        <p class='title'>
          <span>交易</span>
          <span class='more_date'>
            <router-link to='./transactionInfor'>更多</router-link>
          </span>
        </p>
        <ul class='transaction_data_list'>
          <li style='justify-content: center;color:#8993a2' v-show='!t_data.length'>暂无数据</li>
          <li v-for='(item, index) in t_data' :key='index'>
             <div>
              <p :title='item.transHash'>{{item.transHash}}</p>
              <p class='detial'>
                <span :title=item.transFrom>{{item.transFrom}}</span>
                <i class='el-icon-right'></i>
                <span :title=item.transTo>{{item.transTo}}</span>
              </p>
            </div>
            <span class='time'>
              {{item.blockTimestamp}}
            </span>
          </li>
        </ul>
      </div>
    </div>
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
      transaction: 0,
      b_data: [],
      t_data: []
    }
  },
  methods: {
    general () {
      let url = '/' + sessionStorage.getItem('groupId') + '?brokerId=' + sessionStorage.getItem('brokerId')
      API.general(url).then(res => {
        if (res.status === 200) {
          this.node = res.data.data.nodeCount
          this.block = res.data.data.latestBlock
          this.transaction = res.data.data.transactionCount
        }
      })
    },
    transList () {
      let url = '/' + sessionStorage.getItem('groupId') + '/1/6?brokerId=' + sessionStorage.getItem('brokerId')
      API.transList(url).then(res => {
        if (res.status === 200) {
          this.t_data = res.data.data
        }
      })
    },
    blockList () {
      let url = '/' + sessionStorage.getItem('groupId') + '/1/6?brokerId=' + sessionStorage.getItem('brokerId')
      API.blockList(url).then(res => {
        if (res.status === 200) {
          this.b_data = res.data.data
        }
      })
    }
  },
  mounted () {
    this.general()
    this.transList()
    this.blockList()
  }
}
</script>
