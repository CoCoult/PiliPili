package com.PiliPili.utils;

import com.PiliPili.entity.config.AppConfig;
import com.PiliPili.entity.constants.Constants;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.math.BigDecimal;

@Component
public class FFmpegUtils {
    @Resource
    AppConfig appConfig;

    public void CreateThumbnail(String filaPath) {
//        String CMD = "ffmpeg -i \"%s\" -vf scale=200:-1 \"%s\"";
//        CMD = String.format(CMD,  filaPath + Constants.THUMBNAIL_SUFFIX);
//        ProcessUtils.executeCommand(CMD, false);
        String inputPath = filaPath + Constants.THUMBNAIL_SUFFIX;
        String outputPath = filaPath + "_thumbnail" + Constants.THUMBNAIL_SUFFIX;

        String CMD = "ffmpeg -i \"%s\" -vf scale=200:-1 \"%s\"";
        CMD = String.format(CMD, inputPath, outputPath);
    }

    /**
     * 获取视频播放时长
     *
     * @return
     */
    public Integer GetVideoInfoDuration(String completeVideo) {
        final String CMD_GET_CODE = "ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 \"%s\"";
        String CMD = String.format(CMD_GET_CODE, completeVideo);
        String result = ProcessUtils.executeCommand(CMD, true);
        if (result == null || result.isEmpty()) {
            return 0;
        }
        result = result.replace("\n", "");
        return new BigDecimal(result).intValue();
    }

    /**
     * 获取视频文件格式
     */
    public String GetVideoCodec(String videoFilePath) {
        final String CMD_GET_CODE = "ffprobe -v error -select_streams v:0 -show_entries stream=codec_name \"%s\"";
        String cmd = String.format(CMD_GET_CODE, videoFilePath);
        String result = ProcessUtils.executeCommand(cmd, true);
        result = result.replace("\n", "");
        result = result.substring(result.indexOf("=") + 1);
        return result.substring(0, result.indexOf("["));
    }

    /**
     * 视频转码
     */
    public void convertHevc2mp4(String newFileName, String videoFilePath) {
        String CMD_HEVC_264 = "ffmpeg -i \"%s\" -c:v libx264 -crf 20 \"%s\" -y";
        String cmd = String.format(CMD_HEVC_264, newFileName, videoFilePath);
        ProcessUtils.executeCommand(cmd, true);
    }

    /**
     * 生成Ts格式
     */
    public void convert2Ts(File tsFolder, String videoFilePath) {
        final String CMD_TRANSFER_2TS = "ffmpeg -y -i \"%s\" -vcodec copy -acodec copy -bsf:v h264_mp4toannexb \"%s\"";
        final String CMD_CUT_TS = "ffmpeg -i \"%s\" -c copy -map 0 -f segment -segment_list \"%s\" -segment_time 10 %s/%%4d.ts";
        String tsPath = tsFolder + "/" + Constants.TS_NAME;

        // 生成 .ts 文件
        String cmd = String.format(CMD_TRANSFER_2TS, videoFilePath, tsPath);
        ProcessUtils.executeCommand(cmd, true);

        // 生成索引文件 .m3u8 和切片 .ts
        cmd = String.format(CMD_CUT_TS, tsPath, tsFolder.getPath() + "/" + Constants.M3U8_NAME, tsFolder.getPath());
        ProcessUtils.executeCommand(cmd, true);

        // 删除临时生成的 index.ts
        new File(tsPath).delete();
    }

}
