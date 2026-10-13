<template>
  <div class="video-detail">
    <div class="video-header">
      <div class="video-title">
        <div class="title">{{ videoInfo.videoName }}</div>
        <div class="video-info">
          <div class="iconfont icon-play2">{{ videoInfo.playCount }}</div>
          <div class="iconfont icon-danmu">{{ videoInfo.barrageCount }}</div>
          <div class="iconfont">{{ videoInfo.createTime }}</div>
        </div>
      </div>
      <div class="video-user-info">
        <Avatar :userId="userInfo.userId" :avatar="userInfo.avatar"></Avatar>
        <div class="user-info">
          <router-link
            class="nick-name"
            :to="`/user/${userInfo.userId}`"
            target="_blank"
            >{{ userInfo.userName }}</router-link
          >
          <div class="introduction">
            {{ userInfo.selfIntroduction || "这个人没有填简介啊~~~~" }}
          </div>
          <div class="op-btns">
            <router-link
              class="btn-go-home"
              :to="`/user/${userInfo.userId}`"
              target="_blank"
              >访问主页</router-link
            >
            <div class="focus-btn">
              <el-dropdown v-if="userInfo.haveFocus">
                <el-button class="btn" type="info" :style="{ width: '100%' }">
                  <span class="iconfont icon-list"></span>已关注
                  {{ userInfo.fansCount }}
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="focusUser(-1)"
                      >取消关注</el-dropdown-item
                    >
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-button class="btn" type="primary" @click="focusUser(1)" v-else
                >关注 {{ userInfo.fansCount }}</el-button
              >
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="video-body">
      <div class="video-left">
        <div
          class="video-panel"
          :style="{ position: wideScreen ? 'absolute' : 'static' }"
        >
          <Player
            ref="playerRef"
            @changeWideScreen="changeWideScreenHandler"
          ></Player>
        </div>
        <div
          :style="{
            'margin-top': wideScreen ? playerHeight + 56 + 'px' : '0px',
          }"
        >
          <VideoAction></VideoAction>
          <VideoSummary v-if="Object.keys(videoInfo).length > 0"></VideoSummary>
          <VideoComment></VideoComment>
        </div>
      </div>
      <div
        class="video-right"
        :style="{ 'margin-top': wideScreen ? playerHeight + 70 + 'px' : '0px' }"
      >
        <VideoDanmu></VideoDanmu>
        <VideoPList></VideoPList>
        <VideoRecommend
          v-if="Object.keys(videoInfo).length > 0"
        ></VideoRecommend>
      </div>
    </div>
  </div>
</template>
<script setup>
import VideoRecommend from "./VideoRecommend.vue";
import VideoComment from "./VideoComment.vue";
import VideoSummary from "./VideoSummary.vue";
import { ACTION_TYPE } from "@/utils/Constants";
import VideoAction from "./VideoAction.vue";
import VideoDanmu from "./VideoDanmu.vue";
import VideoPList from "./VideoPList.vue";
import Player from "@/components/Player.vue";
import moment from "moment";
import {
  ref,
  reactive,
  getCurrentInstance,
  nextTick,
  onMounted,
  provide,
} from "vue";

const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router";
const route = useRoute();
const router = useRouter();
import { useNavAction } from "@/stores/navActionStore";
const navActionStore = useNavAction();

import { useLoginStore } from "@/stores/loginStore.js";
const loginStore = useLoginStore();

const videoInfo = ref({});
const userInfo = ref({});

const getUserInfo = async (userId) => {
  let result = await proxy.Request({
    url: proxy.Api.uHomeGetUsesrInfo,
    params: {
      userId,
    },
  });
  if (!result) {
    return;
  }
  userInfo.value = result.data;
};

const focusUser = async (changeCount) => {
  if (Object.keys(loginStore.userInfo).length == 0) {
    loginStore.setLogin(true);
    return;
  }
  let result = await proxy.Request({
    url: changeCount == 1 ? proxy.Api.uHomeFocus : proxy.Api.uHomeCancelFocus,
    params: {
      focusUserId: userInfo.value.userId,
    },
  });
  if (!result) {
    return;
  }
  if (changeCount == 1) {
    userInfo.value.haveFocus = true;
    userInfo.value.fansCount++;
  } else {
    userInfo.value.haveFocus = false;
    userInfo.value.fansCount--;
  }
};

const getVideoInfo = async () => {
  let result = await proxy.Request({
    url: proxy.Api.getVideoInfo,
    params: {
      videoId: route.params.videoId,
    },
  });
  if (!result) {
    return;
  }
  const resultData = result.data.videoInfo;
  getUserInfo(resultData.userId);
  videoInfo.value = resultData;
  const date = videoInfo.value.createTime;
  videoInfo.value.createTime = moment(date).format("YYYY-MM-DD HH:mm");
  const tags = resultData.tags ? resultData.tags.split(",") : [];
  resultData.tags = tags;
  // let introduction = proxy.Utils.resetHtmlContent(introduction);
  // resultData.introduction = introduction;

  //获取用户行为
  const userActionList = result.data.userAction;
  userActionList.forEach((item) => {
    if (item.actionType == ACTION_TYPE.VIDEO_LIKE.value) {
      videoInfo.value.likeCountActive = true;
    } else if (item.actionType == ACTION_TYPE.VIDEO_COLLECT.value) {
      videoInfo.value.collectCountActive = true;
    } else if (item.actionType == ACTION_TYPE.VIDEO_COIN.value) {
      videoInfo.value.coinCountActive = true;
    }
  });
};
getVideoInfo();

provide("videoInfo", videoInfo);

onMounted(() => {
  nextTick(() => {
    navActionStore.setShowHeader(false);
    navActionStore.setFixedHeader(true);
    navActionStore.setFixedCategory(false);
    navActionStore.setShowCategory(false);
    navActionStore.setForceFixedHeader(true);
  });
});

const wideScreen = ref(false);
const changeWideScreenHandler = (result) => {
  wideScreen.value = result;
};

const playerHeight = ref(0);
provide("playerHeight", (height) => {
  playerHeight.value = height;
});
</script>

<style lang="scss" scoped>
.video-detail {
  margin-top: 64px;
  min-height: calc(100vh - 64px);
  padding: 0 150px;
  .video-header {
    padding-top: 20px;
    display: flex;
    .video-title {
      flex: 1;
      .title {
        font-size: 22px;
      }
      .video-info {
        margin-top: 10px;
        display: flex;
        color: #9499a0;
        .iconfont {
          margin-right: 20px;
          font-size: 14px;
          &::before {
            font-size: 18px;
            margin-right: 3px;
          }
        }
      }
    }
    .video-user-info {
      margin-left: 20px;
      width: 410px;
      display: flex;
      align-items: center;
      .user-info {
        flex: 1;
        margin-left: 10px;
        .nick-name {
          font-size: 16px;
          text-overflow: ellipsis;
          white-space: nowrap;
          overflow: hidden;
          color: var(--text);
          text-decoration: none;
        }
        .introduction {
          color: var(--text2);
          font-size: 13px;
          text-overflow: ellipsis;
          white-space: nowrap;
          overflow: hidden;
          margin: 3px 0px;
        }
        .op-btns {
          display: flex;
          .btn-go-home {
            margin-right: 10px;
            text-decoration: none;
            color: #fb7299;
            border: 1px solid #fb7299;
            line-height: 28px;
            border-radius: 5px;
            padding: 0px 20px;
            &:hover {
              background: #ffecf1;
            }
          }
          .focus-btn {
            flex: 1;
            width: 230px;
            .el-dropdown {
              width: 100%;
            }
            .btn {
              width: 100%;
              .iconfont {
                margin-right: 5px;
              }
            }
          }
        }
      }
    }
  }
  .video-body {
    margin-top: 20px;
    position: relative;
    display: flex;
    .video-left {
      flex: 1;
    }
    .video-panel {
      position: static;
      width: 100%;
    }
    .video-right {
      margin-left: 30px;
      width: 410px;
      padding-bottom: 20px;
    }
  }
}
</style>
