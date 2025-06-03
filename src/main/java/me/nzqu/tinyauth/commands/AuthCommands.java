package me.nzqu.tinyauth.commands;

import java.util.List;
import java.util.ArrayList;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import me.nzqu.tinyauth.TinyAuth;
import me.nzqu.tinyauth.TinyAuthConfigHandler;
import me.nzqu.tinyauth.utils.AuthUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class AuthCommands {
    public static final String LOGIN_COMMAND = "login";
    public static final String REGISTER_COMMAND = "register";
    public static final String CHANGE_PASSWORD_COMMAND = "changepwd";
    public static final String AUTH_ADMIN_COMMAND = "auth";

    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event){
        event.getDispatcher()
                .register(Commands.literal(REGISTER_COMMAND)
                        .then(Commands.argument("password",StringArgumentType.word())
                                .then(Commands.argument("repeat_password",StringArgumentType.word())
                                        .executes(AuthCommands::onRegisterCommand)))
                        );

        event.getDispatcher()
                .register(Commands.literal(LOGIN_COMMAND)
                        .then(Commands.argument("password",StringArgumentType.word())
                                .executes(AuthCommands::onLoginCommand)));

        event.getDispatcher()
                .register(
                Commands.literal(CHANGE_PASSWORD_COMMAND)
                .then(Commands.argument("old_password",StringArgumentType.word())
                        .then(Commands.argument("new_password",StringArgumentType.word())
                                .executes(AuthCommands::onChangePasswordCommand))));
                                
        // 管理员命令
        event.getDispatcher()
                .register(Commands.literal(AUTH_ADMIN_COMMAND)
                        .requires(source -> source.hasPermission(2)) // 需要OP权限
                        .then(Commands.literal("logout")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(AuthCommands::onLogoutCommand)))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(AuthCommands::onRemoveCommand)))
                        .then(Commands.literal("reset")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("new_password", StringArgumentType.word())
                                                .executes(AuthCommands::onResetCommand))))
                        .then(Commands.literal("audit")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(AuthCommands::onAuditCommand)))
                        .then(Commands.literal("ip")
                                .then(Commands.literal("list")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(AuthCommands::onIPListCommand)))
                                .then(Commands.literal("add")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .then(Commands.argument("ip", StringArgumentType.word())
                                                        .executes(AuthCommands::onIPAddCommand))))
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .then(Commands.argument("ip", StringArgumentType.word())
                                                        .executes(AuthCommands::onIPRemoveCommand))))
                                .then(Commands.literal("clear")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(AuthCommands::onIPClearCommand)))
                                .then(Commands.literal("stats")
                                        .then(Commands.argument("ip", StringArgumentType.word())
                                                .executes(AuthCommands::onIPStatsCommand)))));
    }

    private static int onChangePasswordCommand(CommandContext<CommandSourceStack> ctx) {
        if(ctx.getSource().getEntity() instanceof ServerPlayer player){
            if(!AuthUtils.isRegistered(player)){
                AuthUtils.sendAuthMessage(TinyAuthConfigHandler.NotRegisteredMessage.get(),player);
                return 0;
            }
            String oldPassword = StringArgumentType.getString(ctx,"old_password");
            String newPassword = StringArgumentType.getString(ctx,"new_password");
            if(AuthUtils.checkPwd(player,oldPassword)){
                AuthUtils.changePassword(player,newPassword);
                AuthUtils.sendAuthMessage(TinyAuthConfigHandler.ChangePasswordSuccessMessage.get(),player);
                return 1;
            }
            AuthUtils.sendAuthMessage(TinyAuthConfigHandler.LoginFailMessage.get(),player);
            return 0;
        }
        return 0;
    }


    private static int onLoginCommand(CommandContext<CommandSourceStack> ctx) {
        if(ctx.getSource().getEntity() instanceof ServerPlayer player){
            TinyAuth.LOGGER.info("Login command executed at {}", player.getName());
            if(!AuthUtils.isRegistered(player)){
                AuthUtils.sendAuthMessage(TinyAuthConfigHandler.NotRegisteredMessage.get(),player);
                return 0;
            }
            if(AuthUtils.isLoggedIn(player)){
                AuthUtils.sendAuthMessage(TinyAuthConfigHandler.AlreadyLoggedInMessage.get(),player);
                return 0;
            }
            String password = StringArgumentType.getString(ctx,"password");
            if(AuthUtils.login(player,password)){
                AuthUtils.sendAuthMessage(TinyAuthConfigHandler.LoginSuccessMessage.get(),player);
                return 1;
            }
            AuthUtils.sendAuthMessage(TinyAuthConfigHandler.LoginFailMessage.get(),player);
            return 0;
        }
        return 0;
    }


    private static int onRegisterCommand(CommandContext<CommandSourceStack> ctx) {
        if(ctx.getSource().getEntity() instanceof ServerPlayer player){
            if(AuthUtils.isRegistered(player)){
                AuthUtils.sendAuthMessage(TinyAuthConfigHandler.AlreadyRegisteredMessage.get(),player);
                return 0;
            }
            String password = StringArgumentType.getString(ctx,"password");
            String repeatPassword =  StringArgumentType.getString(ctx,"repeat_password");
            if(!password.equals(repeatPassword)){
                AuthUtils.sendAuthMessage(TinyAuthConfigHandler.PasswordMismatchMessage.get(),player);
                return 0;
            }
            if(!password.matches(TinyAuthConfigHandler.PasswordRegex.get())){
                AuthUtils.sendAuthMessage(TinyAuthConfigHandler.PasswordRegexError.get(),player);
                return 0;
            }
            AuthUtils.register(player,password);
            AuthUtils.sendAuthMessage(TinyAuthConfigHandler.RegisterSuccessMessage.get(),player);
            return 1;
        }
        return 0;
    }
    
    // 管理员命令：登出玩家
    private static int onLogoutCommand(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer targetPlayer = EntityArgument.getPlayer(ctx, "player");
            if (AuthUtils.isLoggedIn(targetPlayer)) {
                AuthUtils.logout(targetPlayer);
                AuthUtils.sendAuthMessage(TinyAuthConfigHandler.LogoutMessage.get(), targetPlayer);

                ctx.getSource().sendSuccess(new TextComponent("已登出玩家 " + targetPlayer.getName().getString()), true);
                TinyAuth.LOGGER.info("Admin {} logged out player {}", 
                    ctx.getSource().getPlayerOrException().getName().getString(), 
                    targetPlayer.getName().getString());
                return 1;
            } else {
                ctx.getSource().sendFailure(new TextComponent("该玩家当前未登录"));
                return 0;
            }
        } catch (Exception e) {
            ctx.getSource().sendFailure(new TextComponent("执行命令时出错"));
            TinyAuth.LOGGER.error("Error executing logout command", e);
            return 0;
        }
    }

    // 管理员命令：注销玩家账号
    private static int onRemoveCommand(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer targetPlayer = EntityArgument.getPlayer(ctx, "player");
            if (AuthUtils.isRegistered(targetPlayer)) {
                AuthUtils.removeAccount(targetPlayer);
                AuthUtils.sendAuthMessage(TinyAuthConfigHandler.RemoveMessage.get(), targetPlayer);
                ctx.getSource().sendSuccess(new TextComponent("已注销玩家 " + targetPlayer.getName().getString() + " 的账号"), true);
                TinyAuth.LOGGER.info("Admin {} removed account of player {}", 
                    ctx.getSource().getPlayerOrException().getName().getString(), 
                    targetPlayer.getName().getString());
                return 1;
            } else {
                ctx.getSource().sendFailure(new TextComponent("该玩家尚未注册"));
                return 0;
            }
        } catch (Exception e) {
            ctx.getSource().sendFailure(new TextComponent("执行命令时出错"));
            TinyAuth.LOGGER.error("Error executing remove command", e);
            return 0;
        }
    }

    // 管理员命令：重置玩家密码
    private static int onResetCommand(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer targetPlayer = EntityArgument.getPlayer(ctx, "player");
            String newPassword = StringArgumentType.getString(ctx, "new_password");
            
            if (!newPassword.matches(TinyAuthConfigHandler.PasswordRegex.get())) {
                ctx.getSource().sendFailure(new TextComponent(TinyAuthConfigHandler.PasswordRegexError.get()));
                return 0;
            }
            
            if (AuthUtils.isRegistered(targetPlayer)) {
                // 修改密码并登出玩家
                AuthUtils.changePassword(targetPlayer, newPassword);
                if (AuthUtils.isLoggedIn(targetPlayer)) {
                    AuthUtils.logout(targetPlayer);
                }
                AuthUtils.sendAuthMessage(TinyAuthConfigHandler.ResetMessage.get(), targetPlayer);
                ctx.getSource().sendSuccess(new TextComponent("已重置玩家 " + targetPlayer.getName().getString() + " 的密码"), true);
                TinyAuth.LOGGER.info("Admin {} reset password for player {}", 
                    ctx.getSource().getPlayerOrException().getName().getString(), 
                    targetPlayer.getName().getString());
                return 1;
            } else {
                ctx.getSource().sendFailure(new TextComponent("该玩家尚未注册"));
                return 0;
            }
        } catch (Exception e) {
            ctx.getSource().sendFailure(new TextComponent("执行命令时出错"));
            TinyAuth.LOGGER.error("Error executing reset command", e);
            return 0;
        }
    }
    
    // 管理员命令：审计玩家登录历史
    private static int onAuditCommand(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer targetPlayer = EntityArgument.getPlayer(ctx, "player");
            if (AuthUtils.isRegistered(targetPlayer)) {
                List<String> loginHistory = AuthUtils.getLoginHistory(targetPlayer);
                
                if (loginHistory.isEmpty()) {
                    ctx.getSource().sendSuccess(new TextComponent("玩家 " + targetPlayer.getName().getString() + " 没有登录记录"), false);
                    return 1;
                }
                
                ctx.getSource().sendSuccess(new TextComponent("§6=== 玩家 " + targetPlayer.getName().getString() + " 的登录历史 ==="), false);
                for (int i = 0; i < loginHistory.size(); i++) {
                    String record = loginHistory.get(i);
                    // 分离时间和IP地址，确保IPv6地址完整显示
                    String[] parts = record.split(" - ", 2);
                    if (parts.length == 2) {
                        String time = parts[0];
                        String ip = parts[1];
                        ctx.getSource().sendSuccess(new TextComponent("§7" + (i+1) + ". §e" + time), false);
                        ctx.getSource().sendSuccess(new TextComponent("   §f" + ip), false);
                    } else {
                        // 如果格式不匹配，直接显示原始记录
                        ctx.getSource().sendSuccess(new TextComponent("§7" + (i+1) + ". §f" + record), false);
                    }
                }
                ctx.getSource().sendSuccess(new TextComponent("§6=== 共 " + loginHistory.size() + " 条记录 ==="), false);
                
                TinyAuth.LOGGER.info("Admin {} audited login history of player {}", 
                    ctx.getSource().getPlayerOrException().getName().getString(), 
                    targetPlayer.getName().getString());
                return 1;
            } else {
                ctx.getSource().sendFailure(new TextComponent("该玩家尚未注册"));
                return 0;
            }
        } catch (Exception e) {
            ctx.getSource().sendFailure(new TextComponent("执行命令时出错"));
            TinyAuth.LOGGER.error("Error executing audit command", e);
            return 0;
        }
    }

    public static void onLoginCommand(){
        // 空方法，可能是用于未完成的功能
    }
    
    // IP管理命令：查看玩家允许的IP列表
    private static int onIPListCommand(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer targetPlayer = EntityArgument.getPlayer(ctx, "player");
            if (AuthUtils.isRegistered(targetPlayer)) {
                List<String> allowedIPs = AuthUtils.getAllowedIPs(targetPlayer);
                
                if (allowedIPs.isEmpty()) {
                    ctx.getSource().sendSuccess(new TextComponent("玩家 " + targetPlayer.getName().getString() + " 没有设置IP限制"), false);
                    return 1;
                }
                
                ctx.getSource().sendSuccess(new TextComponent("§6=== 玩家 " + targetPlayer.getName().getString() + " 的允许IP列表 ==="), false);
                for (int i = 0; i < allowedIPs.size(); i++) {
                    String ip = allowedIPs.get(i);
                    // 为IPv6地址提供更好的显示格式
                    if (ip.contains(":") && ip.length() > 15) {
                        // 可能是IPv6地址，使用单独一行显示
                        ctx.getSource().sendSuccess(new TextComponent("§7" + (i+1) + ". IPv6地址:"), false);
                        ctx.getSource().sendSuccess(new TextComponent("   §f" + ip), false);
                    } else {
                        // IPv4地址或较短的地址，正常显示
                        ctx.getSource().sendSuccess(new TextComponent("§7" + (i+1) + ". §f" + ip), false);
                    }
                }
                ctx.getSource().sendSuccess(new TextComponent("§6=== 共 " + allowedIPs.size() + " 个IP ==="), false);
                
                return 1;
            } else {
                ctx.getSource().sendFailure(new TextComponent("该玩家尚未注册"));
                return 0;
            }
        } catch (Exception e) {
            ctx.getSource().sendFailure(new TextComponent("执行命令时出错"));
            TinyAuth.LOGGER.error("Error executing IP list command", e);
            return 0;
        }
    }
    
    // IP管理命令：添加允许的IP
    private static int onIPAddCommand(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer targetPlayer = EntityArgument.getPlayer(ctx, "player");
            String ip = StringArgumentType.getString(ctx, "ip");
            
            if (AuthUtils.isRegistered(targetPlayer)) {
                if (AuthUtils.tryAddAllowedIP(targetPlayer, ip)) {
                    ctx.getSource().sendSuccess(new TextComponent("已为玩家 " + targetPlayer.getName().getString() + " 添加允许IP: " + ip), true);
                    String message = TinyAuthConfigHandler.IPAddedMessage.get().replace("%ip%", ip);
                    AuthUtils.sendAuthMessage(message, targetPlayer);
                    TinyAuth.LOGGER.info("Admin {} added allowed IP {} for player {}", 
                        ctx.getSource().getPlayerOrException().getName().getString(), 
                        ip, targetPlayer.getName().getString());
                    return 1;
                } else {
                    ctx.getSource().sendFailure(new TextComponent("无法添加IP，可能已达到最大限制或IP已存在"));
                    return 0;
                }
            } else {
                ctx.getSource().sendFailure(new TextComponent("该玩家尚未注册"));
                return 0;
            }
        } catch (Exception e) {
            ctx.getSource().sendFailure(new TextComponent("执行命令时出错"));
            TinyAuth.LOGGER.error("Error executing IP add command", e);
            return 0;
        }
    }
    
    // IP管理命令：移除允许的IP
    private static int onIPRemoveCommand(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer targetPlayer = EntityArgument.getPlayer(ctx, "player");
            String ip = StringArgumentType.getString(ctx, "ip");
            
            if (AuthUtils.isRegistered(targetPlayer)) {
                if (AuthUtils.removeAllowedIP(targetPlayer, ip)) {
                    ctx.getSource().sendSuccess(new TextComponent("已为玩家 " + targetPlayer.getName().getString() + " 移除允许IP: " + ip), true);
                    String message = TinyAuthConfigHandler.IPRemovedMessage.get().replace("%ip%", ip);
                    AuthUtils.sendAuthMessage(message, targetPlayer);
                    TinyAuth.LOGGER.info("Admin {} removed allowed IP {} for player {}", 
                        ctx.getSource().getPlayerOrException().getName().getString(), 
                        ip, targetPlayer.getName().getString());
                    return 1;
                } else {
                    ctx.getSource().sendFailure(new TextComponent("IP不存在于允许列表中"));
                    return 0;
                }
            } else {
                ctx.getSource().sendFailure(new TextComponent("该玩家尚未注册"));
                return 0;
            }
        } catch (Exception e) {
            ctx.getSource().sendFailure(new TextComponent("执行命令时出错"));
            TinyAuth.LOGGER.error("Error executing IP remove command", e);
            return 0;
        }
    }
    
    // IP管理命令：清空允许的IP列表
    private static int onIPClearCommand(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer targetPlayer = EntityArgument.getPlayer(ctx, "player");
            
            if (AuthUtils.isRegistered(targetPlayer)) {
                AuthUtils.clearAllowedIPs(targetPlayer);
                ctx.getSource().sendSuccess(new TextComponent("已清空玩家 " + targetPlayer.getName().getString() + " 的允许IP列表"), true);
                AuthUtils.sendAuthMessage(TinyAuthConfigHandler.IPClearedMessage.get(), targetPlayer);
                TinyAuth.LOGGER.info("Admin {} cleared allowed IPs for player {}", 
                    ctx.getSource().getPlayerOrException().getName().getString(), 
                    targetPlayer.getName().getString());
                return 1;
            } else {
                ctx.getSource().sendFailure(new TextComponent("该玩家尚未注册"));
                return 0;
            }
        } catch (Exception e) {
            ctx.getSource().sendFailure(new TextComponent("执行命令时出错"));
            TinyAuth.LOGGER.error("Error executing IP clear command", e);
            return 0;
        }
    }

    // IP管理命令：查看IP地址的玩家统计信息
    private static int onIPStatsCommand(CommandContext<CommandSourceStack> ctx) {
        try {
            String targetIP = StringArgumentType.getString(ctx, "ip");
            
            // 获取当前从该IP登录的玩家列表
            List<String> loggedInPlayers = new ArrayList<>();
            List<String> allPlayersFromIP = new ArrayList<>();
            
            for (ServerPlayer player : ctx.getSource().getServer().getPlayerList().getPlayers()) {
                String playerIP = AuthUtils.getPlayerIP(player);
                if (targetIP.equals(playerIP)) {
                    allPlayersFromIP.add(player.getName().getString());
                    if (AuthUtils.isLoggedIn(player)) {
                        loggedInPlayers.add(player.getName().getString());
                    }
                }
            }
            
            // 显示统计信息
            ctx.getSource().sendSuccess(new TextComponent("§6=== IP地址 " + targetIP + " 的统计信息 ==="), false);
            ctx.getSource().sendSuccess(new TextComponent("§7当前在线玩家数: §f" + allPlayersFromIP.size()), false);
            ctx.getSource().sendSuccess(new TextComponent("§7已登录玩家数: §f" + loggedInPlayers.size()), false);
            
            // 显示配置限制
            if (TinyAuthConfigHandler.EnableIPPlayerLimit.get()) {
                int maxPlayers = TinyAuthConfigHandler.MaxPlayersPerIP.get();
                ctx.getSource().sendSuccess(new TextComponent("§7最大允许玩家数: §f" + maxPlayers), false);
                
                if (loggedInPlayers.size() >= maxPlayers) {
                    ctx.getSource().sendSuccess(new TextComponent("§c状态: 已达到限制"), false);
                } else {
                    ctx.getSource().sendSuccess(new TextComponent("§a状态: 正常"), false);
                }
            } else {
                ctx.getSource().sendSuccess(new TextComponent("§7IP玩家数限制: §e未启用"), false);
            }
            
            // 显示在线玩家列表
            if (!allPlayersFromIP.isEmpty()) {
                ctx.getSource().sendSuccess(new TextComponent("§7在线玩家列表:"), false);
                for (String playerName : allPlayersFromIP) {
                    boolean isLoggedIn = loggedInPlayers.contains(playerName);
                    String status = isLoggedIn ? "§a已登录" : "§c未登录";
                    ctx.getSource().sendSuccess(new TextComponent("  §f" + playerName + " " + status), false);
                }
            } else {
                ctx.getSource().sendSuccess(new TextComponent("§7当前没有玩家从此IP在线"), false);
            }
            
            ctx.getSource().sendSuccess(new TextComponent("§6=== 统计完成 ==="), false);
            
            TinyAuth.LOGGER.info("Admin {} checked IP stats for {}", 
                ctx.getSource().getPlayerOrException().getName().getString(), targetIP);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(new TextComponent("执行命令时出错"));
            TinyAuth.LOGGER.error("Error executing IP stats command", e);
            return 0;
        }
    }
}
