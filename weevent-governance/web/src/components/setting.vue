<template>
  <div class="setPart">
    <div class='setContent'>
      <h3>个人设置</h3>
      <div class='login_part'>
        <span>帐号:</span> <el-input v-model='name' readonly disabled></el-input>
        <span>密码:</span ><el-input v-model='passWord'></el-input>
        <span class='midify'>修改密码</span>
      </div>
    </div>
  </div>
</template>
<script>
// import API from '../API/resource.js'
export default {
  data () {
    return {
      name: 'xxx',
      passWord: 'xxxxx'
    }
  }
}
</script>
