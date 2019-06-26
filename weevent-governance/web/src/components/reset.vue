<template>
  <div class='registered'>
    <div class='registered_part'>
      <img src="../assets/image/login.png" alt="">
      <p style='margin:5px 0 30px'>区块链应用平台</p>
      <el-form :model="ruleForm2" status-icon :rules="rules2" ref="ruleForm2" class="demo-ruleForm" >
        <el-form-item label="用户名">
          <el-input v-model.trim="ruleForm2.name" disabled></el-input>
        </el-form-item>
         <el-form-item label="新密码" prop="newPass" >
          <el-input type="password" v-model.trim="ruleForm2.newPass" autocomplete="off"></el-input>
        </el-form-item>
         <el-form-item label="再次输入" prop="checkNewPass" >
          <el-input type="password" v-model.trim="ruleForm2.checkNewPass" autocomplete="off"></el-input>
        </el-form-item>
         <el-form-item>
          <el-button type="primary" @click="submit('ruleForm2')">重置密码</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>
<script>
import API from '../API/resource'
export default {
  data () {
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
    return {
      ruleForm2: {
        name: '',
        newPass: '',
        checkNewPass: ''
      },
      rules2: {
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
    submit (formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
        } else {
          return false
        }
      })
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
  mounted () {
    this.ruleForm2.name = this.$route.query.userName
  }
}
</script>
