# TinyAuth

> 🚀 **自动化发布系统**: 本项目已配置自动化发布系统，支持通过 commit 信息自动生成结构化的更新日志！

## ✨ 自动化发布功能

- **🎯 一键发布**: 使用脚本一键完成版本发布
- **📝 智能更新日志**: 自动分析 commit 信息，按类型生成结构化更新日志
- **🔄 自动构建**: GitHub Actions 自动构建和发布 Release
- **📦 完整包装**: 自动生成版本信息、安装说明和下载链接

### 快速发布
```bash
# Windows
.\release.ps1 -Version "0.0.4"

# Linux/macOS  
./release.sh 0.0.4
```

### 预览更新日志
```bash
# Windows
.\preview-changelog.ps1

# Linux/macOS
./preview-changelog.sh
```

📚 **详细文档**: 
- [发布指南](./RELEASE_GUIDE.md) - 完整的发布流程说明
- [Commit 规范](./COMMIT_CONVENTION.md) - 编写标准化提交信息

![Brand](img/tinyAuthBrand.png)
![Build](https://github.com/myot233/tinyauth/actions/workflows/gradle.yml/badge.svg)
![Minecraft](https://img.shields.io/badge/Minecraft-1.18.2+-green.svg)
![Forge](https://img.shields.io/badge/Forge-Mod-orange.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)
![Java](https://img.shields.io/badge/Java-17+-red.svg)
![Version](https://img.shields.io/badge/Version-0.0.2-brightgreen.svg)
![Issues](https://img.shields.io/github/issues/myot233/tinyauth)
![Stars](https://img.shields.io/github/stars/myot233/tinyauth)

一个轻量级的Minecraft Forge服务器登录认证插件，提供完整的用户注册、登录和管理功能。

## 🌟 功能特性
### 核心功能
- ✅ **用户注册与登录** - 支持密码注册和登录验证
- ✅ **密码安全** - SHA-256加密存储，支持自定义密码规则
- ✅ **登录超时** - 可配置的登录超时时间和自动踢出
- ✅ **游戏模式保护** - 未登录玩家自动切换为观察者模式
- ✅ **位置锁定** - 未登录玩家无法移动和交互

### 高级功能
- 🛡️ **防多开系统** - 限制每个IP地址的最大玩家数量
- 📝 **IP白名单** - 信任的IP地址可绕过防多开限制
- 🔒 **IP访问控制** - 限制每个账号可登录的IP数量
- 📊 **登录历史** - 记录玩家登录历史和IP地址
- 🎯 **大屏幕提示** - 重要操作显示大标题提示
- ⏰ **延迟消息** - 登录成功后的延迟欢迎消息和命令

### 管理功能
- 👨‍💼 **完整的管理员命令** - 登出、注销、重置密码等
- 📈 **详细统计信息** - IP地址统计、玩家分组显示
- 🔧 **高度可配置** - 所有消息和参数都可自定义
- 🌐 **IPv6支持** - 完整支持IPv4和IPv6地址

## 📋 功能总览

| 功能类别 | 功能名称 | 描述 | 状态 |
|---------|---------|------|------|
| **用户认证** | 用户注册 | 支持密码注册，密码规则可配置 | ✅ |
| | 用户登录 | SHA-256加密验证 | ✅ |
| | 密码修改 | 用户可自行修改密码 | ✅ |
| | 登录超时 | 超时自动踢出，时间可配置 | ✅ |
| **安全保护** | 游戏模式锁定 | 未登录玩家强制观察者模式 | ✅ |
| | 位置锁定 | 未登录玩家无法移动 | ✅ |
| | 交互限制 | 未登录玩家无法交互 | ✅ |
| | 命令限制 | 未登录玩家只能使用登录命令 | ✅ |
| **防多开** | IP玩家数限制 | 每个IP最大玩家数量限制 | ✅ |
| | IP白名单 | 信任IP绕过防多开限制 | ✅ |
| | 实时监控 | 动态检查IP玩家数量 | ✅ |
| **IP管理** | IP访问控制 | 限制每个账号可登录的IP数量 | ✅ |
| | IP历史记录 | 记录玩家所有登录IP | ✅ |
| | IP统计信息 | 查看IP地址详细统计 | ✅ |
| | IPv6支持 | 完整支持IPv4和IPv6地址 | ✅ |
| **管理工具** | 强制登出 | 管理员可强制登出玩家 | ✅ |
| | 账号注销 | 管理员可注销玩家账号 | ✅ |
| | 密码重置 | 管理员可重置玩家密码 | ✅ |
| | 登录审计 | 查看玩家详细登录历史 | ✅ |
| **用户体验** | 大屏幕提示 | 重要操作显示大标题 | ✅ |
| | 延迟消息 | 登录成功后延迟欢迎消息 | ✅ |
| | 自定义消息 | 所有提示消息完全可配置 | ✅ |
| | 颜色支持 | 支持Minecraft颜色代码 | ✅ |
| **数据管理** | 数据持久化 | 使用Capability系统存储 | ✅ |


> ✅ 已实现 | ❌ 计划中 | 🔄 开发中

## 📦 安装

1. 下载最新版本的TinyAuth jar文件
2. 将jar文件放入服务器的 `mods` 文件夹
3. 启动服务器，插件会自动生成配置文件
4. 根据需要修改配置文件 `config/tinyauth-server.toml`

## 🎮 玩家命令

| 命令 | 描述 | 示例 |
|------|------|------|
| `/register <密码> <确认密码>` | 注册新账号 | `/register mypassword mypassword` |
| `/login <密码>` | 登录账号 | `/login mypassword` |
| `/changepwd <旧密码> <新密码>` | 修改密码 | `/changepwd oldpwd newpwd` |

## 🛠️ 管理员命令

### 基础管理
| 命令 | 描述 |
|------|------|
| `/auth logout <玩家>` | 强制登出玩家 |
| `/auth remove <玩家>` | 注销玩家账号 |
| `/auth reset <玩家> <新密码>` | 重置玩家密码 |
| `/auth audit <玩家>` | 查看玩家登录历史 |

### IP管理
| 命令 | 描述 |
|------|------|
| `/auth ip list <玩家>` | 查看玩家允许的IP列表 |
| `/auth ip add <玩家> <IP>` | 为玩家添加允许的IP |
| `/auth ip remove <玩家> <IP>` | 移除玩家的允许IP |
| `/auth ip clear <玩家>` | 清空玩家的IP列表 |
| `/auth ip stats <IP>` | 查看指定IP的统计信息 |
| `/auth ip players` | 查看所有在线玩家的IP |
| `/auth ip groups` | 按IP分组显示玩家 |

### 白名单管理
| 命令 | 描述 |
|------|------|
| `/auth whitelist list` | 查看IP白名单 |
| `/auth whitelist add <IP>` | 添加IP到白名单 |
| `/auth whitelist remove <IP>` | 从白名单移除IP |
| `/auth whitelist check <IP>` | 检查IP是否在白名单中 |

## ⚙️ 配置说明

### 基础配置
```toml
# 密码规则正则表达式
pwdMatch = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_])[^\\s]{8,}$"

# 消息发送间隔（游戏刻）
tickPerMsg = 300

# 登录超时时间（游戏刻）
timeOutTick = 3000
```

### 防多开配置
```toml
# 启用每个IP最大玩家数限制
enableIPPlayerLimit = true

# 每个IP最多允许的玩家数
maxPlayersPerIP = 1

# IP白名单（这些IP不受防多开限制）
ipWhitelist = ["127.0.0.1", "localhost", "192.168.1.100"]
```

### IP访问控制
```toml
# 启用每个账号的IP数量限制
enableIPRestriction = false

# 每个账号最多允许的IP数量
maxAllowedIPs = 3
```

### 延迟消息配置
```toml
# 登录成功后延迟发送消息的时间（游戏刻）
loginDelayTicks = 60

# 延迟发送的欢迎消息
loginDelayMessage = "§a欢迎回来！祝你游戏愉快！"

# 延迟执行的命令（%player% 会被替换为玩家名）
loginDelayCommand = "say 欢迎 %player% 回到服务器！"
```

## 🎨 消息自定义

所有玩家看到的消息都可以在配置文件中自定义，支持Minecraft颜色代码：

```toml
# 提示消息
registerPrompt = "§e请使用 /register <密码> <确认密码> 注册账号"
loginPrompt = "§e请使用 /login <密码> 登录账号"

# 状态消息
loginSuccessMessage = "§b登录成功！"
loginFailMessage = "§4密码错误"
registerSuccessMessage = "§b注册成功！"

# 错误消息
passwordMismatchMessage = "§4两次输入的密码不一致"
ipPlayerLimitMessage = "§c此IP地址已达到最大玩家数量限制"
```

## 🔧 技术特性

- **Minecraft版本**: 1.18.2+ (Forge)
- **数据存储**: 使用Minecraft原生的Capability系统
- **密码加密**: SHA-256哈希算法
- **IPv6支持**: 完整支持IPv4和IPv6地址格式
- **性能优化**: 轻量级设计，对服务器性能影响极小

## 📝 使用场景

### 1. 防止恶意登录
- 保护玩家账号安全
- 防止未授权访问

### 2. 防止多开
- 限制每个IP的玩家数量
- 防止玩家使用多个账号

### 3. 服务器管理
- 详细的登录日志
- 灵活的IP管理
- 完整的管理员工具

## 🤝 贡献

欢迎提交Issue和Pull Request来改进这个项目！

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 🆘 支持

如果你遇到问题或有建议，请：

1. 查看配置文件确保设置正确
2. 检查服务器日志获取错误信息
3. 在GitHub上提交Issue

---

**TinyAuth** - 让你的Minecraft服务器更安全！ 🛡️