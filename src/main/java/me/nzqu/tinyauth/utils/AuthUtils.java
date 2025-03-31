package me.nzqu.tinyauth.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.nzqu.tinyauth.TinyAuth;
import me.nzqu.tinyauth.capabilities.AuthCapability;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public class AuthUtils {
    public static AuthCapability getAuthCapability(ServerPlayer player){
        return player.getCapability(AuthCapability.AUTH_CAPABILITY).orElse(null);
    }

    public static boolean isRegistered(ServerPlayer player){
        AuthCapability capability = getAuthCapability(player);
        return capability != null && capability.PlayerState != AuthCapability.AccountState.UNREGISTERED;
    }
    
    public static boolean isLoggedIn(ServerPlayer player){
        AuthCapability capability = getAuthCapability(player);
        return capability != null && capability.PlayerState == AuthCapability.AccountState.LOGIN;
    }

    public static void register(ServerPlayer player, String password){
        AuthCapability capability = getAuthCapability(player);
        Objects.requireNonNull(capability);
        capability.setPlayerPassword(sha256(password));
        capability.setPlayerState(AuthCapability.AccountState.LOGIN);
        player.setGameMode(capability.PlayerGameMode);
        
        // 记录首次登录IP
        capability.addLoginRecord(getPlayerIP(player));
        
        TinyAuth.LOGGER.info("Player {} registered successfully from IP {}", 
            player.getName().getString(), getPlayerIP(player));
    }

    public static void changePassword(ServerPlayer player, String password){
        AuthCapability capability = getAuthCapability(player);
        Objects.requireNonNull(capability);
        capability.setPlayerPassword(sha256(password));
        TinyAuth.LOGGER.info("Password changed for player {}", player.getName().getString());
    }
    
    public static void logout(ServerPlayer player){
        AuthCapability capability = getAuthCapability(player);
        if(capability != null && capability.PlayerState == AuthCapability.AccountState.LOGIN) {
            // 保存当前游戏模式
            capability.PlayerGameMode = player.gameMode.getGameModeForPlayer();
            capability.msgTick = 0;
            capability.timeOutTick = 0;
            // 切换为观察者模式
            player.setGameMode(GameType.SPECTATOR);
            // 修改状态为已注册但未登录
            capability.setPlayerState(AuthCapability.AccountState.REGISTERED);
            TinyAuth.LOGGER.info("Player {} logged out", player.getName().getString());
        }
    }
    
    public static void removeAccount(ServerPlayer player){
        AuthCapability capability = getAuthCapability(player);
        if(capability != null) {
            // 保存当前游戏模式
            if(isLoggedIn(player)) {
                capability.PlayerGameMode = player.gameMode.getGameModeForPlayer();
            }
            // 切换为观察者模式
            player.setGameMode(GameType.SPECTATOR);
            // 修改状态为未注册
            capability.setPlayerState(AuthCapability.AccountState.UNREGISTERED);
            // 清除密码
            capability.setPlayerPassword("");
            capability.msgTick = 0;
            capability.timeOutTick = 0;
            // 清除登录历史
            capability.getLoginHistory().clear();
            TinyAuth.LOGGER.info("Account removed for player {}", player.getName().getString());
        }
    }
    
    /**
     * 获取玩家登录历史
     * @param player 目标玩家
     * @return 登录历史列表，如果没有返回空列表
     */
    public static List<String> getLoginHistory(ServerPlayer player) {
        AuthCapability capability = getAuthCapability(player);
        if (capability != null) {
            return capability.getLoginHistory();
        }
        return List.of(); // 返回空列表
    }
    
    /**
     * 获取玩家的IP地址
     * @param player 目标玩家
     * @return IP地址字符串
     */
    public static String getPlayerIP(ServerPlayer player) {
        String ip = player.getIpAddress();
        return ip != null ? ip : "unknown";
    }

    private static String sha256(String password){
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static boolean checkCommand(List<ParsedCommandNode<CommandSourceStack>> parsedCommandNode, String command){
        AtomicBoolean result = new AtomicBoolean(false);
       var first =  parsedCommandNode.stream().findFirst();
        first.ifPresent(parsedCommandNode1 -> {
            if(parsedCommandNode1.getNode() instanceof LiteralCommandNode literalCommandNode) {
                result.set(literalCommandNode.getLiteral().equals(command));
            }
        });
        return result.get();
    }

    public static boolean login(ServerPlayer player, String password){
        AuthCapability capability = getAuthCapability(player);
        if(checkPwd(player, password)){
            capability.setPlayerState(AuthCapability.AccountState.LOGIN);
            player.setGameMode(capability.PlayerGameMode);

            return true;
        }else{
            TinyAuth.LOGGER.info("Failed login attempt for player {} from IP {}", 
                player.getName().getString(), getPlayerIP(player));
            return false;
        }
    }

    public static boolean checkPwd(ServerPlayer player, String password) {
        AuthCapability capability = getAuthCapability(player);
        return capability != null && capability.getPlayerPassword().equals(sha256(password));
    }
    
    public static void sendAuthMessage(String message, ServerPlayer player){
        player.displayClientMessage(new TextComponent("§d[登录系统] " + message), false);
    }

    // 为新玩家设置初始状态
    public static void setupNewPlayer(ServerPlayer player) {
        AuthCapability capability = getAuthCapability(player);
        if(capability != null) {
            // 保存玩家当前游戏模式
            capability.PlayerGameMode = player.gameMode.getGameModeForPlayer();
            // 设置为观察者模式
            player.setGameMode(GameType.SPECTATOR);
        }
    }
}
