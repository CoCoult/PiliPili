<template>
  <div class="comment-panel">
    <VideoSearchSelect @loadData="loadData4VideoSelect"></VideoSearchSelect>
    <Table
      ref="tableInfoRef"
      :columns="columns"
      :fetch="loadDataList"
      :dataSource="tableData"
      :options="tableOptions"
      :tableHeight="tableOptions.extHeight"
    >
      <template #slotComment="{ index, row }">
        <div class="comment-info">
          <Avatar :avatar="row.avatar" :userId="row.userId"></Avatar>
          <div class="comment">
            <div>
              <router-link
                target="_blank"
                class="a-link nick-name"
                :to="`/user/${row.userId}`"
              >
                {{ row.userName }}
              </router-link>
              <template v-if="row.replyUserId">
                回复
                <router-link
                  target="_blank"
                  class="a-link nick-name"
                  :to="`/user/${row.replyUserId}`"
                >
                  {{ row.replyUserName }}
                </router-link>
              </template>
            </div>
            <div class="content">{{ row.content }}</div>
            <div v-if="row.imgPath" class="image-show">
              <Cover :source="row.imgPath" :preview="true" fit="cover" />
            </div>
            <div class="time-info">
              <div class="time">{{ proxy.Utils.formatTime(row.postTime) }}</div>
              <div
                class="iconfont icon-delete"
                @click="delComment(row.commentId)"
              ></div>
            </div>
          </div>
        </div>
      </template>
      <template #slotVideo="{ index, row }">
        <router-link
          :to="`/video/${row.videoId}`"
          target="_blank"
          class="a-link"
        >
          <Cover :source="row.videoCover"></Cover>
          <div class="video-name">{{ row.videoName }}</div>
        </router-link>
      </template>
    </Table>
  </div>
</template>

<script setup>
import VideoSearchSelect from "./VideoSearchSelect.vue";
import { ref, reactive, getCurrentInstance, nextTick, onMounted } from "vue";
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router";
const route = useRoute();
const router = useRouter();

const currentVideoId = ref();
const loadData4VideoSelect = () => {
  currentVideoId.value = videoId;
  loadDataList();
};

const columns = [
  {
    label: "评论信息",
    scopedSlots: "slotComment",
  },
  {
    label: "视频信息",
    scopedSlots: "slotVideo",
    width: 150,
  },
];
const tableInfoRef = ref();
const tableOptions = ref({
  extHeight: 10,
});
const tableData = ref({});
const loadDataList = async (videoId) => {
  let params = {
    pageNum: tableData.value.pageNum,
    pageSize: tableData.value.pageSize,
    videoId: currentVideoId.value,
  };
  let result = await proxy.Request({
    url: proxy.Api.ucloadComment,
    params: params,
  });
  if (!result) {
    return;
  }
  Object.assign(tableData.value, result.data);
};

const delComment = (commentId) => {
  proxy.Confirm({
    message: "确定要删除吗？",
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.Api.ucDelComment,
        params: {
          commentId,
        },
      });
      if (!result) {
        return;
      }
      proxy.Message.success("删除成功");
      loadDataList();
    },
  });
};
</script>

<style lang="scss" scoped>
.comment-panel {
  .comment-info {
    display: flex;
    .comment {
      margin-left: 10px;
    }
    .time-info {
      display: flex;
      font-size: 12px;
      .iconfont {
        margin-left: 5px;
        font-size: 13px;
        cursor: pointer;
      }
    }
  }
  .video-name {
    text-decoration: none;
    color: var(--text3);
    font-size: 13px;
    margin-top: 5px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
.image-show {
  width: 100px;
  height: 100px;
  overflow: hidden;
  margin: 5px 0px;
}
</style>
