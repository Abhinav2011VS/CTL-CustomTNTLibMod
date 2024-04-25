package net.abhinav.ctl.ctl.forge;

import net.abhinav.ctl.ctl.CustomTNTLib;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CustomTNTLib.MOD_ID)
public class CustomTNTLibForge {
    public CustomTNTLibForge() {
        CustomTNTLib.init();
    }
}