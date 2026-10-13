<template>
  <div
    class="main-container"
    :style="{
      'max-width': proxy.bodyMaxWidth + 'px',
      'mim-width': proxy.bodyMinWidth + 'px',
    }"
  >
    <div
      class="header"
      v-show="navActionStore.showHeader"
      :style="{
        'background-image': `url(${
          backgroundImage
            ? backgroundImage
            : proxy.Utils.getLocalImage('banner_bg2.png')
        })`,
      }"
    >
      <LayoutHeader :hotSearchList="hotSearchList"></LayoutHeader>
    </div>
    <!-- v-if="showFixedHeader" -->
    <div
      class="header-fixed"
      v-if="
        (navActionStore.fixedHeader && showFixedHeader) ||
        navActionStore.forceFixedHeader
      "
    >
      <LayoutHeader :hotSearchList="hotSearchList" theme="dark"></LayoutHeader>
    </div>
    <!--固定在顶部-->
    <!-- v-show="navActionStore.fixedCategory && showFixedCategory" -->
    <div
      class="category-fixed"
      v-show="navActionStore.fixedCategory && showFixedCategory"
      :style="{
        'max-width': proxy.bodyMaxWidth + 'px',
        'min-width': proxy.bodyMinWidth + 'px',
      }"
      @mouseover="lineCategoryMouseHandler(1)"
      @mouseout="lineCategoryMouseHandler(0)"
    >
      <div class="category-fixed-inner">
        <Category :showType="1" :mouseOver="mouseOver"></Category>
      </div>
    </div>
    <div
      class="body-container"
      :style="{
        'max-width': proxy.bodyMaxWidth + 'px',
        'min-width': proxy.bodyMinWidth + 'px',
      }"
    >
      <div class="category" v-show="navActionStore.showCategory">
        <Category :showType="0"></Category>
      </div>
      <div class="body-main">
        <router-view :key="$route.fullPath"></router-view>
      </div>
    </div>
    <Account></Account>
  </div>
</template>

<script setup>
import { mitter } from "@/eventbus/eventBus.js";
import Account from "@/views/account/Account.vue";
import LayoutHeader from "../layout/LayoutHeader.vue";
import Category from "../layout/Category.vue";
import {
  ref,
  reactive,
  getCurrentInstance,
  nextTick,
  onMounted,
  onUnmounted,
  computed,
  watch,
} from "vue";
const { proxy } = getCurrentInstance();
import { useRoute, useRouter, onBeforeRouteUpdate } from "vue-router";

import { useNavAction } from "@/stores/navActionStore";
const navActionStore = useNavAction();

import { useCategoryStore } from "@/stores/CategoryStore";

import { useLoginStore } from "@/stores/loginStore.js";
const loginStore = useLoginStore();
const route = useRoute();
const router = useRouter();

onMounted(() => {
  console.log(111);
  window.addEventListener("scroll", windowScrollHandler);
  window.addEventListener("resize", windowScrollHandler);
  navActionStore.setShowHeader(true);
  navActionStore.setFixedHeader(true);
  navActionStore.setShowCategory(true);
  navActionStore.setFixedCategory(true);
  navActionStore.setForceFixedHeader(false);
});
// 响应路由更新
onBeforeRouteUpdate((to, from, next) => {
  if (from.path == "/search") {
    navActionStore.setShowHeader(true);
    navActionStore.setFixedHeader(true);
    navActionStore.setShowCategory(true);
    navActionStore.setFixedCategory(true);
    navActionStore.setForceFixedHeader(false); // 在路由更新时重新初始化
  }
  next();
});
onUnmounted(() => {
  window.removeEventListener("scroll", windowScrollHandler);
  window.removeEventListener("resize", windowScrollHandler);
});

const showFixedHeader = ref(false);
const showFixedCategory = ref(false);

const showFixedTopHandler = (scrollTop) => {
  if (scrollTop < 40) {
    showFixedHeader.value = false;
  } else {
    showFixedHeader.value = true;
  }
  if (scrollTop > 250) {
    showFixedCategory.value = true;
  } else {
    showFixedCategory.value = false;
  }
};

const windowScrollHandler = () => {
  const scrollTop = window.scrollY;
  showFixedTopHandler(scrollTop);
  mitter.emit("windowScroll", scrollTop);
};

const mouseOver = ref(false);
const lineCategoryMouseHandler = (type) => {
  if (type == 1) {
    mouseOver.value = true;
  } else {
    mouseOver.value = false;
  }
};

const windowResizeHandler = () => {
  mitter.emit("windowResize");
};
const backgroundImage = computed(() => {
  const background = useCategoryStore.currentPCategory
    ? useCategoryStore.currentPCategory.background
    : null;
  if (background) {
    return proxy.Api.sourcePath + background;
  } else {
    return null;
  }
});

const hotSearchList = ref([]);
const getSearchKeywordTop = async () => {
  let result = await proxy.Request({
    url: proxy.Api.getSearchKeywordTop,
  });
  if (!result) {
    return;
  }
  hotSearchList.value = result.data;
};
getSearchKeywordTop();

const getNoReadCount = async () => {
  if (Object.keys(loginStore.userInfo).length == 0) {
    return;
  }
  let result = await proxy.Request({
    url: proxy.Api.getNoReadCount,
  });
  if (!result) {
    return;
  }
  loginStore.saveMessageNoReadCount(result.data);
};

watch(
  () => loginStore.userInfo,
  (newVal, oldVal) => {
    if (newVal) {
      getNoReadCount();
    }
  },
  { immediate: true, deep: true }
);
</script>

<style>
body {
  background: #ebe9e9 !important;
}
</style>

<style lang = "scss" scoped>
.main-container {
  position: relative;
  background: #fff;
  margin: 0px auto;
  min-height: calc(100vh);
  .header {
    margin: 0px auto;
    background-color: #7c9ce1;
    width: 100%;
    height: 180px;
    background-position: center;
    background-repeat: no-repeat;
    position: relative;
  }

  .header-fixed {
    position: fixed;
    width: 100%;
    z-index: 1001;
    top: 0px;
    background: #fff;
    height: 64px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
  }

  .category-fixed {
    position: fixed;
    z-index: 1000;
    top: 64px;
    width: 100%;
    background: #fff;
    padding: 10px 150px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
    .category-fixed-inner {
      margin: 0px auto;
    }
  }
  .body-container {
    padding: 0px var(--bodypadding);
    .category {
      margin-top: 20px;
      padding: 0 150px;
    }
  }
}

@media screen and (max-width: 1500px) {
  .main-container {
    .body-container {
      padding: 0px 60px;
    }
  }
}
</style>
