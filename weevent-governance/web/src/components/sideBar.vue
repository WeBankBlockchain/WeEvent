<template>
  <el-menu class="el-menu-vertical" :collapse="!contraction" @select="menuSelect" :default-active="active">
    <el-submenu index='1'>
      <template slot='title'>
        <i class='el-icon-share'></i>
        <span>{{$t('sideBar.blockChainInfor')}}</span>
      </template>
      <el-menu-item index='1-1'>
        {{$t('sideBar.overview')}}
      </el-menu-item>
      <el-menu-item index='1-2'>
        {{$t('sideBar.transaction')}}
      </el-menu-item>
      <el-menu-item index='1-3'>
        {{$t('sideBar.nodeList')}}
      </el-menu-item>
    </el-submenu>
    <el-submenu index='2'>
      <template slot='title'>
        <i class="el-icon-edit-outline"></i>
        <span slot="title">{{$t('sideBar.topic')}}</span>
      </template>
      <el-menu-item index='2-1'>
        {{$t('sideBar.topicList')}}
      </el-menu-item>
      <el-menu-item index='2-2'>
        {{$t('sideBar.subcription')}}
      </el-menu-item>
      <el-menu-item index='2-3'>
        {{$t('sideBar.statistics')}}
      </el-menu-item>
    </el-submenu>
    <el-submenu index="4">
      <template slot='title'>
        <i class="el-icon-s-tools"></i>
        <span slot="title">{{$t('sideBar.engine')}}</span>
      </template>
      <el-menu-item index='4-1'>
        {{$t('sideBar.ruleMana')}}
      </el-menu-item>
      <el-menu-item index='4-2'>
        {{$t('sideBar.sources')}}
      </el-menu-item>
    </el-submenu>
  </el-menu>
</template>
<script>
export default {
  props: {
    contraction: Boolean
  },
  methods: {
    menuSelect (e) {
      let active = this.$store.state.active
      if (e !== active) {
        this.$store.commit('set_active', e)
        this.$emit('selecChange', e)
      }
      if (e === '1-2') {
        let url = this.$route.path
        if (url === '/transactionInfor') {
          this.$store.commit('set_active', e)
          this.$emit('selecChange', e)
        }
      }
      if (e === '4-1') {
        let url = this.$route.path
        if (url === '/ruleDetail') {
          this.$store.commit('set_active', e)
          this.$emit('selecChange', e)
        }
      }
    }
  },
  computed: {
    active () {
      return this.$store.state.active
    },
    isConfigRule () {
      return this.$store.state.isConfigRule
    }
  }
}
</script>
