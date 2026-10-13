SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- PiliPili 数据库表结构重建（依据项目 MyBatis Mapper + 实体类 + Navicat 导出）
-- ============================================================

-- 1. category 分类表
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `category_id` int NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `category_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类编码',
  `category_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类名称',
  `p_category_id` int NULL DEFAULT 0 COMMENT '父级分类ID',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类图标',
  `background` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '背景图',
  `sort` int NULL DEFAULT 0 COMMENT '排序号',
  PRIMARY KEY (`category_id`) USING BTREE,
  INDEX `idx_p_category_id`(`p_category_id` ASC) USING BTREE,
  INDEX `idx_sort`(`sort` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '分类表' ROW_FORMAT = Dynamic;

-- 2. user 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `user_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户ID',
  `user_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `email` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `password` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码(MD5)',
  `gender` tinyint NULL DEFAULT NULL COMMENT '0:女 1:男',
  `birthday` date NULL DEFAULT NULL COMMENT '生日',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `school` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学校',
  `self_introduction` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '个人简介',
  `create_time` datetime NULL DEFAULT NULL COMMENT '注册时间',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后登录IP',
  `status` tinyint NULL DEFAULT NULL COMMENT '0:禁用 1:正常',
  `notice` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '公告',
  `total_coin` int NULL DEFAULT 0 COMMENT '总硬币',
  `current_coin` int NULL DEFAULT 0 COMMENT '当前硬币',
  `theme` tinyint NULL DEFAULT 0 COMMENT '主题',
  PRIMARY KEY (`user_id`) USING BTREE,
  INDEX `idx_user_name`(`user_name` ASC) USING BTREE,
  INDEX `idx_email`(`email` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- 3. video_info 视频信息表
DROP TABLE IF EXISTS `video_info`;
CREATE TABLE `video_info` (
  `video_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '视频ID',
  `video_cover` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '视频封面',
  `video_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '视频名称',
  `introduction` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '视频简介',
  `user_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户ID',
  `create_time` datetime NOT NULL COMMENT '上传时间',
  `last_update_time` datetime NULL DEFAULT NULL COMMENT '最后修改时间',
  `p_category_id` int NOT NULL COMMENT '父级分类ID',
  `category_id` int NULL DEFAULT NULL COMMENT '分类ID',
  `post_type` tinyint NOT NULL COMMENT '0:自制 1:转载',
  `origin_info` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '源资源说明',
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签',
  `interaction` varchar(2550) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '互动',
  `duration` int NULL DEFAULT 0 COMMENT '视频时长(秒)',
  `play_count` int NULL DEFAULT 0 COMMENT '播放数',
  `like_count` int NULL DEFAULT 0 COMMENT '点赞数',
  `barrage_count` int NULL DEFAULT 0 COMMENT '弹幕数',
  `comment_count` int NULL DEFAULT 0 COMMENT '评论数',
  `coin_count` int NULL DEFAULT 0 COMMENT '硬币数',
  `collect_count` int NULL DEFAULT 0 COMMENT '收藏数',
  `recommend_type` int NULL DEFAULT 0 COMMENT '0:未推荐 1:推荐',
  `last_play_time` datetime NULL DEFAULT NULL COMMENT '最后播放时间',
  PRIMARY KEY (`video_id`) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_pcategory_id`(`p_category_id` ASC) USING BTREE,
  INDEX `idx_recommend_type`(`recommend_type` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '视频信息表' ROW_FORMAT = Dynamic;

-- 4. video_info_post 视频投稿表
DROP TABLE IF EXISTS `video_info_post`;
CREATE TABLE `video_info_post` (
  `video_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '视频ID',
  `video_cover` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '视频封面',
  `video_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '视频名称',
  `introduction` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '视频简介',
  `user_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户ID',
  `create_time` datetime NOT NULL COMMENT '上传时间',
  `last_update_time` datetime NOT NULL COMMENT '最后修改时间',
  `p_category_id` int NOT NULL COMMENT '父级分类ID',
  `category_id` int NULL DEFAULT NULL COMMENT '分类ID',
  `status` tinyint NOT NULL COMMENT '0:转码中 1:转码失败 2:待审核 3:审核成功 4:审核失败',
  `post_type` tinyint NOT NULL COMMENT '0:自制 1:转载',
  `origin_info` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '源资源说明',
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签',
  `interaction` varchar(2550) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '互动',
  `duration` int NULL DEFAULT NULL COMMENT '视频时长',
  PRIMARY KEY (`video_id`) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_pcategory_id`(`p_category_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '视频投稿表' ROW_FORMAT = Dynamic;

-- 5. video_info_file 视频文件表
DROP TABLE IF EXISTS `video_info_file`;
CREATE TABLE `video_info_file` (
  `file_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '唯一ID',
  `video_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '视频ID',
  `user_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户ID',
  `file_index` int NOT NULL COMMENT '文件索引',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件名',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小',
  `file_path` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件路径',
  `duration` int NULL DEFAULT NULL COMMENT '视频时长(秒)',
  PRIMARY KEY (`file_id`) USING BTREE,
  INDEX `idx_video_id`(`video_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '视频文件表' ROW_FORMAT = Dynamic;

-- 6. video_info_file_post 视频投稿文件表
DROP TABLE IF EXISTS `video_info_file_post`;
CREATE TABLE `video_info_file_post` (
  `file_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '唯一ID',
  `upload_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '上传ID',
  `video_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '视频ID',
  `user_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户ID',
  `file_index` int NOT NULL COMMENT '文件索引',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件名',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小',
  `file_path` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件路径',
  `update_type` tinyint NULL DEFAULT NULL COMMENT '0:无更新 1:有更新',
  `transfer_result` tinyint NULL DEFAULT NULL COMMENT '0:转码中 1:转码失败 2:转码成功',
  `duration` int NULL DEFAULT NULL COMMENT '视频时长(秒)',
  PRIMARY KEY (`file_id`) USING BTREE,
  UNIQUE INDEX `idx_key_upload_id`(`upload_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_video_id`(`video_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '视频投稿文件表' ROW_FORMAT = Dynamic;

-- 7. video_comment 评论表
DROP TABLE IF EXISTS `video_comment`;
CREATE TABLE `video_comment` (
  `comment_id` int NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `p_comment_id` int NULL DEFAULT NULL COMMENT '父级评论ID',
  `video_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '视频ID',
  `video_user_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '视频所属用户ID',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评论内容',
  `img_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评论图片路径',
  `user_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评论用户ID',
  `reply_user_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '回复用户ID',
  `top_type` tinyint NULL DEFAULT 0 COMMENT '0:未置顶 1:置顶',
  `post_time` datetime NULL DEFAULT NULL COMMENT '评论时间',
  `like_count` int NULL DEFAULT 0 COMMENT '点赞数',
  `hate_count` int NULL DEFAULT 0 COMMENT '讨厌数',
  PRIMARY KEY (`comment_id`) USING BTREE,
  INDEX `idx_video_id`(`video_id` ASC) USING BTREE,
  INDEX `idx_p_comment_id`(`p_comment_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论表' ROW_FORMAT = Dynamic;

-- 8. video_barrage 弹幕表
DROP TABLE IF EXISTS `video_barrage`;
CREATE TABLE `video_barrage` (
  `barrage_id` int NOT NULL AUTO_INCREMENT COMMENT '弹幕ID',
  `video_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '视频ID',
  `file_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件ID',
  `user_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户ID',
  `post_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `text` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '弹幕内容',
  `mode` tinyint NULL DEFAULT 0 COMMENT '0:滚动 1:顶部 2:底部',
  `color` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '颜色',
  `time` int NULL DEFAULT NULL COMMENT '弹幕出现时间(秒)',
  PRIMARY KEY (`barrage_id`) USING BTREE,
  INDEX `idx_video_id`(`video_id` ASC) USING BTREE,
  INDEX `idx_file_id`(`file_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '弹幕表' ROW_FORMAT = Dynamic;

-- 9. user_action 用户行为表
DROP TABLE IF EXISTS `user_action`;
CREATE TABLE `user_action` (
  `action_id` int NOT NULL AUTO_INCREMENT COMMENT '行为ID',
  `video_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '视频ID',
  `video_user_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '视频所属用户ID',
  `comment_id` int NULL DEFAULT NULL COMMENT '评论ID',
  `action_type` tinyint NULL DEFAULT NULL COMMENT '0:点赞评论 1:讨厌评论 2:点赞视频 3:收藏视频 4:投币视频',
  `action_count` int NULL DEFAULT 0 COMMENT '数量',
  `user_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户ID',
  `action_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
  PRIMARY KEY (`action_id`) USING BTREE,
  INDEX `idx_video_id`(`video_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户行为表' ROW_FORMAT = Dynamic;

-- 10. user_focus 用户关注表
DROP TABLE IF EXISTS `user_focus`;
CREATE TABLE `user_focus` (
  `user_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户ID',
  `focus_user_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '被关注用户ID',
  `focus_time` datetime NULL DEFAULT NULL COMMENT '关注时间',
  `focus_type` tinyint NULL DEFAULT 0 COMMENT '关注类型',
  PRIMARY KEY (`user_id`, `focus_user_id`) USING BTREE,
  INDEX `idx_focus_user_id`(`focus_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户关注表' ROW_FORMAT = Dynamic;

-- 11. user_message 站内消息表
DROP TABLE IF EXISTS `user_message`;
CREATE TABLE `user_message` (
  `message_id` int NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `user_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '接收用户ID',
  `video_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '视频ID',
  `message_type` tinyint NULL DEFAULT NULL COMMENT '消息类型',
  `send_user_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发送用户ID',
  `read_type` tinyint NULL DEFAULT 0 COMMENT '0:未读 1:已读',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `extend_json` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '扩展信息',
  PRIMARY KEY (`message_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户消息表' ROW_FORMAT = Dynamic;

-- 12. user_video_series 用户视频合集表
DROP TABLE IF EXISTS `user_video_series`;
CREATE TABLE `user_video_series` (
  `series_id` int NOT NULL AUTO_INCREMENT COMMENT '合集ID',
  `series_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '合集名称',
  `series_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '合集描述',
  `user_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户ID',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`series_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户视频合集表' ROW_FORMAT = Dynamic;

-- 13. series_video 合集视频关联表
DROP TABLE IF EXISTS `series_video`;
CREATE TABLE `series_video` (
  `series_id` int NOT NULL COMMENT '合集ID',
  `video_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '视频ID',
  `user_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户ID',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`series_id`, `video_id`) USING BTREE,
  INDEX `idx_video_id`(`video_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '合集视频关联表' ROW_FORMAT = Dynamic;

-- 14. video_play_history 播放历史表
DROP TABLE IF EXISTS `video_play_history`;
CREATE TABLE `video_play_history` (
  `user_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户ID',
  `video_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '视频ID',
  `file_index` int NOT NULL DEFAULT 1 COMMENT '文件索引',
  `last_update_time` datetime NULL DEFAULT NULL COMMENT '最后观看时间',
  PRIMARY KEY (`user_id`, `video_id`, `file_index`) USING BTREE,
  INDEX `idx_last_update_time`(`last_update_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '播放历史表' ROW_FORMAT = Dynamic;

-- 15. statistics_info 统计信息表
DROP TABLE IF EXISTS `statistics_info`;
CREATE TABLE `statistics_info` (
  `statistics_date` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '统计日期',
  `user_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户ID',
  `data_type` tinyint NOT NULL COMMENT '数据类型',
  `statistics_count` int NULL DEFAULT 0 COMMENT '统计数量',
  PRIMARY KEY (`statistics_date`, `user_id`, `data_type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '统计信息表' ROW_FORMAT = Dynamic;

-- ============================================================
-- 分类基础数据（依据原库 category 表内容恢复）
-- ============================================================
INSERT INTO `category` (`category_id`, `category_code`, `category_name`, `p_category_id`, `icon`, `background`, `sort`) VALUES
(40, 'fiction', '科幻', 0, NULL, NULL, 23),
(39, '8', '8', 0, NULL, NULL, 22),
(38, '7', '7', 0, NULL, NULL, 21),
(37, '6', '6', 0, NULL, NULL, 20),
(36, '5', '5', 0, NULL, NULL, 19),
(35, '4', '4', 0, NULL, NULL, 18),
(34, '3', '3', 0, NULL, NULL, 17),
(33, '2', '2', 0, NULL, NULL, 16),
(31, 'knowledge', '知识', 0, NULL, NULL, 14),
(30, 'music', '音乐', 0, NULL, NULL, 13),
(29, 'game', '游戏', 0, NULL, NULL, 12),
(28, 'movie', '电影', 0, NULL, NULL, 11),
(27, 'recipt', '美食', 0, NULL, NULL, 10),
(26, 'tech', '科技', 0, NULL, NULL, 9),
(25, 'amusement', '娱乐', 0, NULL, NULL, 8),
(24, 'dance', '舞蹈', 0, NULL, NULL, 7),
(17, 'golang', 'golang', 11, NULL, NULL, 6),
(23, 'guichu', '鬼畜', 0, NULL, NULL, 6),
(11, 'biancheng', '编程', 0, NULL, NULL, 5),
(16, 'c#', 'c#', 11, NULL, NULL, 5),
(6, 'tv', '电视剧', 0, NULL, NULL, 4),
(15, 'c plus plus', 'c plus plus', 11, NULL, NULL, 4),
(5, 'zongyi', '综艺', 0, NULL, NULL, 3),
(14, 'c', 'c', 11, NULL, NULL, 3),
(20, 'sa''d', '鬼畜调教', 3, NULL, NULL, 3),
(13, 'python', 'python', 11, NULL, NULL, 2),
(19, 'funny', '搞笑', 3, NULL, NULL, 2),
(3, 'camedy', '喜剧', 0, 'COVER/2025-05/38ypDknVE6H8jaI7pi25DEuauVJXj1.png', 'COVER/2025-05/jj2o4njj80baiSvG1XPhk5CDuArDDY.jpg', 1),
(12, 'java', 'java', 11, NULL, NULL, 1),
(18, 'terror', '恐怖', 3, NULL, NULL, 1),
(22, 'kuaile101', 'kuaile101', 6, NULL, NULL, 1);

SET FOREIGN_KEY_CHECKS = 1;
