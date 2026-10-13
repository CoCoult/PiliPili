package com.PiliPili.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component("redisUtils")
public class RedisUtils<V> {

    @Resource
    private RedisTemplate<String, V> redisTemplate;

    private static final Logger logger = LoggerFactory.getLogger(RedisUtils.class);

    /**
     * 删除缓存
     *
     * @param key 可以传一个值 或多个
     */
    public void delete(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete((Collection<String>) CollectionUtils.arrayToList(key));
            }
        }
    }

    public V get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public boolean set(String key, V value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            logger.error("设置redisKey:{},value:{}失败", key, value);
            return false;
        }
    }

    public boolean keyExists(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public boolean setex(String key, V value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.MILLISECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            logger.error("设置redisKey:{},value:{}失败", key, value);
            return false;
        }
    }

    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.MILLISECONDS);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public List<V> getQueueList(String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }


    public boolean lpush(String key, V value, Long time) {
        try {
            redisTemplate.opsForList().leftPush(key, value);
            if (time != null && time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public long remove(String key, Object value) {
        try {
            Long remove = redisTemplate.opsForList().remove(key, 1, value);
            return remove;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public boolean lpushAll(String key, List<V> values, long time) {
        try {
            redisTemplate.opsForList().leftPushAll(key, values);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public V rpop(String key) {
        try {
            return redisTemplate.opsForList().rightPop(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Long increment(String key) {
        Long count = redisTemplate.opsForValue().increment(key, 1);
        return count;
    }

    public Long incrementex(String key, long milliseconds) {
        Long count = redisTemplate.opsForValue().increment(key, 1);
        if (count == 1) {
            //设置过期时间1天
            expire(key, milliseconds);
        }
        return count;
    }

    public Long decrement(String key) {
        Long count = redisTemplate.opsForValue().increment(key, -1);
        if (count <= 0) {
            redisTemplate.delete(key);
        }
        logger.info("key:{},减少数量{}", key, count);
        return count;
    }


    public Set<String> getByKeyPrefix(String keyPrifix) {
        Set<String> keyList = redisTemplate.keys(keyPrifix + "*");
        return keyList;
    }


    public Map<String, V> getBatch(String keyPrifix) {
        Set<String> keySet = redisTemplate.keys(keyPrifix + "*");
        List<String> keyList = new ArrayList<>(keySet);
        List<V> keyValueList = redisTemplate.opsForValue().multiGet(keyList);
        Map<String, V> resultMap = keyList.stream().collect(Collectors.toMap(key -> key, value -> keyValueList.get(keyList.indexOf(value))));
        return resultMap;
    }

    public void zaddCount(String key, V v) {
        redisTemplate.opsForZSet().incrementScore(key, v, 1);
    }


    public List<V> getZSetList(String key, Integer count) {
        Set<V> topElements = redisTemplate.opsForZSet().reverseRange(key, 0, count);
        List<V> list = new ArrayList<>(topElements);
        return list;
    }

    // ==================== 分布式锁 / 缓存辅助 ====================

    /**
     * SET key value NX PX milliseconds —— 分布式锁的"抢锁"
     * 语义：key 不存在时才设置成功（原子操作），并带自动过期
     *
     * @param key          锁的 key
     * @param value        锁的值，务必放"唯一标识"（如 UUID），释放时可校验，防止误删别人的锁
     * @param milliseconds 自动过期时间(毫秒)。必须设置！否则持锁线程挂掉会导致死锁
     * @return true=抢到锁；false=已被别人持有
     */
    public boolean setnx(String key, V value, long milliseconds) {
        try {
            Boolean success = redisTemplate.opsForValue().setIfAbsent(key, value, milliseconds, TimeUnit.MILLISECONDS);
            return Boolean.TRUE.equals(success);
        } catch (Exception e) {
            logger.error("抢锁失败,key:{}", key, e);
            return false;
        }
    }

    /**
     * 原子释放锁：用 Lua 脚本保证"判断值是否为自己 + 删除"两步不可分割
     * <p>
     * 为什么不能直接 delete？反例：
     * A 拿锁 → A 业务卡住超过锁过期时间 → 锁自动释放 → B 拿到锁
     * → A 业务执行完执行 delete，把 B 的锁删了 → C 又能拿锁，锁失效
     *
     * @return true=释放成功；false=锁已不属于自己（已过期或被别人持有）
     */
    public boolean releaseLock(String key, V value) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        try {
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
            Long result = redisTemplate.execute(redisScript, Collections.singletonList(key), value);
            return result != null && result > 0;
        } catch (Exception e) {
            logger.error("释放锁失败,key:{}", key, e);
            return false;
        }
    }

    /**
     * 获取 key 的剩余过期时间(毫秒)
     *
     * @return >0 剩余毫秒；-1 永久；-2 key 不存在
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
    }

}
