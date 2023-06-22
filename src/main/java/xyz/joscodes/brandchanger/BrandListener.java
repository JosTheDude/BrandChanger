package me.santio.brandchanger;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.ConnectionHandshakeEvent;
import com.velocitypowered.api.proxy.LoginPhaseConnection;


public class BrandListener {

    @Subscribe
    public void onPlayerBrandEvent(ConnectionHandshakeEvent event) {
        LoginPhaseConnection connection = (LoginPhaseConnection) event.getConnection();
        BrandChanger.send(connection);
    }
    
}
