import './assets/scss/base.scss'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import '@/assets/icon/iconfont.css'

import App from './App.vue'
import router from './router'
import VueCookies from 'vue-cookies'

import Dialog from "@/components/Dialog.vue"
import Cover from "@/components/Cover.vue"
import Avatar from "@/components/Avatar.vue"
import VideoItem from "@/components/VideoItem.vue"
import DataLoadMoreList from "@/components/DataLoadMoreList.vue"
import NoData from "@/components/NoData.vue"
import Collapse from "@/components/Collapse.vue"
import DataList from "@/components/DataList.vue"
import Table from "@/components/Table.vue"
import Player from "@/components/Player.vue"


import Verify from "@/utils/Verify"
import Request from "@/utils/Request"
import Message from "@/utils/Message"
import { Api } from "@/utils/Api"
import Utils from './utils/Utils'
import Confirm from './utils/Confirm'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.component("Dialog", Dialog)
app.component("Cover", Cover)
app.component("Avatar", Avatar)
app.component("VideoItem", VideoItem)
app.component("DataLoadMoreList", DataLoadMoreList)
app.component("NoData", NoData)
app.component("Collapse", Collapse)
app.component("DataList", DataList)
app.component("Table", Table)
app.component("Player", Player)

app.config.globalProperties.VueCookies = VueCookies;
app.config.globalProperties.bodyMaxWidth = 2000;
app.config.globalProperties.bodyMinWidth = 1250;
app.config.globalProperties.Verify = Verify;
app.config.globalProperties.Request = Request;
app.config.globalProperties.Message = Message;
app.config.globalProperties.Api = Api;
app.config.globalProperties.Utils = Utils;
app.config.globalProperties.Confirm = Confirm;
//一行展示多少个分类
app.config.globalProperties.rowCategoryCount = 10;
app.config.globalProperties.imageAccept = ".jpg,.png,.gif,.bmp,.webp";
//视频后缀
app.config.globalProperties.videoAccept = ".mp4,.avi,.rmvb,.mkv,.mov";


app.config.globalProperties.imageThumbnailSuffix = "_thumbnail.jpg";

app.config.globalProperties.bodyPadding = 150;
app.config.globalProperties.carouselMaxCount = 5;

// 分片大小
app.config.globalProperties.chunkSize = 1024 * 512
// 最多同时上传数量
app.config.globalProperties.maxUploading = 3




app.mount('#app')
