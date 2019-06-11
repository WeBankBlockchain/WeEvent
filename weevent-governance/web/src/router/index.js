import Vue from 'vue'
import Router from 'vue-router'

import login from '../login/login'
import registered from '../components/registered'
import index from '../module/index'
import topicList from '../components/topicList'
import mainCont from '../components/mainCont'
import subcription from '../components/subcription'
// WeBase Module
import overview from '../components/webase/overview'
import group from '../components/webase/group'
import blockInfor from '../components/webase/blockInfor'
import transactionInfor from '../components/webase/transactionInfor'
import setting from '../components/setting'
import servers from '../components/servers'

Vue.use(Router)

export default new Router({
  mode: 'history',
  routes: [
    {
      path: '/login',
      name: 'login',
      component: login
    }, {
      path: '/registered',
      name: 'registered',
      component: registered
    }, {
      path: '/',
      name: 'index',
      component: index,
      children: [{
        path: '/topicList',
        name: 'topicList',
        component: topicList
      }, {
        path: '/index',
        name: 'mainCont',
        component: mainCont
      }, {
        path: '/subcription',
        name: 'subcription',
        component: subcription
      }, {
        path: '/overview',
        name: 'overview',
        component: overview
      }, {
        path: '/group',
        name: 'group',
        component: group
      }, {
        path: '/blockInfor',
        name: 'blockInfor',
        component: blockInfor
      }, {
        path: '/transactionInfor',
        name: 'transactionInfor',
        component: transactionInfor
      }]
    }, {
      path: '/setting',
      name: 'setting',
      component: setting
    }, {
      path: '/servers',
      name: 'servers',
      component: servers
    }
  ]
})
