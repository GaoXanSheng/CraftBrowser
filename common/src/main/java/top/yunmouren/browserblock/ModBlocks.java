package top.yunmouren.browserblock.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries; // 1.19.3+ 使用这个
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import top.yunmouren.browserblock.block.BrowserBlock;
import top.yunmouren.browserblock.block.BrowserBlockEntity;

import static top.yunmouren.craftbrowser.Craftbrowser.MOD_ID;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(MOD_ID, Registries.BLOCK);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, Registries.ITEM);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    // 2. 注册方块
    public static final RegistrySupplier<Block> BROWSER_BLOCK = BLOCKS.register("browser_block", () ->
            new BrowserBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f).noOcclusion()));

    public static final RegistrySupplier<Item> BROWSER_BLOCK_ITEM = ITEMS.register("browser_block", () ->
            new BlockItem(BROWSER_BLOCK.get(), new Item.Properties()));
    public static final RegistrySupplier<BlockEntityType<BrowserBlockEntity>> BROWSER_BLOCK_ENTITY = BLOCK_ENTITIES.register("browser_block_entity", () ->
            BlockEntityType.Builder.of(BrowserBlockEntity::new, BROWSER_BLOCK.get()).build(null));

    public static void register() {
        BLOCKS.register();
        ITEMS.register();
        BLOCK_ENTITIES.register();
    }
}