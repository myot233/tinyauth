#!/bin/bash

# 预览更新日志脚本
# 使用方法: ./preview-changelog.sh

echo "🔍 预览自动生成的更新日志"

# 获取最新标签
PREVIOUS_TAG=$(git describe --tags --abbrev=0 HEAD 2>/dev/null || echo "")

# 获取当前版本
CURRENT_VERSION=$(grep 'mod_version=' gradle.properties | cut -d'=' -f2)

echo "当前版本: v$CURRENT_VERSION"
if [ ! -z "$PREVIOUS_TAG" ]; then
    echo "上一个标签: $PREVIOUS_TAG"
    COMMIT_RANGE="$PREVIOUS_TAG..HEAD"
else
    echo "未找到上一个标签，将显示所有提交"
    COMMIT_RANGE="HEAD"
fi

echo "提交范围: $COMMIT_RANGE"
echo ""

echo "📝 分析提交信息..."

# 收集不同类型的提交
FEATURES=$(git log $COMMIT_RANGE --pretty=format:"- %s" --grep="^feat" --grep="^feature" --grep="^add" --grep="^新增" --grep="^添加" -i | grep -v -E "^(bump version|release|merge)" || true)
FIXES=$(git log $COMMIT_RANGE --pretty=format:"- %s" --grep="^fix" --grep="^bug" --grep="^修复" --grep="^bugfix" -i | grep -v -E "^(bump version|release|merge)" || true)
IMPROVEMENTS=$(git log $COMMIT_RANGE --pretty=format:"- %s" --grep="^improve" --grep="^enhance" --grep="^优化" --grep="^改进" --grep="^perf" -i | grep -v -E "^(bump version|release|merge)" || true)
DOCS=$(git log $COMMIT_RANGE --pretty=format:"- %s" --grep="^docs" --grep="^doc" --grep="^文档" -i | grep -v -E "^(bump version|release|merge)" || true)
OTHERS=$(git log $COMMIT_RANGE --pretty=format:"- %s" --invert-grep --grep="^feat" --grep="^feature" --grep="^add" --grep="^新增" --grep="^添加" --grep="^fix" --grep="^bug" --grep="^修复" --grep="^bugfix" --grep="^improve" --grep="^enhance" --grep="^优化" --grep="^改进" --grep="^perf" --grep="^docs" --grep="^doc" --grep="^文档" --grep="^bump version" --grep="^release" --grep="^merge" -i | head -10 || true)

# 生成预览
echo ""
echo "📄 预览生成的更新日志:"
echo "═══════════════════════════════════════"

echo "## 🎉 TinyAuth v$CURRENT_VERSION"
echo ""

if [ ! -z "$FEATURES" ]; then
    echo "### ✨ 新功能"
    echo "$FEATURES"
    echo ""
fi

if [ ! -z "$FIXES" ]; then
    echo "### 🐛 Bug 修复"
    echo "$FIXES"
    echo ""
fi

if [ ! -z "$IMPROVEMENTS" ]; then
    echo "### 🚀 性能优化"
    echo "$IMPROVEMENTS"
    echo ""
fi

if [ ! -z "$DOCS" ]; then
    echo "### 📚 文档更新"
    echo "$DOCS"
    echo ""
fi

if [ ! -z "$OTHERS" ]; then
    echo "### 🔧 其他更改"
    echo "$OTHERS"
    echo ""
fi

if [ -z "$FEATURES" ] && [ -z "$FIXES" ] && [ -z "$IMPROVEMENTS" ] && [ -z "$DOCS" ] && [ -z "$OTHERS" ]; then
    echo "⚠️ 没有找到新的提交或提交信息不符合规范"
    echo "请确保:"
    echo "1. 有新的提交（相对于上个标签）"
    echo "2. 提交信息遵循规范（参考 COMMIT_CONVENTION.md）"
    echo ""
fi

echo "### 📋 版本信息"
echo "- **Minecraft 版本**: 1.18.2"
echo "- **Forge 版本**: 40.3.9"
echo "- **Java 版本**: 17"
echo ""

echo "═══════════════════════════════════════"
echo "💡 这就是发布时自动生成的更新日志预览"
echo "🔧 如需改进，请参考 COMMIT_CONVENTION.md 调整提交信息格式" 