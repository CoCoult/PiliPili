import Request from "./Request";
//单服务版本
const Api = {
    checkCode: "/account/checkCode",//
    login: "/account/login",//
    logout: "/account/logout",//
    register: "/account/register",//
    sendEmailCode: "/account/sendEmailCode",
    autoLogin: "/account/autoLogin",//
    getUserCountInfo: "/account/getUserCountInfo",
    sourcePath: "/api/file/getResource?resourceName=",//
    getCategory: "/category/getCategory",//
    getSysSetting: "/sysSetting/getSysSetting",//
    //发布视频
    preUploadVideo: "/file/preUploadFile",//
    uploadVideo: "/file/uploadVideo",//
    delUploadVideo: "/file/deleteVideo",//
    postVideo: "/videoCenter/postVideo",//
    saveVideoInteraction: "/videoCenter/saveVideoInteraction",//
    getVideoByVideoId: "/videoCenter/getVideoByVideoId",//
    loadUcenterVideoList: "/videoCenter/getVideoPost",//
    getUcenterVideoCountInfo: "/videoCenter/getVideoCountInfo",//
    uploadImage: "/file/uploadCover", //
    //个人中心
    ucLoadAllVideo: "/userCenter/loadAllVideo",//
    ucloadComment: "/userCenter/loadComment",//
    ucDelComment: "/userCenter/delComment",//
    ucLoadDanmu: "/userCenter/loadDanmu",//
    ucDelDanmu: "/userCenter/delDanmu",//
    ucGetActualTimeStatisticsInfo: "/userCenter/getAllStatisticInfo",//
    getWeekStatisticsInfo: "/userCenter/getWeeksStatisticInfo",//
    ucDeleteVideo: "/videoCenter/deleteVideo",//
    //获取视频列表
    loadRecommendVideo: "/video/getRecommendVideo",//
    loadVideo: "/video/getVideo",//
    loadVideoPList: "/video/getVideoPList",//
    getVideoResource: "/api/file/GetVideoSource",//
    getVideoInfo: "/video/getVideoInfo",//
    //评论
    loadComment: "/comment/getComment", // 
    postComment: "/comment/postComment",//
    userDelComment: "/comment/deleteComment",//
    userTopComment: "/comment/topComment",//
    userCancelTopComment: "/comment/cancelTopComment",//
    //弹幕
    loadDanmu: "/barrage/getBarrage",//
    danmuIncrement: "/barrage/getBarrageIncrement",//增量拉取弹幕（5秒轮询，只取新增）
    postDanmu: "/barrage/postBarrage",//
    //上报在线人数
    reportVideoPlayOnline: "/video/reportVideoPlayOnline",//
    //点赞，评论，投币，评论，收藏
    userAction: "/userAction/doAction",//
    //播放历史
    playHisotry: "/history/getVideoHistory",//
    delHistory: "/history/delVideoHistory",//
    cleanHistory: "/history/cleanVideoHistory",//
    //消息
    getNoReadCount: "/message/getMessageCount",//
    loadUserMessage: "/message/getUserMessageList",//
    delMessage: "/message/delUserMessage",//
    getNoReadCountGroup: "/message/getMessageCountGroup",//
    readAll: "/message/readAll",//
    //个人主页
    uHomeUpdateUserInfo: "/userCenter/updateUserInfo",//
    uHomeLoadVideo: "/userCenter/getVideoList",//
    uHomeGetUsesrInfo: "/userCenter/getUserInfo",//
    //关注
    uHomeFocus: "/userCenter/focusUser",//
    //取消关注
    uHomeCancelFocus: "/userCenter/cancelFocusUser",//
    //关注列表
    uHomeFocusList: "/userCenter/getFocusUserList",//
    //粉丝列表
    uHomeFansList: "/userCenter/getFansUserList",//
    //视频系列
    uHomeSeriesLoadVideoSeries: "/userCenter/series/getVideoSeries",//
    //获取系列视频
    uHomeSeriesLoadAllVideo: "/userCenter/series/getAllVideo",//
    //保存系列
    uHomeSeriesSaveVideoSeries: "/userCenter/series/saveVideoSeries",//
    //修改系列顺序
    uHomeSeriesChangeVideoSeriesSort: "/userCenter/series/changeVideoSeriesSort",//
    //获取系列详情
    uHomeSeriesGetVideoSeriesDetail: "/userCenter/series/getSeriesVideoInfo",//
    //删除系列
    uHomeSeriesDelVideoSeries: "/userCenter/series/delVideoSeries",//
    //保存系列视频
    uHomeSeriesSaveSeriesVideo: "/userCenter/series/saveSeriesVideo",//
    //删除系列视频
    uHomeSeriesDelSeriesVideo: "/userCenter/series/delSeriesVideo",//
    //获取所有列表
    uHomeSeriesLoadVideoSeriesWithVideo: "/userCenter/series/getSeriesWithVideo",//
    //收藏列表
    uHomeLoadCollection: "/userCenter/getCollectionVideo",//
    //设置主题
    saveTheme: "/uhome/saveTheme",
    //搜索
    search: "/video/search",//
    getSearchKeywordTop: "/video/getSearchKeywordPop",//
    //推荐视频
    getVideoRecommend: "/video/getVideoRecommend",
    //热门视频
    hotVideoList: "/video/getHotVideoList"//
}

//上传封面
const uploadImage = async (coverFile, createThumbnail = false) => {
    let result = await Request({
        url: Api.uploadImage,
        params: {
            coverFile,
            createThumbnail
        },
    })
    if (!result) {
        return;
    }
    return result.data;
}

const doUserAction = async (config, callback) => {
    let result = await Request({
        url: Api.userAction,
        params: config,
        showLoading: true,
    })
    if (!result) {
        return;
    }
    callback()
}

export {
    Api,
    uploadImage,
    doUserAction
}