# TinyAuth 更新日志

## 新增功能

### 1. IPv6支持优化
- **修复**: 审计界面IPv6地址显示不全的问题
- **改进**: IP地址解析逻辑，完整保留IPv6格式
- **优化**: 审计界面显示格式，使用颜色代码提升可读性

### 2. IP登录限制功能
- **新增**: IP白名单系统，支持IPv4和IPv6
- **配置项**:
  - `enableIPRestriction`: 是否启用IP登录限制 (默认: false)
  - `maxAllowedIPs`: 每个账号允许的最大IP数量 (默认: 3)
  - `ipRestrictedMessage`: IP受限时的提示消息
- **功能**:
  - 首次注册/登录时自动添加当前IP到白名单
  - 超出IP限制时阻止登录
  - 管理员可以管理玩家的IP白名单

### 3. 登录成功延迟消息和命令
- **新增**: 登录成功后延迟发送自定义消息
- **新增**: 登录成功后延迟执行自定义命令
- **配置项**:
  - `loginDelayTicks`: 延迟时间（游戏刻，默认: 60）
  - `loginDelayMessage`: 延迟发送的消息
  - `loginDelayCommand`: 延迟执行的命令（支持%player%占位符）

### 4. 新增管理员命令

#### IP管理命令
- `/auth ip list <玩家>` - 查看玩家的允许IP列表
- `/auth ip add <玩家> <IP>` - 为玩家添加允许的IP
- `/auth ip remove <玩家> <IP>` - 移除玩家的允许IP
- `/auth ip clear <玩家>` - 清空玩家的IP白名单

#### 现有命令优化
- `/auth audit <玩家>` - 审计界面显示优化，支持完整IPv6显示

## 配置文件示例

```toml
[auth_settings]
    # 密码规则
    pwdMatch = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_])[^\\s]{8,}$"
    pwdMatchError = "§r密码必须包含至少一个小写字母、一个大写字母、一个数字、一个特殊字符（包括下划线），不能有空格，且长度至少为8位。"
    
    # 消息间隔和超时
    tickPerMsg = 300
    timeOutTick = 3000
    timeoutKickMessage = "§c登录超时，请重新连接服务器"
    
    # IP限制功能
    enableIPRestriction = false
    maxAllowedIPs = 3
    ipRestrictedMessage = "§c此账号已达到IP登录限制，请联系管理员"
    
    # 延迟消息和命令
    loginDelayTicks = 60
    loginDelayMessage = "§a欢迎回来！祝你游戏愉快！"
    loginDelayCommand = "say 欢迎 %player% 回到服务器！"
```

## 使用说明

### 启用IP限制
1. 在配置文件中设置 `enableIPRestriction = true`
2. 调整 `maxAllowedIPs` 为合适的数值
3. 重启服务器

### 管理玩家IP
```
# 查看玩家IP列表
/auth ip list Steve

# 为玩家添加新IP
/auth ip add Steve 192.168.1.100
/auth ip add Steve 2001:db8::1

# 移除玩家IP
/auth ip remove Steve 192.168.1.100

# 清空玩家所有IP
/auth ip clear Steve
```

### 自定义登录消息
在配置文件中设置：
- `loginDelayMessage`: 登录后显示的欢迎消息
- `loginDelayCommand`: 登录后执行的命令（如全服通知）
- `loginDelayTicks`: 延迟时间（20刻 = 1秒）

## 注意事项

1. **IPv6支持**: 系统完全支持IPv6地址，包括完整的地址显示和存储
2. **向后兼容**: 现有玩家数据不会受到影响，新功能默认关闭
3. **性能优化**: IP检查逻辑高效，不会影响服务器性能
4. **安全性**: IP限制功能可以有效防止账号被盗用 