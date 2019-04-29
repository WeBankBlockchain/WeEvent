import BaseModel from './BaseModel'
import api from './api.json'

class ResoruceService extends BaseModel {
  topicList (data) {
    return this.request(api.topicList, data)
  }
  nodeList (data) {
    return this.request(api.nodeList, data)
  }
  openTopic (data) {
    return this.request(api.openTopic, data)
  }
  getHost (data) {
    return this.request(api.getHost, data)
  }
  blockchaininfo (data) {
    return this.request(api.blockchaininfo, data)
  }
  topicState (data) {
    return this.request(api.topicState, data)
  }
  topicControl (data) {
    return this.request(api.topicControl, data)
  }
  subscription (data) {
    return this.request(api.subscription, data)
  }
}

export default new ResoruceService()
