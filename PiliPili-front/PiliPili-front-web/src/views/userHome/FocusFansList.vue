<template>
  <div class="body-container">
    <div class="body-title">
      {{ route.name == "uHomeFocus" ? "我的关注" : "我的粉丝" }}
    </div>
    <DataList :dataSource="dataSource" @loadData="loadDataList">
      <template #default="{ data }">
        <div class="data-item">
          <Avatar :userAvatar="data.userAvatar" :userId="data.userId"></Avatar>
          <div class="user-info">
            <div class="nick-name">
              <router-link
                :to="`/user/${route.name == 'uHomeFocus' ? data.focusUserId:data.userId}`"
                target="_blank"
                class="a-link"
              >
                {{ data.userName }}
              </router-link>
            </div>
            <div class="introduction">
              {{ data.selfIntroduction || "这个人没有填简介~~" }}
            </div>
          </div>
          <div class="op-btns">
            <div v-if="data.focusType == 1" class="focus-eachother">已互粉</div>
            <el-button
              link
              type="primary"
              @click="cancelFocus(data.focusUserId)"
              v-if="route.name == 'uHomeFocus' || data.focusType == 1"
              >取消关注</el-button
            >
            <el-button
              type="primary"
              v-if="route.name == 'uHomeFans' && data.focusType == 0"
              @click="focus(data.userId)"
              >关注</el-button
            >
          </div>
        </div>
      </template>
    </DataList>
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
  inject,
} from "vue";
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router";
const route = useRoute();
const router = useRouter();
const dataSource = ref({});
const loadDataList = async () => {
  let params = {
    pageNum: dataSource.value.pageNum,
  };
  let result = await proxy.Request({
    url:
      route.name == "uHomeFocus"
        ? proxy.Api.uHomeFocusList
        : proxy.Api.uHomeFansList,
    params,
  });
  if (!result) {
    return;
  }
  dataSource.value = result.data;
};

watch(
  () => route.name,
  (newVal, oldVal) => {
    if (newVal == "uHomeFocus" || newVal == "uHomeFans") {
      loadDataList();
    }
  },
  { immediate: true, deep: true }
);

const cancelFocusUser = inject("cancelFocusUser");
const cancelFocus = (focusUserId) => {
  cancelFocusUser(focusUserId);
  loadDataList();
};

const focusUser = inject("focusUser");
const focus = (userId) => {
  focusUser(userId);
  loadDataList();
};
</script>

<style lang="scss" scoped>
.body-container {
  background: #ffff;
  padding: 20px;
  border-radius: 5px;
  .body-title {
    font-size: 18px;
    color: #6d757a;
    border-bottom: 1px solid #e5e9ef;
    padding: 0px 0px 10px 0px;
  }
  .data-item {
    display: flex;
    align-items: center;
    padding: 10px;
    .user-info {
      flex: 1;
      margin: 0px 10px;
      .introduction {
        margin-top: 10px;
        font-size: 13px;
        color: #6d757a;
        overflow: hidden;
        white-space: nowrap;
        width: 100%;
        text-overflow: ellipsis;
        padding-right: 10px;
      }
    }
    .op-btns {
      display: flex;
      align-items: center;
      .focus-eachother {
        margin-right: 10px;
        color: var(--text3);
      }
    }
  }
}
</style>
