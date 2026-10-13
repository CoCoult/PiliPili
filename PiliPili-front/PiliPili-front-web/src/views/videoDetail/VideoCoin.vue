<template>
  <Dialog
    :show="dialogConfig.show"
    :title="dialogConfig.title"
    :buttons="dialogConfig.buttons"
    width="400px"
    :showCancel="false"
    @close="dialogConfig.show = false"
  >
    <div class="coin-panel">
      <div class="title-info">
        给up主投上<span class="coin-count"> {{ coinCount }} </span>枚硬币
      </div>
      <div class="coin-list">
        <div
          :class="['coin-item', coinCount == 1 ? 'active' : '']"
          @click="setCoinCount(1)"
        >
          <img :src="proxy.Utils.getLocalImage('coin1.png')" />
          <div class="coin-count-inner">1硬币</div>
        </div>
        <div
          :class="['coin-item', coinCount == 2 ? 'active' : '']"
          @click="setCoinCount(2)"
        >
          <img :src="proxy.Utils.getLocalImage('coin2.png')" />
          <div class="coin-count-inner">2硬币</div>
        </div>
      </div>
      <div class="sub-btn">
        <el-button type="primary" @click="submitCoin">确定</el-button>
      </div>
    </div>
  </Dialog>
</template>

<script setup>
import { doUserAction } from "@/utils/Api";
import { ACTION_TYPE } from "@/utils/Constants";
import { config, validator } from "artplayer";
import {
  ref,
  reactive,
  getCurrentInstance,
  nextTick,
  onMounted,
  inject,
} from "vue";
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router";
const route = useRoute();
const router = useRouter();

const dialogConfig = ref({
  show: false,
  title: "投币",
  buttons: [],
});

const videoInfo = inject("videoInfo");

const coinCount = ref(1);

const setCoinCount = (count) => {
  coinCount.value = count;
};

const submitCoin = () => {
  doUserAction(
    {
      videoId: route.params.videoId,
      actionType: ACTION_TYPE.VIDEO_COIN.value,
      actionCount: coinCount.value,
    },
    () => {
      videoInfo.value.coinCountActive = true;
      videoInfo.value.coinCount = videoInfo.value.coinCount + coinCount.value;
      dialogConfig.value.show = false;
      proxy.Message.success("投币成功");
    }
  );
};

const show = () => {
  dialogConfig.value.show = true;
};

defineExpose({
  show,
});
</script>

<style lang="scss" scoped>
.coin-panel {
  .title-info {
    text-align: center;
    font-size: 16px;
    .coin-count {
      font-size: 18px;
      padding: 0px 3px;
      color: var(--blue2);
    }
  }
  .coin-list {
    margin-top: 30px;
    display: flex;
    justify-content: space-around;
    .coin-item {
      width: 150px;
      border: 1px dashed #ddd;
      border-radius: 5px;
      overflow: hidden;
      position: relative;
      cursor: pointer;
      &:hover {
        border: 2px solid var(--blue);
        .coin-count-inner {
          color: var(--blue);
        }
      }
      img {
        width: 100%;
      }
      .coin-count-inner {
        position: absolute;
        left: 10px;
        top: 10px;
        color: var(--text3);
      }
    }
    .active {
      border: 2px solid var(--blue);
      .coin-count-inner {
        color: var(--blue);
      }
    }
  }
  .sub-btn {
    text-align: center;
    margin: 30px 0px 5px 0px;
  }
}
</style>
