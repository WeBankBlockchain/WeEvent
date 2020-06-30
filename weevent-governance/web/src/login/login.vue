<template>
  <div id='login'>
    <img src="../assets/image/role1.png" class='role1' alt="">
    <img src="../assets/image/role2.svg" class='role2' alt="">
    <img src="../assets/image/role2.svg" class='role3' alt="">
    <img src="../assets/image/bg.png" class='bg_img' alt="">
    <div class='login_box'>
      <img src="../assets/image/login.png" alt="" class='login_logo'>
      <div class='login_error'>
        <p :class="{'show_error': show_error}">{{$t('userSet.errorLogin')}}</p>
      </div>
      <el-form label-position="right" :rules="rules" :model='form' ref='loginForm'>
        <el-form-item label='' prop='name'>
          <el-input v-model.trim='form.name' auto-complete="off" prefix-icon='el-icon-user' :placeholder="$t('userSet.userName')"></el-input>
        </el-form-item>
        <el-form-item label='' prop='passWord'>
          <el-input v-model.trim='form.passWord' auto-complete="off" type='password' prefix-icon='el-icon-lock' :placeholder="$t('userSet.passWord')"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type='primary' @click='onSubmit()'>{{$t('userSet.login')}}</el-button>
          <span class='registered_btn' @click='registered'>{{$t('userSet.quickRegistered')}}</span>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>
<script>
import API from '../API/resource'
export default {
  data () {
    var checkName = (rule, value, callback) => {
      if (value === '') {
        callback(new Error(this.$t('userSet.enterUserName')))
      } else {
        if (this.form.passWord !== '') {
          this.$refs.loginForm.validateField('checkPass')
        }
        callback()
      }
    }
    var checkPassWord = (rule, value, callback) => {
      if (value === '') {
        callback(new Error(this.$t('userSet.enterPassWord')))
      }
      callback()
    }
    return {
      show_error: false,
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
  methods: {
    onSubmit () {
      const sha256 = require('js-sha256').sha256
      const password = sha256(this.form.name + this.form.passWord)
      const data = {
        username: this.form.name,
        password: password.toUpperCase()
      }
      this.$refs.loginForm.validate((valid) => {
        if (valid) {
          // login
          API.login(data).then(res => {
            if (res.status === 200 && res.data.code === 0) {
              const base = JSON.parse(res.data.data)
              localStorage.setItem('user', base.username)
              localStorage.setItem('token', base.Authorization)
              this.show_error = false
              this.$router.push('./index')
            } else if (res.data.code === 202034) {
              this.form.passWord = ''
              this.show_error = true
              this.$refs.loginForm.fields[1].$el.style.borderColor = '#000'
              this.$refs.loginForm.fields[1].$el.children[0].children[0].children[0].focus()
            }
          })
        } else {
          return false
        }
      })
    },
    registered () {
      this.$router.push({ path: './registered', query: { reset: 1 } })
    }
  },
  mounted () {
    localStorage.removeItem('brokerId')
    this.$store.commit('back', false)
    const vm = this
    this.$nextTick(fun => {
      document.addEventListener('keyup', function (e) {
        if (e.keyCode === 13) {
          vm.onSubmit()
        }
      })
    })
  }
}
</script>
