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
import { useRoute, useRouter } from "vue-router";
import VueCookies from "vue-cookies";
import { Api } from "@/utils/Api";
import Request from "@/utils/Request";
import { useLoginStore } from "@/stores/loginStore.js";
const loginStore = useLoginStore();
import { useCategoryStore } from "@/stores/CategoryStore.js";
const CategoryStore = useCategoryStore();

import { useSysSettingStore } from "@/stores/SysSettingStore.js";
const sysSettingStore = useSysSettingStore();
const route = useRoute();
const router = useRouter();

const config = ref({
  max: 1,
});

//获取系统设置信息
const getSysSetting = async () => {
  let result = await proxy.Request({
    url: Api.getSysSetting,
  });
  if (!result) {
    return;
  }
  sysSettingStore.saveSetting(result.data);
};

const autoLogin = async () => {
  const token = VueCookies.get("token");
  if (!token) {
    return;
  }
  let result = await Request({
    url: Api.autoLogin,
  });
  if (!result) {
    return;
  }
  saveLoinInfo(result.data);
};

const saveLoinInfo = (loginInfo) => {
  if (!loginInfo) {
    loginStore.saveUserInfo({});
  } else {
    loginStore.saveUserInfo(loginInfo);
  }
};

const getDeviceId = async () => {
  let deviceId = VueCookies.get("deviceId");
  if (!deviceId) {
    const fpPromise = await FingerprintJS.load();
    const result = await fpPromise.get();
    deviceId = result.visitorId;
    VueCookies.set("deviceId", deviceId, -1);
  }
  loginStore.saveDeviceId(deviceId);
};

onBeforeMount(() => {
  autoLogin();
  loadCategory();
  getDeviceId();
  getSysSetting();
});

let categoryList = [];
let categoryMap = {};

const loadCategory = async () => {
  let result = await Request({
    url: Api.getCategory,
  });
  if (!result) {
    return;
  }
  categoryList = result.data;
  result.data.forEach((element) => {
    categoryMap[element.categoryCode] = element;
    element.children.forEach((sub) => {
      categoryMap[sub.categoryCode] = sub;
    });
  });
  CategoryStore.saveCategoryMap(categoryMap);
  CategoryStore.saveCategoryList(categoryList);
  console.log(CategoryStore.categoryMap);
};
</script>

<style scoped>
</style>
