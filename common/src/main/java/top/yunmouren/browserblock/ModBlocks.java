package top.yunmouren.browserblock;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import top.yunmouren.browserblock.block.*;

import static top.yunmouren.craftbrowser.Craftbrowser.MOD_ID;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(MOD_ID, Registries.BLOCK);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, Registries.ITEM);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<Block> BROWSER_MASTER_BLOCK = BLOCKS.register("browser_master", () ->
            new BrowserMasterBlock(BlockBehaviour.Properties.of().mapColor(MapColor.DIAMOND).strength(2.0f).noOcclusion()));
    public static final RegistrySupplier<Item> BROWSER_MASTER_ITEM = ITEMS.register("browser_master", () ->
            new BlockItem(BROWSER_MASTER_BLOCK.get(), new Item.Properties()));
    public static final RegistrySupplier<BlockEntityType<BrowserMasterBlockEntity>> BROWSER_MASTER_ENTITY = BLOCK_ENTITIES.register("browser_master_entity", () ->
            BlockEntityType.Builder.of(BrowserMasterBlockEntity::new, BROWSER_MASTER_BLOCK.get()).build(null));

    public static final RegistrySupplier<Block> BROWSER_NODE_BLOCK = BLOCKS.register("browser_node", () ->
            new BrowserNodeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f).noOcclusion()));
    public static final RegistrySupplier<Item> BROWSER_NODE_ITEM = ITEMS.register("browser_node", () ->
            new BlockItem(BROWSER_NODE_BLOCK.get(), new Item.Properties()));
    public static final RegistrySupplier<BlockEntityType<BrowserNodeBlockEntity>> BROWSER_NODE_ENTITY = BLOCK_ENTITIES.register("browser_node_entity", () ->
            BlockEntityType.Builder.of(BrowserNodeBlockEntity::new, BROWSER_NODE_BLOCK.get()).build(null));

    public static void register() {
        BLOCKS.register();
        ITEMS.register();
        BLOCK_ENTITIES.register();
    }
}