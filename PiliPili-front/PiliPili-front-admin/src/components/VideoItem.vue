<template>
  <div
    :class="['video-item', layoutType == 1 ? 'video-item2' : '']"
    :style="{
      'margin-top': marginTop + 'px',
    }"
  >
    <router-link :to="`/video/${data.videoId}`" target="_blank">
      <div class="cover">
        <Cover :source="data.videoCover"></Cover>
        <div class="shade">
          <div class="play-count" v-show="layoutType == 0">
            <div class="iconfont icon-play2">{{ data.playCount }}</div>
            <div class="iconfont icon-danmu">{{ data.barrageCount }}</div>
          </div>
          <!-- <div class="play-time">{{ data.lastPlayTime }}</div> -->
        </div>
      </div>
    </router-link>
    <div class="video-info">
      <router-link class="title" :to="`/video/${data.videoId}`"
        ><div class="title" v-html="data.videoName"></div
      ></router-link>
      <router-link
        class="user-name"
        :to="`/user/${data.userId}`"
        target="_blank"
      >
        <span class="iconfont icon-upzhu">{{ data.userName }} · </span>
        <span>{{ proxy.Utils.formatDate(data.createTime) }}</span>
      </router-link>
      <div class="play-count" v-show="layoutType == 1">
        <div class="iconfont icon-play2">{{ data.playCount }}</div>
        <div class="iconfont icon-danmu">{{ data.barrageCount }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, onMounted } from "vue";
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router";
const route = useRoute();
const router = useRouter();

const props = defineProps({
  data: {
    type: Object,
    default: {},
  },
  //布局类型 0：上下布局, 1：左右布局
  layoutType: {
    type: Number,
    default: 0,
  },
  marginTop: {
    type: Number,
    default: 0,
  },
});
</script>

<style lang="scss" scoped>
.video-item {
  width: 100%;
  overflow: hidden;
  .video-info {
    cursor: pointer;
    .user-name {
      margin-top: 5px;
      color: #9499a0;
      font-size: 13px;
      cursor: pointer;
      text-decoration: none;
      &:hover {
        color: var(--blue);
      }
      .iconfont {
        font-size: 13px;
        &::before {
          font-size: 18px;
          margin-right: 3px;
          float: left;
        }
      }
    }
    .title {
      height: 40px;
      color: var(--text2);
      font-size: 14px;
      margin-top: 10px;
      display: -webkit-box;
      overflow: hidden;
      text-decoration: none;
      -webkit-box-orient: vertical;
      text-overflow: -o-ellipsis-lastline;
      text-overflow: ellipsis;
      word-break: break-word !important;
      word-break: break-all;
      line-break: anywhere;
      -webkit-line-clamp: 2;
      cursor: pointer;
      &:hover {
        color: var(--blue);
      }
    }
    :deep(.highlight) {
      color: red !important;
    }
  }
  .cover {
    cursor: pointer;
    position: relative;
    overflow: hidden;
    .image-style {
      width: 100%;
      height: 100%;
      overflow: hidden;
      border-radius: 5px;
    }
    .shade {
      position: absolute;
      bottom: 0;
      left: 0;
      z-index: 1;
      box-sizing: border-box;
      padding: 8px8px6px;
      width: 100%;
      height: 38px;
      border-bottom-right-radius: 6px;
      border-bottom-left-radius: 6px;
      background-image: linear-gradient(
        180deg,
        rgba(0, 0, 0, 0) 0%,
        rgba(0, 0, 0, 0.8) 100%
      );
      color: #fff;
      opacity: 1;
      display: -webkit-flex;
      display: flex;
      align-items: center;
      justify-content: space-between;
      .play-count {
        display: flex;
        .iconfont {
          font-size: 13px;
          &::before {
            font-size: 16px;
            margin-right: 2px;
          }
        }
        .icon-danmu {
          margin-left: 15px;
        }
      }
    }
  }
}
//

.video-item2 {
  display: flex;
  .cover {
    width: 190px;
    .shade {
      justify-content: end;
    }
  }
  .video-info {
    flex: 1;
    margin-left: 15px;
    .title {
      margin-top: 0px;
    }
    .play-count {
      display: flex;
      width: 100%;
      margin-top: 5px;
      color: #9499a0;
      .iconfont {
        font-size: 14px;
        &::before {
          font-size: 16px;
          margin-right: 2px;
        }
      }
      .icon-danmu {
        margin-left: 15px;
      }
    }
  }
}
</style>
