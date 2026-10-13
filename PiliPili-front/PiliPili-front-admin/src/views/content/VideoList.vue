<template>
  <div class="top-panel">
    <el-card
      ><el-form :model="searchForm" label-width="70px" label-position="right"
        ><el-row
          ><el-col :span="5"
            ><el-form-item label="视频名称"
              ><el-input
                class="password-input"
                v-model="searchForm.videoNameFuzzy"
                clearable
                placeholder="支持模糊搜索"
                @keyup.enter="loadDataList"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-form-item label="分类">
              <el-cascader
                style="width: 100%"
                v-model="searchForm.categoryIdArray"
                :options="categoryList"
                clearable
                :props="{
                  value: 'categoryId',
                  label: 'categoryName',
                  checkStrictly: true,
                }"
              />
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-form-item label="推荐"
              ><!-- 下拉框 -->
              <el-select
                clearable
                placeholder="请选择推荐状态"
                v-model="searchForm.recommendType"
                ><el-option :value="0" label="未推荐"></el-option
                ><el-option :value="1" label="已推荐"> </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="4" :style="{ paddingLeft: '10px' }">
            <el-button type="success" @click="loadDataList()"> 查询 </el-button>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
  </div>
  <el-card class="table-data-card">
    <Table
      ref="tableInfoRef"
      :columns="columns"
      :fetch="loadDataList"
      :dataSource="tableData"
      :options="tableOptions"
      :extHeight="tableOptions.extHeight"
    >
      <template #videoCover="{ index, row }"
        ><div class="cover-info">
          <Cover :source="row.videoCover" :width="160"></Cover>
          <div class="duration">
            {{ proxy.Utils.convertSecondsToHMS(row.duration) }}
          </div>
        </div>
      </template>
      <template #videoName="{ index, row }">
        <div class="video-info">
          <div class="video-name">{{ row.videoName }}</div>
          <div class="user-name iconfont icon-upzhu">{{ row.userName }}</div>
          <div class="video-count">
            <span class="iconfont icon-play-solid">{{
              row.playCount != null ? row.playCount : 0
            }}</span>
            <span class="iconfont icon-like-solid">{{
              row.likeCount != null ? row.likeCount : 0
            }}</span>
            <span class="iconfont icon-danmu-solid">{{
              row.barrageCount != null ? row.barrageCount : 0
            }}</span>
            <span class="iconfont icon-comment-solid">{{
              row.commentCount != null ? row.commentCount : 0
            }}</span>
            <span class="iconfont icon-toubi">{{
              row.coinCount != null ? row.coinCount : 0
            }}</span>
            <span class="iconfont icon-collection-solid">{{
              row.collectCount != null ? row.collectCount : 0
            }}</span>
          </div>
        </div>
      </template>
      <template #statusName="{ row, index }"
        ><span :style="{ color: statusMap[row.status] }">{{
          row.statusName
        }}</span>
      </template>
      <template #recommendType="{ row, index }">{{
        row.recommendType == 1 ? "已推荐" : "未推荐"
      }}</template>

      <template #slotOperation="{ index, row }">
        <div class="row-op-panel">
          <a
            class="a-link"
            href="javascript:void(0)"
            @click.prevent="showDetail(row)"
            >详情</a
          ><el-divider direction="vertical" /><template v-if="row.status == 2"
            ><a class="a-link" href="javascript:void(0)" @click="audit(row)"
              >审核</a
            ><el-divider direction="vertical" /></template
          ><template v-if="row.status == 3"
            ><a
              class="a-link"
              href="javascript:void(0)"
              @click="recommend(row)"
              >{{ row.recommendType == 1 ? "取消推荐" : "推荐" }}</a
            ><el-divider direction="vertical" /></template
          ><a
            class="a-link"
            href="javascript:void(0)"
            @click.prevent="delAccount(row)"
            >删除</a
          >
        </div>
      </template>
    </Table>
  </el-card>
  <VideoAudit ref="videoAuditRef"></VideoAudit>

  <VideoDetail ref="videoDetailRef"></VideoDetail>
</template>

<script setup>
import VideoDetail from "./VideoDetail.vue";
import VideoAudit from "./VideoAudit.vue";
import { ref, reactive, getCurrentInstance, nextTick, onMounted } from "vue";
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router";
const route = useRoute();
const router = useRouter();

const categoryList = ref([]);
const loadCategory = async () => {
  let result = await proxy.Request({
    url: proxy.Api.loadCategory,
  });
  if (!result) {
    return;
  }
  categoryList.value = result.data;
};
loadCategory();

const searchForm = ref({});

const statusMap = {
  0: "#e6a23c", // 转码中
  1: "#f56c6c", // 转码失败
  2: "#e6a23c", // 待审核
  3: "#67c23a", // 成功
  4: "#f56c6c", // 审核失败
};

const tableData = ref({});
const tableOptions = ref({
  extHeightL: 0,
});

const columns = [
  { label: "封面", prop: "videoCover", width: 220, scopedSlots: "videoCover" },
  { label: "视频信息", prop: "videoName", scopedSlots: "videoName" },
  { label: "最后更新时间", prop: "lastUpdateTime", width: 200 },
  { label: "状态", prop: "statusName", width: 100, scopedSlots: "statusName" },
  {
    label: "推荐",
    prop: "recommendType",
    width: 100,
    scopedSlots: "recommendType",
  },
  {
    label: "操作",
    prop: "operation",
    width: 190,
    scopedSlots: "slotOperation",
  },
];

const loadDataList = async () => {
  let params = {
    pageNumber: tableData.value.pageNum,
    pageSize: tableData.value.pageSize,
  };
  Object.assign(params, searchForm.value);
  if (params.categoryIdArray && params.categoryIdArray.length == 2) {
    params.categoryId = params.categoryIdArray[1];
  } else if (params.categoryIdArray && params.categoryIdArray.length == 1) {
    params.categoryId = params.categoryIdArray[0];
  }
  delete params.categoryIdArray;
  let result = await proxy.Request({
    url: proxy.Api.loadVideo,
    params,
  });
  if (!result) {
    return;
  }
  const resultData = result.data.rows;
  resultData.forEach((item) => {
    item.lastUpdateTime = proxy.Utils.formatTime(item.lastUpdateTime);
    item.createTime = proxy.Utils.formatTime(item.createTime);
  });
  Object.assign(tableData.value, result.data);
};

const videoAuditRef = ref();
const audit = (row) => {
  videoAuditRef.value.show(row.videoId);
};

const videoDetailRef = ref();
const showDetail = (data) => {
  videoDetailRef.value.show(data);
}
</script>

<style lang="scss" scoped>
.detail-tree-panel {
  height: calc(100vh - 273px);
  overflow: auto;
  width: 100%;
}
.cover-info {
  min-width: 0;
  width: 160px;
  position: relative;
  .duration {
    position: absolute;
    right: 0px;
    bottom: 0px;
    padding: 3px 5px;
    border-radius: 5px 0px 5px 0px;
    background: rgba(0, 0, 0, 0.7);
    opacity: 0.8;
    color: #fff;
    font-size: 13px;
  }
}
.video-info {
  .user-name {
    margin-top: 5px;
    color: var(--text3);
    font-size: 14px;
    &::before {
      margin-right: 5px;
    }
  }
  .video-count {
    margin-top: 10px;
    color: var(--text3);
    display: flex;
    align-items: center;
    .iconfont {
      font-size: 14px;
      margin-right: 20px;
      &::before {
        font-size: 18px;
        margin-right: 5px;
      }
    }
  }
}
</style>
