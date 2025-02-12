package net.cubecraft.internal;

import me.gb2022.commons.event.EventHandler;
import me.gb2022.commons.event.SubscribedEvent;
import net.cubecraft.ContentRegistries;
import net.cubecraft.CoreRegistries;
import net.cubecraft.SharedContext;
import net.cubecraft.Side;
import net.cubecraft.event.mod.ModConstructEvent;
import net.cubecraft.internal.auth.OfflineSessionService;
import net.cubecraft.internal.block.BlockBehaviorRegistry;
import net.cubecraft.internal.entity.EntityRegistry;
import net.cubecraft.internal.inventory.InventoryRegistry;
import net.cubecraft.internal.item.BlockItem;
import net.cubecraft.internal.item.ItemRegistry;
import net.cubecraft.mod.CubecraftMod;
import net.cubecraft.world.block.blocks.BlockRegistry;
import net.cubecraft.world.block.blocks.Blocks;
import net.cubecraft.world.environment.DimensionRegistry;
import net.cubecraft.world.environment.Environments;
import net.cubecraft.world.item.behavior.ItemBehaviorRegistry;
import net.cubecraft.world.worldGen.pipeline.pipelines.WorldGeneratorPipelineRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@CubecraftMod(side = Side.SHARED)
public class CubecraftCoreInternalMod {
    public static final String MOD_ID = "cubecraft_core";
    public static final Logger LOGGER = LogManager.getLogger("CoreInternalMod");

    @EventHandler
    @SubscribedEvent(MOD_ID)
    public static void onModConstruct(ModConstructEvent event) {
        Blocks.REGISTRY.handle(Blocks.class);
        Blocks.REGISTRY.withShadow((reg) -> CoreRegistries.ITEMS.register(reg.getName(), new BlockItem(reg.getName())));

        Environments.REGISTRY.handle(Environments.class);


        ContentRegistries.ENTITY.registerGetFunctionProvider(EntityRegistry.class);
        ContentRegistries.INVENTORY.registerGetFunctionProvider(InventoryRegistry.class);
        //ContentRegistries.BIOME.registerFieldHolder(BiomesRegistry.class);
        //SharedContext.PACKET.registerGetFunctionProvider(PacketRegistry.class);
        SharedContext.SESSION_SERVICE.registerItem("cubecraft:default", new OfflineSessionService());


        ContentRegistries.DIMENSION.registerFieldHolder(DimensionRegistry.class);
        ContentRegistries.CHUNK_GENERATE_PIPELINE.registerFieldHolder(WorldGeneratorPipelineRegistry.class);
        ContentRegistries.ITEM_BEHAVIOR.registerFieldHolder(ItemBehaviorRegistry.class);
        ContentRegistries.ITEM.registerFieldHolder(ItemRegistry.class);

        ContentRegistries.BLOCK_BEHAVIOR.registerFieldHolder(BlockBehaviorRegistry.class);
        ContentRegistries.BLOCK.registerFieldHolder(BlockRegistry.class);
    }
}
