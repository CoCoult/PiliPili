<template>
  <div class="player-panel">
    <div
      class="player-style"
      ref="playerRef"
      :style="{ height: playerHeight + 'px' }"
    ></div>
    <div id="play"><img :src="proxy.Utils.getLocalImage('play.png')" /></div>
  </div>
</template>

<script setup>
import {
  ref,
  reactive,
  getCurrentInstance,
  nextTick,
  onMounted,
  inject,
  onBeforeUnmount,
  computed,
} from "vue";
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from "vue-router";
const route = useRoute();
const router = useRouter();

import Hls from "hls.js";
import Artplayer from "artplayer";
import artplayerPluginDanmuku from "artplayer-plugin-danmuku";
import { useLoginStore } from "@/stores/loginStore.js";
import { mitter } from "@/eventbus/eventBus";
import { init } from "echarts";
const loginStore = useLoginStore();

const playerRef = ref();
let player = null;

const initPlayer = () => {
  if(player) {
    player.destroy();
  }
  //隐藏右键菜单
  Artplayer.CONTEXTMENU = false;
  //自动回放功能的最大记录数，默认为10
  Artplayer.AUTO_PLAYBACK_MAX = 20;
  //自动回放功能的最小记录时长，单位为秒，默认为5
  Artplayer.AUTO_PLAYBACK_MIN = 10;
  player = new Artplayer({
    container: playerRef.value,
    url: "",
    type: "m3u8",
    customType: {
      m3u8: function (video, url, art) {
        if (Hls.isSupported()) {
          if (art.hls) art.hls.destroy();
          const hls = new Hls();
          hls.loadSource(url);
          hls.attachMedia(video);
          art.hls = hls;
          art.on("destroy", () => hls.destroy());
        } else if (video.canPlayType("application/vnd.apple.mpegurl")) {
          video.src = url;
        } else {
          art.notice.show = "浏览器不支持该播放器";
        }
      },
    },
    theme: "#23ade5", //播放器主题颜色，目前用于 进度条 和 高亮元素 上
    volume: 0.7, //播放器的默认音量
    autoplay: true, //是否自动播放，假如希望默认进入页面就能自动播放视频，muted 必需为 true
    autoMini: false, //当播放器滚动到浏览器视口以外时，自动进入 迷你播放 模式
    fullscreen: true, //设置和获取播放器窗口全屏
    fullscreenWeb: true, //设置和获取播放器网页全屏
    setting: true,
    pip: true, //开启画中画
    playbackRate: true, //是否显示视频播放速度功能，会出现在 设置面板 和 右键菜单 里
    flip: true, //是否显示视频翻转功能，目前只出现在 设置面板 和 右键菜单
    aspectRatio: true, //比例
    miniProgressBar: true, //迷你进度条，只在播放器失去焦点后且正在播放时出现
    screenshot: true, //截图
    autoPlayback: true, //自动回放
    //自定义图标
    icons: {
      state: document.querySelector("#play"),
    },
    plugins: [],
  });
  player.on("hover", (state) => {
    let display = "none";
    if (state) {
      display = "flex";
    }
    player.template.$bottom.style.display = display;
  });
  player.on("video:ended", (e) => {
    mitter.emit("playEnd");
  });
};

onMounted(() => {
  nextTick(() => {
    initPlayer();
    const height = Math.round(playerRef.value.clientWidth - 8) * 0.6;
    playerHeight.value = height;
  });

  mitter.on("changeP", (_fileId) => {
    fileId.value = _fileId;
    player.switch = `${proxy.Api.getVideoResource}/${_fileId}/`;
  });
});

onBeforeUnmount(() => {
  if (player) {
    player.destroy(false);
  }
  mitter.off("changeP");
});

const playerHeight = ref(500);

const fileId = ref();

const showPlayer = (height) => {
  playerHeight.value = height;
  initPlayer();
};

const destroyPlayer = () => {
  if (player) {
    player.destroy();
  }
};

defineExpose({
  showPlayer,
  destroyPlayer,
});
</script>

<style lang="scss" scoped>
.player-pshowPlayeranel {
  .player-style {
    width: 100%;
    :deep(.art-video-player.art-mask-show.art-state) {
      position: absolute;
      right: 40px;
      bottom: 50px;
      .art-icon-state {
        width: 60px;
        height: 60px;
        img {
          width: 100%;
        }
      }
    }

    //改变播放器的右侧操作按钮
    :deep(.art-controls-right) {
      position: relative;
      display: block;
      width: 280px;
      .art-control {
        position: absolute;
      }

      //截屏
      .art-control-screenshot {
        left: 0px;
      }
      //设置按钮
      .art-control-setting {
        left: 46px;
      }
      //画中画
      .art-control-pip {
        left: 92px;
      }
      //宽屏
      .art-control-wide-screen,
      .art-control-narrow-screen {
        left: 138px;
        .iconfont {
          font-size: 20px;
        }
      }

      //网页全屏按按钮
      .art-control-fullscreenWeb {
        left: 184px;
      }

      //全屏按钮
      .art-control-fullscreen {
        left: 230px;
      }
    }
  }

  .danmu-panel {
    box-shadow: 0 2px 6px #ddd;
    height: 56px;
    border-top: none;
    background: #fff;
    display: flex;
    align-items: center;
    padding: 0px 15px;
    .watcher {
      width: 250px;
      color: var(--text2);
    }
    #danmu {
      flex: 1;
    }
    .close-danmu {
      flex: 1;
      color: var(--text2);
      text-align: center;
    }
  }
  :deep(.danmu-setting) {
    background: none !important;
    padding: 0px;
    border: none;
    .inner-panel {
      width: 300px;
      height: 280px;
      position: relative;
      .inner {
        position: absolute;
        left: 0px;
        top: 0px;
        z-index: 99999;
        color: #fff;
      }
      .mask {
        position: absolute;
        top: 0px;
        right: 0px;
        left: 0px;
        bottom: 0px;
        background: #000;
        opacity: 0.6;
      }
    }
  }
}
</style>
