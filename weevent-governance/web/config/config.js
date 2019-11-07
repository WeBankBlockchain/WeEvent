const url = ''
let root
if (process.env.NODE_ENV === 'development') {
  root = 'api/'
} else {
  root = url
}

exports.proxyRoot = url
exports.ROOT = root
