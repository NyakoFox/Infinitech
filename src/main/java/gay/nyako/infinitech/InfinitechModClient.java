package gay.nyako.infinitech;

import alexiil.mc.lib.multipart.api.render.PartDynamicModelRegisterEvent;
import alexiil.mc.lib.multipart.api.render.PartStaticModelRegisterEvent;
import gay.nyako.infinitech.block.conveyor.ConveyorBeltBlockEntityRenderer;
import gay.nyako.infinitech.block.furnace_generator.FurnaceGeneratorGuiDescription;
import gay.nyako.infinitech.block.furnace_generator.FurnaceGeneratorScreen;
import gay.nyako.infinitech.block.pipe.AbstractPipePart;
import gay.nyako.infinitech.block.pipe.PipePartModelBaker;
import gay.nyako.infinitech.block.pipe.PipePartModelKey;
import gay.nyako.infinitech.block.power_bank.PowerBankGuiDescription;
import gay.nyako.infinitech.block.power_bank.PowerBankScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;

public class InfinitechModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.INSTANCE.register(InfinitechMod.CONVEYOR_BELT_BLOCK_ENTITY, ConveyorBeltBlockEntityRenderer::new);
        ScreenRegistry.<FurnaceGeneratorGuiDescription, FurnaceGeneratorScreen>register(InfinitechMod.FURNACE_GENERATOR_SCREEN_HANDLER, (gui, inventory, title) -> new FurnaceGeneratorScreen(gui, inventory.player, title));
        ScreenRegistry.<PowerBankGuiDescription, PowerBankScreen>register(InfinitechMod.POWER_BANK_SCREEN_HANDLER, (gui, inventory, title) -> new PowerBankScreen(gui, inventory.player, title));

        PartStaticModelRegisterEvent.EVENT.register((renderer) -> {
            renderer.register(PipePartModelKey.class, new PipePartModelBaker());
        });

        ClientSpriteRegistryCallback.event(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).register((atlasTexture, registry) -> {
            registry.register(new Identifier("infinitech", "block/pipe/energy"));
            registry.register(new Identifier("infinitech", "block/pipe/item"));
            registry.register(new Identifier("infinitech", "block/pipe/connector"));
        });
    }
}
