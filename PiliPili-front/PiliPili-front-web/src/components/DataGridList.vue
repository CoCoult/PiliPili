<template>
  <div v-if="dataSource.rows != null && dataSource.rows.length === 0">
    <NoData msg="空空如也" />
  </div>
  <div
    class="data-list"
    :style="{ 'grid-template-columns': `repeat(${gridCount}, 1fr)` }"
  >
    <template v-for="item in dataSource.rows">
      <slot :data="item"></slot>
    </template>
  </div>
  <div class="pagination" v-if="showPagination && dataSource.pageTotal > 1">
    <el-pagination
      v-if="dataSource.pageTotal > 1"
      background
      :total="dataSource.total"
      :current-page.sync="dataSource.pageNum"
      layout="prev, pager, next"
      @current-change="handlePageNoChange"
      :page-size="dataSource.pageSize"
    ></el-pagination>
  </div>
</template>

<script setup>
import { formEmits } from "element-plus";

const props = defineProps({
  gridCount: {
    type: Number,
    default: 5,
  },
  dataSource: {
    type: Object,
  },
  showPagination: {
    type: Boolean,
    default: true,
  },
});

const emits = defineEmits(["loadData"]);
const handlePageNoChange = (pageNum) => {
  props.dataSource.pageNum = pageNum;
  emits("loadData");
};
</script>

<style lang="scss" scoped>
.data-list {
  display: grid;
  grid-gap: 20px;
}
.pagination {
  margin-top: 20px;
  padding: 10px 0px 5px 0px;
  text-align: center;
  overflow: hidden;
  height: 50px;
  display: flex;
  justify-content: left;
}
</style>
