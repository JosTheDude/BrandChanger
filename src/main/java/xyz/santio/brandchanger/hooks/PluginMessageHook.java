package me.santio.brandchanger.hooks;


import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.connection.backend.BackendPlaySessionHandler;
import com.velocitypowered.proxy.connection.backend.VelocityServerConnection;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.packet.PluginMessage;
import com.velocitypowered.proxy.protocol.util.PluginMessageUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.santio.brandchanger.BrandChanger;

import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.function.Supplier;

class PluginMessageHook extends PluginMessage implements PacketHook {
    
    protected static MethodHandle SERVER_CONNECTION_FIELD;
    
    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        if (handler instanceof BackendPlaySessionHandler && PluginMessageUtil.isMcBrand(this)) {
            try {
                ConnectedPlayer player = ((VelocityServerConnection) SERVER_CONNECTION_FIELD.invoke(handler)).getPlayer();
                player.getConnection().write(this.rewriteMinecraftBrand(this, player.getProtocolVersion()));
                return true;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        
        return super.handle(handler);
    }
    
    private PluginMessage rewriteMinecraftBrand(PluginMessage message, ProtocolVersion protocolVersion) {
        String currentBrand = PluginMessageUtil.readBrandMessage(message.content());
        String rewrittenBrand = MessageFormat.format(BrandChanger.config.getString("brand", "Hello World"), currentBrand);
        ByteBuf rewrittenBuf = Unpooled.buffer();
        if (protocolVersion.compareTo(ProtocolVersion.MINECRAFT_1_8) >= 0) {
            ProtocolUtils.writeString(rewrittenBuf, rewrittenBrand);
        } else {
            rewrittenBuf.writeCharSequence(rewrittenBrand, StandardCharsets.UTF_8);
        }
        
        return new PluginMessage(message.getChannel(), rewrittenBuf);
    }
    
    @Override
    public Supplier<MinecraftPacket> getHook() {
        return PluginMessageHook::new;
    }
    
    @Override
    public Class<? extends MinecraftPacket> getType() {
        return PluginMessage.class;
    }
    
    @Override
    public Class<? extends MinecraftPacket> getHookClass() {
        return this.getClass();
    }
}
