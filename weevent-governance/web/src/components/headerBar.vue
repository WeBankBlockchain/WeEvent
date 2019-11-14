<template>
<div class="headerBar">
  <div class='navigation'>
    <img src="../assets/image/weEvent.png" alt="" @click='home'>
    <span class='server_title' v-show='!noServer'>{{$t('header.broker')}} :</span>
    <el-dropdown trigger="click" @command='selecServers'  v-show='!noServer'>
      <span>{{server}} <i class="el-icon-arrow-down el-icon-caret-bottom"></i></span>
      <el-dropdown-menu slot="dropdown">
        <el-dropdown-item v-for='(item, index) in servers' :key='index' :command='index'>{{item.name}}</el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>
    <span class='server_title' v-show='!noServer'>{{$t('header.group')}} :</span>
    <el-dropdown trigger="click" @command='selectGroup'  v-show='!noServer'>
      <span>{{groupId}} <i class="el-icon-arrow-down el-icon-caret-bottom"></i></span>
      <el-dropdown-menu slot="dropdown">
        <el-dropdown-item v-for='(item, index) in groupList' :key='index' :command='item'>{{item}}</el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>
  </div>
  <div class='right_part'>
    <el-dropdown trigger="click" @command='selectLang'>
      <span>{{$t('header.lang')}}<i class="el-icon-arrow-down el-icon-caret-bottom"></i></span>
      <el-dropdown-menu slot="dropdown">
        <el-dropdown-item command='zh'>中文</el-dropdown-item>
        <el-dropdown-item command='en'>English</el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>
    <span class='el-icon-user-solid' style='margin:0 5px 0 15px'></span>
    <el-dropdown trigger="click" @command='selectItem'>
      <span v-if='!userName' @click='loginIn'>{{$t('header.login')}}</span>
      <span class="el-dropdown-link" v-else-if='userName'>
        {{userName}}<i class="el-icon-arrow-down el-icon-caret-bottom"></i>
      </span>
      <el-dropdown-menu slot="dropdown">
        <el-dropdown-item command='server_setting'>{{$t('header.serverSetting')}}</el-dropdown-item>
        <el-dropdown-item command='user_setting'>{{$t('header.userSetting')}}</el-dropdown-item>
        <el-dropdown-item command='loginOut'>{{$t('header.loginOut')}}</el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>
  </div>
</div>
</template>
<script>
import API from '../API/resource.js'
export default {
  props: {
    noServer: Boolean
  },
  data () {
    return {
      activeName: 'WeEvent',
      userName: localStorage.getItem('user'),
      server: '',
      servers: [],
      groupList: [],
      version: {
        buildTimeStamp: '',
        gitBranch: '',
        gitCommitHash: '',
        weEventVersion: ''
      }
    }
  },
  mounted () {
    this.getServer()
  },
  methods: {
    home () {
      this.$router.push('./index')
      this.$store.commit('set_active', '1-1')
      this.$store.commit('set_menu', [this.$t('sideBar.blockChainInfor'), this.$t('sideBar.overview')])
      this.$emit('selecChange', '1-1')
    },
    loginIn () {
      this.$router.push('./login')
    },
    selectItem (e) {
      switch (e) {
        case 'user_setting':
          this.$router.push({ path: './registered', query: { reset: 0 } })
          break
        case 'server_setting':
          this.$router.push('./servers')
          break
        case 'loginOut':
          API.loginOut('').then(res => {
            if (res.status === 200 && res.data.code === 0) {
              localStorage.removeItem('user')
              localStorage.removeItem('userId')
              localStorage.removeItem('groupId')
              localStorage.removeItem('brokerId')
              this.$router.push('./login')
            }
          })
          break
      }
    },
    selecServers (e) {
      this.server = this.servers[e].name
      this.$store.commit('set_id', this.servers[e].id)
      this.$store.commit('setConfigRule', this.servers[e].isConfigRule)
      localStorage.setItem('brokerId', this.servers[e].id)
    },
    selectGroup (e) {
      this.$store.commit('set_groupId', e)
      localStorage.setItem('groupId', e)
    },
    getServer () {
      let brokerId = localStorage.getItem('brokerId')
      let url = '?userId=' + localStorage.getItem('userId')
      let vm = this
      API.getServer(url).then(res => {
        if (res.status === 200) {
          if (res.data.length) {
            vm.servers = [].concat(res.data)
            if (brokerId) {
              res.data.forEach(e => {
                if (e.id === Number(brokerId)) {
                  vm.server = e.name
                  let id = e.id
                  vm.$store.commit('set_id', id)
                  vm.$store.commit('setConfigRule', e.isConfigRule)
                  localStorage.setItem('brokerId', id)
                }
              })
            } else {
              vm.server = res.data[0].name
              let id = res.data[0].id
              vm.$store.commit('set_id', id)
              vm.$store.commit('setConfigRule', res.data[0].isConfigRule)
              localStorage.setItem('brokerId', id)
            }
            vm.listGroup()
          } else {
            vm.$message({
              type: 'warning',
              message: '检测到您还未添加任何服务,请先添加相关服务!'
            })
            vm.$router.push('./servers')
          }
        }
      })
    },
    listGroup () {
      let vm = this
      API.listGroup('?brokerId=' + localStorage.getItem('brokerId')).then(res => {
        // if groupId is not existed so set it
        // else use existed groupId
        vm.groupList = []
        if (res.data.code === 0) {
          vm.groupList = [].concat(res.data.data)
          vm.$nextTick(fun => {
            vm.$store.commit('set_groupId', res.data.data[0])
            localStorage.setItem('groupId', res.data.data[0])
          })
        }
      })
    },
    selectLang (e) {
      this.$i18n.locale = e
      localStorage.setItem('lang', e)
      this.$store.commit('setlang', e)
    }
  },
  computed: {
    menu () {
      return this.$store.state.menu
    },
    brokerId () {
      return this.$store.state.brokerId
    },
    groupId () {
      return this.$store.state.groupId
    }
  },
  watch: {
    brokerId (nVal) {
      this.listGroup()
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
