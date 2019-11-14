import axios from 'axios'
import { Message } from 'element-ui'
import qs from 'qs'
import store from '../store'
import i18n from '../i18n/index'
const con = require('../../config/config.js')

class BaseModule {
  constructor () {
    this.$http = axios.create({
      timeout: 5 * 1000,
      withCredentials: true
    })

    this.dataMethodDefaults = {
      headers: {
        'Content-Type': 'application/json;charset=UTF-8',
        'X-Requested-With': 'XMLHttpRequest'
      }
    }

    this.$http.interceptors.request.use(config => {
      config.url = con.ROOT + config.url
      if (config.method === 'delete' || config.method === 'get') {
        config.url = config.url + config.data
      }
      return config
    })

    this.$http.interceptors.response.use(config => {
      return new Promise((resolve, reject) => {
        let data = config.data
        let status = config.status
        if (((status >= 200 && status < 300) || status === 302) && data) {
          if (data.code === 302000) {
            store.commit('back', true)
          } else {
            resolve(config)
          }
        } else {
          Message({
            type: 'warning',
            message: i18n.messages[i18n.locale].common.reqException
          })
          reject(config)
        }
      }).catch((e) => {
        Message({
          type: 'error',
          message: i18n.messages[i18n.locale].common.reqException
        })
      })
    }, error => {
      if (error.message.includes('timeout')) {
        Message({
          type: 'error',
          message: i18n.messages[i18n.locale].common.timeOut
        })
      } else {
        Message({
          type: 'error',
          message: i18n.messages[i18n.locale].common.reqException
        })
      }
    })
  }

  request (config, data = undefined) {
    if (config.method && config.method.toLowerCase() === 'post') {
      if (config.headers) {
        return this.$http({
          url: config.url,
          method: 'post',
          data: qs.stringify(data),
          headers: config.headers
        })
      } else {
        return this.post(config.url, data, config)
      }
    } else {
      return this.$http({
        url: config.url,
        method: 'get',
        data: data
      })
    }
  }

  get (url, config = {}) {
    return this.$http.get(url.config)
  }

  put (url, data = undefined, config = {}) {
    return this.$http.put(url, data, { ...this.dataMethodDefaults, ...config })
  }

  post (url, data = undefined, config = {}) {
    return this.$http.post(url, data, { ...this.dataMethodDefaults, ...config })
  }

  delete (url, config = {}) {
    return this.$http.delete(url.config)
  }
}

export default BaseModule
