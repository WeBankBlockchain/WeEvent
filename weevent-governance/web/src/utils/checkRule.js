const checkRule = (e) => {
  let pass = true
  let nodes = document.getElementsByClassName('tree_content')
  let lang = localStorage.getItem('lang')
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
        if (lang === 'zh') {
          warning = '请填写完整的条件语句'
        } else {
          warning = 'please input completed rule'
        }
        console.log('connectionOperator is empty')
      } else {
        if (!item.columnName || item.sqlCondition === '') {
          // has empty
          pass = false
          if (lang === 'zh') {
            warning = '请填写完整的条件语句'
          } else {
            warning = 'please input completed rule'
          }
          console.log('has empty')
        } else {
          if (item.functionType) {
            if (['abs', 'ceil', 'floor', 'round'].indexOf(item.functionType) !== -1) {
              try {
                let fun = Math[item.functionType]
                let operator = item.conditionalOperator
                let val = item.sqlCondition
                let patrn = /^(-)?\d+(\.\d+)?$/
                if (patrn.exec(val) == null) {
                  // data type error - not a number
                  pass = false
                  if (lang === 'zh') {
                    warning = '填写的数据不是数字类型'
                  } else {
                    warning = 'data type error - not a number'
                  }
                  console.log('data type error - not a number')
                } else {
                  if (item.functionType === 'ceil' || item.functionType === 'floor' || item.functionType === 'round') {
                    // data error - not an integer
                    if (operator === '==' || operator === '!=') {
                      if (fun(val) !== Number(val)) {
                        pass = false
                        if (lang === 'zh') {
                          warning = '方法异常，请填写整数类型'
                        } else {
                          warning = 'data type error - not an integer'
                        }
                        console.log('data error - not an integer')
                      }
                    }
                  }
                  // data error - not a natural number
                  if (item.functionType === 'abs' && Number(val) <= 0) {
                    if (operator === '<' || operator === '<=' || operator === '==') {
                      if (Number(val) < 0) {
                        pass = false
                        if (lang === 'zh') {
                          warning = '输入值请大于0'
                        } else {
                          warning = 'please input the value bigger then 0'
                        }
                        console.log('not a natural number')
                      }
                    }
                  }
                }
              } catch (error) {
                // error
                pass = false
                console.log('fun is error')
              }
            }
            if (['substring', 'concat', 'trim', 'lcase'].indexOf(item.functionType) !== -1) {
              let operator = item.conditionalOperator
              let val = item.sqlCondition
              // val witch ""
              if (val[0] !== '"' || val[val.length - 1] !== '"') {
                if (lang === 'zh') {
                  warning = '填写的数据类型错误-字符串类型请用双引号包裹'
                } else {
                  warning = 'please warp the string in double quotes'
                }
                console.log('not a strging')
                pass = false
              } else {
                if (operator !== '!=' && operator !== '==') {
                  if (lang === 'zh') {
                    warning = '该方法的关系符只能是 != 或 == '
                  } else {
                    warning = 'operator is error plese use != or =='
                  }
                  pass = false
                } else {
                  if (item.functionType === 'substring') {
                    if (item.columnMark === '') {
                      pass = false
                      if (lang === 'zh') {
                        warning = '请填写完整的函数方法'
                      } else {
                        warning = 'please input completed function'
                      }
                      console.log('columnMark is empty')
                    } else {
                      let indexList = item.columnMark
                      let index = indexList.split(',')
                      if (index.length > 2) {
                        if (lang === 'zh') {
                          warning = '该函数的参数错误'
                        } else {
                          warning = 'function error - parameter is error'
                        }
                        pass = false
                      } else {
                        if (!index[1]) {
                          if (!Number.isInteger(Number(index[0]))) {
                            pass = false
                            if (lang === 'zh') {
                              warning = '该函数的下标不是整数类型'
                            } else {
                              warning = 'function error - index is not a integer'
                            }
                            console.log('index not a number')
                          }
                        } else {
                          if (!Number.isInteger(Number(index[0])) || !Number.isInteger(Number(index[1]))) {
                            pass = false
                            if (lang === 'zh') {
                              warning = '该函数的下标不是整数类型'
                            } else {
                              warning = 'function error - index is not a integer'
                            }
                            console.log('index not a number')
                          }
                        }
                      }
                    }
                  }
                  if (item.functionType === 'concat' && !item.columnMark) {
                    pass = false
                    if (lang === 'zh') {
                      warning = '请填写完整的函数方法'
                    } else {
                      warning = 'function error - columnMark is not a integer'
                    }
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
export {
  checkRule
}
