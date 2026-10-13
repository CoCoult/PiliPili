<template>
  <div class="history-list">
    <div class="top-info">
      <div class="iconfont icon-wating">历史记录</div>
      <el-button type="danger" @click="cleanAll">清空历史</el-button>
    </div>
    <el-timeline>
      <div class="data-list">
        <DataLoadMoreList
          :dataSource="dataSource"
          :loading="loadingData"
          @loadData="loadDataList"
          layoutType="line"
        >
          <template #default="{ data }">
            <el-timeline-item
              :timestamp="proxy.Utils.formatDate(data.lastUpdateTime)"
              placement="top"
            >
              <div class="history-item">
                <div class="cover" @click="goDetail(data.videoId)">
                  <Cover :source="data.videoCover"></Cover>
                </div>
                <div class="video-info">
                  <div @click="goDetail(data.videoId)">
                    {{ data.videoName || "已失效视频" }}
                  </div>
                </div>
                <div class="op-btns">
                  <div
                    class="iconfont icon-delete"
                    @click="delHisotry(data.videoId)"
                  ></div>
                </div>
              </div>
            </el-timeline-item>
          </template>
        </DataLoadMoreList>
      </div>
    </el-timeline>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, onMounted } from "vue";
const { proxy } = getCurrentInstance();
import { useRoute, useRouter, onBeforeRouteUpdate } from "vue-router";
const route = useRoute();
const router = useRouter();

import { useNavAction } from "@/stores/navActionStore";
const navActionStore = useNavAction();

onMounted(() => {
  nextTick(() => {
    navActionStore.setShowHeader(true);
    navActionStore.setFixedHeader(true);
    navActionStore.setFixedCategory(false);
    navActionStore.setShowCategory(false);
  });
});

const dataSource = ref({});
const loadingData = ref(false);

const loadDataList = async () => {
  let params = {
    pageNum: dataSource.value.pageNum,
  };
  loadingData.value = true;
  let result = await proxy.Request({
    url: proxy.Api.playHisotry,
    params,
  });
  loadingData.value = false;
  if (!result) {
    return;
  }
  const dataList = dataSource.value.rows;
  dataSource.value = Object.assign({}, result.data);
  if (result.data.pageNum > 1) {
    dataList.value.rows = dataList.concat(result.data.rows);
  }
};
loadDataList();

const cleanAll = () => {
  proxy.Confirm({
    message: "确定要清空历史记录吗？",
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.Api.cleanHistory,
      });
      if (!result) {
        return;
      }
      proxy.Message.success("删除成功");
      dataSource.value = { list: [] };
    },
  });
  loadDataList();
};

const delHisotry = (videoId) => {
  proxy.Confirm({
    message: "确定要删除记录吗？",
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.Api.delHistory,
        params: {
          videoId,
        },
      });
      if (!result) {
        return;
      }
      proxy.Message.success("删除成功");
      dataSource.value.list = dataSource.value.list.filter((item) => {
        return item.videoId !== videoId;
      });
    },
  });
  loadDataList();
};

const goDetail = (videoId) => {
  router.push(`/video/${videoId}`);
};
</script>

<style lang="scss" scoped>
.history-list {
  margin: 20px auto 0px;
  width: 1200px;
  .top-info {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0px;
    margin-bottom: 20px;
    .icon-wating {
      font-size: 16px;
      &::before {
        margin-right: 5px;
        font-size: 22px;
        color: #e3936c;
        float: left;
      }
    }
  }

  .data-list {
    .history-item {
      margin-top: 10px;
      display: flex;
      align-items: center;
      .cover {
        width: 200px;
      }
      .video-info {
        flex: 1;
        margin-left: 10px;
        cursor: pointer;
        color: var(--blue3);
      }

      .op-btns {
        .iconfont {
          cursor: pointer;
          color: #e3936c;
        }
      }
    }
  }
}
</style>
