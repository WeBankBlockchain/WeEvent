import { checkCurrentData, checkCurrentTime } from './formatTime.js'
export const checkLoad = (value, remakeItem, array, selectField) => {
  const newPayload = JSON.parse(value)
  const funArray = JSON.parse(array)
  for (let i = 0; i < selectField.length; i++) {
    const e = selectField[i]
    if (e !== 'eventId') {
      if (newPayload[e] !== undefined) {
        const newType = typeof (newPayload[e])
        const oldType = typeof (remakeItem[e])
        if (newType !== oldType) {
          return false
        }
      } else {
        return false
      }
    }
  }
  for (let i = 0; i < funArray.length; i++) {
    const item = funArray[i]
    if (item[2] === 'substring' || item[2] === 'concat' || item[2] === 'now' || item[2] === 'currentDate' || item[2] === 'currentTime') {
      if (item[2] === 'substring' || item[2] === 'concat') {
        const key = item[3].split(',')
        const e = key[0]
        if (newPayload[e] !== undefined) {
          const newType = typeof (newPayload[e])
          let oldType = typeof (remakeItem[e])
          if (newType !== oldType) {
            return false
          }
        } else {
          return false
        }
      }
      if (item[2] === 'concat') {
        const key = item[3].split(',')
        for (let i = 0; i < key.length; i++) {
          const e = item[i]
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
        const e = item[3]
        if (newPayload[e] !== undefined) {
          let newType = typeof (newPayload[e])
          let oldType = typeof (remakeItem[e])
          if (newType !== oldType) {
            return false
          } else {
            const reg = /^(\d{4})(-|\/)(\d{2})\2(\d{2}) (\d{2}):(\d{2}):(\d{2})$/
            const t = newPayload[e]
            if (t.match(reg) == null) {
              return false
            } else {
              const time = t.split(' ')
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
        const e = item[3]
        if (newPayload[e] !== undefined) {
          let newType = typeof (newPayload[e])
          let oldType = typeof (remakeItem[e])
          if (newType !== oldType) {
            return false
          } else {
            const reg = /^(\d{4})(-|\/)(\d{2})\2(\d{2})$/
            const t = newPayload[e]
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
            const reg = /^(\d{2}):(\d{2}):(\d{2})$/
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
