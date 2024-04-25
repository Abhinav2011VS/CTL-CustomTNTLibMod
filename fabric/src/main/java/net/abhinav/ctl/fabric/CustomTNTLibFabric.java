package net.abhinav.ctl.ctl.fabric;

import net.abhinav.ctl.ctl.CustomTNTLib;
import net.fabricmc.api.ModInitializer;

public class CustomTNTLibFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        CustomTNTLib.init();
    }
}