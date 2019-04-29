<template>
  <div id="app">
    <router-view/>
  </div>
</template>
<script>
export default {
  name: 'App',
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
  }
}
</script>
<style lang="less">
@import './assets/less/index';
</style>
