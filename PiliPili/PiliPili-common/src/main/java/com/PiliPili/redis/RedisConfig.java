package com.PiliPili.redis;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig<V> {
    @Bean("redisTemplate")
    public RedisTemplate<String, V> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, V> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        // 设置key的序列化方式
        template.setKeySerializer(RedisSerializer.string());
        // 设置value的序列化方式
        template.setValueSerializer(buildJsonSerializer());
        // 设置hash的key的序列化方式
        template.setHashKeySerializer(RedisSerializer.string());
        // 设置hash的value的序列化方式
        template.setHashValueSerializer(buildJsonSerializer());
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 构建 JSON 序列化器（供 RedisTemplate 和集成测试共用，保证测试验证的就是线上行为）
     * <p>
     * 为什么不直接用 {@code RedisSerializer.json()}？
     * 它内部创建的 ObjectMapper 【没有注册 JavaTimeModule】，一旦缓存对象里含有
     * LocalDateTime（例如 VideoInfo.createTime / lastUpdateTime），序列化会抛
     * "Java 8 date/time type `java.time.LocalDateTime` not supported by default"。
     * 而这个异常会被 RedisUtils.setex 的 try-catch 吞掉，表现为：
     * <b>缓存静默失效——不报错，但每次都查库</b>，非常难排查。
     * <p>
     * 该问题由集成测试 VideoInfoCacheTest 暴露（不带时间字段时全过，带上就集体失败）。
     */
    public static RedisSerializer<Object> buildJsonSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 关键：注册 Java8 时间模块，支持 LocalDateTime / LocalDate / Instant 等
        objectMapper.registerModule(new JavaTimeModule());
        // 时间序列化成 ISO-8601 字符串而不是时间戳，方便用 redis-cli 直接看懂
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 保留类型信息（写入 @class 字段），反序列化时才能还原成原始类型
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }
}
