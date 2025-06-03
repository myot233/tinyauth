package me.nzqu.tinyauth.utils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import io.netty.channel.local.LocalAddress;
import me.nzqu.tinyauth.TinyAuth;
import me.nzqu.tinyauth.capabilities.AuthCapability;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.slf4j.LoggerFactory;

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
        String currentIP = getPlayerIP(player);
        
        // 检查每个IP最大玩家数限制（防多开）
        if (!checkIPPlayerLimit(player, currentIP)) {
            sendAuthMessage(me.nzqu.tinyauth.TinyAuthConfigHandler.IPPlayerLimitMessage.get(), player);
            TinyAuth.LOGGER.warn("IP player limit blocked registration for player {} from IP {} (too many players from this IP)", 
                player.getName().getString(), currentIP);
            return;
        }
        
        capability.setPlayerPassword(sha256(password));
        capability.setPlayerState(AuthCapability.AccountState.LOGIN);
        player.setGameMode(capability.PlayerGameMode);
        
        // 记录首次注册IP并添加到允许列表
        capability.addLoginRecord(currentIP);
        if (me.nzqu.tinyauth.TinyAuthConfigHandler.EnableIPRestriction.get()) {
            capability.addAllowedIP(currentIP);
        }
        
        // 重置延迟计时器，准备发送延迟消息
        capability.loginDelayTick = 0;
        
        TinyAuth.LOGGER.info("Player {} registered successfully from IP {}", 
            player.getName().getString(), currentIP);
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
            // 显示大屏幕登出提示
            String titleText = me.nzqu.tinyauth.TinyAuthConfigHandler.LogoutTitleText.get();
            String subtitleText = me.nzqu.tinyauth.TinyAuthConfigHandler.LogoutSubtitleText.get();
            showBigTitle(titleText, subtitleText, player);
            
            // 保存当前游戏模式
            capability.PlayerGameMode = player.gameMode.getGameModeForPlayer();
            capability.msgTick = 0;
            capability.timeOutTick = 0;
            capability.setPlayerX(player.getX());
            capability.setPlayerY(player.getY());
            capability.setPlayerZ(player.getZ());
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
            // 显示大屏幕账号注销提示
            String titleText = me.nzqu.tinyauth.TinyAuthConfigHandler.RemoveTitleText.get();
            String subtitleText = me.nzqu.tinyauth.TinyAuthConfigHandler.RemoveSubtitleText.get();
            showBigTitle(titleText, subtitleText, player);
            
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
            capability.allowedIPs.clear();
            // 清除登录历史
            //capability.getLoginHistory().clear();
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
     * 获取玩家的IP地址，支持IPv6
     * @param player 目标玩家
     * @return IP地址字符串，完整保留IPv6格式
     */
    public static String getPlayerIP(ServerPlayer player) {
        SocketAddress ipAddr = player.connection.getConnection().getRemoteAddress();
        String ip;
        if(ipAddr instanceof InetSocketAddress inetSocketAddress){
            ip = inetSocketAddress.getAddress().getHostAddress();
        }else if (ipAddr instanceof LocalAddress){
            ip = "host";
        }else{
            ip = "unknown";
        }
//        LoggerFactory.getLogger("ipaddr").warn(ip);
        if (ip != null) {
//            // 处理IPv6格式 [IPv6]:port
//            if (ip.startsWith("[") && ip.contains("]:")) {
//                int bracketEnd = ip.indexOf("]:");
//                if (bracketEnd > 0) {
//                    return ip.substring(1, bracketEnd); // 移除方括号和端口，保留完整IPv6地址
//                }
//            }
//            // 处理IPv4格式 IPv4:port
//            else if (ip.contains(":") && !ip.contains("::")) {
//                // 检查是否是IPv4:port格式（IPv4不包含::）
//                int lastColon = ip.lastIndexOf(':');
//                if (lastColon > 0) {
//                    String portPart = ip.substring(lastColon + 1);
//                    // 如果冒号后面是纯数字，说明是端口号
//                    if (portPart.matches("\\d+")) {
//                        return ip.substring(0, lastColon);
//                    }
//                }
//            }
            // 如果是纯IPv6地址（没有端口）或者其他格式，直接返回
            return ip;
        }
        return "unknown";
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
        String currentIP = getPlayerIP(player);
        
        // 检查每个IP最大玩家数限制（防多开）
        if (!checkIPPlayerLimit(player, currentIP)) {
            sendAuthMessage(me.nzqu.tinyauth.TinyAuthConfigHandler.IPPlayerLimitMessage.get(), player);
            TinyAuth.LOGGER.warn("IP player limit blocked login attempt for player {} from IP {} (too many players from this IP)", 
                player.getName().getString(), currentIP);
            return false;
        }
        
        // 检查IP是否被允许
        if (!isIPAllowed(player, currentIP)) {
            // 尝试添加新IP（如果还有空间）
            if (!tryAddAllowedIP(player, currentIP)) {
                sendAuthMessage(me.nzqu.tinyauth.TinyAuthConfigHandler.IPRestrictedMessage.get(), player);
                TinyAuth.LOGGER.warn("IP restriction blocked login attempt for player {} from IP {}", 
                    player.getName().getString(), currentIP);
                return false;
            }
        }
        
        if(checkPwd(player, password)){
            capability.setPlayerState(AuthCapability.AccountState.LOGIN);
            player.setGameMode(capability.PlayerGameMode);
            
            // 记录登录IP
            capability.addLoginRecord(currentIP);
            
            // 重置延迟计时器，准备发送延迟消息
            capability.loginDelayTick = 0;
            
            TinyAuth.LOGGER.info("Player {} logged in successfully from IP {}", 
                player.getName().getString(), currentIP);
            return true;
        }else{
            TinyAuth.LOGGER.info("Failed login attempt for player {} from IP {}", 
                player.getName().getString(), currentIP);
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

    /**
     * 向玩家显示大屏幕标题提示
     * @param title 主标题
     * @param subtitle 副标题
     * @param player 目标玩家
     */
    public static void showBigTitle(String title, String subtitle, ServerPlayer player) {
        // 设置标题动画时间：淡入20刻，停留60刻，淡出20刻
        ClientboundSetTitlesAnimationPacket animationPacket = new ClientboundSetTitlesAnimationPacket(20, 60, 20);
        player.connection.send(animationPacket);
        
        // 发送主标题
        if (title != null && !title.isEmpty()) {
            ClientboundSetTitleTextPacket titlePacket = new ClientboundSetTitleTextPacket(new TextComponent(title));
            player.connection.send(titlePacket);
        }
        
        // 发送副标题
        if (subtitle != null && !subtitle.isEmpty()) {
            ClientboundSetSubtitleTextPacket subtitlePacket = new ClientboundSetSubtitleTextPacket(new TextComponent(subtitle));
            player.connection.send(subtitlePacket);
        }
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

    /**
     * 检查IP是否被允许登录
     * @param player 目标玩家
     * @param ip 要检查的IP地址
     * @return 如果允许登录返回true
     */
    public static boolean isIPAllowed(ServerPlayer player, String ip) {
        if (!me.nzqu.tinyauth.TinyAuthConfigHandler.EnableIPRestriction.get()) {
            return true; // 如果未启用IP限制，则允许所有IP
        }
        
        AuthCapability capability = getAuthCapability(player);
        if (capability == null) {
            return false;
        }
        
        // 如果是第一次登录（没有任何允许的IP），则自动添加当前IP
        if (capability.getAllowedIPs().isEmpty()) {
            capability.addAllowedIP(ip);
            return true;
        }
        
        return capability.isIPAllowed(ip);
    }

    /**
     * 尝试添加新的IP到允许列表
     * @param player 目标玩家
     * @param ip 要添加的IP地址
     * @return 如果成功添加返回true
     */
    public static boolean tryAddAllowedIP(ServerPlayer player, String ip) {
        if (!me.nzqu.tinyauth.TinyAuthConfigHandler.EnableIPRestriction.get()) {
            return true;
        }
        
        AuthCapability capability = getAuthCapability(player);
        if (capability == null) {
            return false;
        }
        
        if (capability.canAddNewIP()) {
            return capability.addAllowedIP(ip);
        }
        
        return false;
    }

    /**
     * 获取玩家允许的IP列表
     * @param player 目标玩家
     * @return IP列表
     */
    public static List<String> getAllowedIPs(ServerPlayer player) {
        AuthCapability capability = getAuthCapability(player);
        if (capability != null) {
            return capability.getAllowedIPs();
        }
        return List.of();
    }

    /**
     * 移除允许的IP
     * @param player 目标玩家
     * @param ip 要移除的IP
     * @return 如果成功移除返回true
     */
    public static boolean removeAllowedIP(ServerPlayer player, String ip) {
        AuthCapability capability = getAuthCapability(player);
        if (capability != null) {
            return capability.removeAllowedIP(ip);
        }
        return false;
    }

    /**
     * 清空玩家的允许IP列表
     * @param player 目标玩家
     */
    public static void clearAllowedIPs(ServerPlayer player) {
        AuthCapability capability = getAuthCapability(player);
        if (capability != null) {
            capability.clearAllowedIPs();
        }
    }

    /**
     * 检查IP地址的玩家数量限制（防多开）
     * @param currentPlayer 当前尝试登录的玩家
     * @param ip 要检查的IP地址
     * @return 如果允许登录返回true
     */
    public static boolean checkIPPlayerLimit(ServerPlayer currentPlayer, String ip) {
        if (!me.nzqu.tinyauth.TinyAuthConfigHandler.EnableIPPlayerLimit.get()) {
            return true; // 如果未启用IP玩家数限制，则允许
        }
        
        int maxPlayersPerIP = me.nzqu.tinyauth.TinyAuthConfigHandler.MaxPlayersPerIP.get();
        int currentLoggedInPlayers = 0;
        
        // 遍历服务器上的所有玩家，统计来自同一IP且已登录的玩家数量
        for (ServerPlayer player : currentPlayer.getServer().getPlayerList().getPlayers()) {
            // 跳过当前玩家自己
            if (player.equals(currentPlayer)) {
                continue;
            }
            
            // 检查玩家是否已登录且来自同一IP
            if (isLoggedIn(player) && ip.equals(getPlayerIP(player))) {
                currentLoggedInPlayers++;
            }
        }
        
        // 检查是否超过限制
        return currentLoggedInPlayers < maxPlayersPerIP;
    }

    /**
     * 获取指定IP地址当前已登录的玩家数量
     * @param ip IP地址
     * @param server 服务器实例
     * @return 已登录的玩家数量
     */
    public static int getLoggedInPlayersFromIP(String ip, net.minecraft.server.MinecraftServer server) {
        int count = 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (isLoggedIn(player) && ip.equals(getPlayerIP(player))) {
                count++;
            }
        }
        return count;
    }
}
