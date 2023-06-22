package me.santio.brandchanger;

import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.LoginPhaseConnection;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import me.santio.brandchanger.hooks.HooksInitializer;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

@Plugin(
    id = "brandchanger",
    name = "BrandChanger",
    version = "1.0.0"
)
public class BrandChanger {
    
    private static File dataFolder;
    private static Logger logger;
    private static ProxyServer proxy;
    public static Toml config;
    
    @Inject
    public BrandChanger(ProxyServer proxy, Logger logger, @DataDirectory Path config) {
        BrandChanger.logger = logger;
        BrandChanger.proxy = proxy;
        dataFolder = config.toFile();
        if (!dataFolder.exists()) dataFolder.mkdirs();
        
        proxy.getChannelRegistrar().register(MinecraftChannelIdentifier.forDefaultNamespace("brand"));
        loadConfig();
    }
    
    // Register command
    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        HooksInitializer.init(proxy);
        
        CommandManager commandManager = proxy.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("brander")
            .plugin(this)
            .build();
        commandManager.register(commandMeta, new BranderCommand());
    }
    
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void createConfig() {
        File file = new File(dataFolder, "config.toml");
        if (!file.exists()) {
            try {
                file.createNewFile();
    
                logger.info("Creating config.toml");
                InputStream in = BrandChanger.class.getClassLoader().getResourceAsStream("config.toml");
                if (in != null) FileUtils.copyInputStreamToFile(in, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void loadConfig() {
        createConfig();
        
        File file = new File(dataFolder, "config.toml");
        config = new Toml().read(file);
    }
    
    public static void send(LoginPhaseConnection player) {
        ChannelIdentifier brand = MinecraftChannelIdentifier.forDefaultNamespace("brand");
        LoginPhaseConnection.MessageConsumer consumer = responseBody -> {
        };
        player.sendLoginPluginMessage(brand, config.getString("brand", "Hello World").getBytes(), consumer);
    }
}
