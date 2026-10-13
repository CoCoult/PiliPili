<template>
  <div class="category-video-body">
    <div
      :class="['category-list', categoryFixed ? 'categoryFixed' : '']"
      id="category-list"
    >
      <div class="category-title">
        {{ CategoryStore.currentPCategory.categoryName }}
      </div>
      <div
        :class="['category-item', !route.params.categoryCode ? 'active' : '']"
        @click="jump(item)"
      >
        首页
      </div>
      <div
        :class="[
          'category-item',
          route.params.categoryCode == item.categoryCode ? 'active' : '',
        ]"
        v-for="item in CategoryStore.currentPCategory.children"
        :key="item"
        @click="jump(item)"
      >
        {{ item.categoryName }}
      </div>
    </div>
    <VideoList></VideoList>
  </div>
</template>

<script setup>
import { mitter } from "@/eventbus/eventBus.js";
import VideoList from "@/views/videoList/VideoList.vue";
import { ref, reactive, getCurrentInstance, nextTick, onMounted } from "vue";
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router";
const route = useRoute();
const router = useRouter();

import { useCategoryStore } from "@/stores/CategoryStore";
const CategoryStore = useCategoryStore();

import { useNavAction } from "@/stores/navActionStore";
const navActionStore = useNavAction();

const jump = (item = { categoryCode: "" }) => {
  if (!item.categoryCode) {
    router.push({
      name: "categoryVideo",
    });
    return;
  }
  router.push({
    name: "subCategoryVideo",
    params: {
      categoryCode: item.categoryCode,
    },
  });
};

const categoryFixed = ref(false);
const categoryTopDistance = ref(200);

let initSCrollTop = 0;
let scrollDown = true;

const scrollHandler = (curScrollTop) => {
  categoryFixed.value = true;
  if (curScrollTop - initSCrollTop > 0) {
    scrollDown = true;
  } else {
    scrollDown = false;
  }
  initSCrollTop = curScrollTop;
  if (curScrollTop >= categoryTopDistance.value) {
    if (scrollDown) {
      navActionStore.setFixedHeader(false);
    } else {
      navActionStore.setFixedHeader(true);
    }
  } else {
    categoryFixed.value = false;
    navActionStore.setFixedHeader(false);
  }
};

onMounted(() => {
  categoryTopDistance.value = document
    .querySelector("#category-list")
    .getBoundingClientRect().top;

  navActionStore.setShowHeader(true);
  navActionStore.setFixedHeader(false);
  navActionStore.setShowCategory(true);
  navActionStore.setFixedCategory(false);

  mitter.on("windowScroll", (curScrollTop) => {
    scrollHandler(curScrollTop);
  });
});
</script>

<style lang="scss" scoped>
.category-video-body {
  margin-top: 30px;
  .category-list {
    display: flex;
    align-items: center;
    line-height: 30px;
    position: sticky;
    top: 0px;
    height: 60px;
    background: #fff;
    z-index: 2;
    .category-title {
      padding-left: 150px;
      font-size: 24px;
      margin-right: 40px;
      cursor: pointer;
    }
    .category-item {
      margin-right: 40px;
      font-size: 15px;
      cursor: pointer;
      border-bottom: 3px solid #fff;
      &:hover {
        color: var(--blue);
        border-color: var(--blue);
      }
    }
    .active {
      color: var(--blue);
      border-color: var(--blue);
    }
  }
  .category-fixed {
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
  }
}
</style>
