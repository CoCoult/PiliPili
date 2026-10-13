<template>
  <div class="video-list-panel">
    <div class="my-video-list-title" v-if="serieslist.length > 0">
      <router-link class="title" :to="`/user/${route.params.userid}/series`"
        >我的视频列表</router-link
      >
      <span
        class="iconfont icon-add op-btn new-btn"
        @click="showVideoSeries"
        v-if="isMyself"
        >新建</span
      >
    </div>
    <div class="part-item" v-for="item in serieslist">
      <div class="part-title">
        <div class="title-panel">
          <router-link
            class="title"
            :to="`/user/${route.params.userid}/series/${item.seriesId}`"
            >{{ item.seriesName }}</router-link
          >
          <div class="count-info">{{ item.videoInfoList.length }}</div>
        </div>
        <router-link
          class="op-btn"
          :to="`/user/${route.params.userid}/series/${item.seriesId}`"
          >更多&gt;</router-link
        >
      </div>
      <div class="video-list5">
        <VideoItem
          :data="video"
          v-for="(video, index) in item.videoInfoList"
          :key="index"
        ></VideoItem>
      </div>
    </div>
  </div>
  <VideoSeriesEdit ref="VideoSeriesEditRef" @reload="loadSeriesList"></VideoSeriesEdit>
</template>

<script setup>
import VideoSeriesEdit from "./VideoSeriesEdit.vue";
import VideoItem from "./VideoItem.vue";
import {
  ref,
  reactive,
  getCurrentInstance,
  nextTick,
  onMounted,
  computed,
} from "vue";
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router";
const route = useRoute();
const router = useRouter();

import { useLoginStore } from "@/stores/loginStore.js";
const loginStore = useLoginStore();

const isMyself = computed(() => {
  return route.params.userId == loginStore.userInfo.userId;
});

const serieslist = ref([]);
const loadSeriesList = async () => {
  let result = await proxy.Request({
    url: proxy.Api.uHomeSeriesLoadVideoSeriesWithVideo,
    params: {
      userId: route.params.userId,
    },
  });
  if (!result) {
    return;
  }
  serieslist.value = result.data;
};
loadSeriesList();

const VideoSeriesEditRef = ref()
const showVideoSeries = () => {};
</script>

<style lang="scss" scoped>
.my-video-list-title {
  margin-top: 15px;
  .title {
    text-decoration: none;
    color: var(--text);
    font-size: 16px;
  }
  .new-btn {
    margin-left: 10px;
    padding: 5px 10px;
    cursor: pointer;
  }
}
.part-title {
  .title-panel {
    .title {
      font-size: 15px;
    }
  }
}
</style>
