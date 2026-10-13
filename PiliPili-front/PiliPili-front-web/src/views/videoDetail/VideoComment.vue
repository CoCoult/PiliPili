<template>
  <div class="comment-panel">
    <div class="comment-title">
      <div class="title">
        评论<span class="comment-count"> {{ dataSource.total }} </span>
      </div>
      <div
        :class="['order-type-item', orderType == 0 ? 'active' : '']"
        @click="changeOrder(0)"
      >
        最热
      </div>
      <el-divider direction="vertical" />
      <div
        :class="['order-type-item', orderType == 1 ? 'active' : '']"
        @click="changeOrder(1)"
      >
        最新
      </div>
    </div>
    <div class="comment-content-panel">
      <VideoCommentSend :sendType="0"></VideoCommentSend>
    </div>
    <div class="comment-list">
      <DataLoadMoreList
        :dataSource="dataSource"
        :loading="loadingData"
        @loadData="loadCommentList"
        layoutType="list"
        loadingEndMsg="没有更多评论"
      >
        <template #default="{ data }">
          <VideoCommentItem :data="data"></VideoCommentItem>
        </template>
      </DataLoadMoreList>
    </div>
  </div>
</template>

<script setup>
import {
  ref,
  reactive,
  getCurrentInstance,
  nextTick,
  onMounted,
  inject,
  computed,
  provide,
  onUnmounted,
} from "vue";
import { ACTION_TYPE } from "@/utils/Constants";
import { mitter } from "@/eventbus/eventBus";
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router";
import VideoCommentItem from "./VideoCommentItem.vue";
import VideoCommentSend from "./VideoCommentSend.vue";
const route = useRoute();
const router = useRouter();

const videoInfo = inject("videoInfo");

const showReplyHandler = (commentId) => {
  dataSource.value.rows.forEach((item) => {
    if (item.commentId == commentId) {
      item.showReply = true;
    } else {
      item.showReply = false;
    }
  });
};

provide("showReply", showReplyHandler);

const showComment = computed(() => {
  return (
    videoInfo.value.interaction == null ||
    videoInfo.value.interaction.indexOf("1") == -1
  );
});
const dataSource = ref({
  pageNum: 1,
  rows: [],
});
const loadingData = ref(true);
const orderType = ref(0);
const changeOrder = (type) => {
  orderType.value = type;
  loadCommentList();
};

const loadCommentList = async () => {
  if (!showComment.value) {
    return;
  }
  let result = await proxy.Request({
    url: proxy.Api.loadComment,
    params: {
      videoId: route.params.videoId,
      pageNum: dataSource.value.pageNum,
      orderType: orderType.value,
    },
  });
  loadingData.value = false;
  if (!result) {
    return;
  }

  const userActionMAp = {};
  const userActionList = result.data.userActionList;
  userActionList.forEach((item) => {
    userActionMAp[item.commentId] = item;
  });

  const commentData = result.data.commentList;
  commentData.rows.forEach((item) => {
    setCommentActive(userActionMAp, item);
    if (item.children) {
      item.children.forEach((sub) => {
        setCommentActive(userActionMAp, sub);
      });
    }
  });

  const dataList = dataSource.value.rows;
  dataSource.value = Object.assign({}, commentData);
  if (commentData.pageNum > 1) {
    dataSource.value.rows = dataList.concat(commentData.rows);
  }
};

const setCommentActive = (userActionMAp, item) => {
  const userAction = userActionMAp[item.commentId];
  if (userAction) {
    if (ACTION_TYPE.COMMENT_LIKE.value == userAction.actionType) {
      item.likeCountActive = true;
    } else if (ACTION_TYPE.COMMENT_HATE.value == userAction.actionType) {
      item.hateCountActive = true;
    }
  }
};
loadCommentList();

onMounted(() => {
  mitter.on("topCommentCallback", () => {
    loadCommentList();
  });
  mitter.on("delCommentCallback", ({ pcommentId, commentId }) => {
    if (pcommentId == 0) {
      dataSource.value.rows = dataSource.value.rows.filter((item) => {
        return item.commentId != commentId;
      });
      dataSource.value.total--;
    } else {
      const pComment = dataSource.value.rows.find((item) => {
        return item.commentId == pcommentId;
      });
      pComment.children = pComment.children.filter((item) => {
        return item.commentId != commentId;
      });
    }
  });
  mitter.on("postCommentSuccess", (comment) => {
    if (comment.pcommentId === 0) {
      dataSource.value.rows.unshift(comment);
      if (!dataSource.value.total) {
        dataSource.value.total = 1;
      } else {
        dataSource.value.total++;
      }
    } else {
      const curComment = dataSource.value.rows.find((item) => {
        return (item.commentId == comment.pcommentId);
      });
      if (!curComment) {
        return;
      }
      if (!curComment.children) {
        curComment.children = [];
      }
      curComment.children.push(comment);
    }
  });
});

onUnmounted(() => {
  mitter.off("topCommentCallback");
  mitter.off("delCommentCallback");
});
</script>

<style lang="scss" scoped>
.comment-panel {
  margin-top: 20px;
  .comment-title {
    display: flex;
    align-items: center;
    font-size: 15px;
    .title {
      font-size: 20px;
      font-weight: 500;
      .comment-count {
        margin-left: 5px;
        font-size: 14px;
        margin-right: 30px;
        color: var(--text2);
      }
    }
    .order-type-item {
      cursor: pointer;
    }
    .active {
      color: var(--blue);
    }
  }
  .comment-content-panel {
    padding-left: 10px;
    position: relative;
    .comment-list {
      padding-bottom: 20px;
    }
  }
}
</style>
