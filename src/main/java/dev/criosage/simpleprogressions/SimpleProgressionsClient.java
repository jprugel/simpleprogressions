package dev.criosage.simpleprogressions;

import dev.criosage.simpleprogressions.block.renderer.CopperChestRenderer;
import dev.criosage.simpleprogressions.block.renderer.IronChestRenderer;
import dev.criosage.simpleprogressions.item.renderer.CopperChestItemRenderer;
import dev.criosage.simpleprogressions.item.renderer.IronChestItemRenderer;
import dev.criosage.simpleprogressions.screenhandler.CopperChestScreen;
import dev.criosage.simpleprogressions.screenhandler.IronChestScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import software.bernie.example.block.tile.FertilizerTileEntity;
import software.bernie.example.registry.TileRegistry;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

@Environment(EnvType.CLIENT)
public class SimpleProgressionsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        GeoItemRenderer.registerItemRenderer(SimpleProgressions.COPPER_CHEST_ITEM, new CopperChestItemRenderer());
        BlockEntityRendererRegistry.register(SimpleProgressions.COPPER_CHEST_ENTITY, (BlockEntityRendererFactory.Context rendererDispatcherIn) -> new CopperChestRenderer());
        ScreenRegistry.register(SimpleProgressions.COPPER_CHEST_SCREEN_HANDLER, CopperChestScreen::new);

        GeoItemRenderer.registerItemRenderer(SimpleProgressions.IRON_CHEST_ITEM, new IronChestItemRenderer());
        BlockEntityRendererRegistry.register(SimpleProgressions.IRON_CHEST_ENTITY, (BlockEntityRendererFactory.Context rendererDispatcherIn) -> new IronChestRenderer());
        ScreenRegistry.register(SimpleProgressions.IRON_CHEST_SCREEN_HANDLER, IronChestScreen::new);
    }
}
