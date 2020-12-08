const url = 'http://127.0.0.1:7009'

let root
if (process.env.NODE_ENV === 'development') {
  root = 'api/'
} else {
  root = url
}

exports.proxyRoot = url
exports.ROOT = root