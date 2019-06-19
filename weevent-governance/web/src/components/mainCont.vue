<template>
  <div class='home-content'>
    <img src="../assets/image/quote.svg" class='quote' alt="">
    <img src="../assets/image/Plant.png" class='plant' alt="">
    <pre class='top_cont'>
  Welcome to
  WeEvent Governance
    </pre>
    <div class='bottom_cont'>
      <div class='left_cont'>
        <pre>
  WeEvent Governance是WeEvent服务的一部分,
  为用户提供一个事件治理的Web管理端。
  支持区块链信息查看，事件治理、系统监控等。</pre>
        <p>更多WeEvent信息，请参见<a target='_blank' href="https://github.com/WeBankFinTech/WeEvent">github源码</a></p>
      </div>
      <div class='right_cont'>
        <p>
          <span>
            <i class='el-icon-minus'></i>区块链节信息:
          </span>
          <span>
            可以查看机构、节点、区块以及交易详情；查看区块链所有节点的当前状态。
          </span>
        </p>
        <p>
          <span>
            <i class='el-icon-minus'></i>事件治理:
          </span>
          <span>
            管理事件主题，查看事件详情和各种统计信息。
          </span>
        </p>
        <p>
          <span>
            <i class='el-icon-minus'></i>系统监控:
          </span>
          <span>
            监控系统接口调用情况，在系统异常情况下触发各种告警。
          </span>
        </p>
      </div>
    </div>
  </div>
</template>
<script>
export default {
  mounted () {
    this.$store.commit('set_menu', ['首页'])
    this.$store.commit('set_active', '0')
  }
}
</script>
