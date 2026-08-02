package com.nekotune.mdm;

import com.nekotune.mdm.client.ClientCommonClass;

import net.fabricmc.api.ClientModInitializer;

public class ClientModpackDependencyManager implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        ClientCommonClass.init();
    }
}
