# Ԥ��������־�ű�
# ʹ�÷���: .\preview-changelog.ps1

Write-Host "? Ԥ���Զ����ɵĸ�����־" -ForegroundColor Green

# ��ȡ���±�ǩ
$PREVIOUS_TAG = ""
try {
    $PREVIOUS_TAG = git describe --tags --abbrev=0 HEAD 2>$null
    if ($LASTEXITCODE -ne 0) {
        $PREVIOUS_TAG = ""
    }
} catch {
    $PREVIOUS_TAG = ""
}

# ��ȡ��ǰ�汾
$CURRENT_VERSION = (Get-Content gradle.properties | Where-Object { $_ -match "mod_version=" } | ForEach-Object { $_.Split("=")[1] }).Trim()

Write-Host "��ǰ�汾: v$CURRENT_VERSION" -ForegroundColor Cyan
if ($PREVIOUS_TAG) {
    Write-Host "��һ����ǩ: $PREVIOUS_TAG" -ForegroundColor Cyan
    $COMMIT_RANGE = "$PREVIOUS_TAG..HEAD"
} else {
    Write-Host "δ�ҵ���һ����ǩ������ʾ�����ύ" -ForegroundColor Yellow
    $COMMIT_RANGE = "HEAD"
}

Write-Host "�ύ��Χ: $COMMIT_RANGE" -ForegroundColor Cyan
Write-Host ""

# �ռ���ͬ���͵��ύ
Write-Host "? �����ύ��Ϣ..." -ForegroundColor Cyan

$FEATURES = @()
$FIXES = @()
$IMPROVEMENTS = @()
$DOCS = @()
$OTHERS = @()

# ��ȡ�����ύ
$commits = git log $COMMIT_RANGE --pretty=format:"%s" 2>$null

if ($commits) {
    foreach ($commit in $commits) {
        $commit = $commit.Trim()
        if ($commit -match "^(feat|feature|add|����|���)" -and $commit -notmatch "^(bump version|release|merge)") {
            $FEATURES += "- $commit"
        }
        elseif ($commit -match "^(fix|bug|�޸�|bugfix)" -and $commit -notmatch "^(bump version|release|merge)") {
            $FIXES += "- $commit"
        }
        elseif ($commit -match "^(improve|enhance|�Ż�|�Ľ�|perf)" -and $commit -notmatch "^(bump version|release|merge)") {
            $IMPROVEMENTS += "- $commit"
        }
        elseif ($commit -match "^(docs|doc|�ĵ�)" -and $commit -notmatch "^(bump version|release|merge)") {
            $DOCS += "- $commit"
        }
        elseif ($commit -notmatch "^(feat|feature|add|����|���|fix|bug|�޸�|bugfix|improve|enhance|�Ż�|�Ľ�|perf|docs|doc|�ĵ�|bump version|release|merge)") {
            $OTHERS += "- $commit"
        }
    }
}

# ����Ԥ��
Write-Host "? Ԥ�����ɵĸ�����־:" -ForegroundColor Green
Write-Host "�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T" -ForegroundColor Gray

Write-Host "## ? TinyAuth v$CURRENT_VERSION" -ForegroundColor White
Write-Host ""

if ($FEATURES.Count -gt 0) {
    Write-Host "### ? �¹���" -ForegroundColor Green
    foreach ($feature in $FEATURES) {
        Write-Host $feature -ForegroundColor White
    }
    Write-Host ""
}

if ($FIXES.Count -gt 0) {
    Write-Host "### ? Bug �޸�" -ForegroundColor Red
    foreach ($fix in $FIXES) {
        Write-Host $fix -ForegroundColor White
    }
    Write-Host ""
}

if ($IMPROVEMENTS.Count -gt 0) {
    Write-Host "### ? �����Ż�" -ForegroundColor Blue
    foreach ($improvement in $IMPROVEMENTS) {
        Write-Host $improvement -ForegroundColor White
    }
    Write-Host ""
}

if ($DOCS.Count -gt 0) {
    Write-Host "### ? �ĵ�����" -ForegroundColor Yellow
    foreach ($doc in $DOCS) {
        Write-Host $doc -ForegroundColor White
    }
    Write-Host ""
}

if ($OTHERS.Count -gt 0) {
    Write-Host "### ? ��������" -ForegroundColor Magenta
    foreach ($other in $OTHERS) {
        Write-Host $other -ForegroundColor White
    }
    Write-Host ""
}

if ($FEATURES.Count -eq 0 -and $FIXES.Count -eq 0 -and $IMPROVEMENTS.Count -eq 0 -and $DOCS.Count -eq 0 -and $OTHERS.Count -eq 0) {
    Write-Host "?? û���ҵ��µ��ύ���ύ��Ϣ�����Ϲ淶" -ForegroundColor Yellow
    Write-Host "��ȷ��:" -ForegroundColor Yellow
    Write-Host "1. ���µ��ύ��������ϸ���ǩ��" -ForegroundColor Yellow
    Write-Host "2. �ύ��Ϣ��ѭ�淶���ο� COMMIT_CONVENTION.md��" -ForegroundColor Yellow
}

Write-Host "### ? �汾��Ϣ" -ForegroundColor Cyan
Write-Host "- **Minecraft �汾**: 1.18.2" -ForegroundColor White
Write-Host "- **Forge �汾**: 40.3.9" -ForegroundColor White
Write-Host "- **Java �汾**: 17" -ForegroundColor White
Write-Host ""

Write-Host "�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T�T" -ForegroundColor Gray
Write-Host "? ����Ƿ���ʱ�Զ����ɵĸ�����־Ԥ��" -ForegroundColor Green
Write-Host "? ����Ľ�����ο� COMMIT_CONVENTION.md �����ύ��Ϣ��ʽ" -ForegroundColor Cyan 