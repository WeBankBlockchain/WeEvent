<template>
  <div id="app">
    <router-view/>
    <!-- <global-uploader></global-uploader> -->
  </div>
</template>
<script>
// import globalUploader from './components/tool/globalUploader.vue'
export default {
  name: 'App',
  components: {
    // globalUploader
  },
  beforeCreate () {
    if (self === top) {
      let antiCLickjack = document.getElementById('app')
      antiCLickjack.parentNode.removeChild(antiCLickjack)
    } else {
      top.location = self.location
    }
    if (top.location !== location) {
      parent.location = self.location
    }
  },
  watch: {
    isLogin (nVal) {
      if (nVal) {
        this.$router.push('./login')
      }
    }
  },
  computed: {
    isLogin () {
      return this.$store.state.goLogin
    }
  }
}
</script>
<style lang="less">
@import './assets/less/index';
</style>
