<template>
  <el-container>
    <el-header>
      <headerBar></headerBar>
    </el-header>
  <el-container>
    <el-aside width='auto'>
      <sideBar :contraction='arrow' @selecChange='menuChange'></sideBar>
    </el-aside>
    <el-main>
      <el-breadcrumb separator-class="el-icon-arrow-right">
        <el-breadcrumb-item v-for='(item, index) in menu' :key='index'>{{item}}</el-breadcrumb-item>
      </el-breadcrumb>
      <router-view></router-view>
    </el-main>
  </el-container>
</el-container>
</template>
<script>
import headerBar from '../components/headerBar'
import sideBar from '../components/sideBar'
export default {
  components: {
    headerBar,
    sideBar
  },
  data () {
    return {
      arrow: true,
      input: ''
    }
  },
  methods: {
    menuChange (e) {
      switch (e) {
        case '1-1':
          this.$store.commit('set_menu', ['区块链信息', '数据概览'])
          this.$router.push('./index')
          break
        case '1-2':
          this.$store.commit('set_menu', ['区块链信息', '区块'])
          this.$router.push('./blockInfor')
          break
        case '1-3':
          this.$store.commit('set_menu', ['区块链信息', '节点列表'])
          this.$router.push('./group')
          break
        case '2-1':
          this.$store.commit('set_menu', ['主题管理', '主题列表'])
          this.$router.push('./topicList')
          break
        case '3':
          this.$store.commit('set_menu', ['订阅列表'])
          this.$router.push('./subcription')
          break
      }
    }
  },
  mounted () {
    let vm = this
    let H = document.body.clientWidth
    if (H < 740) {
      vm.arrow = false
    } else {
      vm.arrow = true
    }
    window.onresize = function () {
      let w = document.body.clientWidth
      if (w < 740) {
        vm.arrow = false
      } else {
        vm.arrow = true
      }
    }
  },
  created () {
    let url = this.$route.path
    switch (url) {
      case '/':
        this.$store.commit('set_active', '1-1')
        this.$store.commit('set_menu', ['区块链信息', '数据概览'])
        break
      case '/index':
        this.$store.commit('set_active', '1-1')
        this.$store.commit('set_menu', ['区块链信息', '数据概览'])
        break
      case '/blockInfor':
        this.$store.commit('set_active', '1-2')
        this.$store.commit('set_menu', ['区块链信息', '区块'])
        break
      case '/transactionInfor':
        this.$store.commit('set_active', '1-2')
        this.$store.commit('set_menu', ['区块链信息', '区块', '交易详情'])
        break
      case '/group':
        this.$store.commit('set_active', '1-3')
        this.$store.commit('set_menu', ['区块链信息', '节点列表'])
        break
      case '/topicList':
        this.$store.commit('set_active', '2-1')
        this.$store.commit('set_menu', ['主题管理', '主题列表'])
        break
      case '/subcription':
        this.$store.commit('set_active', '3')
        break
    }
  },
  computed: {
    menu () {
      return this.$store.state.menu
    }
  }
}
</script>
