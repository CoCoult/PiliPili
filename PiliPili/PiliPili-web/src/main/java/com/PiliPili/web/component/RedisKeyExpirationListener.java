package com.PiliPili.web.component;

import com.PiliPili.component.RedisComponent;
import com.PiliPili.entity.constants.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class    RedisKeyExpirationListener extends KeyExpirationEventMessageListener {
    @Resource
    RedisComponent redisComponent;

    public RedisKeyExpirationListener(RedisMessageListenerContainer redisMessageListenerContainer) {
        super(redisMessageListenerContainer);
    }

    /**
     * 当用户退出视频时更行在线人数
     *
     * @param message
     * @param pattern
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String key = message.toString();
        //不是用户在线的key则不进行操作
        if (!key.startsWith(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE_PREFIX + Constants.REDIS_KEY_VIDEO_PLAY_COUNT_USE_PREFIX)) {
            return;
        }
        Integer userIndex = key.indexOf(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_USE_PREFIX) + Constants.REDIS_KEY_VIDEO_PLAY_COUNT_USE_PREFIX.length();
        //截取视频id更新在线人数
        String fileId = key.substring(userIndex, userIndex + Constants.LENGTH_10 * 2);
        redisComponent.DecreaseVideoPlayOnlineKey(String.format(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE, fileId));
    }
}
