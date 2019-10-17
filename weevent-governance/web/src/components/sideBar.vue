<template>
  <el-menu class="el-menu-vertical" :collapse="!contraction" @select="menuSelect" :default-active="active">
    <el-submenu index='1'>
      <template slot='title'>
        <i class='el-icon-share'></i>
        <span>区块链信息</span>
      </template>
      <el-menu-item index='1-1'>
        数据概览
      </el-menu-item>
      <el-menu-item index='1-2'>
        交易详情
      </el-menu-item>
      <el-menu-item index='1-3'>
        节点列表
      </el-menu-item>
    </el-submenu>
    <el-submenu index='2'>
      <template slot='title'>
        <i class="el-icon-edit-outline"></i>
        <span slot="title">主题管理</span>
      </template>
      <el-menu-item index='2-1'>
        主题列表
      </el-menu-item>
      <el-menu-item index='2-2'>
        事件统计
      </el-menu-item>
    </el-submenu>
    <el-menu-item index="3">
      <i class="el-icon-document"></i>
      <span slot="title">订阅列表</span>
    </el-menu-item>
    <!-- <el-submenu index="4" v-show="isConfigRule === '1'"> -->
    <el-submenu index="4">
      <template slot='title'>
        <i class="el-icon-s-tools"></i>
        <span slot="title">规则引擎</span>
      </template>
      <el-menu-item index='4-1'>
        规则管理
      </el-menu-item>
      <el-menu-item index='4-2'>
        数据源设置
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
