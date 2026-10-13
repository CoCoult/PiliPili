<template>
  <div class="message-item">
    <Avatar :avatar="data.avatar" :userId="data.sendUserId"></Avatar>
    <div class="user-info-panel">
      <div class="user-info">
        <router-link
          :to="`/user/${data.sendUserId}`"
          class="user-name"
          target="_blank"
        >
          {{ data.userName }}
        </router-link>
        <span class="title-info">{{ convertTitle() }}</span>
      </div>
      <template v-if="data.messageType == 3">
        <div class="comment">{{ data.userMessageDto.messageContents }}</div>
        <div class="reply" v-if="data.userMessageDto.messageContentReply">
          {{ data.userMessageDto.messageContentReply }}
        </div>
      </template>
      <div class="send-time">
        {{ proxy.Utils.formatDate(data.createTime) }}
        <span
          class="iconfont icon-delete"
          @click="delMessage(data.messageId)"
        ></span>
      </div>
    </div>
    <div class="video-cover">
      <router-link :to="`/video/${data.videoId}`" target="_blank">
        <Cover :source="data.videoCover"></Cover>
      </router-link>
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
});

const MESSAGE_TYPE = {
  0: "点赞",
  2: "收藏",
  3: "评论",
};

const convertTitle = () => {
  if (props.data.messageType == 3) {
    if (props.data.userMessageDto.messageContentReply) {
      return `在视频中回复了你的评论`;
    }
    return `在视频中发表了评论`;
  }
  return `${MESSAGE_TYPE[props.data.messageType]}了视频`;
};

const emit = defineEmits(["delMessage"]);
const delMessage = (messageid) => {
  emit("delMessage", messageid);
};


</script>

<style lang="scss" scoped>
.message-item {
  display: flex;
  padding: 10px 5px;
  border-bottom: 1px solid #ddd;
  align-items: center;
  .user-info-panel {
    margin-left: 10px;
    flex: 1;
    .user-info {
      .user-name {
        font-weight: bold;
        color: var(--text);
        text-decoration: none;
      }
      .title-info {
        margin-left: 5px;
        color: var(--text3);
        font-size: 13px;
      }
    }
    .comment {
      font-size: 13px;
      margin-top: 5px;
      color: var(--text2);
    }
    .reply {
      border-left: 2px solid #ddd;
      padding-left: 5px;
      font-size: 12px;
      margin-top: 5px;
    }
    .send-time {
      margin-top: 5px;
      font-size: 12px;
      color: var(--text3);
    }
    .icon-delete {
      font-size: 14px;
      cursor: pointer;
      margin-left: 10px;
    }
  }
  .video-cover {
    margin-left: 10px;
    width: 100px;
  }
}
</style>
