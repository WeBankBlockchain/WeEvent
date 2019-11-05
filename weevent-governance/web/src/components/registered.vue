<template>
  <div class='registered'>
    <div class='registered_part'>
      <img src="../assets/image/login.png" alt="">
      <el-form :model="ruleForm" status-icon :rules="rules" ref="ruleForm"  class="demo-ruleForm" v-show='reset'>
        <el-form-item :label="$t('userSet.userName')" prop="name">
          <el-input v-model.trim.trim="ruleForm.name" ></el-input>
        </el-form-item>
        <el-form-item :label="$t('userSet.passWord')" prop="pass">
          <el-input type="password" v-model.trim="ruleForm.pass" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item :label="$t('userSet.confirmPassWord')" prop="checkPass" >
          <el-input type="password" v-model.trim="ruleForm.checkPass" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item
          :label="$t('userSet.mail')"
          prop='email'
          v-show='reset'
          >
          <el-input type="email" v-model.trim="ruleForm.email" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item v-show='reset'>
          <el-button type="primary" @click="submitForm('ruleForm')">{{$t('userSet.registered')}}</el-button>
        </el-form-item>
      </el-form>
      <el-form :model="ruleForm2" status-icon :rules="rules2" ref="ruleForm2" class="demo-ruleForm" v-show='!reset' >
        <el-form-item :label="$t('userSet.userName')">
          <el-input v-model.trim="ruleForm2.name" disabled></el-input>
        </el-form-item>
        <el-form-item :label="$t('userSet.oldPassWord')" prop="pass">
          <el-input type="password" v-model.trim="ruleForm2.pass" autocomplete="off"></el-input>
        </el-form-item>
         <el-form-item :label="$t('userSet.newPassWord')" prop="newPass" >
          <el-input type="password" v-model.trim="ruleForm2.newPass" autocomplete="off"></el-input>
        </el-form-item>
         <el-form-item :label="$t('userSet.enterAgain')" prop="checkNewPass" >
          <el-input type="password" v-model.trim="ruleForm2.checkNewPass" autocomplete="off"></el-input>
        </el-form-item>
         <el-form-item>
          <el-button type="primary" @click="submit('ruleForm2')">{{$t('userSet.modify')}}</el-button>
        </el-form-item>
      </el-form>
      <router-link to='./login' v-show='reset'><i class='el-icon-back'></i>{{$t('userSet.hasAccount')}}</router-link>
      <a v-show='!reset' @click='goBack'><i class='el-icon-back' ></i>{{$t('common.back')}}</a>
    </div>
  </div>
</template>
<script>
import API from '../API/resource'
export default {
  data () {
    var checkName = (rule, value, callback) => {
      let regex = /^[0-9A-Za-z]{6,20}$/
      if (!value) {
        return callback(new Error(this.$t('userSet.emptyUserName')))
      } else {
        if (!regex.exec(value)) {
          return callback(new Error(this.$t('userSet.errorUserName')))
        } else {
          let url = '/' + value + '/1'
          API.checkExsit(url).then(res => {
            if (res.data.data) {
              callback()
            } else {
              callback(new Error(this.$t('userSet.exitUserName')))
            }
          })
        }
      }
    }
    var pass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error(this.$t('userSet.enterPassWord')))
      } else {
        callback()
      }
    }
    var checkPass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error(this.$t('userSet.enterPassWord')))
      } else if (value !== this.ruleForm.pass) {
        callback(new Error(this.$t('userSet.passWordInconsistent')))
      } else {
        callback()
      }
    }
    var newPass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error(this.$t('userSet.enterPassWord')))
      } else {
        callback()
      }
    }
    var checkNewPass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error(this.$t('userSet.enterPassWord')))
      } else if (value !== this.ruleForm2.newPass) {
        callback(new Error(this.$t('userSet.passWordInconsistent')))
      } else {
        callback()
      }
    }
    var checkEmail = (rule, value, callback) => {
      let reg = new RegExp(/^([a-zA-Z0-9._-])+@([a-zA-Z0-9_-])+(\.[a-zA-Z0-9_-])+/)
      if (!value) {
        callback()
      } else {
        if (reg.test(value)) {
          callback()
        } else {
          callback(new Error(this.$t('userSet.errorEail')))
        }
      }
    }
    return {
      ruleForm: {
        name: '',
        pass: '',
        checkPass: '',
        email: ''
      },
      ruleForm2: {
        name: localStorage.getItem('user'),
        pass: '',
        newPass: '',
        checkNewPass: ''
      },
      rules: {
        name: [
          { validator: checkName, trigger: 'blur' }
        ],
        pass: [
          { validator: pass, trigger: 'blur' }
        ],
        checkPass: [
          { validator: checkPass, trigger: 'blur' }
        ],
        email: [
          { validator: checkEmail, trigger: 'blur' }
        ]
      },
      rules2: {
        pass: [
          { validator: pass, trigger: 'blur' }
        ],
        newPass: [
          { validator: newPass, trigger: 'blur' }
        ],
        checkNewPass: [
          { validator: checkNewPass, trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    submitForm (formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          let data = {
            'username': this.ruleForm.name,
            'password': this.ruleForm.pass,
            'email': this.ruleForm.email
          }
          API.register(data).then(res => {
            if (res.data.status === 200) {
              this.$message({
                type: 'success',
                message: this.$t('userSet.regSuccess')
              })
              let e = {
                'username': this.ruleForm.name,
                'password': this.ruleForm.pass
              }
              setTimeout(fun => {
                this.login(e)
              }, 1000)
            } else {
              this.$message({
                type: 'warning',
                message: this.$t('userSet.regFail')
              })
            }
          })
        } else {
          return false
        }
      })
    },
    submit (formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          let data = {
            'username': this.ruleForm2.name,
            'oldPassword': this.ruleForm2.pass,
            'password': this.ruleForm2.newPass
          }
          API.update(data).then(res => {
            if (res.status === 200) {
              if (res.data.status === 400) {
                this.ruleForm.oldPass = ''
                this.$refs.ruleForm.validateField('oldPass')
                this.$message({
                  type: 'warning',
                  message: this.$t('userSet.errorOldPassWord')
                })
              } else {
                this.$message({
                  type: 'success',
                  message: this.$t('userSet.passWordModifySuccess')
                })
              }
            }
          })
        } else {
          return false
        }
      })
    },
    goBack () {
      this.$router.go(-1)
    },
    login (e) {
      API.login(e).then(res => {
        if (res.status === 200 && res.data.code === 0) {
          localStorage.setItem('userId', res.data.data.userId)
          localStorage.setItem('user', res.data.data.username)
          this.$router.push('./index')
        } else {
          this.$message({
            type: 'warning',
            message: this.$t('userSet.loginFail')
          })
        }
      })
    }
  },
  computed: {
    reset () {
      return Number(this.$route.query.reset)
    }
  }
}
</script>
