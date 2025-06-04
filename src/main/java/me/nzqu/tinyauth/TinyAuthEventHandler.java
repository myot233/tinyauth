package me.nzqu.tinyauth;

import me.nzqu.tinyauth.capabilities.AuthCapability;
import me.nzqu.tinyauth.utils.AuthUtils;
import static me.nzqu.tinyauth.utils.AuthUtils.getPlayerIP;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class TinyAuthEventHandler {
    @SubscribeEvent
    public static void onChunkTick(TickEvent.WorldTickEvent event) {
        event.world.getChunkSource().getLoadedChunksCount();

    }


    @SubscribeEvent
    public static void onPlayerJoin(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        TinyAuth.LOGGER.info("Player {} joined the game", event.getPlayer().getName().getString());
        if(event.getPlayer() instanceof ServerPlayer player){
            var auth = AuthUtils.getAuthCapability(player);
            if(auth.PlayerState == AuthCapability.AccountState.LOGIN)
                auth.setPlayerState(AuthCapability.AccountState.REGISTERED);
            auth.setPlayerX(player.getX());
            auth.setPlayerY(player.getY());
            auth.setPlayerZ(player.getZ());
            auth.PlayerGameMode = player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR
                    ? auth.PlayerGameMode
                    : player.gameMode.getGameModeForPlayer();
            player.setGameMode(GameType.SPECTATOR);
            String ip = getPlayerIP(player);
            auth.addLoginRecord(ip);
            if (!AuthUtils.isRegistered(player)) {
                AuthUtils.sendAuthMessage(TinyAuthConfigHandler.RegisterPrompt.get(), player);
            } else {
                AuthUtils.sendAuthMessage(TinyAuthConfigHandler.LoginPrompt.get(), player);
            }
        }
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        event.getOriginal().revive();
        AuthCapability original = event.getOriginal().getCapability(AuthCapability.AUTH_CAPABILITY, null).orElse(new AuthCapability());
        AuthCapability clone = event.getEntity().getCapability(AuthCapability.AUTH_CAPABILITY, null).orElse(new AuthCapability());
        clone.PlayerState = original.PlayerState;
        clone.PlayerPassword = original.PlayerPassword;
        clone.PlayerX = original.PlayerX;
        clone.PlayerY = original.PlayerY;
        clone.PlayerZ = original.PlayerZ;
    }


    @SubscribeEvent
    public static void onPlayerMove(LivingEvent.LivingUpdateEvent event) {
        if(event.getEntity() instanceof ServerPlayer player){
            AuthCapability authCapability =  AuthUtils.getAuthCapability(player);
            if(authCapability == null) return;
            if(authCapability.getPlayerState() != AuthCapability.AccountState.LOGIN){
                player.moveTo(authCapability.getPlayerX(),authCapability.getPlayerY(),authCapability.getPlayerZ());

            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        AuthCapability original = event.getPlayer().getCapability(AuthCapability.AUTH_CAPABILITY, null).orElse(new AuthCapability());
        original.PlayerX = event.getPlayer().getX();
        original.PlayerY = event.getPlayer().getY();
        original.PlayerZ = event.getPlayer().getZ();
    }

    @SubscribeEvent
    public static void onPlayerTickEvent(TickEvent.PlayerTickEvent event) {
        if(event.player instanceof ServerPlayer player){
            AuthCapability authCapability =  AuthUtils.getAuthCapability(player);
            if(authCapability == null) return;
            
            // 处理已登录玩家的延迟消息和命令
            if(authCapability.getPlayerState() == AuthCapability.AccountState.LOGIN){
                authCapability.loginDelayTick++;
                if(authCapability.loginDelayTick == TinyAuthConfigHandler.LoginDelayTicks.get()){
                    // 发送延迟消息
                    String delayMessage = TinyAuthConfigHandler.LoginDelayMessage.get();
                    if(!delayMessage.isEmpty()){
                        AuthUtils.sendAuthMessage(delayMessage, player);
                    }
                    
                    // 执行延迟命令
                    String delayCommand = TinyAuthConfigHandler.LoginDelayCommand.get();
                    if(!delayCommand.isEmpty()){
                        // 替换占位符
                        String command = delayCommand.replace("%player%", player.getName().getString());
                        // 执行命令
                        player.getServer().getCommands().performCommand(
                            player.getServer().createCommandSourceStack(), 
                            command
                        );
                    }
                }
                return; // 已登录玩家不需要执行下面的逻辑
            }
            
            // 处理未登录玩家
            if(authCapability.getPlayerState() != AuthCapability.AccountState.LOGIN){
                authCapability.msgTick++;
                authCapability.timeOutTick++;
                if(authCapability.msgTick % TinyAuthConfigHandler.TickPerMsg.get() == 0){
                    if(AuthUtils.isRegistered(player)){
                        AuthUtils.sendAuthMessage(TinyAuthConfigHandler.LoginPrompt.get(), player);
                    }else{
                        AuthUtils.sendAuthMessage(TinyAuthConfigHandler.RegisterPrompt.get(), player);
                    }
                }
                if(authCapability.timeOutTick > TinyAuthConfigHandler.TimeOutTick.get()){
                    player.connection.disconnect(new TextComponent(TinyAuthConfigHandler.TimeoutKickMessage.get()));
                }
            }
        }
    }




    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent event) {
        if(event.getPlayer() instanceof ServerPlayer player){
            AuthCapability authCapability =  AuthUtils.getAuthCapability(player);
            if(authCapability == null) return;
            if(authCapability.getPlayerState() != AuthCapability.AccountState.LOGIN){
                event.setCanceled(true);
            }
        }
    }


    @SubscribeEvent
    public static void onPlayerCommand(CommandEvent event) {
        if(event.getParseResults().getContext().getSource().getEntity() instanceof ServerPlayer player){
            AuthCapability authCapability =  AuthUtils.getAuthCapability(player);
            if(authCapability == null) return;
            if(
                    AuthUtils.checkCommand(event.getParseResults().getContext().getNodes(),"login")||
                    AuthUtils.checkCommand(event.getParseResults().getContext().getNodes(),"register")
            ) return;
            if(authCapability.getPlayerState() != AuthCapability.AccountState.LOGIN){
                AuthUtils.sendAuthMessage(TinyAuthConfigHandler.LoginPrompt.get(), player);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerChat(ServerChatEvent chatEvent){
        ServerPlayer player = chatEvent.getPlayer();
        AuthCapability authCapability =  AuthUtils.getAuthCapability(player);
        if(authCapability.getPlayerState() != AuthCapability.AccountState.LOGIN){
            AuthUtils.sendAuthMessage(TinyAuthConfigHandler.LoginPrompt.get(), player);
            chatEvent.setCanceled(true);
        }
    }

}
