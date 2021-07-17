package gay.nyako.infinitech;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class InfinitechMod implements ModInitializer {

	public static final Block CONVEYOR_BELT_BLOCK = new ConveyorBeltBlock(FabricBlockSettings
			.of(Material.METAL)
			.strength(4.0f)
			.breakByTool(FabricToolTags.PICKAXES, 1)
	);

	public static BlockEntityType<ConveyorBeltBlockEntity> CONVEYOR_BELT_BLOCK_ENTITY;

	public static final Block FURNACE_GENERATOR_BLOCK = new FurnaceGeneratorBlock(FabricBlockSettings
			.of(Material.METAL)
			.strength(4.0f)
			.breakByTool(FabricToolTags.PICKAXES, 1)
	);

	public static BlockEntityType<FurnaceGeneratorBlockEntity> FURNACE_GENERATOR_BLOCK_ENTITY;

	public static final ScreenHandlerType<FurnaceGeneratorScreenHandler> FURNACE_GENERATOR_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(new Identifier("infinitech"),FurnaceGeneratorScreenHandler::new);

	@Override
	public void onInitialize() { // modid is "infinitech"
		System.out.println("hi from infinitech!!");

		Registry.register(Registry.BLOCK, new Identifier("infinitech", "conveyor_belt"), CONVEYOR_BELT_BLOCK);
		Registry.register(Registry.ITEM, new Identifier("infinitech", "conveyor_belt"), new BlockItem(CONVEYOR_BELT_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));

		CONVEYOR_BELT_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "infinitech:conveyor_belt_entity", FabricBlockEntityTypeBuilder.create(ConveyorBeltBlockEntity::new, CONVEYOR_BELT_BLOCK).build(null));

		Registry.register(Registry.BLOCK, new Identifier("infinitech", "furnace_generator"), FURNACE_GENERATOR_BLOCK);
		Registry.register(Registry.ITEM, new Identifier("infinitech", "furnace_generator"), new BlockItem(FURNACE_GENERATOR_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
		FURNACE_GENERATOR_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "infinitech:furnace_generator_entity", FabricBlockEntityTypeBuilder.create(FurnaceGeneratorBlockEntity::new, FURNACE_GENERATOR_BLOCK).build(null));
	}
}
