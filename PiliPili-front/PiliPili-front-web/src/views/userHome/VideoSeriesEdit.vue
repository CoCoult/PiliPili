<template>
  <Dialog
    :show="dialogConfig.show"
    :title="dialogConfig.title"
    :buttons="dialogConfig.buttons"
    width="500px"
    :showCancel="false"
    @close="dialogConfig.show = false"
  >
    <el-form
      :model="formData"
      :rules="rules"
      ref="formDataRef"
      label-width="80px"
      @submit.prevent
    >
      <template v-if="step == 1">
        <el-form-item label="合集名称" prop="seriesName">
          <el-input
            clearable
            placeholder="请填写名称"
            v-model="formData.seriesName"
            :maxlength="100"
            show-word-limit
          ></el-input>
        </el-form-item>
        <el-form-item label="合集简介" prop="">
          <el-input
            clearable
            placeholder="请填写简介（选填）"
            type="textarea"
            :rows="5"
            :maxlength="200"
            show-word-limit
            resize="none"
            v-model="formData.seriesDescription"
          ></el-input>
        </el-form-item>
      </template>
      <template v-if="step == 2">
        <el-scrollbar max-height="400px">
          <el-checkbox-group v-model="formData.videoIds">
            <div class="video-item" v-for="item in videoList">
              <div class="check">
                <el-checkbox :value="item.videoId" />
              </div>
              <div class="cover">
                <Cover :source="item.videoCover"></Cover>
              </div>
              <div class="video-info">
                <div class="video-name">{{ item.videoName }}</div>
                <div class="play-info">
                  <div class="iconfont icon-play2">{{ item.playCount }}</div>
                  <div class="create-time">
                    {{ proxy.Utils.formatDate(item.createTime) }}
                  </div>
                </div>
              </div>
            </div>
          </el-checkbox-group>
        </el-scrollbar>
      </template>
      <div class="op-btns">
        <el-button link @click="cancel">取消</el-button>
        <el-button
          type="primary"
          @click="nextStep"
          v-if="step === 1 && opType != 1"
          >下一步</el-button
        >
        <el-button
          type="primary"
          @click="preStep"
          v-if="step === 2 && opType != 2"
          >上一步</el-button
        >
        <el-button
          type="primary"
          @click="submit"
          v-if="step == 2 || opType == 1"
          >确定</el-button
        >
      </div>
    </el-form>
  </Dialog>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, onMounted } from "vue";
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router";
const route = useRoute();
const router = useRouter();

const dialogConfig = ref({
  show: false,
  title: "标题",
});

const formData = ref({});
const formDataRef = ref();
const rules = {
  seriesName: [{ required: true, message: "请输入合集名称" }],
};

const step = ref(1);
const opType = ref(0); //0：新增合集，添加视频 1：编辑合集 2：增加视频

const cancel = () => {
  dialogConfig.value.show = false;
};

const preStep = () => {
  step.value = 1;
};

const nextStep = () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }
    step.value = 2;
    loadVideoList();
  });
};

const videoList = ref([]);
const loadVideoList = async () => {
  let result = await proxy.Request({
    url: proxy.Api.uHomeSeriesLoadAllVideo,
    params: {
      seriesId: route.params.seriesId,
    },
  });
  if (!result) {
    return;
  }
  videoList.value = result.data;
};

const emits = defineEmits(["reload"]);
const submit = () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }
    let params = Object.assign({}, formData.value);
    if (params.videoIds.length == 0 && step.value == 2) {
      proxy.Message.warning("请添加视频");
      return;
    }
    params.videoIds = params.videoIds.join(",");
    Object.assign(params, formData.value);
    let result = await proxy.Request({
      url:
        opType.value == 2
          ? proxy.Api.uHomeSeriesSaveSeriesVideo
          : proxy.Api.uHomeSeriesSaveVideoSeries,
      params,
    });
    if (!result) {
      return;
    }
    proxy.Message.success("保存成功");
    dialogConfig.value.show = false;
    emits("reload");
  });
};

const show = (data = {}, _opType = 0) => {
  loadVideoList();
  opType.value = _opType;
  if (_opType == 0 || _opType == 1) {
    step.value = 1;
  } else if (_opType == 2) {
    step.value = 2;
  }
  dialogConfig.value.show = true;
  nextTick(() => {
    formDataRef.value.resetFields();
    formData.value = Object.assign({}, data);
    formData.value.videoIds = [];
  });
};

defineExpose({
  show,
});
</script>

<style lang="scss" scoped>
.op-btns {
  text-align: right;
}
.video-item {
  display: flex;
  margin-bottom: 10px;
  padding-right: 15px;
  align-items: center;
  .check {
    width: 30px;
  }
  .cover {
    width: 100px;
  }
  .video-info {
    flex: 1;
    margin-left: 5px;
    min-width: 0;
    .video-name {
      width: 100%;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .play-info {
      margin-top: 5px;
      font-size: 13px;
      color: var(--text3);
      display: flex;
      justify-content: space-between;
      .iconfont {
        font-size: 13px;
        &::before {
          font-size: 16px;
          margin-right: 3px;
        }
      }
    }
  }
}
:deep(.el-checkbox-group) {
  font-size: 13px;
  line-height: normal;
}
</style>
