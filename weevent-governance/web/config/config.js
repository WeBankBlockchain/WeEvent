const url = ''
let root
if (process.env.NODE_ENV === 'development') {
  // 开发环境
  root = 'api/'
} else {
  // 生产环境
  root = url
}

exports.proxyRoot = url
exports.ROOT = root
