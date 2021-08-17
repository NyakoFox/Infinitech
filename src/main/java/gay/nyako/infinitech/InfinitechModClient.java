package gay.nyako.infinitech;

import gay.nyako.infinitech.block.conveyor.ConveyorBeltBlockEntityRenderer;
import gay.nyako.infinitech.block.furnace_generator.FurnaceGeneratorGuiDescription;
import gay.nyako.infinitech.block.furnace_generator.FurnaceGeneratorScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;

public class InfinitechModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.INSTANCE.register(InfinitechMod.CONVEYOR_BELT_BLOCK_ENTITY, ConveyorBeltBlockEntityRenderer::new);
        ScreenRegistry.<FurnaceGeneratorGuiDescription, FurnaceGeneratorScreen>register(InfinitechMod.FURNACE_GENERATOR_SCREEN_HANDLER, (gui, inventory, title) -> new FurnaceGeneratorScreen(gui, inventory.player, title));
    }
}
