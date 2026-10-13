<template>
  <div class="danmu-panel">
    <Collapse>
      <template #title>
        <div class="danmu-panel-title">
          <div>弹幕列表</div>
          <div class="iconfont icon-more"></div>
        </div>
      </template>
      <div class="danmu-list">
        <div class="danmu-title danmu-item">
          <div class="time">时间</div>
          <div class="content">发送内容</div>
          <div class="send-time">发送时间</div>
        </div>
        <el-scrollbar height="400px" v-if="danmuList">
          <div
            :class="[
              'danmu-item',
              item.barrageId == selectDanmu.barrageId ? 'active' : '',
            ]"
            v-for="item in danmuList"
            :key="item"
            @click="danmuSelect(item)"
          >
            <div class="time">
              {{ proxy.Utils.convertSecondsToHMS(item.time) }}
            </div>
            <div class="content">{{ item.text }}</div>
            <div class="send-time">{{ item.postTime }}</div>
          </div>
        </el-scrollbar>
        <NoData msg="暂无弹幕" v-if="danmuList.length == 0"></NoData>
      </div>
    </Collapse>
  </div>
</template>

<script setup>
import { mitter } from "@/eventbus/eventBus.js";
import moment from "moment";
import { ref, reactive, getCurrentInstance, nextTick, onMounted, onUnmounted } from "vue";
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router";
const route = useRoute();
const router = useRouter();

const danmuList = ref([]);
const selectDanmu = ref({});
const danmuSelect = (data) => {
  selectDanmu.value = data;
};

onMounted(() => {
  mitter.on("loadDanmu", (data) => {
    data.sort((a, b) => {
      return a.time - b.time;
    });
    danmuList.value = data;
    for (let index = 0; index < danmuList.value.length; index++) {
      const date = danmuList.value[index].postTime;
      danmuList.value[index].postTime = moment(date).format("MM-DD HH:mm");
    }
  });
  
});

onUnmounted(() => {
  mitter.off("loadDanmu")
})
</script>

<style lang="scss" scoped>
.danmu-panel {
  .danmu-panel-title {
    display: flex;
    align-items: center;
    .icon-more {
      margin-left: 10px;
    }
  }
  .danmu-list {
    .danmu-item,
    .danmu-title {
      padding: 0px 10px;
      cursor: pointer;
      color: var(--text2);
      display: flex;
      font-size: 12px;
      text-align: left;
      line-height: 25px;
      .time {
        width: 50px;
      }
      .content {
        flex: 1;
      }
      .send-time {
        width: 130px;
      }
    }
    .active {
      background: #f4f4f5;
    }
  }
}
</style>
