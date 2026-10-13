<template>
  <div class="top-panel">
    <el-card>
      <el-form model="searchForm" @submit.prevent>
        <el-row :gutter="10">
          <el-col :span="5">
            <el-form-item label="用户">
              <el-input
                clearable
                placeholder="输入用户名称搜索"
                v-model="searchForm.userNameFuzzy"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-form-item label="状态" prop="">
              <el-select
                clearable
                placeholder="请选择状态"
                v-model="searchForm.status"
              >
                <el-option :value="0" label="禁用"></el-option>
                <el-option :value="1" label="启用"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-button type="success" @click="loadDataList">搜索</el-button>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
  </div>
  <div class="table-data-card">
    <div class="comment-panel">
      <Table
        ref="tableInfoRef"
        :columns="columns"
        :fetch="loadDataList"
        :dataSource="tableData"
        :options="tableOptions"
        :tableHeight="tableOptions.extHeight"
      >
        <template #slotAvatar="{ index, row }">
          <Avatar :avatar="row.avatar"></Avatar>
        </template>

        <template #slotNickName="{ index, row }">
          <a
            class="nick-name"
            >{{ row.userName }}</a
          >
          <span v-if="row.sex">{{ SEX_MAP[row.gender] }}</span>
        </template>

        <template #slotJoinTime="{ index, row }">
          <div>加入时间：{{ proxy.Utils.formatTime(row.createTime) }}</div>
        </template>

        <template #slotCoin="{ index, row }">
          <div>当前:{{ row.currentCoin }}</div>
          <div>总数:{{ row.totalCoin }}</div>
        </template>

        <template #slotStatus="{ index, row }">
          {{ row.status === 0 ? "禁用" : "启用" }}
        </template>

        <template #slotOperation="{ index, row }">
          <a
            href="javascript:void(0)"
            class="a-link"
            @click="changeStatus(row)"
            >{{ row.status === 0 ? "启用" : "禁用" }}</a
          >
        </template>
      </Table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, onMounted } from "vue";
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router";
const route = useRoute();
const router = useRouter();

const columns = [
  {
    label: "头像",
    prop: "avatar",
    width: 100,
    scopedSlots: "slotAvatar",
  },
  {
    label: "昵称",
    prop: "nickName",
    width: 150,
    scopedSlots: "slotNickName",
  },
  {
    label: "邮箱",
    prop: "email",
    width: 150,
  },
  {
    label: "出生日期",
    prop: "birthday",
    width: 150,
  },
  {
    label: "加入时间",
    prop: "joinTime",
    scopedSlots: "slotJoinTime",
    width: 300,
  },
  {
    label: "简介",
    prop: "personIntroduction",
  },
  {
    label: "硬币数量",
    prop: "coin",
    scopedSlots: "slotCoin",
  },
  {
    label: "状态",
    prop: "status",
    scopedSlots: "slotStatus",
  },
  {
    label: "操作",
    prop: "operation",
    width: 80,
    scopedSlots: "slotOperation",
  },
];

const tableInfoRef = ref();
const tableOptions = ref({
  extHeight: 10,
});
const searchForm = ref({});
const tableData = ref({});
const loadDataList = async () => {
  let params = {
    pageNum: tableData.value.pageNum,
  };
  Object.assign(params, searchForm.value);
  let result = await proxy.Request({
    url: proxy.Api.loadUser,
    params: params,
  });
  if (!result) {
    return;
  }
  Object.assign(tableData.value, result.data);
};

const changeStatus = (row) => {
  proxy.Confirm({
    message: `确定${row.status == 0 ? "启用" : "禁用"}吗？`,
    okfun: async () => {
      let result = await proxy.Request({
        url: proxy.Api.changeStatus,
        params: {
          userId: row.userId,
          status: row.status == 0 ? 1 : 0,
        },
      });
      if (!result) {
        return;
      }
      proxy.Message.success(`${row.status == 0 ? "启用" : "禁用"}成功`);
      loadDataList();
    },
  });
};
</script>

<style lang="scss" scoped>
.nick-name {
  margin-top: 5px;
  font-size: 12px;
  text-decoration: none;
  font-size: 14px;
  color: var(--text);
}
</style>
