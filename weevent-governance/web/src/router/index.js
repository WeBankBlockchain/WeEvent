import Vue from 'vue'
import Router from 'vue-router'
import index from '../module/index'
import nodeList from '../components/nodeList'
import topicList from '../components/topicList'
import mainCont from '../components/mainCont'
import machine from '../components/machine'
import subcription from '../components/subcription'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'index',
      component: index,
      children: [{
        path: '/nodeList',
        name: 'nodeList',
        component: nodeList
      }, {
        path: '/topicList',
        name: 'topicList',
        component: topicList
      }, {
        path: '/index',
        name: 'mainCont',
        component: mainCont
      }, {
        path: '/machine',
        name: 'machine',
        component: machine
      }, {
        path: '/subcription',
        name: 'subcription',
        component: subcription
      }]
    }
  ]
})
