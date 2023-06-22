package me.santio.brandchanger.hooks;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.proxy.connection.backend.BackendPlaySessionHandler;
import com.velocitypowered.proxy.connection.backend.VelocityServerConnection;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.StateRegistry;
import io.netty.util.collection.IntObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class HooksInitializer {
    
    @SuppressWarnings("unchecked")
    public static void init(ProxyServer server) {
        try {
            PluginMessageHook.SERVER_CONNECTION_FIELD = MethodHandles
                .privateLookupIn(BackendPlaySessionHandler.class, MethodHandles.lookup())
                .findGetter(BackendPlaySessionHandler.class, "serverConn", VelocityServerConnection.class);
            
            MethodHandle versionsField = MethodHandles.privateLookupIn(StateRegistry.PacketRegistry.class, MethodHandles.lookup())
                .findGetter(StateRegistry.PacketRegistry.class, "versions", Map.class);
            
            MethodHandle packetIdToSupplierField = MethodHandles
                .privateLookupIn(StateRegistry.PacketRegistry.ProtocolRegistry.class, MethodHandles.lookup())
                .findGetter(StateRegistry.PacketRegistry.ProtocolRegistry.class, "packetIdToSupplier", IntObjectMap.class);
            
            MethodHandle packetClassToIdField = MethodHandles
                .privateLookupIn(StateRegistry.PacketRegistry.ProtocolRegistry.class, MethodHandles.lookup())
                .findGetter(StateRegistry.PacketRegistry.ProtocolRegistry.class, "packetClassToId", Object2IntMap.class);
            
            List<PacketHook> hooks = new ArrayList<>();
            hooks.add(new PluginMessageHook());
            
            BiConsumer<? super ProtocolVersion, ? super StateRegistry.PacketRegistry.ProtocolRegistry> consumer = (version, registry) -> {
                try {
                    IntObjectMap<Supplier<? extends MinecraftPacket>> packetIdToSupplier
                        = (IntObjectMap<Supplier<? extends MinecraftPacket>>) packetIdToSupplierField.invoke(registry);
                    
                    Object2IntMap<Class<? extends MinecraftPacket>> packetClassToId
                        = (Object2IntMap<Class<? extends MinecraftPacket>>) packetClassToIdField.invoke(registry);
                    
                    hooks.forEach(hook -> {
                        int packetId = packetClassToId.getInt(hook.getType());
                        packetClassToId.put(hook.getHookClass(), packetId);
                        packetIdToSupplier.remove(packetId);
                        packetIdToSupplier.put(packetId, hook.getHook());
                    });
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            };
            
            MethodHandle clientboundGetter = MethodHandles.privateLookupIn(StateRegistry.class, MethodHandles.lookup())
                .findGetter(StateRegistry.class, "clientbound", StateRegistry.PacketRegistry.class);
            
            MethodHandle serverboundGetter = MethodHandles.privateLookupIn(StateRegistry.class, MethodHandles.lookup())
                .findGetter(StateRegistry.class, "serverbound", StateRegistry.PacketRegistry.class);
            
            StateRegistry.PacketRegistry playClientbound = (StateRegistry.PacketRegistry) clientboundGetter.invokeExact(StateRegistry.PLAY);
            StateRegistry.PacketRegistry handshakeServerbound = (StateRegistry.PacketRegistry) serverboundGetter.invokeExact(StateRegistry.HANDSHAKE);
            
            ((Map<ProtocolVersion, StateRegistry.PacketRegistry.ProtocolRegistry>) versionsField.invokeExact(playClientbound)).forEach(consumer);
            ((Map<ProtocolVersion, StateRegistry.PacketRegistry.ProtocolRegistry>) versionsField.invokeExact(handshakeServerbound)).forEach(consumer);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}