<template>
  <div class="-container"></div>
  <el-dialog
    :show-close="showClose"
    :draggable="draggable"
    :model-value="show"
    :close-on-click-modal="false"
    class="cust-dialog"
    :top="top + 'px'"
    :width="width"
    @close="close"
  >
    <template #header="{ close, titleId, titleClose }">
      <div v-if="title" class="title">{{ title }}</div>
      <slot v-else name="header"></slot>
    </template>
    <div
      class="dialog-body"
      :style="{ 'max-height': maxHeight + 'px', padding: padding + 'px' }"
    >
      <slot></slot>
    </div>
    <template v-if="(buttons && buttons.length > 0) || showCancel">
      <div class="dialog-footer">
        <el-button link @click="close" v-if="showCancel">取消</el-button>
        <el-button
          v-for="btn in buttons"
          :key="btn.type"
          :type="btn.type || 'primary'"
          @click="btn.click"
        >
          {{ btn.text }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>



<script lang="ts" setup>
import { ref, reactive, getCurrentInstance, nextTick, onMounted } from "vue";
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router";
const route = useRoute();
const router = useRouter();

const props = defineProps({
  showClose: {
    type: Boolean,
    default: true, // 可根据实际情况修改默认值
  },
  showCancel: {
    type: Boolean,
    default: true, // 可根据实际情况修改默认值
  },
  draggable: {
    type: Boolean,
    default: false, // 可根据实际情况修改默认值
  },
  title: {
    type: String,
  },
  show: {
    type: Boolean,
    default: false,
  },
  top: {
    type: Number,
    default: 50,
  },
  width: {
    type: String,
    default: "30%",
  },
  buttons: {
    type: Array,
  },
  padding: {
    type: Number,
    default: 15,
  },
});

const maxHeight = window.innerHeight - props.top - 120;
const emit = defineEmits();
const close = () => {
  emit("close");
};
</script>

<style lang="scss">
.cust-dialog {
  padding: 0px !important;
  margin-bottom: 5px !important;
  .el-dialog__header {
    padding: 16px;
  }
  .title {
    font-size: 20px;
  }
  .dialog-body {
    min-height: 80px;
    overflow: auto;
    overflow-x: hidden;
    padding: 20px;
  }
  .dialog-footer {
    border-top: 1px solid #ddd;
    text-align: right;
    padding: 5px 20px;
  }
}
</style>