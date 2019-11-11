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
          <span class='forget' @click='changePass'>{{$t('userSet.forgetPassWord')}}</span>
        </el-form-item>
        <el-form-item>
          <el-button type='primary' @click='onSubmit("loginForm")' @keyup.enter.native='onSubmit("loginForm")'>{{$t('userSet.login')}}</el-button>
          <span class='registered_btn' @click='registered'>{{$t('userSet.quickRegistered')}}</span>
        </el-form-item>
      </el-form>
    </div>
    <el-dialog
      :title="$t('userSet.resetPassWord')"
      :visible.sync="getPass"
      width='420px'
      :close-on-click-modal='false'
    >
      <p class='input_title'>{{$t('userSet.enterUserName') + ' :'}}</p>
      <el-input v-model='userName'></el-input>
      <p class='input_warning'><i>*</i>{{$t('userSet.mailWarning')}}</p>
      <span slot='footer' class='dialog-footer'>
        <el-button @click='getPass=false'>{{$t('common.cancel')}}</el-button>
        <el-button type='primary' @click='getPassWord'>{{$t('common.ok')}}</el-button>
      </span>
    </el-dialog>
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
      getPass: false,
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
          // login
          API.login(data).then(res => {
            if (res.status === 200 && res.data.code === 0) {
              localStorage.setItem('userId', res.data.data.userId)
              localStorage.setItem('user', res.data.data.username)
              this.show_error = false
              this.$router.push('./index')
            } else if (res.data.code === 202034) {
              this.form.passWord = ''
              this.show_error = true
              this.$refs[formName].fields[1].$el.style.borderColor = '#000'
              this.$refs[formName].fields[1].$el.children[0].children[0].children[0].focus()
            }
          })
        } else {
          return false
        }
      })
    },
    registered () {
      this.$router.push({ path: './registered', query: { reset: 1 } })
    },
    getPassWord () {
      let url = '?username=' + this.userName
      API.forget(url).then(res => {
        if (res.status === 200) {
          if (res.data.status === 400) {
            this.$message({
              type: 'warning',
              message: this.$t('userSet.noUser')
            })
          } else if (res.data.status === 100107 || res.data.status === 100102) {
            this.$message({
              type: 'warning',
              message: this.$t('userSet.sendMailFail')
            })
          } else {
            this.$message({
              type: 'success',
              message: this.$t('userSet.sendMailSuccess')
            })
          }
        } else {
          this.$message({
            type: 'warning',
            message: this.$t('userSet.sendMailFail')
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
