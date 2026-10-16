# 弹幕 Redis 方案（读路径已实现）

> 本文档说明「弹幕按时间分片缓存 + 增量拉取」的设计取舍、数据结构、落地位置与验证结果。
>
> 面试知识点整理见 [`面试知识点.md` §13](./面试知识点.md#13-弹幕分片缓存与增量拉取)；改动清单见 [`改动记录.md` §十](./改动记录.md#十弹幕读路径改造redis-分片缓存--增量拉取)。

## 1. 为什么要改

| 问题 | 现状（改造前） | 并发下的后果 |
|---|---|---|
| **读放大** | `getBarrage` 每次播放都 `select * from video_barrage where file_id=?`（全量、无分页） | N 个观众 × M 条弹幕 = N×M 行数据被反复读出 |
| **big key 风险** | 若整条视频塞进一个 Redis key：10 万条 ≈ 20MB | 阻塞、删除慢、集群分片倾斜 |
| **无增量** | 发完弹幕前端 `loadDanmuList()` 全量重拉 | 每次刷屏都是一次全量 IO |
| **无防护** | 无缓存穿透/击穿/雪崩处理 | 缓存失效瞬间 DB 被打穿 |

目标：**按时间分片缓存 + 未命中回源回填（TTL 24h）+ 增量拉取**，并且保留缓存三件套防护与 Redis 故障降级。

## 2. 数据结构：索引与内容分离 + 双重分片

单用一个 ZSet（member=弹幕 JSON）或单用一个 Hash（field=时间片，value=数组）都有硬伤：

| 方案 | 硬伤 |
|---|---|
| 单 ZSet，member=JSON | 大 key；删单条要全量扫 member |
| 单 Hash，field=时间片 | 只能按"片"取，无法按秒查；片内新增要重写整个 value |

最终采用 **索引/内容分离**：

| Key | 类型 | 内容 | TTL |
|---|---|---|---|
| `PiliPili:danmu:idx:{fileId}:{slice}` | ZSet | `score = time(秒)`，`member = barrageId` | 24h + 抖动 |
| `PiliPili:danmu:data:{fileId}:{slice}` | Hash | `field = barrageId`，`value = 弹幕 JSON` | 24h + 抖动 |
| `PiliPili:danmu:empty:{fileId}:{slice}` | String | 空分片占位（防穿透） | 60s |
| `PiliPili:danmu:lock:{fileId}:{slice}` | String | 分片回源锁（防击穿，值为 UUID） | 10s |
| `PiliPili:danmu:maxtime:{fileId}` | String | 该分P弹幕最大时间（`-1` 表示没有弹幕） | 24h + 抖动 |

`slice = time / 300`（**5 分钟一片**）：按 1 条/秒的密度，单片约 300 条 ≈ 60KB，远低于 big key 红线；短视频只有一个分片，长视频按播放位置只涉及 2~3 个分片。

## 3. 读路径

1. 读 `maxtime` → 未缓存则 `select max(time)` 回源（一次索引扫描）→ 回填；`-1` 直接返回空列表（防穿透）。
2. 遍历 `slice 0 .. min(maxTime/300, 60)`，逐个分片读取：
   - 命中 `empty` 占位 → 返回空（不再回源）
   - 命中 `idx` → `ZRANGEBYSCORE` 取 ID → `HMGET data` 取内容
   - 未命中 → `SETNX` 抢分片锁 → 双重检查 → 回源 `[片起, 片止)` → 回填；空则写 `empty` 占位
   - 没抢到锁 → 等 50ms 重试读缓存 ×3 → 仍失败则降级直查 DB
3. 按 `(time, barrageId)` 升序返回；Redis 异常时返回 null → 调用方降级查库（弹幕功能不因 Redis 故障不可用）。

## 4. 增量拉取

- 接口：`GET /barrage/getBarrageIncrement?fileId=&fromTime=&fromId=&toTime=&limit=`
- **游标必须是 `(time, id)` 二元组**：`time` 精确到秒，同一秒可能有多条，只用时间会漏取或重复。
- 服务端：跨 1~2 个分片做 `ZRANGEBYSCORE idx (fromTime, toTime]`，再过滤 `time > fromTime || (time == fromTime && id > fromId)`。
- 前端：复用已有的 5 秒定时器，拉回来的新弹幕用 `artplayer-plugin-danmuku` 的 `emit()` **追加**，并按 `barrageId` 在客户端去重（窗口会重叠）。
- 自己发的弹幕：`postBarrage` 现在返回带自增 ID 的对象，前端登记进去重集合，避免下一次轮询重复渲染。

## 5. 缓存一致性（写路径）

当前写路径仍是"直接写 DB"，因此只做**缓存失效**（Cache-Aside）：

- 新增弹幕 → **事务提交后**删除该弹幕所在分片 + `maxtime`（`TransactionSynchronization#afterCommit`）
- 删除弹幕 → 删除对应分片
- 删除视频 → 清空该分P全部分片（`keys` 前缀匹配，仅用于低频管理操作）

> 下一步（写路径改造）：写 Redis 为准 + 异步批量落库（write-behind）、`INCR` 生成弹幕 ID、`HINCRBY` 计数替代 `video_info` 热点行、ES 更新移出事务。

## 6. 验证结果（2026-10-16 实测）

测试数据：fileId `26RJVKo1BszrJobmvroT`，7 条弹幕分布在分片 0（10s/20s/30s×3）、分片 1（400s）、分片 2（700s）。

| 验证项 | 结果 |
|---|---|
| 冷启动首次读取 | 7 条，执行 SQL：`SELECT MAX(time)` ×1 + 分片范围查询 ×3（`performance_schema` 统计） |
| 第二次及之后读取 | **0 条 video_barrage SQL**，完全命中 Redis |
| 分片缓存落位 | `idx:0` ZSet 5 个成员、`idx:1/2` 各 1 个、`data:*` Hash 对应内容 |
| TTL 抖动 | 实测 86456s ~ 86756s（24h + 0~6 分钟随机） |
| 空分片占位 | 删掉 400s 那条后，`empty:{fileId}:1` = `"1"`，TTL 60s，`idx:1` 不存在 |
| 无弹幕的分P | `maxtime` = `-1`，连续 5 次请求仅 1 次 SQL（那次是视频详情缓存回源） |
| 增量拉取 | 游标 `(30,10)` → 只返回 `(30,11)`、`(30,12)`，不漏不重 |
| 索引 | `EXPLAIN` → `type: range`、`key: idx_file_time`、`Extra: Using index condition`（无 filesort） |
| 集成测试 | `VideoBarrageCacheTest` 9 个用例全绿（含 50 并发防击穿、同秒游标、空占位、写后失效） |

复现命令：

```bash
# 单元/集成测试
cd F:/JavaWebProject/PiliPili
mvn test -DskipTests=false -Dtest=VideoBarrageCacheTest -pl PiliPili-common

# 手工验证缓存
powershell -ExecutionPolicy Bypass -File F:/JavaWebProject/docs/redis_status.ps1 -MonitorSeconds 10
```

## 7. 涉及的索引变更

```sql
-- 分片范围查询必须走这个索引，否则会 filesort
ALTER TABLE `video_barrage` ADD INDEX `idx_file_time`(`file_id`, `time`, `barrage_id`);
```

`docs/sql/init.sql` 的建表语句已同步包含该索引。
