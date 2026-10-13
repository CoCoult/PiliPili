import { format } from "echarts";
import moment from "moment";

const getLocalImage = (image) => {
    image = `../assets/${image}`
    return new URL(image, import.meta.url).href;
}

const formatDate = (timestamp) => {
    const timestampTime = moment(timestamp);
    const days = Number.parseInt(moment().format("YYYYMMDD")) - Number.parseInt(timestampTime.format("YYYYMMDD"));
    if (days == 0) {
        return timestampTime.format("HH:mm");
    } else if (days == 1) {
        return "昨天";
    } else if (days >= 2 && days < 7) {
        return timestampTime.format("dddd");
    } else {
        return timestampTime.format("YYYY-MM-DD");
    }
}



const convertSecondsToHMS = (seconds) => {
    const padZero = (num) => {
        return num.toString().padStart(2, '0');
    }
    const hours = Math.floor(seconds / 3600);
    const remainingSeconds = seconds % 3600;
    const minutes = Math.floor(remainingSeconds / 60);
    const sec = remainingSeconds % 60;
    if (hours > 0) {
        return `${padZero(hours)}:${padZero(minutes)}:${padZero(sec)}`;
    } else {
        return `${padZero(minutes)}:${padZero(sec)}`;
    }

}

const resetHtmlContent = (data) => {
    if (!data) {
        return data;
    }
    data = data.replace(/\r\n/g, "<br>");
    data = data.replace(/\n/g, "<br>");
    return data;
}

const formatTime = (date) => {
    return moment(date).format("YYYY-MM-DD HH:mm");
}

const formatTimeYear = (date) => {
    return moment(date).format("YYYY-MM-DD");
}

const getFileName = (fileName) => {
    if (!fileName) {
        return fileName;
    }
    return fileName.lastIndexOf(".") === -1 ? fileName : fileName.substring(0, fileName.lastIndexOf("."));
};

const size2Str = (limit) => {
    var size = "";
    if (limit < 0.1 * 1024) { //小于0.1KB，则转化成B
        size = limit.toFixed(2) + "B"
    } else if (limit < 1024 * 1024) { //小于0.1MB，则转化成KB
        size = (limit / 1024).toFixed(2) + "KB"
    } else if (limit < 1024 * 1024 * 1024) { //小于1GB，则转化成MB
        size = (limit / (1024 * 1024)).toFixed(2) + "MB"
    } else { //其他转化成GB
        size = (limit / (1024 * 1024 * 1024)).toFixed(2) + "GB"
    }
    var sizeStr = size + ""; //转成字符串
    var index = sizeStr.indexOf("."); //获取小数点处的索引
    var dou = sizeStr.substring(index + 1, 2); //获取小数点后两位的值
    if (dou == "00") { //判断后两位是否为00，如果是则删除00
        return sizeStr.substring(0, index) + sizeStr.substr(index + 3, 2)
    }
    return size;
}

export default {
    getLocalImage,
    formatDate,
    convertSecondsToHMS,
    resetHtmlContent,
    formatTime,
    getFileName,
    size2Str,
    formatTimeYear
}