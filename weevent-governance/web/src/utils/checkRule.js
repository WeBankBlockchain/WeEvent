import i18n from '../i18n'
export const checkRule = (e) => {
  let pass = true
  let nodes = document.getElementsByClassName('tree_content')
  let lang = i18n.locale
  if (nodes) {
    for (let i = 0; i < nodes.length; i++) {
      let war = nodes[i].childNodes[nodes[i].childNodes.length - 1]
      let warning = ''
      let item = nodes[i].attributes[0].value
      let index = nodes[i].attributes[1].nodeValue
      item = JSON.parse(item)
      if (index !== '00' && !item.connectionOperator) {
        // not first line and connectionOperator is empty
        pass = false
        warning = i18n.messages[lang].ruleCheck.inputRule
        console.log('connectionOperator is empty')
      } else {
        if (!item.columnName || item.sqlCondition === '') {
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
                  if (item.functionType === 'concat' && !item.columnMark) {
                    pass = false
                    warning = i18n.messages[lang].ruleCheck.inputRule
                    console.log('columnMark is empty')
                  }
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
