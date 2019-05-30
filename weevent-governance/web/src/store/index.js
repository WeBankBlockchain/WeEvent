import Vue from 'vue'
import vuex from 'vuex'
Vue.use(vuex)

export default new vuex.Store({
  state: {
    userName: '',
    menu: '首页'
  },
  mutations: {
    set_name (state, note) {
      state.userName = note
    },
    set_menu (state, menu) {
      state.menu = menu
    }
  }
})
