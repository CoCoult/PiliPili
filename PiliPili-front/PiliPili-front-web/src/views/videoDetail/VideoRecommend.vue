<template>
  <template v-for="(item, index) in dataList" :key="index">
    <VideoItem :data="item" :layoutType="1" :marginTop="20"></VideoItem>
  </template>
</template>

<script setup>
import {
  ref,
  reactive,
  getCurrentInstance,
  nextTick,
  onMounted,
  inject,
} from "vue";
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router";
const route = useRoute();
const router = useRouter();

const videoInfo = inject("videoInfo");
const dataList = ref([]);
const loadVideoList = async () => {
  let result = await proxy.Request({
    url: proxy.Api.loadRecommendVideo,
    params: {
      keysword: videoInfo.value.videoName,
      videoId: videoInfo.value.videoId,
    },
  });
  if (!result) {
    return;
  }
  dataList.value = result.data;
};
loadVideoList();
</script>

<style lang="scss" scoped>
</style>
