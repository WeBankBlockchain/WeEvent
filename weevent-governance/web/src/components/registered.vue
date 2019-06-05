<template>
  <div class='registered'>
    <div class="header_part">
      <div class='logo_part'>
        <img src="../assets/image/weEvent.png" alt="">
        <img src="../assets/image/backhome.svg" alt="" @click='goBack'>
      </div>
    </div>
    <div class='registered_part'>
      <h3>
        {{reset ?'用户注册':'修改用户密码'}}
      </h3>
      <el-form :model="ruleForm"  label-position='top' status-icon :rules="rules" ref="ruleForm" label-width="80px" class="demo-ruleForm" v-show='reset'>
        <el-form-item label="用户名:" prop="name">
          <el-input v-model="ruleForm.name" ></el-input>
        </el-form-item>
        <el-form-item label="密码:" prop="pass">
          <el-input type="password" v-model="ruleForm.pass" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="确认密码:" prop="checkPass" >
          <el-input type="password" v-model="ruleForm.checkPass" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item
          label='邮箱:'
          prop='email'
          v-show='reset'
          >
          <el-input type="email" v-model="ruleForm.email" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item v-show='reset'>
          <el-button type="success" @click="submitForm('ruleForm')">提交</el-button>
        </el-form-item>
      </el-form>

      <el-form :model="ruleForm2"  label-position='top' status-icon :rules="rules2" ref="ruleForm2" label-width="80px" class="demo-ruleForm" v-show='!reset'>
        <el-form-item label="用户名:" prop="name">
          <el-input v-model="ruleForm2.name" disabled></el-input>
        </el-form-item>
        <el-form-item label="密码:" prop="pass">
          <el-input type="password" v-model="ruleForm2.pass" autocomplete="off"></el-input>
        </el-form-item>
         <el-form-item label="输入新密码:" prop="newPass" >
          <el-input type="password" v-model="ruleForm2.newPass" autocomplete="off"></el-input>
        </el-form-item>
         <el-form-item label="再次输入新密码:" prop="checkNewPass" >
          <el-input type="password" v-model="ruleForm2.checkNewPass" autocomplete="off"></el-input>
        </el-form-item>
         <el-form-item>
          <el-button type="primary" @click="submit('ruleForm2')">修改</el-button>
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
      if (!value) {
        return callback(new Error('用户名不能为空'))
      } else {
        let url = '/' + value + '/1'
        API.checkExsit(url).then(res => {
          if (res.data) {
            callback()
          } else {
            callback(new Error('用户名已存在'))
          }
        })
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
          let url = '/' + value + '/2'
          API.checkExsit(url).then(res => {
            if (res.data) {
              callback()
            } else {
              callback(new Error('该邮箱已存在'))
            }
          })
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
        name: sessionStorage.getItem('user'),
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
        name: [
          { validator: checkName, trigger: 'blur' }
        ],
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
        console.log(valid)
        if (valid) {
          let data = {
            'username': this.ruleForm.name,
            'password': this.ruleForm.pass,
            'email': this.ruleForm.email
          }
          API.register(data).then(res => {
            console.log(res)
            if (res.status === 200) {
              this.$message({
                type: 'success',
                message: '注册成功'
              })
              this.$router.push('./')
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
    }
  },
  computed: {
    reset () {
      return Number(this.$route.query.reset)
    }
  }
}
</script>
