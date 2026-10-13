<template>
  <div class="danmu-panel">
    <VideoSearchSelect @loadData="loadData4VideoSelect"></VideoSearchSelect>
    <Table
      ref="tableInfoRef"
      :columns="columns"
      :fetch="loadDataList"
      :dataSource="tableData"
      :options="tableOptions"
      :tableHeight="tableOptions.extHeight"
    >
      <template #slotNickName="{ index, row }">
        <router-link
          target="_blank"
          class="nick-name"
          :to="`/user/${row.userId}`"
        >
          {{ row.userName }}
        </router-link>
      </template>
      <template #time="{ index, row }">
        {{ proxy.Utils.convertSecondsToHMS(Math.round(row.time)) }}
      </template>
      <template #slotText="{ index, row }">
        <div>{{ row.text }}</div>
        <router-link
          target="_blank"
          class="video-info"
          :to="`/video/${row.videoId}`"
        >
          视频：{{ row.videoName }}
        </router-link>
      </template>
      <template #slotOperation="{ index, row }">
        <a
          href="javascript:void(0)"
          class="a-link"
          @click="delDanmu(row.barrageId)"
          >删除</a
        >
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
    label: "发送者",
    prop: "userName",
    width: 150,
    scopedSlots: "slotNickName",
  },
  {
    label: "播放时间",
    prop: "time",
    scopedSlots: "time",
    width: 100,
  },
  {
    label: "弹幕内容",
    prop: "text",
    scopedSlots: "slotText",
  },
  {
    label: "发送时间",
    prop: "postTime",
    width: 180,
  },
  {
    label: "操作",
    prop: "operation",
    width: 80,
    scopedSlots: "slotOperation",
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
    url: proxy.Api.ucLoadDanmu,
    params: params,
  });
  if (!result) {
    return;
  }
  result.data.rows.forEach((item) => {
    item.postTime = proxy.Utils.formatTime(item.postTime);
  });
  Object.assign(tableData.value, result.data);
};

const delDanmu = (barrageId) => {
  proxy.Confirm({
    message: "确定要删除吗？",
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.Api.ucDelDanmu,
        params: {
          barrageId,
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
.video-info,
.nick-name {
  margin-top: 5px;
  font-size: 12px;
  color: var(--text3);
  text-decoration: none;
}

.nick-decoration {
  font-size: 14px;
  color: var(--text2);
}
</style>
