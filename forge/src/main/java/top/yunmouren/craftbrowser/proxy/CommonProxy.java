package top.yunmouren.craftbrowser.proxy;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import top.yunmouren.craftbrowser.Craftbrowser;
import top.yunmouren.craftbrowser.command.ForgeCommand;
import top.yunmouren.httpserver.HttpNetworkHandler;


public class CommonProxy implements IProxy {
    @Override
    public void init() {
        // 生命周期事件（mod 阶段）
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }
    protected void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        ForgeCommand.register(dispatcher);
    }
    protected void onCommonSetup(final FMLCommonSetupEvent event) {
        Craftbrowser.LOGGER.info("Common setup: register packets");
        HttpNetworkHandler.registerC2SReceivers();
    }
}

