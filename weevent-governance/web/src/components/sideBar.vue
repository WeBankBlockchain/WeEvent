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
        节点列表
      </el-menu-item>
    </el-submenu>
    <el-menu-item index="2">
      <i class="el-icon-edit-outline"></i>
      <span slot="title">主题管理</span>
    </el-menu-item>
    <el-menu-item index="3">
      <i class="el-icon-document"></i>
      <span slot="title">订阅列表</span>
    </el-menu-item>
  </el-menu>
</template>
<script>
export default{
  props: {
    contraction: Boolean
  },
  methods: {
    menuSelect (e) {
      this.$store.commit('set_active', e)
      this.$emit('selecChange', e)
    }
  },
  computed: {
    active () {
      return this.$store.state.active
    }
  }
}
</script>
