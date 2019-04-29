/**
* @function getDate 获取日期
* @param  {timestamp} timestamp {时间戳}
* @return {String} {2017-02-02}
*/
const getDate = (timestamp) => {
  const date = new Date(timestamp)
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${year}年${month >= 10 ? month : '0' + month}月${day >= 10 ? day : '0' + day}日`
}

const getDateDetial = (timestamp) => {
  const date = new Date(timestamp)
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hour = date.getHours()
  const minute = date.getMinutes()
  const second = date.getSeconds()
  return `${year}-${month >= 10 ? month : '0' + month}-${day >= 10 ? day : '0' + day} ${hour >= 10 ? hour : '0' + hour}:${minute >= 10 ? minute : '0' + minute}:${second >= 10 ? second : '0' + second}`
}
/**
 * 将00:00格式的数据转成00s
 * @param  {String} duration 00:00格式的时间
 * @return {[type]}          [description]
 */
const convertStrToInt = (duration) => {
  duration = duration || '00:00'
  const arr = duration.split(':')
  if (arr[0] === '00' && Number(arr[1]) > 0) {
    duration = "00'" + arr[1] + '"'
  } else {
    if (arr[1] === '00' && Number(arr[0] > 0)) {
      duration = arr[0] + "'" + '00"'
    } else {
      duration = arr[0] + "'" + arr[1] + '"'
    }
  }
  if (duration === '0"' || duration === "0'") {
    duration = ''
  }
  return duration
}
/**
 * 将00s转成00:00
 * @param  {[type]} timestamp [description]
 * @return {[type]}           [description]
 */
const convertIntToStr = (duration) => {
  let formatTime = '00:00'
  if (duration < 60) {
    formatTime = duration < 10 ? ('00:0' + duration) : '00:' + duration
  } else {
    if (duration % 60 === 0) {
      formatTime = duration / 60 < 10 ? '0' + (duration / 60) + ':00' : duration / 60 + ':00'
    } else {
      var min = parseInt(duration / 60)
      var s = duration - min * 60
      min = min < 10 ? '0' + min : min
      s = s < 10 ? '0' + s : s
      formatTime = min + ':' + s
    }
  }
  return formatTime
}
/**
* @function dateFormat 时间格式化
* @param  {timestamp} timestamp {时间戳}
* @return {String}
*/
const dateBeforeAfter = (timestamp) => {
  const now = Date.now()
  console.log(now + 'timestamp：' + timestamp)
  const minute = 1000 * 60
  const hour = minute * 60
  const publishTime = String(now).length === String(timestamp).length ? timestamp : timestamp * 1000
  const diff = now - publishTime
  if (diff / hour >= 1) {
    if (diff / hour >= 24) {
      return getDate(publishTime)
    } else {
      return `${parseInt(diff / hour, 10)}小时前`
    }
  } else if (diff / minute >= 1) {
    return `${parseInt(diff / minute, 10)}分钟前`
  } else {
    return '1分钟前'
  }
}
/**
 * 转换星座
 * @param  {Number} month [月份]
 * @param  {Number} day   [日期]
 * @return {[type]}       [description]
 */
const getAstro = (month, day) => {
  var s = '魔羯水瓶双鱼白羊金牛双子巨蟹狮子处女天秤天蝎射手魔羯'
  var arr = [20, 19, 21, 21, 21, 22, 23, 23, 23, 23, 22, 22]
  return s.substr(month * 2 - (day < arr[month - 1] ? 2 : 0), 2)
}
/**
 * 获取年龄
 * @param  {[Number]} y [年]
 * @param  {[Number]} m [月]
 * @param  {[Number]} d [日]
 * @return {[type]}   [description]
 */
const getAge = (y, m, d) => {
  var today = new Date()
  var birthDate = new Date(y, m, d)
  var age = today.getFullYear() - birthDate.getFullYear()
  var month = today.getMonth() - birthDate.getMonth()
  if (month < 0 || (month === 0 && today.getDate() < birthDate.getDate())) {
    age--
  }
  return age
}
export {
  getAge,
  getAstro,
  dateBeforeAfter,
  convertIntToStr,
  convertStrToInt,
  getDate,
  getDateDetial
}
