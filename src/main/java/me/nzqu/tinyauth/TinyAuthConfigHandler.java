package me.nzqu.tinyauth;

import net.minecraftforge.common.ForgeConfigSpec;

public class TinyAuthConfigHandler {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec Config;
    public static final ForgeConfigSpec.ConfigValue<String> PasswordRegex  = BUILDER
            .comment("密码规则的正则表达式")
            .define("pwdMatch", "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_])[^\\s]{8,}$");

    public static final ForgeConfigSpec.ConfigValue<String> PasswordRegexError = BUILDER
            .comment("密码规则不匹配时的错误提示")
            .define("pwdMatchError", "§r密码必须包含至少一个小写字母、一个大写字母、一个数字、一个特殊字符（包括下划线），不能有空格，且长度至少为8位。");

    public static final ForgeConfigSpec.ConfigValue<Integer> TickPerMsg = BUILDER
            .comment("每个消息的发送间隔（游戏刻）")
            .define("tickPerMsg", 300);

    public static final ForgeConfigSpec.ConfigValue<Integer> TimeOutTick = BUILDER
           .comment("登录超时时间（游戏刻）")
           .define("timeOutTick", 3000);
           
    // 玩家消息提示
    public static final ForgeConfigSpec.ConfigValue<String> RegisterPrompt = BUILDER
            .comment("注册提示消息")
            .define("registerPrompt", "§e请使用 /register <密码> <确认密码> 注册账号");

    public static final ForgeConfigSpec.ConfigValue<String> LoginPrompt = BUILDER
            .comment("登录提示消息")
            .define("loginPrompt", "§e请使用 /login <密码> 登录账号");

    public static final ForgeConfigSpec.ConfigValue<String> LoginSuccessMessage = BUILDER
            .comment("登录成功消息")
            .define("loginSuccessMessage", "§b登录成功！");

    public static final ForgeConfigSpec.ConfigValue<String> LoginFailMessage = BUILDER
            .comment("登录失败消息")
            .define("loginFailMessage", "§4密码错误");
            
    // 管理员命令消息
    public static final ForgeConfigSpec.ConfigValue<String> LogoutMessage = BUILDER
            .comment("被管理员登出消息")
            .define("logoutMessage", "§e你的账号已被管理员登出");

    public static final ForgeConfigSpec.ConfigValue<String> RemoveMessage = BUILDER
            .comment("账号被注销消息")
            .define("removeMessage", "§e你的账号已被管理员注销");

    public static final ForgeConfigSpec.ConfigValue<String> ResetMessage = BUILDER
            .comment("密码被重置消息")
            .define("resetMessage", "§e你的密码已被管理员重置");

    public static final ForgeConfigSpec.ConfigValue<String> TimeoutKickMessage = BUILDER
            .comment("登录超时踢出消息")
            .define("timeoutKickMessage", "§c登录超时，请重新连接服务器");

    // 注册和登录相关消息
    public static final ForgeConfigSpec.ConfigValue<String> NotRegisteredMessage = BUILDER
            .comment("未注册提示消息")
            .define("notRegisteredMessage", "§y你还没有注册");

    public static final ForgeConfigSpec.ConfigValue<String> AlreadyRegisteredMessage = BUILDER
            .comment("已注册提示消息")
            .define("alreadyRegisteredMessage", "§y你已经注册过了");

    public static final ForgeConfigSpec.ConfigValue<String> AlreadyLoggedInMessage = BUILDER
            .comment("已登录提示消息")
            .define("alreadyLoggedInMessage", "§y你已经登录过了");

    public static final ForgeConfigSpec.ConfigValue<String> PasswordMismatchMessage = BUILDER
            .comment("两次密码不一致消息")
            .define("passwordMismatchMessage", "§4两次输入的密码不一致");

    public static final ForgeConfigSpec.ConfigValue<String> RegisterSuccessMessage = BUILDER
            .comment("注册成功消息")
            .define("registerSuccessMessage", "§b注册成功！");

    public static final ForgeConfigSpec.ConfigValue<String> ChangePasswordSuccessMessage = BUILDER
            .comment("修改密码成功消息")
            .define("changePasswordSuccessMessage", "§b修改密码成功！");

    // IP管理相关消息
    public static final ForgeConfigSpec.ConfigValue<String> IPAddedMessage = BUILDER
            .comment("IP被添加消息")
            .define("ipAddedMessage", "§a管理员已为你添加允许IP: %ip%");

    public static final ForgeConfigSpec.ConfigValue<String> IPRemovedMessage = BUILDER
            .comment("IP被移除消息")
            .define("ipRemovedMessage", "§e管理员已移除你的允许IP: %ip%");

    public static final ForgeConfigSpec.ConfigValue<String> IPClearedMessage = BUILDER
            .comment("IP列表被清空消息")
            .define("ipClearedMessage", "§e管理员已清空你的允许IP列表");

    // IP登录限制配置
    public static final ForgeConfigSpec.ConfigValue<Boolean> EnableIPRestriction = BUILDER
            .comment("是否启用IP登录限制")
            .define("enableIPRestriction", false);

    public static final ForgeConfigSpec.ConfigValue<Integer> MaxAllowedIPs = BUILDER
            .comment("每个账号允许的最大IP数量")
            .define("maxAllowedIPs", 3);

    public static final ForgeConfigSpec.ConfigValue<String> IPRestrictedMessage = BUILDER
            .comment("IP受限时的提示消息")
            .define("ipRestrictedMessage", "§c此账号已达到IP登录限制，请联系管理员");

    // 登录成功后的延迟消息配置
    public static final ForgeConfigSpec.ConfigValue<Integer> LoginDelayTicks = BUILDER
            .comment("登录成功后延迟发送消息的时间（游戏刻）")
            .define("loginDelayTicks", 60);

    public static final ForgeConfigSpec.ConfigValue<String> LoginDelayMessage = BUILDER
            .comment("登录成功后延迟发送的消息")
            .define("loginDelayMessage", "§a欢迎回来！祝你游戏愉快！");

    public static final ForgeConfigSpec.ConfigValue<String> LoginDelayCommand = BUILDER
            .comment("登录成功后延迟执行的命令（不包含/）")
            .define("loginDelayCommand", "say 欢迎 %player% 回到服务器！");

    // 大屏幕提示配置
    public static final ForgeConfigSpec.ConfigValue<String> LogoutTitleText = BUILDER
            .comment("被登出时的大标题文本")
            .define("logoutTitleText", "§c§l账号已登出");

    public static final ForgeConfigSpec.ConfigValue<String> LogoutSubtitleText = BUILDER
            .comment("被登出时的副标题文本")
            .define("logoutSubtitleText", "§e你的账号已被管理员强制登出");

    public static final ForgeConfigSpec.ConfigValue<String> RemoveTitleText = BUILDER
            .comment("账号被注销时的大标题文本")
            .define("removeTitleText", "§4§l账号已注销");

    public static final ForgeConfigSpec.ConfigValue<String> RemoveSubtitleText = BUILDER
            .comment("账号被注销时的副标题文本")
            .define("removeSubtitleText", "§c你的账号已被管理员注销");
            
    static {
        BUILDER.push("Auth Settings");
        // 配置已在上面定义，这里不需要重复定义
        BUILDER.pop();
         Config = BUILDER.build();
    }
}
