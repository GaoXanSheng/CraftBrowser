package top.yunmouren.craftbrowser.proxy;

import com.mojang.brigadier.CommandDispatcher;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem; // 必须导入
import net.minecraft.world.item.CreativeModeTabs; // 必须导入
import net.minecraft.world.item.Item; // 必须导入
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent; // 必须导入
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import top.yunmouren.browserblock.block.BrowserBlock;
import top.yunmouren.browserblock.block.BrowserBlockEntity;
import top.yunmouren.browserblock.network.NetworkHandler;
import top.yunmouren.craftbrowser.Craftbrowser;
import top.yunmouren.craftbrowser.command.ForgeCommand;
import top.yunmouren.craftbrowser.server.network.BrowserNetworkHandler;
import top.yunmouren.httpserver.HttpNetworkHandler;

import static top.yunmouren.craftbrowser.Craftbrowser.MOD_ID;

public class CommonProxy {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(MOD_ID, Registries.BLOCK);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<Block> BROWSER_BLOCK = BLOCKS.register("browser_block", () ->
            new BrowserBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f).noOcclusion()));

    public static final RegistrySupplier<Item> BROWSER_BLOCK_ITEM = ITEMS.register("browser_block", () ->
            new BlockItem(BROWSER_BLOCK.get(), new Item.Properties()));

    public static final RegistrySupplier<BlockEntityType<BrowserBlockEntity>> BROWSER_BLOCK_ENTITY = BLOCK_ENTITIES.register("browser_block_entity", () ->
            BlockEntityType.Builder.of(BrowserBlockEntity::new, BROWSER_BLOCK.get()).build(null));

    public void init() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 3. 执行注册
        BLOCKS.register();
        BLOCK_ENTITIES.register();

        // 【NEW】新增：注册物品
        ITEMS.register();

        // 注册事件
        modEventBus.addListener(this::onCommonSetup);

        // 【NEW】新增：注册“添加到创造模式物品栏”的事件
        modEventBus.addListener(this::addCreative);

        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(BROWSER_BLOCK_ITEM);
        }
    }

    protected void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        ForgeCommand.register(dispatcher);
    }

    protected void onCommonSetup(final FMLCommonSetupEvent event) {
        Craftbrowser.LOGGER.info("Common setup: register packets");
        HttpNetworkHandler.registerC2SReceivers();

        NetworkHandler.register();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            HttpNetworkHandler.registerS2CReceivers();
            BrowserNetworkHandler.getInstance().registerClientReceiver();
        });
    }
}