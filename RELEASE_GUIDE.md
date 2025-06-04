# TinyAuth 发布指南

## 📋 自动化发布系统

本项目已配置自动化发布系统，可以通过简单的命令自动创建 GitHub Release。

## 🚀 发布新版本

### 方法 1：使用发布脚本（推荐）

#### Windows（PowerShell）
```powershell
.\release.ps1 -Version "0.0.4"
```

#### Linux/macOS（Bash）
```bash
./release.sh 0.0.4
```

### 方法 2：手动发布

1. **更新版本号**
   编辑 `gradle.properties` 文件中的 `mod_version` 字段：
   ```properties
   mod_version=0.0.4
   ```

2. **提交更改**
   ```bash
   git add gradle.properties
   git commit -m "bump version to 0.0.4"
   ```

3. **创建标签**
   ```bash
   git tag -a "v0.0.4" -m "Release version 0.0.4"
   ```

4. **推送到 GitHub**
   ```bash
   git push origin 1.18.2
   git push origin "v0.0.4"
   ```

## 🔄 发布流程

1. **触发条件**：推送以 `v` 开头的标签（如 `v0.0.4`）
2. **自动构建**：GitHub Actions 自动构建项目
3. **创建 Release**：自动创建 GitHub Release 并上传 JAR 文件
4. **生成说明**：自动生成包含版本信息和安装说明的 Release 描述

## 📦 Release 内容

每个 Release 将包含：
- 编译好的 MOD JAR 文件
- 版本信息（Minecraft、Forge、Java 版本）
- 安装说明
- 链接到更新日志

## ⚠️ 注意事项

1. **版本格式**：必须使用语义化版本格式（如 `1.0.0`）
2. **工作目录**：发布前确保所有更改已提交
3. **分支**：Release 将基于 `1.18.2` 分支
4. **权限**：确保有推送标签的权限

## 🔍 监控发布

发布后可以在以下位置监控进度：
- GitHub Actions：`https://github.com/你的用户名/TinyAuth/actions`
- Releases 页面：`https://github.com/你的用户名/TinyAuth/releases`

## 🐛 故障排除

### 常见问题

1. **权限错误**
   - 确保有推送权限
   - 检查 `GITHUB_TOKEN` 权限

2. **构建失败**
   - 检查 Java 版本兼容性
   - 验证 Gradle 配置

3. **标签已存在**
   - 删除现有标签：`git tag -d v0.0.4`
   - 删除远程标签：`git push origin :refs/tags/v0.0.4`

### 重新发布

如果需要重新发布相同版本：
```bash
# 删除本地标签
git tag -d v0.0.4

# 删除远程标签
git push origin :refs/tags/v0.0.4

# 重新创建并推送
git tag -a "v0.0.4" -m "Release version 0.0.4"
git push origin "v0.0.4"
```

## 📚 更多信息

- [GitHub Actions 文档](https://docs.github.com/cn/actions)
- [语义化版本规范](https://semver.org/lang/zh-CN/)
- [Minecraft Forge 文档](https://docs.minecraftforge.net/) 