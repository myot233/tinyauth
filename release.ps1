# TinyAuth 发布脚本
# 使用方法: .\release.ps1 -Version "0.0.4"

param(
    [Parameter(Mandatory=$true)]
    [string]$Version
)

Write-Host "? 开始发布 TinyAuth v$Version" -ForegroundColor Green

# 验证版本格式
if ($Version -notmatch '^\d+\.\d+\.\d+$') {
    Write-Host "? 版本格式错误！请使用格式: x.y.z (例如: 1.0.0)" -ForegroundColor Red
    exit 1
}

# 检查工作目录是否干净
$gitStatus = git status --porcelain
if ($gitStatus) {
    Write-Host "? 工作目录不干净，请先提交所有更改！" -ForegroundColor Red
    Write-Host "未提交的文件:" -ForegroundColor Yellow
    git status --short
    exit 1
}

# 更新 gradle.properties 中的版本号
Write-Host "? 更新版本号到 $Version..." -ForegroundColor Cyan
$gradlePropsPath = "gradle.properties"
$content = Get-Content $gradlePropsPath
$newContent = $content -replace "mod_version=.*", "mod_version=$Version"
$newContent | Set-Content $gradlePropsPath

# 提交版本更改
Write-Host "? 提交版本更改..." -ForegroundColor Cyan
git add gradle.properties
git commit -m "bump version to $Version"

# 创建标签
Write-Host "?? 创建标签 v$Version..." -ForegroundColor Cyan
git tag -a "v$Version" -m "Release version $Version"

# 推送到远程仓库
Write-Host "?? 推送到 GitHub..." -ForegroundColor Cyan
git push origin 1.18.2
git push origin "v$Version"

Write-Host "? 发布流程完成！" -ForegroundColor Green
Write-Host "GitHub Actions 将自动构建并创建 Release。" -ForegroundColor Cyan
Write-Host "请访问 https://github.com/$((git remote get-url origin) -replace '\.git$', '' -replace 'git@github\.com:', 'https://github.com/')/actions 查看构建状态" -ForegroundColor Cyan 