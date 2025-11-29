package top.yunmouren.craftbrowser.proxy;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
// 必须导入
// 必须导入
// 必须导入
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
// 必须导入
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import top.yunmouren.browserblock.network.BrowserBlockNetworkHandler;
import top.yunmouren.craftbrowser.Craftbrowser;
import top.yunmouren.craftbrowser.command.ForgeCommand;
import top.yunmouren.craftbrowser.server.network.BrowserNetworkHandler;
import top.yunmouren.httpserver.HttpNetworkHandler;

public class CommonProxy {
    public void init() {
        top.yunmouren.browserblock.registry.ModBlocks.register();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::onCommonSetup);
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }


    protected void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        ForgeCommand.register(dispatcher);
    }

    protected void onCommonSetup(final FMLCommonSetupEvent event) {
        Craftbrowser.LOGGER.info("Common setup: register packets");
        HttpNetworkHandler.registerC2SReceivers();
        BrowserBlockNetworkHandler.registerC2SReceivers();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            HttpNetworkHandler.registerS2CReceivers();
            BrowserNetworkHandler.getInstance().registerClientReceiver();
        });
    }
}