<template>
  <div class="data-list">
    <DataLoadMoreList
      :dataSource="dataSource"
      @loadData="loadDataList"
      :loading="loadingData"
    >
      <template #default="{ data }">
        <VideoItem :data="data"></VideoItem>
      </template>
    </DataLoadMoreList>
  </div>
</template>
 
<script setup>
import {
  ref,
  reactive,
  getCurrentInstance,
  nextTick,
  onMounted,
  watch,
} from "vue";
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router";
const route = useRoute();
const router = useRouter();

const dataSource = ref({});
const loadingData = ref(false);

import { init } from "echarts";

import { useCategoryStore } from "@/stores/CategoryStore";
const CategoryStore = useCategoryStore();

const loadDataList = async () => {
  let params = {
    pageNum: dataSource.value.pageNum,
  };
  Object.assign(params, categoryIdInfo.value);
  loadingData.value = true;
  let result = await proxy.Request({
    url: proxy.Api.loadVideo,
    params,
  });
  loadingData.value = false;
  if (!result) {
    return;
  }

  const dataList = dataSource.value.rows;
  dataSource.value = Object.assign({}, result.data);
  if (result.data.pageNum > 1) {
    dataSource.value.rows = dataList.concat(result.data.rows);
  }
};

const categoryMap = CategoryStore.categoryMap;
const categoryIdInfo = ref({});

const convertCode2Id = (pCategoryCode, categoryCode) => {
  
  let pCategoryId = "";
  let categoryId = "";
  if (pCategoryCode && !categoryCode) {
    categoryId = categoryMap[pCategoryCode]
      ? categoryMap[pCategoryCode].categoryId
      : "";
  }
  if (pCategoryCode && categoryCode) {
    pCategoryId = categoryMap[pCategoryCode]
      ? categoryMap[pCategoryCode].categoryId
      : "";
    categoryId = categoryMap[categoryCode]
      ? categoryMap[categoryCode].categoryId
      : "";
  }
  categoryIdInfo.value = {
    pCategoryId,
    categoryId,
  };
};

const initData = () => {
  convertCode2Id(route.params.pCategoryCode, route.params.categoryCode);
  loadDataList();
};

const categoryInit = ref(false);

watch(
  () => route.params,
  (newVal, oldVal) => {
    if (newVal) {
      if (categoryInit.value) {
        return;
      }
      CategoryStore.setCureentPCategory(route.params.pCategoryCode);
      initData();
    }
  },
  { immediate: true, deep: true }
);

watch(
  () => CategoryStore.categoryMap,
  (newVal, oldVal) => {
    if (newVal || Object.keys(newVal).length == 0) {
      return;
    }
    categoryInit.value = true;
    CategoryStore.setCureentPCategory(route.params.pCategoryCode);
    initData();
  },
  { immediate: true, deep: true }
);
</script>
 
<style lang="scss" scoped>
</style>
 