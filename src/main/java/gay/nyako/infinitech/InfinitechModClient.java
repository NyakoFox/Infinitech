package gay.nyako.infinitech;

import alexiil.mc.lib.multipart.api.render.PartStaticModelRegisterEvent;
import gay.nyako.infinitech.block.conveyor.ConveyorBeltBlockEntityRenderer;
import gay.nyako.infinitech.block.fluid_tank.FluidTankRenderer;
import gay.nyako.infinitech.block.furnace_generator.FurnaceGeneratorGuiDescription;
import gay.nyako.infinitech.block.furnace_generator.FurnaceGeneratorScreen;
import gay.nyako.infinitech.block.pipe.PipePartModelBaker;
import gay.nyako.infinitech.block.pipe.PipePartModelKey;
import gay.nyako.infinitech.block.power_bank.PowerBankGuiDescription;
import gay.nyako.infinitech.block.power_bank.PowerBankScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;

public class InfinitechModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.INSTANCE.register(InfinitechMod.CONVEYOR_BELT_BLOCK_ENTITY, ConveyorBeltBlockEntityRenderer::new);
        BlockEntityRendererRegistry.INSTANCE.register(InfinitechMod.FLUID_TANK_BLOCK_ENTITY, ctx -> new FluidTankRenderer());
        BuiltinItemRendererRegistry.INSTANCE.register(InfinitechMod.FLUID_TANK_BLOCK_ITEM, new FluidTankRenderer());
        ScreenRegistry.<FurnaceGeneratorGuiDescription, FurnaceGeneratorScreen>register(InfinitechMod.FURNACE_GENERATOR_SCREEN_HANDLER, (gui, inventory, title) -> new FurnaceGeneratorScreen(gui, inventory.player, title));
        ScreenRegistry.<PowerBankGuiDescription, PowerBankScreen>register(InfinitechMod.POWER_BANK_SCREEN_HANDLER, (gui, inventory, title) -> new PowerBankScreen(gui, inventory.player, title));

        BlockRenderLayerMap.INSTANCE.putBlock(InfinitechMod.FLUID_TANK_BLOCK, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(InfinitechMod.ITEM_GRATE_BLOCK, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(InfinitechMod.XP_DRAIN_BLOCK,   RenderLayer.getCutout());

        ModelLoadingRegistry.INSTANCE.registerResourceProvider(manager -> new InfinitechModelProvider());

        PartStaticModelRegisterEvent.EVENT.register((renderer) -> {
            renderer.register(PipePartModelKey.class, new PipePartModelBaker());
        });

        ClientSpriteRegistryCallback.event(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).register((atlasTexture, registry) -> {
            registry.register(new Identifier(InfinitechMod.MOD_ID, "block/pipe/energy_on"));
            registry.register(new Identifier(InfinitechMod.MOD_ID, "block/pipe/energy_off"));
            registry.register(new Identifier(InfinitechMod.MOD_ID, "block/pipe/energy_on_end"));
            registry.register(new Identifier(InfinitechMod.MOD_ID, "block/pipe/energy_off_end"));
            registry.register(new Identifier(InfinitechMod.MOD_ID, "block/pipe/item"));
            registry.register(new Identifier(InfinitechMod.MOD_ID, "block/pipe/item_end"));
            registry.register(new Identifier(InfinitechMod.MOD_ID, "block/pipe/fluid"));
            registry.register(new Identifier(InfinitechMod.MOD_ID, "block/pipe/fluid_end"));
            registry.register(new Identifier(InfinitechMod.MOD_ID, "block/pipe/connector"));
            registry.register(new Identifier(InfinitechMod.MOD_ID, "block/fluid_tank"));
        });

        FabricModelPredicateProviderRegistry.register(new Identifier(InfinitechMod.MOD_ID, "percentage"), (stack, world, entity, i) ->
                stack.getOrCreateSubNbt("BlockEntityTag").getFloat("percentage"));

        FluidRenderHandlerRegistry.INSTANCE.register(InfinitechMod.STILL_LIQUID_XP, InfinitechMod.FLOWING_LIQUID_XP, new SimpleFluidRenderHandler(
                new Identifier("infinitech:block/liquid_xp_still"),
                new Identifier("infinitech:block/liquid_xp_flowing"),
                0xFFFFFF
        ));

        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), InfinitechMod.STILL_LIQUID_XP, InfinitechMod.FLOWING_LIQUID_XP);

        //if you want to use custom textures they needs to be registered.
        //In this example this is unnecessary because the vanilla water textures are already registered.
        //To register your custom textures use this method.
        ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).register((atlasTexture, registry) -> {
            registry.register(new Identifier("infinitech:block/liquid_xp_still"));
            registry.register(new Identifier("infinitech:block/liquid_xp_flowing"));
        });
    }
}
