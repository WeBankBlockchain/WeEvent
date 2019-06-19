<template>
  <div id='login'>
    <img src="../assets/image/role1.png" class='role1' alt="">
    <img src="../assets/image/role2.svg" class='role2' alt="">
    <img src="../assets/image/role2.svg" class='role3' alt="">
    <div class='login_box'>
      <img src="../assets/image/login.png" alt="" class='login_logo'>
      <p class='words'>区块链应用平台</p>
      <el-form label-position="right" :rules="rules" :model='form' ref='loginForm'>
        <el-form-item label='' prop='name'>
          <el-input v-model='form.name' auto-complete="off" prefix-icon='el-icon-user' placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label='' prop='passWord'>
          <el-input v-model='form.passWord' auto-complete="off" type='password' prefix-icon='el-icon-lock' placeholder="请输入密码"></el-input>
          <span class='forget' @click='getPassWord'>忘记密码？</span>
        </el-form-item>
        <el-form-item>
          <el-button type='primary' @click='onSubmit("loginForm")' @keyup.enter.native='onSubmit("loginForm")'>登录</el-button>
          <span class='registered_btn' @click='registered'>快速注册</span>
        </el-form-item>
      </el-form>
      <img src="../assets/image/shape.svg" class='shape' alt="">
    </div>
  </div>
</template>
<script>
import API from '../API/resource'
export default{
  data () {
    var checkName = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入用户名'))
      } else {
        if (this.form.passWord !== '') {
          this.$refs.loginForm.validateField('checkPass')
        }
        callback()
      }
    }
    var checkPassWord = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入密码'))
      }
      callback()
    }
    return {
      form: {
        name: '',
        passWord: ''
      },
      rules: {
        name: [
          { validator: checkName, trigger: 'blur' }
        ],
        passWord: [
          { validator: checkPassWord, trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    onSubmit (formName) {
      let data = {
        'username': this.form.name,
        'password': this.form.passWord
      }
      this.$refs[formName].validate((valid) => {
        if (valid) {
          // 登录操作
          API.login(data).then(res => {
            if (res.status === 200 && res.data.code === 0) {
              localStorage.setItem('userId', res.data.data.userId)
              localStorage.setItem('user', res.data.data.username)
              this.$router.push('./index')
            } else if (res.data.code === 202034) {
              this.form.name = ''
              this.form.passWord = ''
              this.$refs[formName].validate((valid))
              this.$message({
                type: 'warning',
                message: '用户名或密码错误'
              })
            }
          })
        } else {
          return false
        }
      })
    },
    registered () {
      this.$router.push({path: './registered', query: { reset: 1 }})
    },
    getPassWord () {
      this.$prompt('请输入用户名', '获取密码', {
        confirmButtonText: '确定',
        inputValue: this.form.name
      }).then(({ value }) => {
        let url = '?username=' + value
        API.forget(url).then(res => {
          if (res.status === 200) {
            this.$message({
              type: 'success',
              message: '密码已发送到注册邮箱!'
            })
          } else {
            this.$message({
              type: 'warning',
              message: '获取密码失败'
            })
          }
        })
      })
    }
  },
  mounted () {
    localStorage.removeItem('brokerId')
    this.$store.commit('back', false)
    let vm = this
    this.$nextTick(fun => {
      document.addEventListener('keyup', function (e) {
        if (e.keyCode === 13) {
          vm.onSubmit('loginForm')
        }
      })
    })
  }
}
</script>
