# TinyAuth �����ű�
# ʹ�÷���: .\release.ps1 -Version "0.0.4"

param(
    [Parameter(Mandatory=$true)]
    [string]$Version
)

Write-Host "? ��ʼ���� TinyAuth v$Version" -ForegroundColor Green

# ��֤�汾��ʽ
if ($Version -notmatch '^\d+\.\d+\.\d+$') {
    Write-Host "? �汾��ʽ������ʹ�ø�ʽ: x.y.z (����: 1.0.0)" -ForegroundColor Red
    exit 1
}

# ��鹤��Ŀ¼�Ƿ�ɾ�
$gitStatus = git status --porcelain
if ($gitStatus) {
    Write-Host "? ����Ŀ¼���ɾ��������ύ���и��ģ�" -ForegroundColor Red
    Write-Host "δ�ύ���ļ�:" -ForegroundColor Yellow
    git status --short
    exit 1
}

# ���� gradle.properties �еİ汾��
Write-Host "? ���°汾�ŵ� $Version..." -ForegroundColor Cyan
$gradlePropsPath = "gradle.properties"
$content = Get-Content $gradlePropsPath
$newContent = $content -replace "mod_version=.*", "mod_version=$Version"
$newContent | Set-Content $gradlePropsPath

# �ύ�汾����
Write-Host "? �ύ�汾����..." -ForegroundColor Cyan
git add gradle.properties
git commit -m "bump version to $Version"

# ������ǩ
Write-Host "?? ������ǩ v$Version..." -ForegroundColor Cyan
git tag -a "v$Version" -m "Release version $Version"

# ���͵�Զ�ֿ̲�
Write-Host "?? ���͵� GitHub..." -ForegroundColor Cyan
git push origin 1.18.2
git push origin "v$Version"

Write-Host "? ����������ɣ�" -ForegroundColor Green
Write-Host "GitHub Actions ���Զ����������� Release��" -ForegroundColor Cyan
Write-Host "����� https://github.com/$((git remote get-url origin) -replace '\.git$', '' -replace 'git@github\.com:', 'https://github.com/')/actions �鿴����״̬" -ForegroundColor Cyan 