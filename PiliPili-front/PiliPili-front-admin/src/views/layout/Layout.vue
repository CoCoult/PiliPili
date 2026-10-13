<template>
  <div class="layout-body">
    <el-container>
      <el-aside class="aside">
        <div class="logo">
          <span>PiliPili后台管理</span>
        </div>
        <div class="menu-panel">
          <el-menu
            :default-openeds="defaultOpeneds"
            :collapse-transition="false"
            class="el-menu-vertical-demo"
            background-color="#3d3c4a"
            text-color="#fff"
            active-text-color="#fff"
            router
            :defaultActive="route.path"
          >
            <template v-for="item in menuList">
              <el-sub-menu :key="item" :index="item.path" v-if="item.children">
                <template #title>
                  <i :class="['iconfont', item.icon]"></i>
                  <span class="menu-name">{{ item.menuName }}</span>
                </template>
                <el-menu-item
                  :index="subItem.path"
                  v-for="subItem in item.children"
                  :key="subItem"
                >
                  <span class="menu-name">{{ subItem.menuName }}</span>
                </el-menu-item>
              </el-sub-menu>
              <el-menu-item :index="item.path" v-else
                ><i :class="['iconfont', item.icon]"></i
                ><template #title
                  ><span class="menu-name">{{ item.menuName }}</span>
                </template>
              </el-menu-item>
            </template>
          </el-menu>
        </div>
      </el-aside>
      <el-container>
        <el-header class="header"> {{userInfo}} </el-header>
        <el-main class="main-content">
          <div class="content-body">
            <router-view />
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, onMounted } from "vue";
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router";
const route = useRoute();
const router = useRouter();

const menuList = [
  {
    menuName: "首页",
    path: "/home",
    icon: "icon-home",
  },
  {
    menuName: "内容管理",
    path: "/content",
    icon: "icon-content",
    children: [
      {
        menuName: "分类管理",
        path: "/content/category",
      },
      {
        menuName: "稿件管理",
        path: "/content/video",
      },
    ],
  },
  {
    menuName: "互动管理",
    path: "/interact",
    icon: "icon-hudong",
    children: [
      {
        menuName: "评论管理",
        path: "/interact/comment",
      },
      {
        menuName: "弹幕管理",
        path: "/interact/danmu",
      },
    ],
  },
  {
    menuName: "用户管理",
    path: "/user/userList",
    icon: "icon-fans",
  },
  {
    menuName: "系统设置",
    path: "/setting",
    icon: "icon-setting",
  },
];

const defaultActive = ref();
const defaultOpeneds = ref([]);

const init = () => {
  menuList.forEach((item) => {
    defaultOpeneds.value.push(item.path);
  });
};
init();

const userInfo = ref(proxy.VueCookies.get("account"));
</script>

<style lang="scss" scoped>
.layout-body {
  .aside {
    background: #3d3c4a;
    width: 250px;
    .logo {
      display: flex;
      height: 50px;
      color: #fff;
      background: #191828;
      align-items: center;
      font-size: 18px;
      padding-left: 5px;
    }
    .menu-panel {
      height: calc(100vh - 50px);
      .menu-name {
        padding-left: 10px;
      }
      .el-menu {
        border-right: none;
      }
      //每个菜单的颜色
      .el-menu-item {
        background: #353544;
        .is-active {
          color: #fff;
          background: var(--el-color-primary);
        }
      }
      //鼠标移上去的颜色
      .el-menu-item:hover {
        color: #d8d8ee;
      }
    }
  }
  .header {
    background: #fff;
    border-bottom: 1px solid #ddd;
    height: 50px;
    line-height: 50px;
    padding: 0px 10px !important;
    text-align: right;
  }
  .main-content {
    padding: 0px;
    .tag-content {
      .el-tabs--border-card {
        border: none;
      }
      .el-tabs__content {
        display: none;
      }
    }
    .content-body {
      overflow: hidden;
      padding: 10px 10px 5px 10px;
    }
  }
}
</style>
