import Request from "./Request";
//单服务版本
const Api = {
    sourcePath: "/api/file/getResource?resourceName=",
    checkCode: "/account/checkCode",
    login: "/account/login",
    uploadImage: "/file/uploadImage",
    loadCategory: "/category/getCategory",//
    saveCategory: "/category/saveCategory",//
    delCategory: "/category/deleteCategory",//
    changeCategorySort: "/category/changeSort",
    //视频
    loadVideo: "/videoInfo/getVideoPost",//
    loadVideoPList: "/videoInfo/getVideoPList",//
    //推荐视频
    recommendVideo: "/videoInfo/recommendVideo",
    //互动
    loadComment: "/interact/getComment",//
    delComment: "/interact/deleteComment",//
    //弹幕
    loadDanmu: "/interact/getBarrage",//
    delDanmu: "/interact/deleteBarrage",//
    //用户管理
    loadUser: "/user/getUser",//
    changeStatus: "/user/changeStatus",//
    //视频资源
    getVideoResource: "/api/file/GetVideoSource",//
    auditVideo: "/videoInfo/auditVideo",//
    deleteVideo: "/videoInfo/deleteVideo",
    getSetting: "/setting/getSetting",//
    saveSetting: "/setting/saveSetting",//
    //首页统计数据
    getActualTimeStatisticsInfo: "/index/getAllStatisticInfo",//
    getWeekStatisticsInfo: "/index/getWeeksStatisticInfo"//
}

//上传封面
const uploadImage = async (file, createThumbnail = false) => {
    let result = await Request({
        url: Api.uploadImage,
        params: {
            file,
            createThumbnail
        },
    })
    if (!result) {
        return;
    }
    return result.data;
}

export {
    Api,
    uploadImage,
}