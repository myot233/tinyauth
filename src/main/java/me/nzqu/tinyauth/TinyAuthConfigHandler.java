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
            
    static {
        BUILDER.push("Auth Settings");
        // 配置已在上面定义，这里不需要重复定义
        BUILDER.pop();
         Config = BUILDER.build();
    }
}
