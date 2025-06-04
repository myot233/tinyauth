# 预览更新日志脚本
# 使用方法: .\preview-changelog.ps1

Write-Host "? 预览自动生成的更新日志" -ForegroundColor Green

# 获取最新标签
$PREVIOUS_TAG = ""
try {
    $PREVIOUS_TAG = git describe --tags --abbrev=0 HEAD 2>$null
    if ($LASTEXITCODE -ne 0) {
        $PREVIOUS_TAG = ""
    }
} catch {
    $PREVIOUS_TAG = ""
}

# 获取当前版本
$CURRENT_VERSION = (Get-Content gradle.properties | Where-Object { $_ -match "mod_version=" } | ForEach-Object { $_.Split("=")[1] }).Trim()

Write-Host "当前版本: v$CURRENT_VERSION" -ForegroundColor Cyan
if ($PREVIOUS_TAG) {
    Write-Host "上一个标签: $PREVIOUS_TAG" -ForegroundColor Cyan
    $COMMIT_RANGE = "$PREVIOUS_TAG..HEAD"
} else {
    Write-Host "未找到上一个标签，将显示所有提交" -ForegroundColor Yellow
    $COMMIT_RANGE = "HEAD"
}

Write-Host "提交范围: $COMMIT_RANGE" -ForegroundColor Cyan
Write-Host ""

# 收集不同类型的提交
Write-Host "? 分析提交信息..." -ForegroundColor Cyan

$FEATURES = @()
$FIXES = @()
$IMPROVEMENTS = @()
$DOCS = @()
$OTHERS = @()

# 获取所有提交
$commits = git log $COMMIT_RANGE --pretty=format:"%s" 2>$null

if ($commits) {
    foreach ($commit in $commits) {
        $commit = $commit.Trim()
        if ($commit -match "^(feat|feature|add|新增|添加)" -and $commit -notmatch "^(bump version|release|merge)") {
            $FEATURES += "- $commit"
        }
        elseif ($commit -match "^(fix|bug|修复|bugfix)" -and $commit -notmatch "^(bump version|release|merge)") {
            $FIXES += "- $commit"
        }
        elseif ($commit -match "^(improve|enhance|优化|改进|perf)" -and $commit -notmatch "^(bump version|release|merge)") {
            $IMPROVEMENTS += "- $commit"
        }
        elseif ($commit -match "^(docs|doc|文档)" -and $commit -notmatch "^(bump version|release|merge)") {
            $DOCS += "- $commit"
        }
        elseif ($commit -notmatch "^(feat|feature|add|新增|添加|fix|bug|修复|bugfix|improve|enhance|优化|改进|perf|docs|doc|文档|bump version|release|merge)") {
            $OTHERS += "- $commit"
        }
    }
}

# 生成预览
Write-Host "? 预览生成的更新日志:" -ForegroundColor Green
Write-Host "═══════════════════════════════════════" -ForegroundColor Gray

Write-Host "## ? TinyAuth v$CURRENT_VERSION" -ForegroundColor White
Write-Host ""

if ($FEATURES.Count -gt 0) {
    Write-Host "### ? 新功能" -ForegroundColor Green
    foreach ($feature in $FEATURES) {
        Write-Host $feature -ForegroundColor White
    }
    Write-Host ""
}

if ($FIXES.Count -gt 0) {
    Write-Host "### ? Bug 修复" -ForegroundColor Red
    foreach ($fix in $FIXES) {
        Write-Host $fix -ForegroundColor White
    }
    Write-Host ""
}

if ($IMPROVEMENTS.Count -gt 0) {
    Write-Host "### ? 性能优化" -ForegroundColor Blue
    foreach ($improvement in $IMPROVEMENTS) {
        Write-Host $improvement -ForegroundColor White
    }
    Write-Host ""
}

if ($DOCS.Count -gt 0) {
    Write-Host "### ? 文档更新" -ForegroundColor Yellow
    foreach ($doc in $DOCS) {
        Write-Host $doc -ForegroundColor White
    }
    Write-Host ""
}

if ($OTHERS.Count -gt 0) {
    Write-Host "### ? 其他更改" -ForegroundColor Magenta
    foreach ($other in $OTHERS) {
        Write-Host $other -ForegroundColor White
    }
    Write-Host ""
}

if ($FEATURES.Count -eq 0 -and $FIXES.Count -eq 0 -and $IMPROVEMENTS.Count -eq 0 -and $DOCS.Count -eq 0 -and $OTHERS.Count -eq 0) {
    Write-Host "?? 没有找到新的提交或提交信息不符合规范" -ForegroundColor Yellow
    Write-Host "请确保:" -ForegroundColor Yellow
    Write-Host "1. 有新的提交（相对于上个标签）" -ForegroundColor Yellow
    Write-Host "2. 提交信息遵循规范（参考 COMMIT_CONVENTION.md）" -ForegroundColor Yellow
}

Write-Host "### ? 版本信息" -ForegroundColor Cyan
Write-Host "- **Minecraft 版本**: 1.18.2" -ForegroundColor White
Write-Host "- **Forge 版本**: 40.3.9" -ForegroundColor White
Write-Host "- **Java 版本**: 17" -ForegroundColor White
Write-Host ""

Write-Host "═══════════════════════════════════════" -ForegroundColor Gray
Write-Host "? 这就是发布时自动生成的更新日志预览" -ForegroundColor Green
Write-Host "? 如需改进，请参考 COMMIT_CONVENTION.md 调整提交信息格式" -ForegroundColor Cyan 