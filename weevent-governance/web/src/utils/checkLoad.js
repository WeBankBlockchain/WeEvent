import { checkCurrentData, checkCurrentTime } from './formatTime.js'
export const checkLoad = (value, remakeItem, array, selectField) => {
  let newPayload = JSON.parse(value)
  let funArray = JSON.parse(array)
  for (let i = 0; i < selectField.length; i++) {
    let e = selectField[i]
    if (e !== 'eventId') {
      if (newPayload[e] !== undefined) {
        let newType = typeof (newPayload[e])
        let oldType = typeof (remakeItem[e])
        if (newType !== oldType) {
          return false
        }
      } else {
        return false
      }
    }
  }
  for (let i = 0; i < funArray.length; i++) {
    let item = funArray[i]
    if (item[2] === 'substring' || item[2] === 'concat' || item[2] === 'now' || item[2] === 'currentDate' || item[2] === 'currentTime') {
      if (item[2] === 'substring' || item[2] === 'concat') {
        let key = item[3].split(',')
        let e = key[0]
        if (newPayload[e] !== undefined) {
          let newType = typeof (newPayload[e])
          let oldType = typeof (remakeItem[e])
          if (newType !== oldType) {
            return false
          }
        } else {
          return false
        }
      }
      if (item[2] === 'concat') {
        let key = item[3].split(',')
        for (let i = 0; i < key.length; i++) {
          let e = item[i]
          if (newPayload[e] !== undefined) {
            let newType = typeof (newPayload[e])
            let oldType = typeof (remakeItem[e])
            if (newType !== oldType) {
              return false
            }
          } else {
            return false
          }
        }
      }
      if (item[2] === 'now') {
        let e = item[3]
        if (newPayload[e] !== undefined) {
          let newType = typeof (newPayload[e])
          let oldType = typeof (remakeItem[e])
          if (newType !== oldType) {
            return false
          } else {
            let reg = /^(\d{4})(-|\/)(\d{2})\2(\d{2}) (\d{2}):(\d{2}):(\d{2})$/
            let t = newPayload[e]
            if (t.match(reg) == null) {
              return false
            } else {
              let time = t.split(' ')
              if (!checkCurrentData(time[0]) || !checkCurrentTime(time[1])) {
                return false
              }
            }
          }
        } else {
          return false
        }
      }
      if (item[2] === 'currentDate') {
        let e = item[3]
        if (newPayload[e] !== undefined) {
          let newType = typeof (newPayload[e])
          let oldType = typeof (remakeItem[e])
          if (newType !== oldType) {
            return false
          } else {
            let reg = /^(\d{4})(-|\/)(\d{2})\2(\d{2})$/
            let t = newPayload[e]
            if (t.match(reg) == null) {
              return false
            } else {
              if (!checkCurrentData(t)) {
                return false
              }
            }
          }
        } else {
          return false
        }
      }
      if (item[2] === 'currentTime') {
        let e = item[3]
        if (newPayload[e] !== undefined) {
          let newType = typeof (newPayload[e])
          let oldType = typeof (remakeItem[e])
          if (newType !== oldType) {
            return false
          } else {
            let reg = /^(\d{2}):(\d{2}):(\d{2})$/
            let t = newPayload[e]
            if (t.match(reg) == null) {
              return false
            } else {
              if (!checkCurrentTime(t)) {
                return false
              }
            }
          }
        } else {
          return false
        }
      }
    } else {
      let e = item[3]
      if (newPayload[e] !== undefined) {
        let newType = typeof (newPayload[e])
        let oldType = typeof (remakeItem[e])
        if (newType !== oldType) {
          return false
        }
      } else {
        return false
      }
    }
  }
  return true
}
