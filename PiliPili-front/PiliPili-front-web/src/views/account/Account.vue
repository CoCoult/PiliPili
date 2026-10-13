<template>
  <Dialog
    :show="loginStore.showLogin"
    :title="dialogConfig.title"
    :buttons="dialogConfig.buttons"
    width="1000px"
    :showCancel="false"
    @close="closeDialog()"
  >
    <div class="dialog-panel">
      <div class="bg">
        <img src="../../assets/login_bg.png" />
      </div>
      <el-form
        class="login-register"
        :model="formData"
        :rules="rules"
        ref="formDataRef"
        label-width="80px"
        @submit.prevent
      >
        <div class="tab-panel">
          <div :class="[opType == 1 ? 'active' : '']" @click="showPanel(1)">
            登录
          </div>
          <el-divider direction="vertical"></el-divider>
          <div :class="[opType == 0 ? 'active' : '']" @click="showPanel(0)">
            注册
          </div>
        </div>
        <!--input输入-->
        <el-form-item prop="email">
          <el-input
            clearable
            placeholder="请输入邮箱"
            v-model.trim="formData.email"
            :maxlength="150"
            size="large"
          >
            <template #prefix>
              <span class="iconfont icon-account"></span>
            </template>
          </el-input>
        </el-form-item>

        <!--input输入-->
        <el-form-item prop="password" v-if="opType == 1 || opType == null">
          <el-input
            show-password
            placeholder="请输入密码"
            v-model.trim="formData.password"
            size="large"
          >
            <template #prefix>
              <span class="iconfont icon-password"></span>
            </template>
          </el-input>
        </el-form-item>
        <!--注册 -->
        <div v-if="opType == 0">
          <!--input输入-->
          <el-form-item prop="userName" v-if="opType == 0">
            <el-input
              clearable
              placeholder="请输入昵称"
              v-model.trim="formData.userName"
              :maxlength="150"
              size="large"
            >
              <template #prefix>
                <span class="iconfont icon-account"></span>
              </template>
            </el-input>
          </el-form-item>
          <!--input输入-->
          <el-form-item prop="registerPassword">
            <el-input
              show-password
              placeholder="请输入密码"
              v-model.trim="formData.registerPassword"
              size="large"
            >
              <template #prefix>
                <span class="iconfont icon-password"></span>
              </template>
            </el-input>
          </el-form-item>
          <!--input输入-->
          <el-form-item prop="reRegisterPassword">
            <el-input
              show-password
              placeholder="请再次输入密码"
              v-model.trim="formData.reRegisterPassword"
              size="large"
            >
              <template #prefix>
                <span class="iconfont icon-password"></span>
              </template>
            </el-input>
          </el-form-item>
        </div>
        <!--input输入-->
        <el-form-item prop="checkCode">
          <div class="check-code-panel">
            <el-input
              placeholder="请输入验证码"
              v-model.trim="formData.checkCode"
              size="large"
            >
              <template #prefix>
                <span class="iconfont icon-checkcode"></span>
              </template>
            </el-input>
            <img :src="checkCodeInfo.checkCode" @click="changeCheckCode()" />
          </div>
        </el-form-item>
        <!--input输入-->
        <el-form-item label="" prop="">
          <el-button
            class="login-btn"
            size="large"
            type="primary"
            @click="doSubmit"
          >
            <span v-if="opType == 1">登录</span>
            <span v-if="opType == 0">注册</span>
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </Dialog>
</template>

<script setup>
import { ElForm } from "element-plus";
import {
  ref,
  reactive,
  getCurrentInstance,
  nextTick,
  onMounted,
  onUpdated,
} from "vue";
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router";
const route = useRoute();
const router = useRouter();
import md5 from "js-md5";

import { useLoginStore } from "@/stores/loginStore.js";
const loginStore = useLoginStore();

const checkCodeInfo = ref({});
const changeCheckCode = async () => {
  let result = await proxy.Request({
    url: proxy.Api.checkCode,
  });
  if (!result) {
    return;
  }
  checkCodeInfo.value = result.data;
};

const dialogVisible = ref(true);

const dialogConfig = ref({
  buttons: [],
});

const formData = ref({
  registerPassword: "",
});
const formDataRef = ref();
const checkPassword = (rule, value, callback) => {
  if (value != formData.value.registerPassword) {
    callback(new Error(rule.message));
  } else {
    callback();
  }
};
const rules = {
  email: [
    { required: true, message: "请输入内容" },
    {
      validator: proxy.Verify.email,
      message: "请输入正确的邮箱",
    },
  ],
  password: [{ required: true, message: "请输入密码" }],
  userName: [{ required: true, message: "请输入昵称" }],
  registerPassword: [
    { required: true, message: "请输入密码" },
    {
      validator: proxy.Verify.password,
      message: "密码只能是数字、字母或特殊符号8-18位组成",
    },
  ],
  reRegisterPassword: [
    { required: true, message: "请再次输入密码" },
    {
      validator: checkPassword,
      message: "两次输入密码不一致",
    },
  ],
  checkCode: [{ required: true, message: "请输入图片验证码" }],
};

const doSubmit = () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }
    let params = {};
    Object.assign(params, formData.value);
    params.checkCodeKey = checkCodeInfo.value.checkCodeKey;
    console.log(params);
    if (opType.value == 1) {
      params.password = md5(params.password);
    } else {
      params.password = md5(params.reRegisterPassword);
    }
    let result = await proxy.Request({
      url: opType.value == 0 ? proxy.Api.register : proxy.Api.login,
      params,
      errorCallback: () => {
        changeCheckCode();
      },
    });
    if (!result) {
      return;
    }
    if (opType.value == 0) {
      proxy.Message.success("注册成功，请登录");
      showPanel(1);
    } else if (opType.value == 1) {
      proxy.Message.success("登陆成功");
      loginStore.setLogin(false);
      loginStore.saveUserInfo(result.data);
    }
  });
};

const resetForm = () => {
  changeCheckCode();
  nextTick(() => {
    formDataRef.value.resetFields();
  });
};
const submitForm = () => {};

const opType = ref(1);

const showPanel = (type) => {
  opType.value = type;
  if (loginStore.showLogin.valueOf) {
    resetForm();
  }
};

const closeDialog = () => {
  loginStore.setLogin(false);
};

onUpdated(() => {
  showPanel(1);
});

onMounted(() => {
  showPanel(1);
});
</script>

<style lang="scss" scoped>
.dialog-panel {
  display: flex;
  align-items: center;
  justify-content: space-around;
  .bg {
    width: 450px;
    height: 580px;
    overflow: hidden;
    img {
      width: 100%;
    }
  }
  .login-register {
    width: 350px;
    .tab-panel {
      margin: 10px auto;
      display: flex;
      width: 130px;
      font-size: 18px;
      align-items: center;
      justify-content: space-around;
      cursor: pointer;
      .active {
        color: var(--blue);
      }
    }
    .no-account {
      width: 100%;
      display: flex;
      justify-content: space-between;
    }
    .login-btn {
      width: 100%;
    }
    .bottom-btn {
      margin-bottom: 0px;
    }
  }
}

.check-code-panel {
  display: flex;
  align-items: center;
  width: 100%;
  .input {
    flex: 1;
  }
  .right-panel {
    margin-left: 5px;
    cursor: pointer;
  }
}
</style>
