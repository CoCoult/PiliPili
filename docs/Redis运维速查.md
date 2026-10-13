# Redis 运维速查（PiliPili）

> 本文档回答两个问题：**怎么确认 Redis 是活的 / 健康的 / 缓存真的生效了**。
>
> 涉及的知识点整理见 [`面试知识点.md` §12 Redis 运维与监控指标](./面试知识点.md#12-redis-运维与监控指标)。

## 0. 环境信息

| 项 | 值 |
|---|---|
| 可执行文件 | `G:\redis\Redis-x64-3.2.100\redis-cli.exe` |
| 配置文件 | `G:\redis\Redis-x64-3.2.100\redis.windows-service.conf` |
| 地址 / 端口 | `127.0.0.1:6379` |
| 版本 | **3.2.100**（Windows 移植版，2016 年） |

启动命令（需要 key 过期事件通知，在线观看人数功能依赖它）：

```powershell
G:\redis\Redis-x64-3.2.100\redis-server.exe "G:\redis\Redis-x64-3.2.100\redis.windows-service.conf" --notify-keyspace-events Ex
```

---

## 1. 最常用的三条命令

```powershell
$cli = "G:\redis\Redis-x64-3.2.100\redis-cli.exe"

& $cli ping                 # PONG = 活着（唯一必须记住的一条）
& $cli info                 # 全景状态（内存/命中率/连接/持久化）
& $cli keys "PiliPili:*"    # 本项目所有 key（生产禁用，见 §7）
```

---

## 2. 一键状态面板（推荐日常用这个）

我在仓库里放了一个脚本：[`docs/redis_status.ps1`](./redis_status.ps1)

```powershell
# 基础面板：连通性 / 内存 / 命中率 / keyspace / 每个 key 的类型+TTL / 持久化 / 慢查询
powershell -ExecutionPolicy Bypass -File F:\JavaWebProject\docs\redis_status.ps1

# 额外做大 key 扫描（会遍历全部 key，key 多时慎用）
powershell -ExecutionPolicy Bypass -File F:\JavaWebProject\docs\redis_status.ps1 -BigKeys

# 实时看命令流 10 秒（调试「到底有没有读缓存」的利器）
powershell -ExecutionPolicy Bypass -File F:\JavaWebProject\docs\redis_status.ps1 -MonitorSeconds 10
```

它会自动标黄两类隐患：`maxmemory=0`、以及**没有 TTL 的 key**。

---

## 3. 命令速查表

### 3.1 连通性与基本状态

| 命令 | 含义 |
|---|---|
| `ping` | `PONG` = 服务正常 |
| `info` | 全部状态段 |
| `info server` | 版本、运行天数、配置文件路径 |
| `info clients` | 当前连接数（`connected_clients`） |
| `client list` | 每个连接的来源 IP、空闲时长、执行的最后一条命令 |
| `config get maxclients` | 最大连接数（默认 10000） |
| `dbsize` | 当前库 key 总数 |
| `info keyspace` | 每个 db 的 key 数 / 带过期时间的 key 数 |

### 3.2 内存与淘汰

| 命令 | 含义 |
|---|---|
| `info memory` | `used_memory_human`、`used_memory_peak_human`、`mem_fragmentation_ratio` |
| `config get maxmemory` | 内存上限，`0` = 不限制 |
| `config get maxmemory-policy` | 淘汰策略，`noeviction` = 写满直接报错 |
| `memory usage <key>` | 单个 key 占用（**Redis 4.0+ 才有**，本项目 3.2 不可用） |
| `redis-cli --bigkeys` | 各类数据结构中最大的 key 排行 |
| `redis-cli --memkeys` | 按内存占用排行（4.0+） |

### 3.3 命中率（判断缓存有没有用）

| 命令 | 含义 |
|---|---|
| `info stats` | 见下表字段 |
| `config resetstat` | 清零统计（做 A/B 对比前先清，非常重要） |

| 字段 | 含义 |
|---|---|
| `keyspace_hits` | 读缓存命中次数 |
| `keyspace_misses` | 读缓存未命中次数 |
| `expired_keys` | 因过期被删除的 key 数 |
| `evicted_keys` | 因内存不足被淘汰的 key 数（**>0 说明内存不够了**） |
| `rejected_connections` | 因超限被拒绝的连接数（**>0 说明连接池/上限有问题**） |
| `instantaneous_ops_per_sec` | 当前 QPS |
| `total_commands_processed` | 累计命令数 |

命中率 = `keyspace_hits / (keyspace_hits + keyspace_misses)`。

### 3.4 Key 级操作

| 命令 | 含义 |
|---|---|
| `keys "PiliPili:video:info:*"` | 匹配 key（**O(N)，生产禁用**） |
| `scan 0 match "PiliPili:video:info:*" count 100` | 游标式遍历，生产用它替代 `keys` |
| `type <key>` | 类型（string / list / zset / hash / set） |
| `ttl <key>` | 剩余秒数：`-1` 无过期、`-2` 已不存在 |
| `pttl <key>` | 毫秒精度 |
| `get <key>` | 看值（本项目是 JSON） |
| `zrange PiliPili:video:search 0 -1 withscores` | 看搜索热词榜（ZSet） |
| `lrange PiliPili:queue:transfer 0 -1` | 看转码队列（List） |
| `del <key>` | 删除（调试缓存用） |
| `expire <key> 60` | 改过期时间 |

### 3.5 监控与排查

| 命令 | 含义 |
|---|---|
| `monitor` | 实时打印服务端收到的每条命令（**只在本地调试用**） |
| `slowlog get 10` | 最近 10 条慢查询 |
| `slowlog len` | 慢查询条数 |
| `config get slowlog-log-slower-than` | 慢查询阈值（微秒，`10000` = 10ms） |
| `info persistence` | RDB/AOF 状态 |
| `lastsave` | 上次成功落盘的时间戳 |

---

## 4. 当前真实快照解读（2026-10-13 实测）

```
redis_version   : 3.2.100        uptime_in_days : 0
used_memory     : 684.38K        peak           : 758.63K
maxmemory       : 0B             policy         : noeviction
db0:keys=9, expires=7            connected_clients : 1
keyspace_hits   : 349            keyspace_misses : 563   -> 命中率 38.27%
expired_keys    : 0              evicted_keys   : 0
rdb_changes_since_last_save : 4  aof_enabled    : 0
slowlog length  : 0              notify-keyspace-events : xE
```

逐项结论：

1. **`up. PONG`，`connected_clients=1`** —— 只有我一条 `redis-cli` 连接，说明**后端服务当前是停的**（后端启动后这里会变成 5～20 个连接）。用连接数就能判断应用有没有连上 Redis。
2. **命中率 38%** —— 看着低，但**这个口径包含所有读**（登录 token 校验、验证码、分类列表、播放数、视频详情），而且含大量「本来就不该命中」的一次性读（如验证码、上传状态）。真正衡量视频详情缓存要看 §6 的定向对比，不要看全局命中率。
3. **`expired_keys=0`** —— 不是没配过期，而是运行时间还短、key 都还没到点。
4. **`evicted_keys=0` 且 `maxmemory=0`（不限制）** —— 现在没事，但意味着**一旦内存被写满，Redis 不会淘汰旧 key，而是直接对写命令报 OOM 错误**（`noeviction`）。生产环境必须设 `maxmemory` + `allkeys-lru`。
5. **7/9 个 key 有过期时间** —— 剩下 2 个永久 key 是 `PiliPili:categoryList:` 和 `PiliPili:video:search`，它们由 `redisUtils.set()` 写入（无 TTL）。分类列表是低频变更的字典数据，可以接受；**搜索热词 ZSet 是持续 `zincrby` 增长的，永远不会自己释放**，属于「内存缓慢泄漏」型隐患 —— 已记入待优化清单。
6. **`rdb_changes_since_last_save=4`，`aof_enabled=0`** —— 只开了 RDB、没开 AOF，掉电最多丢几分钟（本项目足够）。

---

## 5. 本项目 Redis Key 全清单

命名统一前缀 `PiliPili:`，代码里都定义在 `Constants.java`，写入集中在 `RedisComponent` / `VideoInfoServiceImpl`。

| Key 模板 | 类型 | TTL | 用途 | 代码位置 |
|---|---|---|---|---|
| `PiliPili:checkCode:<key>` | string | 10 分钟 | 图形验证码 | `RedisComponent.setCheckCode` |
| `PiliPili:token:web:<token>` | string(JSON) | 7 天 | 用户端登录态 | `RedisComponent.saveTokenInfo4Web` |
| `PiliPili:token:admin:<token>` | string | 1 天 | 管理端登录态 | `RedisComponent.saveTokenInfo4Admin` |
| `PiliPili:categoryList:` | string(JSON) | **无** | 分类字典缓存 | `RedisComponent.getCategoryList` |
| `PiliPili:sysSetting:` | string(JSON) | **无** | 系统设置 | `RedisComponent.getSysSettingDto` |
| `PiliPili:uploading:<userId><uploadId>` | string(JSON) | 1 天 | 分片上传/断点续传状态 | `RedisComponent.saveUploadingFile` |
| `PiliPili:file:list:delete<videoId>` | list | 7 天 | 待删除文件清单 | `RedisComponent.addFile2DelQueue` |
| `PiliPili:queue:transfer` | list | 无 | **转码任务队列**（LPUSH + RPOP） | `RedisComponent.addFile2TransferQueue` |
| `PiliPili:queue:video:play:` | list | 无 | 播放数异步落库队列 | `RedisComponent.addVideoPlay2Queue` |
| `PiliPili:video:playcount:<yyyy-MM-dd>:<videoId>` | string | 2 天 | 当日播放数计数 | `RedisComponent.addVideoPlayCount` |
| `PiliPili:video:play:online:count:<fileId>` | string | 10 秒 | 在线观看人数 | `RedisComponent.reportVideoPlayOnline` |
| `PiliPili:video:play:online:user:<fileId>:<deviceId>` | string | 8 秒 | 在线观众去重 | 同上 |
| `PiliPili:video:search` | **zset** | **无** | 搜索热词榜（`zincrby`） | `RedisComponent.addKeywordCount` |
| **`PiliPili:video:info:<videoId>`** | string(JSON) | **1800±300 秒** | **视频详情缓存（本次新增）** | `VideoInfoServiceImpl.GetVideoInfoByVideoId` |
| **`PiliPili:video:info:lock:<videoId>`** | string(UUID) | 10 秒 | 防击穿互斥锁 | 同上 |
| `PiliPili:userScoreMatrix` / `userScoreAverageList` | string | 无 | 推荐算法中间结果 | `UserInfoServiceImpl` |

> 观察技巧：`PiliPili:video:info:lock:*` 正常情况下**几乎看不到**（只在缓存失效的那一瞬间存在，默认 10 秒内被释放）。如果它长期存在，说明有请求持锁后异常退出了 —— 这正是分布式锁必须设过期时间的原因。

---

## 6. 怎么验证「视频详情缓存」真的生效了

### 6.1 手工三步法（本地单机最直观）

```powershell
$cli = "G:\redis\Redis-x64-3.2.100\redis-cli.exe"

# ① 先清干净，清空统计计数器
& $cli del "PiliPili:video:info:13PnebBty0"
& $cli config resetstat

# ② 调一次详情接口（浏览器打开视频详情页，或用 curl 带 token 调）
#    curl.exe "http://127.0.0.1:7071/video/getVideoInfo?videoId=13PnebBty0" -H "token: <你的token>"

# ③ 立刻看：key 存在 + TTL 在 1500~2100 之间 + 命中率上升
& $cli exists "PiliPili:video:info:13PnebBty0"
& $cli ttl    "PiliPili:video:info:13PnebBty0"
& $cli info stats | Select-String "keyspace_hits|keyspace_misses"
```

**判读标准**

| 现象 | 结论 |
|---|---|
| `exists=1` 且 `ttl` 落在 **1500~2100** 之间 | 缓存写入成功；TTL 不是整数 1800，说明**随机抖动（防雪崩）生效** |
| 第二次请求后 `ttl` **几乎不变小**，`keyspace_hits` +1，MySQL 无 SQL 日志 | 命中缓存，没查库 |
| 查**不存在的** videoId，`exists=1` 但 `ttl ≈ 120` | **空值占位（防穿透）** 生效，存的是带 `isNullHolder` 标记的对象 |
| 高并发下 `keyspace_hits` 猛涨、`keyspace_misses` 只 +1 | **互斥锁 + 双重检查（防击穿）** 生效，只有 1 个线程打到 DB |

### 6.2 看「谁在读、读了什么」

```powershell
# 另开一个窗口，观察 MySQL 有没有被查
powershell -ExecutionPolicy Bypass -File F:\JavaWebProject\docs\redis_status.ps1 -MonitorSeconds 10
```

`monitor` 里能看到 `GET PiliPili:video:info:xxx`（命中）与 `SET PiliPili:video:info:lock:xxx ... PX 10000`（抢锁）两类命令，这是最硬的证据。

### 6.3 并发压测怎么看结果

代码里已经有集成测试 `VideoInfoCacheTest`（50 线程并发 + Mockito `verify(times(n))` 断言 DB 只被查 1 次）：

```bash
cd F:\JavaWebProject\PiliPili
mvn test -DskipTests=false -Dtest=VideoInfoCacheTest -pl PiliPili-common
```

测试跑完 Redis 里会残留 `PiliPili:video:info:TESTCACHE1`（TTL 正好 1800±300），**这正是可以肉眼验证抖动 TTL 的地方**（本次实测剩余 1490s，落在设计区间内 ✅）。

---

## 7. 排查 SOP 与危险命令

### 7.1 启动前端/后端前先做这三步

```powershell
& $cli ping                      # 1. Redis 活了吗
& $cli dbsize                    # 2. 有多少 key
Get-Process redis-server         # 3. 进程在不在（Windows 任务管理器也行）
```

### 7.2 常见故障对照

| 现象 | 排查 |
|---|---|
| 后端启动报 `Unable to connect to Redis` | `ping` 是否 PONG；端口是否被占（`netstat -ano \| findstr 6379`）；防火墙 |
| 缓存住了但数据不更新 | `ttl` 是否过长；是否走了 `updateVideoInfoCacheField`（保 TTL 更新）；事务是否真的提交（`afterCommit` 才删缓存） |
| 内存一直涨 | `--bigkeys` 找大 key；查**无 TTL 的 key**（脚本会标黄）；热词 ZSet 无上限增长 |
| 慢查询变多 | `slowlog get`；重点抓 `keys` / `hgetall` / 大 ZSet 的 `zrange 0 -1` |
| 命中率异常低 | `config resetstat` 后重测，排除历史累计干扰；确认读的 key 与写的 key 前缀一致 |

### 7.3 危险命令（生产红线）

| 命令 | 为什么危险 |
|---|---|
| `keys *` | O(N) 阻塞单线程，key 百万级直接卡死 → 用 `scan` |
| `flushall` / `flushdb` | 清库，本项目会把登录态、上传进度、转码队列全清掉 |
| `monitor` | 生产高峰期会把 QPS 拖垮，且能看到所有命令（含敏感数据） |
| `del` 大 key | 阻塞主线程 → 4.0+ 用 `unlink`（**本项目 3.2 没有**） |

> ⚠️ 本项目 Redis 是 **3.2.100**：**没有 `unlink`（4.0+）、没有 Stream（5.0+）、没有 `memory usage`（4.0+）**。想在项目里用 Redis 做限流（Lua 可以）、可靠消息队列（Stream 不行，需升级）、BigKey 异步删除（需升级），都得先升级版本。已记入待优化清单。

---

## 8. GUI 工具（不想敲命令时）

| 工具 | 说明 |
|---|---|
| **Another Redis Desktop Manager** | 免费开源、轻量，Windows 直接装，支持树形看 key + TTL + 控制台 |
| **RedisInsight**（Redis 官方） | 功能最全，含内存分析、慢查询可视化；新版对 3.x 老版本兼容性一般 |
| IDEA 插件 **Redis Helper** / **Iedis2** | 不想开额外窗口时够用 |

连接参数：`127.0.0.1:6379`，无密码，库 `db0`。

---

## 9. 相关文档

- [`改动记录.md`](./改动记录.md) —— 代码改动全过程
- [`面试知识点.md`](./面试知识点.md) —— 面试知识点（§12 即本篇对应的理论部分）
- [`sql/init.sql`](./sql/init.sql) —— 建表脚本
