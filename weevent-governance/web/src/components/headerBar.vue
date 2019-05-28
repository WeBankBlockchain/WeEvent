<template>
<div class="headerBar">
  <div class='navigation'>
    <img src="../assets/image/weEvent.png" alt="" @click='home'>
    <el-breadcrumb separator="/" v-show='!noServer'>
      <el-breadcrumb-item >
        <el-dropdown trigger="click" @command='selectItem'>
          <span>{{server}}</span>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item v-for='(item, index) in servers' :key='index' :command='index'>{{item.name}}</el-dropdown-item>
            <el-dropdown-item command='servers'>服务设置</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </el-breadcrumb-item>
      <el-breadcrumb-item>{{menu}}</el-breadcrumb-item>
    </el-breadcrumb>
  </div>
  <div class='right_part'>
    <img src="../assets/image/backhome.svg" v-show='noServer' class='back_home' @click='home' title='返回首页'/>
    <!-- <el-dropdown trigger="click" @command='selectItem'>
      <span v-if='!userName' @click='loginIn'>请登录</span>
      <span class="el-dropdown-link" v-else-if='userName'>
        {{userName}}<i class="el-icon-arrow-down el-icon-caret-bottom"></i>
      </span>
      <el-dropdown-menu slot="dropdown">
        <el-dropdown-item command='setting'>设置</el-dropdown-item>
        <el-dropdown-item command='loginOut'>退出</el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown> -->
  </div>
</div>
</template>
<script>
import API from '../API/resource.js'
export default{
  props: {
    noServer: Boolean
  },
  data () {
    return {
      activeName: 'WeEvent',
      userName: 'xxxx',
      server: '',
      servers: [],
      group: []
    }
  },
  mounted () {
    this.getServer()
  },
  methods: {
    home () {
      this.$router.push('./index')
    },
    selectItem (e) {
      if (e === 'servers') {
        this.$router.push('./servers')
      } else {
        this.server = this.servers[e].name
        sessionStorage.setItem('userId', this.servers[e].id)
      }
    },
    getServer () {
      API.getServer('').then(res => {
        if (res.status === 200) {
          this.servers = [].concat(res.data)
          this.server = res.data[0].name
          let id = res.data[0].id
          sessionStorage.setItem('userId', id)
          sessionStorage.setItem('groupId', 1)
          // this.getAll(res.data[0].id)
        }
      })
    },
    getAll (e) {
      let url = '/brokerId?' + e
      API.getAll(url).then(res => {
        if (res.status === 200) {
          this.group = [].concat(res.data)
          sessionStorage.setItem('groupId', res.data[0].id)
        }
      })
    }
  },
  computed: {
    menu () {
      return this.$store.state.menu
    }
  }
}
</script>
<style scoped>
  img{
    width:155px;
  }
  .user{
    font-size: 14px;
    display: inline-block;
    margin-right:5px;
    cursor: pointer;
  }

</style>
