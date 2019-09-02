<template>
  <div class='registered'>
    <div class='registered_part'>
      <img src="../assets/image/login.png" alt="">
      <el-form :model="ruleForm" status-icon :rules="rules" ref="ruleForm"  class="demo-ruleForm" v-show='reset'>
        <el-form-item label="用户名" prop="name">
          <el-input v-model.trim.trim="ruleForm.name" ></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="pass">
          <el-input type="password" v-model.trim="ruleForm.pass" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="checkPass" >
          <el-input type="password" v-model.trim="ruleForm.checkPass" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item
          label='邮箱'
          prop='email'
          v-show='reset'
          >
          <el-input type="email" v-model.trim="ruleForm.email" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item v-show='reset'>
          <el-button type="primary" @click="submitForm('ruleForm')">注册</el-button>
        </el-form-item>
      </el-form>
      <el-form :model="ruleForm2" status-icon :rules="rules2" ref="ruleForm2" class="demo-ruleForm" v-show='!reset'>
        <el-form-item label="用户名">
          <el-input v-model.trim="ruleForm2.name" disabled></el-input>
        </el-form-item>
        <el-form-item label="旧密码" prop="pass">
          <el-input type="password" v-model.trim="ruleForm2.pass" autocomplete="off"></el-input>
        </el-form-item>
         <el-form-item label="新密码" prop="newPass" >
          <el-input type="password" v-model.trim="ruleForm2.newPass" autocomplete="off"></el-input>
        </el-form-item>
         <el-form-item label="再次输入" prop="checkNewPass" >
          <el-input type="password" v-model.trim="ruleForm2.checkNewPass" autocomplete="off"></el-input>
        </el-form-item>
         <el-form-item>
          <el-button type="primary" @click="submit('ruleForm2')">修改</el-button>
        </el-form-item>
      </el-form>
      <router-link to='./login' v-show='reset'><i class='el-icon-back'></i>已有帐号,登录</router-link>
      <a v-show='!reset' @click='goBack'><i class='el-icon-back' ></i>返回</a>
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
        return callback(new Error('用户名不能为空'))
      } else {
        if (!regex.exec(value)) {
          return callback(new Error('用户名只能是6~20位的字母和数字'))
        } else {
          let url = '/' + value + '/1'
          API.checkExsit(url).then(res => {
            if (res.data.data) {
              callback()
            } else {
              callback(new Error('用户名已存在'))
            }
          })
        }
      }
    }
    var pass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入密码'))
      } else {
        callback()
      }
    }
    var checkPass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'))
      } else if (value !== this.ruleForm.pass) {
        callback(new Error('两次输入密码不一致!'))
      } else {
        callback()
      }
    }
    var newPass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入密码'))
      } else {
        callback()
      }
    }
    var checkNewPass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'))
      } else if (value !== this.ruleForm2.newPass) {
        callback(new Error('两次输入密码不一致!'))
      } else {
        callback()
      }
    }
    var checkEmail = (rule, value, callback) => {
      let reg = new RegExp(/^([a-zA-Z0-9._-])+@([a-zA-Z0-9_-])+(\.[a-zA-Z0-9_-])+/)
      if (!value) {
        callback(new Error('邮箱不能为空'))
      } else {
        if (reg.test(value)) {
          callback()
        } else {
          callback(new Error('邮箱格式错误'))
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
            if (res.status === 200) {
              this.$message({
                type: 'success',
                message: '注册成功'
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
                message: '注册失败'
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
                  message: '旧密码输入错误'
                })
              } else {
                this.$message({
                  type: 'success',
                  message: '密码修改成功'
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
            message: '登录失败'
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
