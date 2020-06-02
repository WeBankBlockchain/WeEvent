// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import VueClipboard from 'vue-clipboard2'
import store from './store/index'
import i18n from './i18n/index'
import uploader from 'vue-simple-uploader'
Vue.use(uploader)
Vue.use(ElementUI)
Vue.config.productionTip = false
Vue.use(VueClipboard)

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  store,
  i18n,
  components: { App },
  template: '<App/>'
})
