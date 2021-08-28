package gay.nyako.infinitech;

import alexiil.mc.lib.multipart.api.PartDefinition;
import dev.technici4n.fasttransferlib.api.energy.EnergyApi;
import gay.nyako.infinitech.block.cardboard_box.CardboardBoxBlock;
import gay.nyako.infinitech.block.cardboard_box.CardboardBoxBlockEntity;
import gay.nyako.infinitech.block.conveyor.ConveyorBeltBlock;
import gay.nyako.infinitech.block.conveyor.ConveyorBeltBlockEntity;
import gay.nyako.infinitech.block.furnace_generator.FurnaceGeneratorBlock;
import gay.nyako.infinitech.block.furnace_generator.FurnaceGeneratorBlockEntity;
import gay.nyako.infinitech.block.furnace_generator.FurnaceGeneratorGuiDescription;
import gay.nyako.infinitech.block.pipe.ItemPipePart;
import gay.nyako.infinitech.block.pipe.PipePartItem;
import gay.nyako.infinitech.block.power_bank.PowerBankBlock;
import gay.nyako.infinitech.block.power_bank.PowerBankBlockEntity;
import gay.nyako.infinitech.block.power_bank.PowerBankGuiDescription;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InfinitechMod implements ModInitializer {
	public static Logger LOGGER = LogManager.getLogger();

	public static final Block CONVEYOR_BELT_BLOCK = new ConveyorBeltBlock(FabricBlockSettings
			.of(Material.METAL)
			.strength(4.0f)
			.breakByTool(FabricToolTags.PICKAXES, 1)
	);
	public static final ScreenHandlerType<FurnaceGeneratorGuiDescription> FURNACE_GENERATOR_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(new Identifier("infinitech","furnace_generator_gui_description"), (syncId, inventory) -> new FurnaceGeneratorGuiDescription(syncId, inventory, ScreenHandlerContext.EMPTY));

	public static final ScreenHandlerType<PowerBankGuiDescription> POWER_BANK_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(new Identifier("infinitech","power_bank_gui_description"), (syncId, inventory) -> new PowerBankGuiDescription(syncId, inventory, ScreenHandlerContext.EMPTY));

	public static BlockEntityType<ConveyorBeltBlockEntity> CONVEYOR_BELT_BLOCK_ENTITY;

	public static final Block FURNACE_GENERATOR_BLOCK = new FurnaceGeneratorBlock(FabricBlockSettings
			.of(Material.METAL)
			.strength(4.0f)
			.luminance(FurnaceGeneratorBlock.getLuminance())
			.breakByTool(FabricToolTags.PICKAXES, 1)
	);

	public static BlockEntityType<FurnaceGeneratorBlockEntity> FURNACE_GENERATOR_BLOCK_ENTITY;

	public static final Block POWER_BANK_BLOCK = new PowerBankBlock(FabricBlockSettings
			.of(Material.METAL)
			.strength(4.0f)
			.breakByTool(FabricToolTags.PICKAXES, 1)
	);

	public static BlockEntityType<PowerBankBlockEntity> POWER_BANK_BLOCK_ENTITY;

	public static final Block CARDBOARD_BOX_BLOCK = new CardboardBoxBlock(FabricBlockSettings
			.of(Material.WOOD)
			.sounds(BlockSoundGroup.WOOD)
			.strength(2.0f)
			.breakByTool(FabricToolTags.AXES, 1)
	);

	public static BlockEntityType<CardboardBoxBlockEntity> CARDBOARD_BOX_BLOCK_ENTITY;

	public static final PartDefinition ITEM_PIPE_PART = new PartDefinition(new Identifier("infinitech", "item_pipe"), ItemPipePart::new, ItemPipePart::new);
	public static final Item ITEM_PIPE_ITEM = new PipePartItem(new FabricItemSettings().group(ItemGroup.INVENTORY), h -> new ItemPipePart(ITEM_PIPE_PART, h));

	@Override
	public void onInitialize() { // modid is "infinitech"
		log(Level.INFO, "hi from infinitech!!");

		Registry.register(Registry.BLOCK, new Identifier("infinitech", "conveyor_belt"), CONVEYOR_BELT_BLOCK);
		Registry.register(Registry.ITEM, new Identifier("infinitech", "conveyor_belt"), new BlockItem(CONVEYOR_BELT_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
		CONVEYOR_BELT_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "infinitech:conveyor_belt_entity", FabricBlockEntityTypeBuilder.create(ConveyorBeltBlockEntity::new, CONVEYOR_BELT_BLOCK).build(null));

		Registry.register(Registry.BLOCK, new Identifier("infinitech", "furnace_generator"), FURNACE_GENERATOR_BLOCK);
		Registry.register(Registry.ITEM, new Identifier("infinitech", "furnace_generator"), new BlockItem(FURNACE_GENERATOR_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
		FURNACE_GENERATOR_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "infinitech:furnace_generator_entity", FabricBlockEntityTypeBuilder.create(FurnaceGeneratorBlockEntity::new, FURNACE_GENERATOR_BLOCK).build(null));
		EnergyApi.SIDED.registerSelf(FURNACE_GENERATOR_BLOCK_ENTITY);

		Registry.register(Registry.BLOCK, new Identifier("infinitech", "power_bank"), POWER_BANK_BLOCK);
		Registry.register(Registry.ITEM, new Identifier("infinitech", "power_bank"), new BlockItem(POWER_BANK_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
		POWER_BANK_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "infinitech:power_bank_entity", FabricBlockEntityTypeBuilder.create(PowerBankBlockEntity::new, POWER_BANK_BLOCK).build(null));
		EnergyApi.SIDED.registerSelf(POWER_BANK_BLOCK_ENTITY);

		Registry.register(Registry.BLOCK, new Identifier("infinitech", "cardboard_box"), CARDBOARD_BOX_BLOCK);
		Registry.register(Registry.ITEM, new Identifier("infinitech", "cardboard_box"), new BlockItem(CARDBOARD_BOX_BLOCK, new FabricItemSettings().group(ItemGroup.DECORATIONS)));
		FlammableBlockRegistry.getDefaultInstance().add(CARDBOARD_BOX_BLOCK, 5, 5);
		CARDBOARD_BOX_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "infinitech:cardboard_box_entity", FabricBlockEntityTypeBuilder.create(CardboardBoxBlockEntity::new, CARDBOARD_BOX_BLOCK).build(null));

		Registry.register(Registry.ITEM, new Identifier("infinitech", "item_pipe"), ITEM_PIPE_ITEM);
		ITEM_PIPE_PART.register();
	}

	public static void log(Level level, String message){
		LOGGER.log(level, message);
	}
}
