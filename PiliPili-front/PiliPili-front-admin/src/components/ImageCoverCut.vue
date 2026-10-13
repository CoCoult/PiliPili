<template>
  <Dialog
    :show="dialogConfig.show"
    :title="dialogConfig.title"
    :buttons="dialogConfig.buttons"
    width="1000px"
    :showCancel="false"
    @close="dialogConfig.show = false"
  >
    <div class="cut-image-panel">
      <VueCropper
        ref="cropperRef"
        class="cropper"
        :img="sourceImage"
        outputType="png"
        :autoCrop="true"
        :autoCropWidth="cutWidth"
        :autoCropHeight="Math.round(props.cutWidth * props.scale)"
        :fixed="true"
        :fixedNumber="[1, props.scale]"
        :centerBox="true"
        :full="false"
        @realTime="preview"
        mode="100%"
      ></VueCropper>
      <div class="preview-panel">
        <div class="preview-image">
          <img :src="previewsImage" />
        </div>
        <el-upload
          :multiple="false"
          :show-file-list="false"
          :http-request="selectFile"
          :accept="proxy.imageAccept"
        >
          <el-button class="select-btn" type="primary">选择图片</el-button>
        </el-upload>
      </div>
    </div>

    <div class="info">
      建议上传至少{{ props.cutWidth }} *
      {{ Math.round(props.cutWidth * props.scale) }}px的图片
    </div>
  </Dialog>
</template>

<script setup>
import "vue-cropper/dist/index.css";
import { VueCropper } from "vue-cropper";
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

const props = defineProps({
  cutWidth: {
    type: Number,
    default: 150,
  },
  scale: {
    type: Number,
    default: 1,
  },
});

const dialogConfig = ref({
  show: false,
  title: "上传图片",
  buttons: [
    {
      type: "primary",
      text: "确定",
      click: (e) => {
        curImage();
      },
    },
  ],
});

const show = () => {
  dialogConfig.value.show = true;
  sourceImage.value = "";
  nextTick(() => {
    previewsImage.value = "";
  });
};

const cutImageCallback = inject("cutImageCallback");

const cropperRef = ref();
const previewsImage = ref();

const sourceImage = ref();
const selectFile = (file) => {
  file = file.file;
  let img = new FileReader();
  img.readAsDataURL(file);
  img.onload = ({ target }) => {
    sourceImage.value = target.result;
  };
};

const preview = (data) => {
  cropperRef.value.getCropData((data) => {
    previewsImage.value = data;
  });
};

const curImage = () => {
  const cropW = Math.round(cropperRef.value.cropW);
  const cropH = Math.round(cropperRef.value.cropH);
  if (cropW == 0 || cropH == 0) {
    proxy.Message.warning("请选择图片");
    return;
  }
  if (cropW < props.cutWidth || cropH < props.cutWidth * props.scale) {
    proxy.Message.warning(`至少为${props.cutWidth} *
      ${Math.round(props.cutWidth * props.scale)}px的图片`);
    return;
  }
  cropperRef.value.getCropBlob((blob) => {
    const file = new File(
      [blob],
      "temp." + blob.type.substring(blob.type.indexOf("/") + 1),
      { type: blob.type }
    );
    dialogConfig.value.show = false;
    cutImageCallback({
      coverImage: file,
    });
  });
};

defineExpose({
  show,
});
</script>

<style lang="scss" scoped>
.cut-image-panel {
  display: flex;

  .cropper {
    flex: 1;
    height: 500px;
  }

  .preview-panel {
    width: 200px;
    margin-left: 20px;
    text-align: center;

    .preview-image {
      width: 100%;
      height: 200px;
      background: #f6f6f6;
      display: flex;
      align-items: center;
    }
    img {
      width: 100%;
    }
  }

  .select-btn {
    margin-top: 20px;
  }
}
.info {
  color: #6b6b6b;
}
</style>

