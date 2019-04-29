import Vue from 'vue'
import vuex from 'vuex'
Vue.use(vuex)

export default new vuex.Store({
  state: {
    userName: ''
  },
  mutations: {
    set_name (state, note) {
      state.userName = note
    }
  }
})
