<template>
  <el-row :gutter="10">
    <el-col :span="16">
      <el-card class="table-data-card">
        <template #header>
          <div class="header">
            <div class="title">一级分类</div>
            <div class="btn" @click="showEdit({}, 0)">新增分类</div>
          </div>
        </template>
        <Table
          ref="tableInfoRef"
          :columns="columns"
          :fetch="loadDataList"
          :dataSource="tableData"
          :options="tableOptions"
          :extHeight="tableOptions.extHeight"
          :showPagination="false"
          @rowClick="rowClick"
        >
          <template #icon="{ index, row }">
            <div class="cover">
              <Cover :source="row.icon" defaultImg="404.png"></Cover>
            </div>
          </template>

          <template #background="{ index, row }">
            <div class="category-background">
              <Cover
                :source="row.background"
                fit="cover"
                defaultImg="404.png"
              ></Cover>
            </div>
          </template>

          <template #slotOperation="{ index, row }">
            <div class="row-op-panel">
              <a
                class="a-link"
                href="javascript:void(0)"
                @click="showEdit(row, 0)"
                >修改</a
              >
              <el-divider direction="vertical" />
              <a
                class="a-link"
                href="javascript:void(0)"
                @click="delCategory(row)"
                >删除</a
              >
              <el-divider direction="vertical" />
              <a
                href="javascript:void(0)"
                @click="changeSort(0, index, 'up')"
                :class="[index === 0 ? 'disable' : 'a-link']"
                >上移</a
              >
              <el-divider direction="vertical" />
              <a
                href="javascript:void(0)"
                @click="changeSort(0, index, 'down')"
                :class="[
                  index == tableData.rows.length - 1 ? 'disable' : 'a-link',
                ]"
                >下移</a
              >
            </div>
          </template>
        </Table>
      </el-card>
    </el-col>
    <el-col :span="8">
      <el-card class="table-data-card">
        <template #header>
          <div class="header">
            <div class="title">二级分类</div>
            <div class="btn" @click="showEdit({}, 1)">新增二分类</div>
          </div>
        </template>
        <Table
          :columns="columnSub"
          :dataSource="subCategoryData"
          :options="tableOptions"
          :extHeight="tableOptions.extHeight"
          :showPagination="false"
        >
          <template #slotOperation="{ index, row }">
            <div class="row-op-panel">
              <a
                class="a-link"
                href="javascript:void(0)"
                @click="showEdit(row, 0)"
                >修改</a
              >
              <el-divider direction="vertical" />
              <a
                class="a-link"
                href="javascript:void(0)"
                @click="delCategory(row)"
                >删除</a
              >
              <el-divider direction="vertical" />
              <a
                href="javascript:void(0)"
                @click="changeSort(0, index, 'up')"
                :class="[index === 0 ? 'disable' : 'a-link']"
                >上移</a
              >
              <el-divider direction="vertical" />
              <a
                href="javascript:void(0)"
                @click="changeSort(0, index, 'down')"
                :class="[
                  index == tableData.rows.length - 1 ? 'disable' : 'a-link',
                ]"
                >下移</a
              >
            </div>
          </template>
        </Table>
      </el-card>
    </el-col>
  </el-row>
  <CategoryEdit ref="categoryEditRef" @reload="loadDataList"></CategoryEdit>
</template>

<script setup>
import CategoryEdit from "./CategoryEdit.vue";
import { ref, reactive, getCurrentInstance, nextTick, onMounted } from "vue";
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router";
const route = useRoute();
const router = useRouter();

const tableData = ref({});
const tableOptions = ref({
  extHeight: 0,
});

const columns = [
  {
    label: "图标",
    prop: "icon",
    scopedSlots: "icon",
    width: 70,
  },
  {
    label: "背景",
    prop: "background",
    scopedSlots: "background",
    width: 180,
  },
  {
    label: "分类编号",
    prop: "categoryCode",
    width: 180,
  },
  {
    label: "分类名称",
    prop: "categoryName",
  },
  {
    label: "操作",
    prop: "type",
    scopedSlots: "slotOperation",
    width: 200,
  },
];
const columnSub = columns.slice(columns.length - 3, columns.length);

const tableInfoRef = ref();

const loadDataList = async () => {
  let result = await proxy.Request({
    url: proxy.Api.loadCategory,
  });
  if (!result) {
    return;
  }
  tableData.value.rows = result.data;
  if (currentSelectCategory.value == null && result.data.length > 0) {
    currentSelectCategory.value = result.data[0];
    subCategoryData.value.rows = currentSelectCategory.value.children;
  } else {
    currentSelectCategory.value = result.data.find((item) => {
      return item.categoryId == currentSelectCategory.value.categoryId;
    });
    subCategoryData.value.rows = currentSelectCategory.value.children;
  }
  nextTick(() => {
    tableInfoRef.value.setCurrentRow(
      "categoryId",
      currentSelectCategory.value.categoryId
    );
  });
};

const delCategory = (data) => {
  proxy.Confirm({
    message: `确定要删除【${data.categoryName}】吗？`,
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.Api.delCategory,
        params: {
          categoryId: data.categoryId,
        },
      });
      if (!result) {
        return;
      }
      proxy.Message.success("操作成功");
      //清空选中
      if (currentSelectCategory.value.categoryId === data.categoryId) {
        currentSelectCategory.value = null;
      }
      loadDataList();
    },
  });
};

const currentSelectCategory = ref();
const subCategoryData = ref([]);
const rowClick = (row) => {
  currentSelectCategory.value = row;
  subCategoryData.value.rows = row.children;
};

const changeSort = async (pCategoryId, index, type) => {
  let dataList =
    pCategoryId == 0
      ? tableData.value.rows
      : currentSelectCategory.value.children;
  if (
    (type == "down" && index == dataList.length - 1) ||
    (type == "up" && index == 0)
  ) {
    return;
  }
  let temp = dataList[index];
};

const categoryEditRef = ref();
const showEdit = (data, type) => {
  if (type == 1 && !currentSelectCategory.value) {
    proxy.Message.warning("请先创建一级分类");
  }
  if (type == 0) {
    data.pCategoryId = 0;
  } else if (type == 1 && Object.keys(data).length == 0) {
    data.pCategoryId = currentSelectCategory.value.categoryId;
  }
  categoryEditRef.value.showEdit(Object.assign({}, data));
};
</script>

<style lang="scss" scoped>
.table-data-card {
  .header {
    display: flex;
    justify-content: space-between;
    .btn {
      cursor: pointer;
      color: var(--blue2);
    }
  }
  .category-background {
    width: 150px;
    height: 80px;
  }
}
</style>
