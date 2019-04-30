<template>
  <el-menu class="el-menu-vertical" :collapse="!contraction" @select="menuSelect">
    <!-- <el-menu-item index="1">
      <i class="el-icon-setting"></i>
      <span slot="title">机器列表</span>
    </el-menu-item> -->
    <el-menu-item index="2">
      <i class="el-icon-menu"></i>
      <span slot="title">当前节点</span>
    </el-menu-item>
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
