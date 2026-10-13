import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'layout',
      component: () => import('@/views/layout/Layout.vue'),
      children: [{
        path: '/',
        name: 'index',
        component: () => import('@/views/index/Index.vue')
      },
      {
        path: '/v/:pCategoryCode',
        name: 'categoryVideo',
        component: () => import('@/views/videoList/CategoryVideo.vue')
      },
      {
        path: '/v/:pCategoryCode/:categoryCode',
        name: 'subCategoryVideo',
        component: () => import('@/views/videoList/CategoryVideo.vue')
      },
      {
        path: '/video/:videoId',
        name: 'videoDetail',
        component: () => import('@/views/videoDetail/VideoDetail.vue')
      },
      {
        path: '/history',
        name: 'history',
        component: () => import('@/views/history/History.vue')
      },
      {
        path: '/message',
        name: 'messageHome',
        component: () => import('@/views/message/UserMessage.vue')
      },
      {
        path: '/message/:messageType',
        name: 'message',
        component: () => import('@/views/message/UserMessage.vue')
      },
      {
        path: '/hot',
        name: 'hot',
        component: () => import('@/views/hot/Hot.vue')
      },
      {
        path: '/search',
        name: 'search',
        component: () => import('@/views/search/Search.vue')
      },
      {
        path: '/agent',
        name: 'agent',
        component: () => import('@/views/agent/AgentView.vue')
      }]
    },
    {
      path: '/userCenter',
      name: 'userCenter',
      redirect: "/userCenter/home",
      component: () => import('@/views/userCenter/UcLayout.vue'),
      children: [
        {
          path: '/userCenter/home',
          name: '用户中心首页',
          component: () => import('@/views/userCenter/Home.vue')
        },
        {
          path: '/userCenter/postVideo',
          name: '上传视频',
          component: () => import('@/views/userCenter/postVideo/Post.vue')
        },
        {
          path: '/userCenter/editVideo',
          name: '编辑视频',
          component: () => import('@/views/userCenter/postVideo/Post.vue')
        },
        {
          path: '/userCenter/video',
          name: '视频列表',
          component: () => import('@/views/userCenter/VideoList.vue')
        },
        {
          path: '/userCenter/comment',
          name: '评论',
          component: () => import('@/views/userCenter/CommentList.vue')
        }, {
          path: '/userCenter/danmu',
          name: '弹幕',
          component: () => import('@/views/userCenter/DanmuList.vue')
        },
      ]
    },
    {
      path: '/user/:userId',
      name: 'userHome',
      component: () => import('@/views/userHome/UserHomeLayout.vue'),
      redirect: '/user/:userId',
      children: [
        {
          path: '/user/:userId',
          name: 'uHome',
          component: () => import('@/views/userHome/Home.vue')
        }, {
          path: '/user/:userId/video',
          name: 'uHomeMyVideo',
          component: () => import('@/views/userHome/VideoList.vue')
        },
        {
          path: '/user/:userId/series',
          name: 'uHomeSeries',
          component: () => import('@/views/userHome/VideoSeries.vue')
        },
        {
          path: '/user/:userId/series/:seriesId',
          name: 'uHomeSeriesDetail',
          component: () => import('@/views/userHome/VideoSeriesDetail.vue')
        },
        {
          path: '/user/:userId/collection',
          name: 'collection',
          component: () => import('@/views/userHome/collection.vue')
        },
        {
          path: '/user/:userId/focus',
          name: 'uHomeFocus',
          component: () => import('@/views/userHome/FocusFansList.vue')
        },
        {
          path: '/user/:userId/fans',
          name: 'uHomeFans',
          component: () => import('@/views/userHome/FocusFansList.vue')
        }
      ]
    }
  ],
})


export default router
