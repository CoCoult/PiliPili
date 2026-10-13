<template>
  <div v-if="dataSource.rows != null && dataSource.rows.length == 0">
    <NoData msg="空空如也"></NoData>
  </div>
  <div class="data-list-panel">
    <template v-for="item in dataSource.rows" :key="item">
      <slot :data="item"></slot>
    </template>
  </div>
  <div class="pagination" v-show="showPagination && dataSource.pageTotal > 1">
    <el-pagination
      background
      :total="dataSource.total || 0"
      :current-page="dataSource.pageNum"
      layout="prev,pager,next"
      @current-change="handlerPageNumChange"
      :page-size="dataSource.pageSize"
    ></el-pagination>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, onMounted } from "vue";
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router";
const route = useRoute();
const router = useRouter();

const props = defineProps({
  dataSource: {
    type: Object,
  },
  showPagination: {
    type: Boolean,
    default: true,
  },
});

const emit = defineEmits(["loadData"]);
const handlerPageNumChange = (pageNum) => {
  props.dataSource.pageNum = pageNum;
  emit("loadData");
};
</script>

<style lang="scss" scoped>
</style>
