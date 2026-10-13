package com.PiliPili.admin.controller;

import com.PiliPili.entity.dto.TokenUserInfoDto;
import com.PiliPili.entity.dto.VideoPlayInfoDto;
import com.PiliPili.entity.pojo.VideoInfoFile;
import com.PiliPili.entity.vo.Result;
import com.PiliPili.entity.config.AppConfig;
import com.PiliPili.entity.constants.Constants;
import com.PiliPili.exception.BusinessException;
import com.PiliPili.service.VideoInfoFileService;
import com.PiliPili.utils.FFmpegUtils;
import com.PiliPili.utils.StringToolsUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.DateUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

@RestController
@RequestMapping("/file")
@Validated
@Slf4j
public class FileController extends BaseController {
    @Resource
    AppConfig appConfig;

    @Resource
    FFmpegUtils fFmpegUtils;

    @Resource
    private VideoInfoFileService videoInfoFileService;

    @RequestMapping("/uploadImage")
    public Result UploadImage(@NotNull MultipartFile file, boolean createThumbnail) {
        //生成日期格式
        String month = DateUtils.formatDate(new Date(), Constants.DATE_FORMAT_1);
        //生成文件存储路径
        String folder = appConfig.getProjectFolder() + Constants.FILE_FOLDER + Constants.FILE_COVER + month;
        File folderFile = new File(folder);
        if (!folderFile.exists()) {
            folderFile.mkdirs();
        }
        String fileName = file.getOriginalFilename();
        //获取图片后缀
        String fileSuffix = fileName.substring(fileName.lastIndexOf("."));
        String realFileName = StringToolsUtils.GetRandomString(Constants.LENGTH_30) + fileSuffix;
        String filePath = Constants.FILE_FOLDER + Constants.FILE_COVER + month + "/" + realFileName;
        try {
            file.transferTo(new File(filePath));
        } catch (IOException e) {
            throw new BusinessException("上传错误，请重新上传");
        }
        if (createThumbnail) {
            //生成缩略图
            fFmpegUtils.CreateThumbnail(filePath);
        }
        return Result.success(filePath);
    }

    /**
     * 获取文件资源
     * @param httpServletResponse
     * @param resourceName
     */
    @RequestMapping("/getResource")
    public void GetResource(HttpServletResponse httpServletResponse, @NotNull String resourceName) {
        if (!StringToolsUtils.pathIsOk(resourceName)) {
            throw new BusinessException(600, "非法访问");
        }
        //获取图片后缀
        String fileSuffix = resourceName.substring(resourceName.lastIndexOf("."));
        httpServletResponse.setContentType("image/" + fileSuffix.replace(".", ""));
        httpServletResponse.setHeader("Cache-Control", "max-age=2592000");
        readFile(httpServletResponse, resourceName);
    }

    /**
     * 读取文件
     * @param response
     * @param filePath
     */
    protected void readFile(HttpServletResponse response, String filePath){
        File file = new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER + filePath);
        if (!file.exists()){
            return;
        }
        try (OutputStream out = response.getOutputStream(); FileInputStream in = new FileInputStream(file)){
            byte[] byteData = new byte[1024];
            int len = 0;
            while ((len = in.read(byteData)) != -1){
                out.write(byteData, 0, len);
            }
            out.flush();
        }catch (Exception e){
            log.error("读取文件错误", e);
        }
    }

    /**
     * 获取视频资源
     */
    @RequestMapping("/GetVideoSource/{fileId}")
    public void GetVideoSource(HttpServletResponse response, @PathVariable @NotEmpty String fileId){
        VideoInfoFile videoInfoFile = this.videoInfoFileService.GetVideoFileByFileId(fileId);
        String filePath = videoInfoFile.getFilePath();
        readFile(response, filePath + "/" + Constants.M3U8_FILE);

        VideoPlayInfoDto videoPlayInfoDto = new VideoPlayInfoDto();
        videoPlayInfoDto.setVideoId(videoInfoFile.getVideoId());
        videoPlayInfoDto.setFileIndex(videoInfoFile.getFileIndex());
        videoPlayInfoDto.setUserId(videoInfoFile.getUserId());


        redisComponent.addVideoPlay(videoPlayInfoDto);
    }

    /**
     * 获取视频资源
     */
    @RequestMapping("/GetVideoSource/{fileId}/{ts}")
    public void GetVideoSource(HttpServletResponse response, @PathVariable @NotEmpty String fileId, @PathVariable @NotEmpty String ts ){
        VideoInfoFile videoInfoFile = this.videoInfoFileService.GetVideoFileByFileId(fileId);
        String filePath = videoInfoFile.getFilePath();
        readFile(response, filePath + "/" + ts);
    }
}
