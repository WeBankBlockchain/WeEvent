<template>
  <div class='registered'>
    <div class='registered_part'>
      <img src="../assets/image/login.png" alt="">
      <el-form :model="ruleForm2" status-icon :rules="rules2" ref="ruleForm2" class="demo-ruleForm" >
        <el-form-item :label="$t('userSet.userName')">
          <el-input v-model.trim="ruleForm2.name" disabled></el-input>
        </el-form-item>
         <el-form-item :label="$t('userSet.newPassWord')" prop="newPass" >
          <el-input type="password" v-model.trim="ruleForm2.newPass" autocomplete="off"></el-input>
        </el-form-item>
         <el-form-item :label="$t('userSet.enterAgain')" prop="checkNewPass" >
          <el-input type="password" v-model.trim="ruleForm2.checkNewPass" autocomplete="off"></el-input>
        </el-form-item>
         <el-form-item>
          <el-button type="primary" @click="submit('ruleForm2')">{{$t('userSet.resetPassWord')}}</el-button>
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
          let data = {
            username: this.ruleForm2.name,
            password: this.ruleForm2.newPass
          }
          API.reset(data).then(res => {
            if (res.status === 200 && res.data.data) {
              this.$message({
                type: 'success',
                message: this.$t('userSet.passWordModifySuccess')
              })
              this.login(data)
            } else {
              this.$message({
                type: 'warning',
                message: this.$t('common.operFail')
              })
            }
          })
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
            message: this.$t('userSet.loginFail')
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
