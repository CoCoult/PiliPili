$ErrorActionPreference = "Stop"

$ffprobe = "F:\quarkdisk\ffmpeg(1)\ffmpeg-9.0.2-essentials_build\bin\ffprobe.exe"
$base = "F:\webServe\PiliPili\file"
$vdir = Join-Path $base "video"
$cdir = Join-Path $base "COVER"
$outSql = "F:\mysql_recovery\rebuild_data.sql"

function New-RandomId([int]$len) {
    $chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    $sb = New-Object System.Text.StringBuilder
    for ($i = 0; $i -lt $len; $i++) { [void]$sb.Append($chars[(Get-Random -Maximum $chars.Length)]) }
    return $sb.ToString()
}

Write-Host "Scanning video dirs..."
$videos = Get-ChildItem $vdir -Directory -Recurse -ErrorAction SilentlyContinue |
    Where-Object { Test-Path (Join-Path $_.FullName "index.m3u8") } |
    Sort-Object FullName
Write-Host ("  found {0} videos" -f $videos.Count)

$coversByDate = @{}
Get-ChildItem $cdir -Directory -ErrorAction SilentlyContinue | ForEach-Object {
    $coversByDate[$_.Name] = @(Get-ChildItem $_.FullName -File -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Name | Sort-Object)
}
Write-Host ("  cover date groups: {0}" -f $coversByDate.Keys.Count)

$sql = New-Object System.Collections.Generic.List[string]
$sql.Add("SET NAMES utf8mb4;")
$sql.Add("USE pilipili;")
$sql.Add("DELETE FROM video_info_file;")
$sql.Add("DELETE FROM video_info;")
$sql.Add("DELETE FROM user;")

$pwdMd5 = "e10adc3949ba59abbe56e057f20f883e"
$userList = @("6598774110", "0368633221")
foreach ($u in $userList) {
    $sql.Add("INSERT INTO ``user`` (user_id, user_name, email, ``password``, gender, create_time, last_login_time, status, total_coin, current_coin, like_count, play_count) VALUES ('$u', 'User$u', '$u@pilipili.com', '$pwdMd5', 1, NOW(), NOW(), 1, 100, 100, 0, 0);")
}

$categoryPool = @(11, 26, 29, 30, 28, 24, 31, 25)
$usedCovers = @{}
$idx = 0
foreach ($v in $videos) {
    $idx++
    $date = $v.Parent.Name
    $dirName = $v.Name
    $userId = ($dirName -split '-')[0]
    if ($userList -notcontains $userId) { $userId = "6598774110" }

    $videoId = New-RandomId 10
    $fileId = New-RandomId 20
    $relPath = "video/$date/$dirName"

    $durInt = 0
    $m3u8 = Join-Path $v.FullName "index.m3u8"
    try {
        $dur = & $ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 $m3u8 2>$null
        if ($dur -match '^\s*[\d\.]+\s*$') { $durInt = [int][double]$dur.Trim() }
    } catch { $durInt = 0 }

    $coverRel = ""
    if ($coversByDate.ContainsKey($date) -and $coversByDate[$date].Count -gt 0) {
        $list = $coversByDate[$date]
        if (-not $usedCovers.ContainsKey($date)) { $usedCovers[$date] = 0 }
        $ci = $usedCovers[$date] % $list.Count
        $usedCovers[$date] = $usedCovers[$date] + 1
        $coverRel = "COVER/$date/$($list[$ci])"
    }

    $pCat = $categoryPool[($idx - 1) % $categoryPool.Count]
    $name = "Video $date-$idx"
    $ct = "$date 12:00:00"
    $playCount = Get-Random -Minimum 0 -Maximum 200

    $sql.Add("INSERT INTO video_info (video_id, video_cover, video_name, introduction, user_id, create_time, last_update_time, p_category_id, category_id, post_type, duration, play_count, like_count, collect_count, barrage_count, comment_count, coin_count, recommend_type) VALUES ('$videoId', '$coverRel', '$name', 'Rebuilt test video record', '$userId', '$ct', '$ct', $pCat, NULL, 0, $durInt, $playCount, 0, 0, 0, 0, 0, 1);")
    $sql.Add("INSERT INTO video_info_file (file_id, video_id, user_id, file_index, file_name, file_size, file_path, duration) VALUES ('$fileId', '$videoId', '$userId', 1, '$dirName', 0, '$relPath', $durInt);")
}

$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllLines($outSql, $sql, $utf8NoBom)

Write-Host ""
Write-Host ("SQL written: {0}" -f $outSql)
Write-Host ("  statements: {0}" -f $sql.Count)
Write-Host ("  users: {0}, videos: {1}" -f $userList.Count, $videos.Count)
