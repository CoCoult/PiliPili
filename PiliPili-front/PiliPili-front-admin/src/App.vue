<template>
  <el-config-provider :locale="zhCn" :message="config">
    <router-view></router-view>
  </el-config-provider>
</template>

<script setup>
import zhCn from "element-plus/dist/locale/zh-cn.mjs";

import {
  ref,
  reactive,
  getCurrentInstance,
  nextTick,
  onBeforeMount,
} from "vue";
import FingerprintJS from "@fingerprintjs/fingerprintjs";
const { proxy } = getCurrentInstance();
const config = ref({
  max: 1,
});

</script>

<style scoped>
</style>
