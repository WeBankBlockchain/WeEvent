<template>
  <el-menu class="el-menu-vertical" :collapse="!contraction" @select="menuSelect" default-active="5-1">
    <el-menu-item index="1">
      <i class="el-icon-menu"></i>
      <span slot="title">节点管理</span>
    </el-menu-item>
    <el-submenu index='2'>
      <template slot='title'>
        <i class='el-icon-news'></i>
        <span>区块链信息</span>
      </template>
      <el-menu-item index='2-1'>
        数据概览
      </el-menu-item>
    </el-submenu>
    <el-menu-item index="3">
      <i class="el-icon-edit-outline"></i>
      <span slot="title">主题管理</span>
    </el-menu-item>
    <el-menu-item index="4">
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
      this.$emit('selecChange', e)
    }
  }
}
</script>
