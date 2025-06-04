#!/bin/bash

# TinyAuth 发布脚本
# 使用方法: ./release.sh 0.0.4

set -e

if [ $# -eq 0 ]; then
    echo "❌ 请提供版本号！"
    echo "使用方法: ./release.sh 0.0.4"
    exit 1
fi

VERSION=$1

echo "🚀 开始发布 TinyAuth v$VERSION"

# 验证版本格式
if ! [[ $VERSION =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "❌ 版本格式错误！请使用格式: x.y.z (例如: 1.0.0)"
    exit 1
fi

# 检查工作目录是否干净
if [ -n "$(git status --porcelain)" ]; then
    echo "❌ 工作目录不干净，请先提交所有更改！"
    echo "未提交的文件:"
    git status --short
    exit 1
fi

# 更新 gradle.properties 中的版本号
echo "📝 更新版本号到 $VERSION..."
sed -i.bak "s/mod_version=.*/mod_version=$VERSION/" gradle.properties
rm gradle.properties.bak

# 提交版本更改
echo "📦 提交版本更改..."
git add gradle.properties
git commit -m "bump version to $VERSION"

# 创建标签
echo "🏷️ 创建标签 v$VERSION..."
git tag -a "v$VERSION" -m "Release version $VERSION"

# 推送到远程仓库
echo "⬆️ 推送到 GitHub..."
git push origin 1.18.2
git push origin "v$VERSION"

echo "✅ 发布流程完成！"
echo "GitHub Actions 将自动构建并创建 Release。"
REPO_URL=$(git remote get-url origin | sed 's/\.git$//' | sed 's/git@github\.com:/https:\/\/github.com\//')
echo "请访问 $REPO_URL/actions 查看构建状态" 