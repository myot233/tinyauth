# TinyAuth 发布指南

## 📋 自动化发布系统

本项目已配置自动化发布系统，可以通过简单的命令自动创建 GitHub Release，并且会**自动收集 commit 信息生成结构化的更新日志**。

## ✨ 新功能：自动更新日志

系统会自动分析两个版本之间的所有 commit 信息，并按类型分类生成结构化的更新日志：

- **🆕 新功能**：自动识别 `feat`、`add`、`新增` 等关键词
- **🐛 Bug 修复**：自动识别 `fix`、`bug`、`修复` 等关键词  
- **🚀 性能优化**：自动识别 `improve`、`enhance`、`优化` 等关键词
- **📚 文档更新**：自动识别 `docs`、`doc`、`文档` 等关键词
- **🔧 其他更改**：其他类型的提交

> 💡 为了获得最佳的自动生成效果，请遵循 [Commit 信息规范](./COMMIT_CONVENTION.md)

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
3. **🆕 收集提交**：自动分析上个版本到当前版本的所有 commit
4. **🆕 生成更新日志**：按照提交类型自动分类生成结构化更新日志
5. **创建 Release**：自动创建 GitHub Release 并上传 JAR 文件

## 📦 Release 内容

每个 Release 将自动包含：

### 🆕 自动生成的更新日志
```markdown
## 🎉 TinyAuth v0.0.4

### ✨ 新功能
- feat: 添加双因素认证功能
- add: 新增用户权限管理

### 🐛 Bug 修复  
- fix: 修复登录超时问题
- 修复: 解决密码重置失败的问题

### 🚀 性能优化
- improve: 优化数据库查询性能
- 优化: 减少内存使用量

### 📚 文档更新
- docs: 更新安装指南
- 文档: 完善使用说明

### 🔧 其他更改
- style: 调整代码格式
- test: 添加单元测试
```

### 📋 版本信息和安装说明
- 编译好的 MOD JAR 文件
- Minecraft、Forge、Java 版本信息
- 详细的安装说明
- 链接到完整更新日志

## 💡 优化发布质量的建议

### 1. 遵循 Commit 规范
请参考 [Commit 信息规范](./COMMIT_CONVENTION.md)，使用标准化的 commit 信息：

```bash
# 好的示例
feat: 添加用户登录功能
fix: 修复密码验证错误
improve: 优化登录性能
docs: 更新 README 文档

# 避免的写法
修改了一些东西
update
WIP
```

### 2. 在发布前整理 Commit
如果最近的提交信息不够规范，可以在发布前使用 `git rebase -i` 重写提交信息。

### 3. 测试自动生成效果
您可以在发布前预览自动生成的更新日志：

```bash
# 查看自上个标签以来的所有提交
git log $(git describe --tags --abbrev=0)..HEAD --oneline

# 查看特定类型的提交
git log $(git describe --tags --abbrev=0)..HEAD --grep="^feat" --oneline
```

## ⚠️ 注意事项

1. **版本格式**：必须使用语义化版本格式（如 `1.0.0`）
2. **工作目录**：发布前确保所有更改已提交
3. **分支**：Release 将基于 `1.18.2` 分支
4. **权限**：确保有推送标签的权限
5. **🆕 Commit 规范**：遵循提交规范以获得更好的自动生成效果

## 🔍 监控发布

发布后可以在以下位置监控进度：
- GitHub Actions：`https://github.com/你的用户名/TinyAuth/actions`
- Releases 页面：`https://github.com/你的用户名/TinyAuth/releases`

在 Actions 页面中，您可以查看"Generate Changelog"步骤的日志，了解自动生成的更新日志内容。

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

4. **🆕 更新日志为空或不完整**
   - 检查 commit 信息是否遵循规范
   - 确保有新的提交（相对于上个标签）
   - 查看 GitHub Actions 日志中的"Generate Changelog"步骤

### 重新发布

如果需要重新发布相同版本：
```bash
# 删除本地标签
git tag -d v0.0.4

# 删除远程标签  
git push origin :refs/tags/v0.0.4

# 删除 GitHub Release（如果已创建）
# 需要在 GitHub 网页上手动删除

# 重新创建并推送
git tag -a "v0.0.4" -m "Release version 0.0.4"
git push origin "v0.0.4"
```

## 📚 更多信息

- [Commit 信息规范](./COMMIT_CONVENTION.md)
- [GitHub Actions 文档](https://docs.github.com/cn/actions)
- [语义化版本规范](https://semver.org/lang/zh-CN/)
- [Minecraft Forge 文档](https://docs.minecraftforge.net/)
- [Conventional Commits](https://www.conventionalcommits.org/) 