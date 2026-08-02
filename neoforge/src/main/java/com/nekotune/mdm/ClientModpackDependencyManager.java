package com.nekotune.mdm;

import com.nekotune.mdm.client.ClientCommonClass;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class ClientModpackDependencyManager {
    
    public ClientModpackDependencyManager() {
        ClientCommonClass.init();
    }
}
