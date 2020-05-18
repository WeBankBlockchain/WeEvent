<template>
  <div class='ruleDes'>
    <h3>
     {{$t('ruleDes.desTitle')}}
    </h3>
    <el-timeline>
      <el-timeline-item :timestamp="$t('ruleDes.creatOption')" placement="top">
        <el-card v-show="active === 1">
          <p>{{$t('ruleDes.creatOptionDes')}}</p>
          <img src="../assets/image/step1.png" alt="">
        </el-card>
      </el-timeline-item>
      <el-timeline-item :timestamp="$t('ruleDes.editOption')" placement="top">
        <el-card v-show="active === 2">
          <p v-html="$t('ruleDes.editOptionDes')"></p>
          <img src="../assets/image/step2.png" alt="">
        </el-card>
      </el-timeline-item>
      <el-timeline-item :timestamp="$t('ruleDes.optionMap')" placement="top">
        <el-card v-show="active === 3">
          <p v-html="$t('ruleDes.optionMapDes')"></p>
          <img src="../assets/image/step3.png" alt="">
        </el-card>
      </el-timeline-item>
    </el-timeline>
    <el-button type="primary" @click='active > 1 ? active-- : active' :disabled="active === 1">back</el-button>
    <el-button type="primary" @click='active < 3 ? active++ : active' :disabled="active === 3">next</el-button>
  </div>
</template>
<script>
export default{
  data () {
    return {
      active: 1
    }
  }
}
</script>
