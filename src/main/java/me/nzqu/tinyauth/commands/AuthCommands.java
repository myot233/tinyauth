package me.nzqu.tinyauth.commands;

import java.util.List;

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
                                        .executes(AuthCommands::onAuditCommand))));
    }

    private static int onChangePasswordCommand(CommandContext<CommandSourceStack> ctx) {
        if(ctx.getSource().getEntity() instanceof ServerPlayer player){
            if(!AuthUtils.isRegistered(player)){
                AuthUtils.sendAuthMessage("§y你还没有注册",player);
                return 0;
            }
            String oldPassword = StringArgumentType.getString(ctx,"old_password");
            String newPassword = StringArgumentType.getString(ctx,"new_password");
            if(AuthUtils.checkPwd(player,oldPassword)){
                AuthUtils.changePassword(player,newPassword);
                AuthUtils.sendAuthMessage("§b修改密码成功！",player);
                return 1;
            }
            AuthUtils.sendAuthMessage("§4密码错误",player);
            return 0;
        }
        return 0;
    }


    private static int onLoginCommand(CommandContext<CommandSourceStack> ctx) {
        if(ctx.getSource().getEntity() instanceof ServerPlayer player){
            TinyAuth.LOGGER.info("Login command executed at {}", player.getName());
            if(!AuthUtils.isRegistered(player)){
                AuthUtils.sendAuthMessage("§y你还没有注册",player);
                return 0;
            }
            if(AuthUtils.isLoggedIn(player)){
                AuthUtils.sendAuthMessage("§y你已经登录过了",player);
                return 0;
            }
            String password = StringArgumentType.getString(ctx,"password");
            if(AuthUtils.login(player,password)){
                AuthUtils.sendAuthMessage("§b登录成功！",player);
                return 1;
            }
            AuthUtils.sendAuthMessage("§4密码错误",player);
            return 0;
        }
        return 0;
    }


    private static int onRegisterCommand(CommandContext<CommandSourceStack> ctx) {
        if(ctx.getSource().getEntity() instanceof ServerPlayer player){
            if(AuthUtils.isRegistered(player)){
                AuthUtils.sendAuthMessage("§y你已经注册过了",player);
                return 0;
            }
            String password = StringArgumentType.getString(ctx,"password");
            String repeatPassword =  StringArgumentType.getString(ctx,"repeat_password");
            if(!password.equals(repeatPassword)){
                AuthUtils.sendAuthMessage("§4两次输入的密码不一致",player);
                return 0;
            }
            if(!password.matches(TinyAuthConfigHandler.PasswordRegex.get())){
                AuthUtils.sendAuthMessage(TinyAuthConfigHandler.PasswordRegexError.get(),player);
                return 0;
            }
            AuthUtils.register(player,password);
            AuthUtils.sendAuthMessage("§b注册成功！",player);
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
                AuthUtils.sendAuthMessage("§e你的账号已被管理员登出", targetPlayer);

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
                AuthUtils.sendAuthMessage("§e你的账号已被管理员注销", targetPlayer);
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
                AuthUtils.sendAuthMessage("§e你的密码已被管理员重置", targetPlayer);
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
                
                ctx.getSource().sendSuccess(new TextComponent("玩家 " + targetPlayer.getName().getString() + " 的登录历史:"), false);
                for (int i = 0; i < loginHistory.size(); i++) {
                    ctx.getSource().sendSuccess(new TextComponent((i+1) + ". " + loginHistory.get(i)), false);
                }
                
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
}
