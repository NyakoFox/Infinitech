package gay.nyako.infinitech;

import alexiil.mc.lib.multipart.api.PartDefinition;
import alexiil.mc.lib.multipart.impl.MultipartBlockEntity;
import dev.technici4n.fasttransferlib.api.energy.EnergyApi;
import gay.nyako.infinitech.block.AbstractMachineBlockEntity;
import gay.nyako.infinitech.block.MachineUtil;
import gay.nyako.infinitech.block.cardboard_box.CardboardBoxBlock;
import gay.nyako.infinitech.block.cardboard_box.CardboardBoxBlockEntity;
import gay.nyako.infinitech.block.conveyor.ConveyorBeltBlock;
import gay.nyako.infinitech.block.conveyor.ConveyorBeltBlockEntity;
import gay.nyako.infinitech.block.fluid_tank.FluidTankBlock;
import gay.nyako.infinitech.block.fluid_tank.FluidTankBlockEntity;
import gay.nyako.infinitech.block.fluid_tank.FluidTankBlockItem;
import gay.nyako.infinitech.block.furnace_generator.FurnaceGeneratorBlock;
import gay.nyako.infinitech.block.furnace_generator.FurnaceGeneratorBlockEntity;
import gay.nyako.infinitech.block.furnace_generator.FurnaceGeneratorGuiDescription;
import gay.nyako.infinitech.block.pipe.EnergyPipeIo;
import gay.nyako.infinitech.block.pipe.EnergyPipePart;
import gay.nyako.infinitech.block.pipe.ItemPipePart;
import gay.nyako.infinitech.block.pipe.PipePartItem;
import gay.nyako.infinitech.block.power_bank.PowerBankBlock;
import gay.nyako.infinitech.block.power_bank.PowerBankBlockEntity;
import gay.nyako.infinitech.block.power_bank.PowerBankGuiDescription;
import gay.nyako.infinitech.storage.FluidInventory;
import gay.nyako.infinitech.storage.FluidStoringBlockItem;
import gay.nyako.infinitech.storage.FluidStoringBlockItemStorage;
import gay.nyako.infinitech.storage.SidedFluidStorage;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InfinitechMod implements ModInitializer {
	public static Logger LOGGER = LogManager.getLogger();

	public static final String MOD_ID = "infinitech";

	public static final Block CONVEYOR_BELT_BLOCK = new ConveyorBeltBlock(FabricBlockSettings
			.of(Material.METAL)
			.strength(4.0f)
			.breakByTool(FabricToolTags.PICKAXES, 1)
	);
	public static final ScreenHandlerType<FurnaceGeneratorGuiDescription> FURNACE_GENERATOR_SCREEN_HANDLER = ScreenHandlerRegistry.registerExtended(new Identifier(MOD_ID,"furnace_generator_gui_description"), (syncId, inventory, buf) -> new FurnaceGeneratorGuiDescription(syncId, inventory, ScreenHandlerContext.EMPTY, buf.readBlockPos()));

	public static final ScreenHandlerType<PowerBankGuiDescription> POWER_BANK_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(new Identifier(MOD_ID,"power_bank_gui_description"), (syncId, inventory) -> new PowerBankGuiDescription(syncId, inventory, ScreenHandlerContext.EMPTY));

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

	public static final FluidTankBlock FLUID_TANK_BLOCK = new FluidTankBlock(FluidConstants.BUCKET * 8, FabricBlockSettings
			.of(Material.GLASS)
			.nonOpaque()
			.luminance(FluidTankBlock::getLuminance)
			.strength(2.0f)
			.breakByTool(FabricToolTags.PICKAXES, 1)
	);
	public static final BlockItem FLUID_TANK_BLOCK_ITEM = new FluidTankBlockItem(FLUID_TANK_BLOCK, new FabricItemSettings().group(ItemGroup.MISC));
	public static BlockEntityType<FluidTankBlockEntity> FLUID_TANK_BLOCK_ENTITY;

	public static final PartDefinition ITEM_PIPE_PART = new PartDefinition(new Identifier(MOD_ID, "item_pipe"), ItemPipePart::new, ItemPipePart::new);
	public static final Item ITEM_PIPE_ITEM = new PipePartItem(new FabricItemSettings().group(ItemGroup.MISC), h -> new ItemPipePart(ITEM_PIPE_PART, h));

	public static final PartDefinition ENERGY_PIPE_PART = new PartDefinition(new Identifier(MOD_ID, "energy_pipe"), EnergyPipePart::new, EnergyPipePart::new);
	public static final Item ENERGY_PIPE_ITEM = new PipePartItem(new FabricItemSettings().group(ItemGroup.MISC), h -> new EnergyPipePart(ENERGY_PIPE_PART, h));

	public static final Identifier SIDE_CHOICE_UI_PACKET_ID = new Identifier(MOD_ID, "side_choice_ui");

	@Override
	public void onInitialize() { // modid is "infinitech"
		log(Level.INFO, "hi from infinitech!!");

		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "conveyor_belt"), CONVEYOR_BELT_BLOCK);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "conveyor_belt"), new BlockItem(CONVEYOR_BELT_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
		CONVEYOR_BELT_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "conveyor_belt_entity"), FabricBlockEntityTypeBuilder.create(ConveyorBeltBlockEntity::new, CONVEYOR_BELT_BLOCK).build(null));

		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "furnace_generator"), FURNACE_GENERATOR_BLOCK);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "furnace_generator"), new BlockItem(FURNACE_GENERATOR_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
		FURNACE_GENERATOR_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "furnace_generator_entity"), FabricBlockEntityTypeBuilder.create(FurnaceGeneratorBlockEntity::new, FURNACE_GENERATOR_BLOCK).build(null));
		EnergyApi.SIDED.registerSelf(FURNACE_GENERATOR_BLOCK_ENTITY);

		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "power_bank"), POWER_BANK_BLOCK);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "power_bank"), new BlockItem(POWER_BANK_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
		POWER_BANK_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "power_bank_entity"), FabricBlockEntityTypeBuilder.create(PowerBankBlockEntity::new, POWER_BANK_BLOCK).build(null));
		EnergyApi.SIDED.registerSelf(POWER_BANK_BLOCK_ENTITY);

		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "cardboard_box"), CARDBOARD_BOX_BLOCK);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "cardboard_box"), new BlockItem(CARDBOARD_BOX_BLOCK, new FabricItemSettings().group(ItemGroup.DECORATIONS)));
		FlammableBlockRegistry.getDefaultInstance().add(CARDBOARD_BOX_BLOCK, 5, 5);
		CARDBOARD_BOX_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "cardboard_box_entity"), FabricBlockEntityTypeBuilder.create(CardboardBoxBlockEntity::new, CARDBOARD_BOX_BLOCK).build(null));

		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "fluid_tank"), FLUID_TANK_BLOCK);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "fluid_tank"), FLUID_TANK_BLOCK_ITEM);
		FLUID_TANK_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "fluid_tank_entity"), FabricBlockEntityTypeBuilder.create(FluidTankBlockEntity::new, FLUID_TANK_BLOCK).build(null));

		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "item_pipe"), ITEM_PIPE_ITEM);
		ITEM_PIPE_PART.register();

		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "energy_pipe"), ENERGY_PIPE_ITEM);
		ENERGY_PIPE_PART.register();

		ItemStorage.SIDED.registerFallback((world, pos, state, blockEntity, side) -> {
			if (blockEntity instanceof MultipartBlockEntity multipartBE) {
				var itemPipe = multipartBE.getContainer().getFirstPart(ItemPipePart.class);
				if (itemPipe != null) {
					return itemPipe.getStorage(side);
				}
			}
			return null;
		});

		FluidStorage.SIDED.registerFallback((world, pos, state, blockEntity, side) -> {
			if (blockEntity instanceof FluidInventory inventory) {
				return SidedFluidStorage.of(inventory, side);
			}
			return null;
		});
		FluidStorage.ITEM.registerFallback((itemStack, context) -> {
			if (itemStack.getItem() instanceof FluidStoringBlockItem fluidItem) {
				return new FluidStoringBlockItemStorage(fluidItem, itemStack, context);
			}
			return null;
		});

		EnergyApi.SIDED.registerFallback((world, pos, state, blockEntity, side) -> {
			if (blockEntity instanceof MultipartBlockEntity multipartBE) {
				var energyPipe = multipartBE.getContainer().getFirstPart(EnergyPipePart.class);
				if (energyPipe != null) {
					return EnergyPipeIo.of(energyPipe, side);
				}
			}
			return null;
		});

		ServerSidePacketRegistry.INSTANCE.register(SIDE_CHOICE_UI_PACKET_ID, (packetContext, attachedData) -> {
			MachineUtil.Sides side = attachedData.readEnumConstant(MachineUtil.Sides.class);
			MachineUtil.SideTypes side_id = attachedData.readEnumConstant(MachineUtil.SideTypes.class);
			BlockPos blockPos = attachedData.readBlockPos();
			packetContext.getTaskQueue().execute(() -> {
				// Execute on the main thread
				if(!packetContext.getPlayer().world.isOutOfHeightLimit(blockPos)){
					if (packetContext.getPlayer().world.getBlockEntity(blockPos) instanceof AbstractMachineBlockEntity blockEntity) {
						blockEntity.sides.put(side,side_id);
						blockEntity.sync();
					}
				}
			});
		});
	}

	public static void log(Level level, String message) {
		LOGGER.log(level, message);
	}
}
