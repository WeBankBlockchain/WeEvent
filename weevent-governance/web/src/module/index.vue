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
      <!-- <div class='select-box' @click='arrow=!arrow'>
          <i class='arrow-icon' :class='{"arrow-right":!arrow}'></i>
      </div> -->
      <router-view></router-view>
    </el-main>
  </el-container>
</el-container>
</template>
<script>
import headerBar from '../components/headerBar'
import sideBar from '../components/sideBar'
export default{
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
          this.$router.push('./overview')
          break
        case '1-2':
          this.$store.commit('set_menu', ['区块链信息', '节点管理'])
          this.$router.push('./group')
          break
        case '2':
          this.$store.commit('set_menu', ['主题管理'])
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
    this.$router.push('./index')
  }
}
</script>
