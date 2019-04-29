import axios from 'axios'
import qs from 'qs'
import { Message } from 'element-ui'

class BaseModule {
  constructor () {
    this.$http = axios.create({
      timeout: 15 * 1000,
      withCredentials: true // 进行cookie数据传递
    })

    this.dataMethodDefaults = {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'X-Requested-With': 'XMLHttpRequest'
      }
    }
    // 收到数据的拦截
    this.$http.interceptors.response.use(config => {
      return new Promise((resolve, reject) => {
        let data = config.data
        let status = config.status
        if (((status >= 200 && status < 300) || status === 304) && data) {
          resolve(config)
        } else {
          Message({
            type: 'warning',
            message: '请求异常'
          })
          reject(config)
        }
      }).catch((e) => {
        Message({
          type: 'error',
          message: '请求未响应,稍后重试'
        })
      })
    }, error => {
      if (error.message.includes('timeout')) {
        Message({
          type: 'error',
          message: '请求超时请稍后重试'
        })
      } else {
        Message({
          type: 'error',
          message: '数据请求失败'
        })
      }
    })
  }

  request (config, data = undefined) {
    if (config.method && config.method.toLowerCase() === 'post') {
      data = qs.stringify(data)
      return this.post(config.url, data, config)
    } else {
      return this.$http({
        url: config.url,
        method: 'get',
        params: data
      })
    }
  }

  get (url, config = {}) {
    return this.$http.get(url.config)
  }

  post (url, data = undefined, config = {}) {
    return this.$http.post(url, data, { ...this.dataMethodDefaults, ...config })
  }
}

export default BaseModule
