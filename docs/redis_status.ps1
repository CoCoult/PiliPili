# =====================================================================
#  PiliPili - Redis status dashboard
#  Usage:
#     powershell -ExecutionPolicy Bypass -File docs\redis_status.ps1
#     powershell -ExecutionPolicy Bypass -File docs\redis_status.ps1 -BigKeys
#     powershell -ExecutionPolicy Bypass -File docs\redis_status.ps1 -MonitorSeconds 10
#  NOTE: ASCII only on purpose (PowerShell may read UTF-8 .ps1 as ANSI).
# =====================================================================
param(
    [string]$RedisHost = "127.0.0.1",
    [int]$Port = 6379,
    [string]$Cli = "G:\redis\Redis-x64-3.2.100\redis-cli.exe",
    [switch]$BigKeys,
    [int]$MonitorSeconds = 0
)

$ErrorActionPreference = "Continue"

if (-not (Test-Path $Cli)) {
    $cmd = Get-Command redis-cli -ErrorAction SilentlyContinue
    if ($cmd) { $Cli = $cmd.Source } else { Write-Host "[ERROR] redis-cli not found. Pass -Cli <path>."; exit 1 }
}

# NOTE: do NOT name this function "R" - in Windows PowerShell 5.1 the alias
#       "r" maps to Invoke-History and would shadow it.
function RedisCmd([string[]]$RedisArgs) { & $Cli -h $RedisHost -p $Port @RedisArgs 2>&1 }

function Section([string]$Title) {
    Write-Host ""
    Write-Host ("=" * 62) -ForegroundColor DarkGray
    Write-Host " $Title" -ForegroundColor Cyan
    Write-Host ("=" * 62) -ForegroundColor DarkGray
}

# ---------- 1. connectivity ----------
Section "1. CONNECTIVITY"
$pong = RedisCmd @("ping")
if ("$pong" -notmatch "PONG") {
    Write-Host "[DOWN] cannot reach $RedisHost`:$Port -> $pong" -ForegroundColor Red
    Write-Host "start it: redis-server.exe redis.windows-service.conf --notify-keyspace-events Ex"
    exit 1
}
Write-Host "[UP] $RedisHost`:$Port  PONG" -ForegroundColor Green

# ---------- 2. info -> hashtable ----------
$info = @{}
foreach ($line in (RedisCmd @("info"))) {
    if ($line -match "^([a-zA-Z_0-9]+):(.+)$") { $info[$Matches[1]] = $Matches[2].Trim() }
}
function V([string]$k, [string]$def = "-") { if ($info.ContainsKey($k)) { $info[$k] } else { $def } }

Section "2. VERSION / UPTIME"
Write-Host ("  redis_version   : {0}" -f (V "redis_version"))
Write-Host ("  os              : {0}" -f (V "os"))
Write-Host ("  tcp_port        : {0}" -f (V "tcp_port"))
Write-Host ("  uptime_in_days  : {0}" -f (V "uptime_in_days"))
Write-Host ("  config_file     : {0}" -f (V "config_file"))
Write-Host ("  role            : {0}" -f (V "role"))

Section "3. MEMORY"
Write-Host ("  used_memory     : {0}" -f (V "used_memory_human"))
Write-Host ("  peak_memory     : {0}" -f (V "used_memory_peak_human"))
Write-Host ("  maxmemory       : {0}" -f (V "maxmemory_human"))
Write-Host ("  maxmemory_policy: {0}" -f (V "maxmemory_policy"))
Write-Host ("  fragmentation   : {0}" -f (V "mem_fragmentation_ratio"))
if ((V "maxmemory" "0") -eq "0") {
    Write-Host "  [WARN] maxmemory=0 -> no eviction limit; OOM risk under real load." -ForegroundColor Yellow
}
if ((V "maxmemory_policy") -eq "noeviction") {
    Write-Host "  [WARN] policy=noeviction -> writes fail instead of evicting when full." -ForegroundColor Yellow
}

Section "4. HIT RATE (cache effectiveness)"
$hits = [int64](V "keyspace_hits" 0)
$miss = [int64](V "keyspace_misses" 0)
$total = $hits + $miss
$rate = if ($total -gt 0) { [math]::Round(100.0 * $hits / $total, 2) } else { 0 }
Write-Host ("  keyspace_hits   : {0}" -f $hits)
Write-Host ("  keyspace_misses : {0}" -f $miss)
Write-Host ("  hit rate        : {0} %   ({1} lookups)" -f $rate, $total)
Write-Host ("  expired_keys    : {0}" -f (V "expired_keys"))
Write-Host ("  evicted_keys    : {0}" -f (V "evicted_keys"))
Write-Host ("  ops/sec         : {0}" -f (V "instantaneous_ops_per_sec"))
Write-Host ("  commands_total  : {0}" -f (V "total_commands_processed"))
Write-Host ("  rejected_conn   : {0}" -f (V "rejected_connections"))
Write-Host "  note: hit rate here counts ALL lookups (token/captcha/playcount), not only video cache."

Section "5. KEYSPACE"
$ks = RedisCmd @("info", "keyspace") | Where-Object { $_ -match "^db\d+:" }
if (-not $ks) { Write-Host "  (empty)" } else { foreach ($l in $ks) { Write-Host ("  {0}" -f $l) } }
$dbsize = RedisCmd @("dbsize")
Write-Host ("  dbsize(db0)     : {0}" -f $dbsize)

Section "6. KEYS BY PREFIX / TTL"
$keys = @(RedisCmd @("keys", "PiliPili:*"))
if ($keys.Count -eq 0) {
    Write-Host "  (no PiliPili:* keys - app not started or all expired)"
} else {
    # MEMORY USAGE exists only since Redis 4.0
    $majorVer = 0
    if ((V "redis_version") -match "^(\d+)") { $majorVer = [int]$Matches[1] }
    $rows = foreach ($k in $keys) {
        $t = RedisCmd @("type", $k)
        $ttl = RedisCmd @("ttl", $k)
        $mem = if ($majorVer -ge 4) { RedisCmd @("memory", "usage", $k) } else { "n/a(<4.0)" }
        $prefix = if ($k -match "^(PiliPili:[^:]+:[^:]+:)") { $Matches[1] + "*" }
                  elseif ($k -match "^(PiliPili:[^:]+:)") { $Matches[1] + "*" }
                  else { "PiliPili:*" }
        [pscustomobject]@{ Prefix = $prefix; Key = $k; Type = "$t"; TTL = "$ttl"; Bytes = "$mem" }
    }
    Write-Host "  -- grouped by prefix --"
    $rows | Group-Object Prefix | Sort-Object Name | ForEach-Object {
        Write-Host ("   {0,-34} {1} key(s)" -f $_.Name, $_.Count) -ForegroundColor Green
    }
    Write-Host ""
    Write-Host "  -- detail (ttl=-1 no expire, -2 missing) --"
    $rows | Sort-Object Key | Format-Table -AutoSize | Out-String | Write-Host
    $noTtl = @($rows | Where-Object { $_.TTL -eq "-1" }).Count
    if ($noTtl -gt 0) {
        Write-Host ("  [WARN] {0} key(s) have NO TTL -> permanent memory growth if written repeatedly." -f $noTtl) -ForegroundColor Yellow
    }
}

Section "7. PERSISTENCE"
Write-Host ("  rdb_changes_since_last_save : {0}" -f (V "rdb_changes_since_last_save"))
Write-Host ("  rdb_last_bgsave_status      : {0}" -f (V "rdb_last_bgsave_status"))
Write-Host ("  aof_enabled                 : {0}" -f (V "aof_enabled"))
Write-Host ("  aof_last_write_status       : {0}" -f (V "aof_last_write_status"))
Write-Host ("  save rules                  : {0}" -f ((RedisCmd @("config", "get", "save")) -join " | "))
Write-Host "  note: this Windows 3.2.100 port prints 'jd' as separator in CONFIG GET save; trust the .conf file."

Section "8. CLIENTS / SLOWLOG"
Write-Host ("  connected_clients : {0}" -f (V "connected_clients"))
Write-Host ("  blocked_clients   : {0}" -f (V "blocked_clients"))
Write-Host ("  slowlog length    : {0}" -f (RedisCmd @("slowlog", "len")))
Write-Host ("  slowlog threshold : {0} us" -f ((RedisCmd @("config", "get", "slowlog-log-slower-than"))[1]))
Write-Host ("  notify-keyspace   : {0}  (need Ex for expired-event listeners)" -f ((RedisCmd @("config", "get", "notify-keyspace-events"))[1]))

if ($BigKeys) {
    Section "9. BIG KEYS  (redis-cli --bigkeys)"
    & $Cli -h $RedisHost -p $Port --bigkeys 2>&1 | Write-Host
}

if ($MonitorSeconds -gt 0) {
    Section ("10. LIVE COMMANDS  (monitor, {0}s)" -f $MonitorSeconds)
    Write-Host "  tip: press Ctrl+C to stop earlier. Never run on prod during peak."
    $job = Start-Job -ScriptBlock { param($c, $h, $p) & $c -h $h -p $p monitor } -ArgumentList $Cli, $RedisHost, $Port
    Start-Sleep -Seconds $MonitorSeconds
    Receive-Job $job | Select-Object -First 60 | Write-Host
    Stop-Job $job; Remove-Job $job -Force
}

Write-Host ""
Write-Host "done." -ForegroundColor Green
