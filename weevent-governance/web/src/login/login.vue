<template>
  <div id='login'>
    <img src="../assets/image/role1.png" class='role1' alt="">
    <img src="../assets/image/role2.svg" class='role2' alt="">
    <img src="../assets/image/role2.svg" class='role3' alt="">
    <img src="../assets/image/bg.png" class='bg_img' alt="">
    <div class='login_box'>
      <img src="../assets/image/login.png" alt="" class='login_logo'>
      <p class='words'>区块链应用平台</p>
      <el-form label-position="right" :rules="rules" :model='form' ref='loginForm'>
        <el-form-item label='' prop='name'>
          <el-input v-model.trim='form.name' auto-complete="off" prefix-icon='el-icon-user' placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label='' prop='passWord'>
          <el-input v-model.trim='form.passWord' auto-complete="off" type='password' prefix-icon='el-icon-lock' placeholder="请输入密码"></el-input>
          <span class='forget' @click='changePass'>忘记密码？</span>
        </el-form-item>
        <el-form-item>
          <el-button type='primary' @click='onSubmit("loginForm")' @keyup.enter.native='onSubmit("loginForm")'>登录</el-button>
          <span class='registered_btn' @click='registered'>快速注册</span>
        </el-form-item>
      </el-form>
    </div>
    <el-dialog
      title='获取密码'
      :visible.sync="getPass"
      width='420px'
    >
      <p class='input_title'>请输入用户名:</p>
      <el-input v-model='userName'></el-input>
      <p class='input_warning'><i>*</i>请注意，重置密码的链接将会发送到帐号绑定的邮箱!</p>
      <span slot='footer' class='dialog-footer'>
        <el-button @click='getPass=false'>取消</el-button>
        <el-button type='primary' @click='getPassWord'>确认</el-button>
      </span>
    </el-dialog>
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
      getPass: false,
      userName: '',
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
  watch: {
    getPass (nVal) {
      if (!nVal) {
        this.userName = ''
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
      let url = '?username=' + this.userName
      API.forget(url).then(res => {
        if (res.status === 200) {
          this.$message({
            type: 'success',
            message: '密码已发送到注册邮箱!'
          })
        } else {
          this.$message({
            type: 'warning',
            message: '操作失败'
          })
        }
      })
    },
    changePass () {
      this.getPass = true
      this.userName = this.form.name
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
