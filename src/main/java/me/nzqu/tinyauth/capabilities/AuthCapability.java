package me.nzqu.tinyauth.capabilities;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class AuthCapability {
    public static final Capability<AuthCapability> AUTH_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final int MAX_LOGIN_HISTORY = 10; // 最多保存10条登录记录

    public AccountState getPlayerState() {
        return PlayerState;
    }

    public void setPlayerState(AccountState playerState) {
        PlayerState = playerState;
    }

    public String getPlayerPassword() {
        return PlayerPassword;
    }

    public void setPlayerPassword(String playerPassword) {
        PlayerPassword = playerPassword;
    }

    public double getPlayerX() {
        return PlayerX;
    }

    public void setPlayerX(double playerX) {
        PlayerX = playerX;
    }

    public double getPlayerY() {
        return PlayerY;
    }

    public void setPlayerY(double playerY) {
        PlayerY = playerY;
    }

    public double getPlayerZ() {
        return PlayerZ;
    }

    public void setPlayerZ(double playerZ) {
        PlayerZ = playerZ;
    }
    
    public List<String> getLoginHistory() {
        return loginHistory;
    }
    
    public void addLoginRecord(String ip) {
        String record = DATE_FORMAT.format(new Date()) + " - " + ip;
        loginHistory.add(record);
        
        // 保持历史记录不超过最大数量
        if (loginHistory.size() > MAX_LOGIN_HISTORY) {
            loginHistory.remove(0); // 移除最旧的记录
        }
    }

    @Override
    public String toString() {
        return "AuthCapability{" +
                "PlayerPassword='" + PlayerPassword + '\'' +
                ", PlayerState=" + PlayerState +
                ", PlayerX=" + PlayerX +
                ", PlayerY=" + PlayerY +
                ", PlayerZ=" + PlayerZ +
                '}';
    }

    public enum AccountState {
        UNREGISTERED,
        REGISTERED,
        LOGIN;
    }
    public String PlayerPassword = "";
    public AccountState PlayerState = AccountState.UNREGISTERED;
    public double PlayerX = 0.0d;
    public double PlayerY = 0.0d;
    public double PlayerZ = 0.0d;
    public int msgTick = 0;
    public int timeOutTick = 0;
    public GameType PlayerGameMode = GameType.SURVIVAL;
    public List<String> loginHistory = new ArrayList<>();
    public List<String> allowedIPs = new ArrayList<>(); // 允许登录的IP列表
    public int loginDelayTick = 0; // 登录成功后的延迟计时器

    /**
     * 检查IP是否被允许登录
     * @param ip 要检查的IP地址
     * @return 如果IP被允许则返回true
     */
    public boolean isIPAllowed(String ip) {
        return allowedIPs.contains(ip);
    }

    /**
     * 添加允许的IP地址
     * @param ip 要添加的IP地址
     * @return 如果成功添加返回true，如果已存在返回false
     */
    public boolean addAllowedIP(String ip) {
        if (!allowedIPs.contains(ip)) {
            allowedIPs.add(ip);
            return true;
        }
        return false;
    }

    /**
     * 移除允许的IP地址
     * @param ip 要移除的IP地址
     * @return 如果成功移除返回true
     */
    public boolean removeAllowedIP(String ip) {
        return allowedIPs.remove(ip);
    }

    /**
     * 获取允许的IP列表
     * @return IP列表的副本
     */
    public List<String> getAllowedIPs() {
        return new ArrayList<>(allowedIPs);
    }

    /**
     * 清空允许的IP列表
     */
    public void clearAllowedIPs() {
        allowedIPs.clear();
    }

    /**
     * 检查是否可以添加新的IP（基于配置的最大IP数量限制）
     * @return 如果可以添加返回true
     */
    public boolean canAddNewIP() {
        return allowedIPs.size() < me.nzqu.tinyauth.TinyAuthConfigHandler.MaxAllowedIPs.get();
    }

    public Tag writeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("PlayerPassword", PlayerPassword);
        tag.putInt("PlayerState", PlayerState.ordinal());
        tag.putDouble("PlayerX", PlayerX);
        tag.putDouble("PlayerY", PlayerY);
        tag.putDouble("PlayerZ", PlayerZ);
        tag.putInt("PlayerGameMode", PlayerGameMode.ordinal());
        
        // 保存登录历史
        ListTag historyTag = new ListTag();
        for (String record : loginHistory) {
            historyTag.add(StringTag.valueOf(record));
        }
        tag.put("LoginHistory", historyTag);
        
        // 保存允许的IP列表
        ListTag allowedIPsTag = new ListTag();
        for (String ip : allowedIPs) {
            allowedIPsTag.add(StringTag.valueOf(ip));
        }
        tag.put("AllowedIPs", allowedIPsTag);
        
        return tag;
    }
    
    public void readNBT(Tag tag) {
        if (tag instanceof CompoundTag compoundTag) {
            PlayerPassword = compoundTag.getString("PlayerPassword");
            PlayerState = AccountState.values()[compoundTag.getInt("PlayerState")];
            PlayerX = compoundTag.getDouble("PlayerX");
            PlayerY = compoundTag.getDouble("PlayerY");
            PlayerZ = compoundTag.getDouble("PlayerZ");
            PlayerGameMode = GameType.values()[compoundTag.getInt("PlayerGameMode")];
            
            // 读取登录历史
            if (compoundTag.contains("LoginHistory")) {
                ListTag historyTag = compoundTag.getList("LoginHistory", 8); // 8 是字符串标签的类型
                loginHistory.clear();
                for (int i = 0; i < historyTag.size(); i++) {
                    loginHistory.add(historyTag.getString(i));
                }
            }
            
            // 读取允许的IP列表
            if (compoundTag.contains("AllowedIPs")) {
                ListTag allowedIPsTag = compoundTag.getList("AllowedIPs", 8); // 8 是字符串标签的类型
                allowedIPs.clear();
                for (int i = 0; i < allowedIPsTag.size(); i++) {
                    allowedIPs.add(allowedIPsTag.getString(i));
                }
            }
        }
    }


    public static class AuthCapabilityProvider implements ICapabilitySerializable<Tag> {
        private final  AuthCapability playerVariables = new  AuthCapability();
        private final LazyOptional<AuthCapability> instance = LazyOptional.of(() -> this.playerVariables);


        @NotNull
        @Override
        public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return cap == AuthCapability.AUTH_CAPABILITY ? instance.cast() : LazyOptional.empty();

        }

        @Override
        public Tag serializeNBT() {
           return this.playerVariables.writeNBT();
        }

        @Override
        public void deserializeNBT(Tag nbt) {
            this.playerVariables.readNBT(nbt);
        }
    }

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.register(AuthCapability.class);
    }

    @SubscribeEvent
    public static void attachCapability(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof ServerPlayer) {
            event.addCapability(ResourceLocation.fromNamespaceAndPath("tinyauth", "auth_capability"), new AuthCapabilityProvider());
        }
    }
}
