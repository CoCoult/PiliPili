<template>
  <div
    :class="[layoutType == 'grid' ? 'data-list-grid' : '']"
    :style="{
      'grid-template-columns': `repeat(${gridCount}, 1fr)`,
    }"
  >
    <template v-for="item in dataSource.rows" :key="item">
      <slot :data="item"></slot>
    </template>
  </div>
  <div class="bottom-loading" v-if="loading">
    <img :src="proxy.Utils.getLocalImage('playing.gif')" /> 数据加载中...
  </div>

  <div
    class="reach-bottom"
    v-if="
      dataSource.pageNum >= dataSource.pageTotal &&
      !loading &&
      dataSource.rows.length > 0
    "
  >
    {{ loadEndMsg }}
  </div>
  <NoData v-if="dataSource.rows && dataSource.rows.length == 0"></NoData>
</template>

<script setup>
import { mitter } from "@/eventbus/eventBus.js";
import {
  ref,
  reactive,
  getCurrentInstance,
  nextTick,
  onMounted,
  onUnmounted,
} from "vue";
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router";
const route = useRoute();
const router = useRouter();

const props = defineProps({
  dataSource: {
    type: Object,
    default: {},
  },
  loading: {
    type: Boolean,
  },
  loadEndMsg: {
    type: String,
    default: "已经到底啦~~~",
  },
  layoutType: {
    type: String,
    default: "grid",
  },
  gridCount: {
    type: Number,
    default: 5,
  },
});

const emit = defineEmits(["loadData"]);

const scrollHandler = (curScrollTop) => {
  console.log(1)
  if (window.innerHeight + curScrollTop < document.body.offsetHeight) {
    return;
  }
  if (props.loading || props.dataSource.pageNum >= props.dataSource.pageTotal) {
    return;
  }
  props.dataSource.pageNum++;
  emit("loadData");
};

onMounted(() => {
  mitter.on("windowScroll", (curScrollTop) => {
    scrollHandler(curScrollTop);
  });
});

onUnmounted(() => {
  mitter.off("windowScroll");
});
</script>

<style lang="scss" scoped>
.data-list-grid {
  display: grid;
  grid-gap: 20px;
  padding-bottom: 10px;
  padding: 0 150px;
}
.reach-bottom {
  text-align: center;
  line-height: 40px;
  color: var(--text3);
}
.bottom-loading {
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text3);
  img {
    width: 20px;
    margin-right: 10px;
  }
}
</style>
