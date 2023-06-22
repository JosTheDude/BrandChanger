package me.santio.brandchanger;

import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class BranderCommand implements SimpleCommand {
    
    @Override
    public void execute(Invocation invocation) {
        if (invocation.arguments().length == 0) {
            invocation.source().sendMessage(
                Component.text("Usage: /brander reload")
                    .color(NamedTextColor.RED)
            );

            return;
        }
        
        if (invocation.arguments()[0].equalsIgnoreCase("reload")) {
            BrandChanger.loadConfig();
            invocation.source().sendMessage(
                Component.text("Config reloaded!")
                    .color(NamedTextColor.GREEN)
            );
        } else {
            invocation.source().sendMessage(
                Component.text("Usage: /brander reload")
                    .color(NamedTextColor.RED)
            );
        }
    }
    
}
