import i18n from '../i18n'
import { checkCurrentData, checkCurrentTime } from './formatTime.js'
export const checkRule = (e, s) => {
  let pass = true
  let nodes = document.getElementsByClassName('tree_content')
  let lang = i18n.locale
  if (nodes) {
    for (let i = 0; i < nodes.length; i++) {
      let war = nodes[i].childNodes[nodes[i].childNodes.length - 1]
      let warning = ''
      let item = nodes[i].attributes[0].value
      let index = nodes[i].attributes[1].nodeValue
      let payLoad = JSON.parse(s)
      item = JSON.parse(item)
      if (index !== '00' && !item.connectionOperator) {
        // not first line and connectionOperator is empty
        pass = false
        warning = i18n.messages[lang].ruleCheck.inputRule
        console.log('connectionOperator is empty')
      } else {
        if (!item.columnName || item.sqlCondition === '' || !item.conditionalOperator) {
          // has empty
          pass = false
          warning = i18n.messages[lang].ruleCheck.inputRule
          console.log('has empty')
        } else {
          if (item.functionType) {
            if (['abs', 'ceil', 'floor', 'round'].indexOf(item.functionType) !== -1) {
              let fun = Math[item.functionType]
              let operator = item.conditionalOperator
              let val = item.sqlCondition
              let patrn = /^(-)?\d+(\.\d+)?$/
              if (patrn.exec(val) == null) {
                // data type error - not a number
                pass = false
                warning = i18n.messages[lang].ruleCheck.notNumber
                console.log('data type error - not a number')
              } else {
                if (item.functionType === 'ceil' || item.functionType === 'floor' || item.functionType === 'round') {
                  // data error - not an integer
                  if (operator === '==') {
                    if (fun(val) !== Number(val)) {
                      pass = false
                      warning = i18n.messages[lang].ruleCheck.inputInteger
                      console.log('data error - not an integer')
                    }
                  }
                }
                // data error - not a natural number
                if (item.functionType === 'abs') {
                  if (operator === '<' || operator === '<=' || operator === '==') {
                    if (Number(val) < 0) {
                      pass = false
                      warning = i18n.messages[lang].ruleCheck.bigger
                      console.log('not a natural number')
                    }
                  }
                }
              }
            }
            if (['substring', 'concat', 'trim', 'lcase'].indexOf(item.functionType) !== -1) {
              let operator = item.conditionalOperator
              let val = item.sqlCondition
              // val witch ""
              if (val[0] !== '"' || val[val.length - 1] !== '"') {
                warning = i18n.messages[lang].ruleCheck.inputString
                console.log('not a strging')
                pass = false
              } else {
                if (operator !== '!=' && operator !== '==') {
                  warning = i18n.messages[lang].ruleCheck.errorOperator
                  pass = false
                } else {
                  if (item.functionType === 'substring') {
                    if (item.columnMark === '') {
                      pass = false
                      warning = i18n.messages[lang].ruleCheck.inputRule
                      console.log('columnMark is empty')
                    } else {
                      let indexList = item.columnMark
                      let index = indexList.split(',')
                      if (index.length > 2) {
                        warning = i18n.messages[lang].ruleCheck.errorParameter
                        pass = false
                      } else {
                        if (!index[1]) {
                          if (!Number.isInteger(Number(index[0]))) {
                            pass = false
                            warning = i18n.messages[lang].ruleCheck.errorIndex
                            console.log('index not a number')
                          }
                        } else {
                          if (!Number.isInteger(Number(index[0])) || !Number.isInteger(Number(index[1]))) {
                            pass = false
                            warning = i18n.messages[lang].ruleCheck.errorIndex
                            console.log('index not a number')
                          }
                        }
                      }
                    }
                  }
                  if (item.functionType === 'concat') {
                    if (!item.columnMark) {
                      pass = false
                      warning = i18n.messages[lang].ruleCheck.inputRule
                      console.log('columnMark is empty')
                    } else {
                      let t = payLoad[item.columnMark]
                      let type = typeof (t)
                      if (type !== 'string') {
                        pass = false
                        warning = i18n.messages[lang].ruleCheck.columnMarkError
                        console.log('columnMark type error11')
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
      if (pass) {
        if (item.functionType !== 'now' && item.functionType !== 'currentDate' && item.functionType !== 'currentTime') {
          let type = typeof (payLoad[item.columnName])
          if (type === 'string') {
            if (item.sqlCondition[0] !== '"' || item.sqlCondition[item.sqlCondition.length - 1] !== '"') {
              pass = false
              warning = i18n.messages[lang].ruleCheck.typeErrorString
              console.log('type error')
            } else {
              if (item.conditionalOperator !== '!=' && item.conditionalOperator !== '==') {
                warning = i18n.messages[lang].ruleCheck.errorOperator
                console.log(i18n.messages[lang].ruleCheck)
                pass = false
              }
            }
          } else if (type === 'number') {
            let re = /^(-)?\d+(\.\d+)?$/
            if (re.exec(item.sqlCondition) == null) {
              pass = false
              warning = i18n.messages[lang].ruleCheck.typeErrorNumber
              console.log('type error')
            }
          }
        } else {
          let t = payLoad[item.columnName]
          let type = typeof (t)
          if (item.functionType === 'now') {
            let reg = /^(\d{4})(-|\/)(\d{2})\2(\d{2}) (\d{2}):(\d{2}):(\d{2})$/
            if (type !== 'string') {
              pass = false
              warning = i18n.messages[lang].ruleCheck.errorTime
            } else {
              if (t.match(reg) == null) {
                pass = false
                warning = i18n.messages[lang].ruleCheck.errorTime
              } else {
                let time = t.split(' ')
                if (!checkCurrentData(time[0]) || !checkCurrentTime(time[1])) {
                  pass = false
                  warning = i18n.messages[lang].ruleCheck.notAdate
                }
              }
            }
          } else if (item.functionType === 'currentDate') {
            let reg = /^(\d{4})(-|\/)(\d{2})\2(\d{2})$/
            if (type !== 'string') {
              pass = false
              warning = i18n.messages[lang].ruleCheck.typeDateError
            } else {
              if (t.match(reg) == null) {
                pass = false
                warning = i18n.messages[lang].ruleCheck.typeDateError
              } else {
                if (!checkCurrentData(t)) {
                  pass = false
                  warning = i18n.messages[lang].ruleCheck.notAdate
                }
              }
            }
          } else {
            let reg = /^(\d{2}):(\d{2}):(\d{2})$/
            if (type !== 'string') {
              pass = false
              warning = i18n.messages[lang].ruleCheck.typeTimeError
            } else {
              if (t.match(reg) == null) {
                pass = false
                warning = i18n.messages[lang].ruleCheck.typeTimeError
              } else {
                if (!checkCurrentTime(t)) {
                  pass = false
                  warning = i18n.messages[lang].ruleCheck.notAtime
                }
              }
            }
          }
        }
      }
      if (pass) {
        war.style.display = 'none'
      } else {
        war.style.display = 'block'
        war.innerHTML = warning
      }
    }
  }
  return pass
}
