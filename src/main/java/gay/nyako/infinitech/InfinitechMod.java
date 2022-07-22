package gay.nyako.infinitech;

import alexiil.mc.lib.multipart.api.AbstractPart;
import alexiil.mc.lib.multipart.api.PartDefinition;
import alexiil.mc.lib.multipart.impl.MultipartBlockEntity;
import alexiil.mc.lib.multipart.impl.PartContainer;
import gay.nyako.infinitech.block.AbstractMachineBlockEntity;
import gay.nyako.infinitech.block.energy_infuser.EnergyInfuserBlock;
import gay.nyako.infinitech.block.MachineUtil;
import gay.nyako.infinitech.block.block_breaker.BlockBreakerBlock;
import gay.nyako.infinitech.block.block_breaker.BlockBreakerBlockEntity;
import gay.nyako.infinitech.block.block_breaker.BlockBreakerGuiDescription;
import gay.nyako.infinitech.block.conveyor.ConveyorBeltBlock;
import gay.nyako.infinitech.block.conveyor.ConveyorBeltBlockEntity;
import gay.nyako.infinitech.block.energy_infuser.EnergyInfuserBlockEntity;
import gay.nyako.infinitech.block.energy_infuser.EnergyInfuserGuiDescription;
import gay.nyako.infinitech.block.fluid_tank.FluidTankBlock;
import gay.nyako.infinitech.block.fluid_tank.FluidTankBlockEntity;
import gay.nyako.infinitech.block.fluid_tank.FluidTankBlockItem;
import gay.nyako.infinitech.block.furnace_generator.FurnaceGeneratorBlock;
import gay.nyako.infinitech.block.furnace_generator.FurnaceGeneratorBlockEntity;
import gay.nyako.infinitech.block.furnace_generator.FurnaceGeneratorGuiDescription;
import gay.nyako.infinitech.block.item_grate.ItemGrateBlock;
import gay.nyako.infinitech.block.item_grate.ItemGrateBlockEntity;
import gay.nyako.infinitech.block.pipe.*;
import gay.nyako.infinitech.block.power_bank.PowerBankBlock;
import gay.nyako.infinitech.block.power_bank.PowerBankBlockEntity;
import gay.nyako.infinitech.block.power_bank.PowerBankGuiDescription;
import gay.nyako.infinitech.block.xp_drain.XPDrainBlock;
import gay.nyako.infinitech.item.ItemFilterGuiDescription;
import gay.nyako.infinitech.item.ItemFilterItem;
import gay.nyako.infinitech.item.StaffOfEnderItem;
import gay.nyako.infinitech.storage.fluid.FluidInventory;
import gay.nyako.infinitech.storage.fluid.FluidStoringBlockItem;
import gay.nyako.infinitech.storage.fluid.FluidStoringBlockItemStorage;
import gay.nyako.infinitech.storage.fluid.SidedFluidStorage;
import io.github.tropheusj.milk.Milk;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.FullItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.*;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import team.reborn.energy.api.EnergyStorage;

public class InfinitechMod implements ModInitializer {
	public static Logger LOGGER = LogManager.getLogger();

	public static final String MOD_ID = "infinitech";

	public static final Block CONVEYOR_BELT_BLOCK = new ConveyorBeltBlock(FabricBlockSettings
			.of(Material.METAL)
			.strength(4.0f)
	);

	public static final Block ITEM_GRATE_BLOCK = new ItemGrateBlock(FabricBlockSettings
			.of(Material.METAL)
			.strength(4.0f)
	);

	public static final Block BLOCK_BREAKER_BLOCK = new BlockBreakerBlock(FabricBlockSettings
			.of(Material.METAL)
			.strength(4.0f)
	);

	public static final Block ENERGY_INFUSER_BLOCK = new EnergyInfuserBlock(FabricBlockSettings
			.of(Material.METAL)
			.strength(4.0f)
	);

	public static final Block XP_DRAIN_BLOCK = new XPDrainBlock(FabricBlockSettings
			.of(Material.METAL)
			.strength(4.0f)
	);

	public static BlockEntityType<ItemGrateBlockEntity> ITEM_GRATE_BLOCK_ENTITY;

	public static BlockEntityType<BlockBreakerBlockEntity> BLOCK_BREAKER_BLOCK_ENTITY;

	public static BlockEntityType<EnergyInfuserBlockEntity> ENERGY_INFUSER_BLOCK_ENTITY;

	public static final ScreenHandlerType<FurnaceGeneratorGuiDescription> FURNACE_GENERATOR_SCREEN_HANDLER = ScreenHandlerRegistry.registerExtended(new Identifier(MOD_ID,"furnace_generator_gui_description"), (syncId, inventory, buf) -> new FurnaceGeneratorGuiDescription(syncId, inventory, ScreenHandlerContext.EMPTY, getPacketBlockEntity(inventory, buf.readBlockPos())));

	public static final ScreenHandlerType<PowerBankGuiDescription> POWER_BANK_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(new Identifier(MOD_ID,"power_bank_gui_description"), (syncId, inventory) -> new PowerBankGuiDescription(syncId, inventory, ScreenHandlerContext.EMPTY));

	public static final ScreenHandlerType<BlockBreakerGuiDescription> BLOCK_BREAKER_SCREEN_HANDLER = ScreenHandlerRegistry.registerExtended(new Identifier(MOD_ID,"block_breaker_gui_description"), (syncId, inventory, buf) -> new BlockBreakerGuiDescription(syncId, inventory, ScreenHandlerContext.EMPTY, getPacketBlockEntity(inventory, buf.readBlockPos())));

	public static final ScreenHandlerType<EnergyInfuserGuiDescription> ENERGY_INFUSER_SCREEN_HANDLER = ScreenHandlerRegistry.registerExtended(new Identifier(MOD_ID,"energy_infuser_gui_description"), (syncId, inventory, buf) -> new EnergyInfuserGuiDescription(syncId, inventory, ScreenHandlerContext.EMPTY, getPacketBlockEntity(inventory, buf.readBlockPos())));

	public static final ScreenHandlerType<PipeGuiDescription> PIPE_GUI_SCREEN_HANDLER = ScreenHandlerRegistry.registerExtended(new Identifier(MOD_ID,"pipe_gui_description"), (syncId, inventory, buf) -> new PipeGuiDescription(syncId, inventory, ScreenHandlerContext.EMPTY, buf.readBlockPos(), buf.readLong()));

	public static final ScreenHandlerType<ItemFilterGuiDescription> ITEM_FILTER_SCREEN_HANDLER = ScreenHandlerRegistry.registerExtended(new Identifier(MOD_ID,"item_filter_gui_description"), (syncId, inventory, buf) -> new ItemFilterGuiDescription(syncId, inventory, ScreenHandlerContext.EMPTY));

	public static BlockEntityType<ConveyorBeltBlockEntity> CONVEYOR_BELT_BLOCK_ENTITY;

	public static final Block FURNACE_GENERATOR_BLOCK = new FurnaceGeneratorBlock(FabricBlockSettings
			.of(Material.METAL)
			.strength(4.0f)
			.luminance(FurnaceGeneratorBlock.getLuminance())
	);

	public static BlockEntityType<FurnaceGeneratorBlockEntity> FURNACE_GENERATOR_BLOCK_ENTITY;

	public static final Block POWER_BANK_BLOCK = new PowerBankBlock(FabricBlockSettings
			.of(Material.METAL)
			.strength(4.0f)
	);

	public static BlockEntityType<PowerBankBlockEntity> POWER_BANK_BLOCK_ENTITY;

	public static final FluidTankBlock FLUID_TANK_BLOCK = new FluidTankBlock(FluidConstants.BUCKET * 16, FabricBlockSettings
			.of(Material.GLASS)
			.nonOpaque()
			.luminance(FluidTankBlock::getLuminance)
			.strength(2.0f)
	);
	public static final BlockItem FLUID_TANK_BLOCK_ITEM = new FluidTankBlockItem(FLUID_TANK_BLOCK, new FabricItemSettings().group(ItemGroup.MISC));
	public static BlockEntityType<FluidTankBlockEntity> FLUID_TANK_BLOCK_ENTITY;

	public static final PartDefinition ITEM_PIPE_PART = new PartDefinition(new Identifier(MOD_ID, "item_pipe"), ItemPipePart::new, ItemPipePart::new);
	public static final Item ITEM_PIPE_ITEM = new PipePartItem(new FabricItemSettings().group(ItemGroup.MISC), h -> new ItemPipePart(ITEM_PIPE_PART, h));

	public static final PartDefinition FLUID_PIPE_PART = new PartDefinition(new Identifier(MOD_ID, "fluid_pipe"), FluidPipePart::new, FluidPipePart::new);
	public static final Item FLUID_PIPE_ITEM = new PipePartItem(new FabricItemSettings().group(ItemGroup.MISC), h -> new FluidPipePart(FLUID_PIPE_PART, h));

	public static final PartDefinition ENERGY_PIPE_PART = new PartDefinition(new Identifier(MOD_ID, "energy_pipe"), EnergyPipePart::new, EnergyPipePart::new);
	public static final Item ENERGY_PIPE_ITEM = new PipePartItem(new FabricItemSettings().group(ItemGroup.MISC), h -> new EnergyPipePart(ENERGY_PIPE_PART, h));

	public static final Item WRENCH_ITEM = new Item(new FabricItemSettings().group(ItemGroup.MISC).maxCount(1));

	public static final Identifier SIDE_CHOICE_UI_PACKET_ID = new Identifier(MOD_ID, "side_choice_ui");

	public static final Identifier OPEN_PIPE_SCREEN_PACKET_ID = new Identifier(MOD_ID, "open_pipe_screen");

	public static final Identifier SWITCH_PIPE_MODE_PACKET_ID = new Identifier(MOD_ID, "switch_pipe_mode");

	public static final Identifier TOGGLE_PIPE_SIDE_PACKET_ID = new Identifier(MOD_ID, "toggle_pipe_side");

	public static FlowableFluid STILL_LIQUID_XP;
	public static FlowableFluid FLOWING_LIQUID_XP;
	public static Item LIQUID_XP_BUCKET;
	public static Block LIQUID_XP;

	public static final Item STAFF_OF_ENDER = new StaffOfEnderItem(new FabricItemSettings().group(ItemGroup.MISC).maxCount(1));
	public static final Item ITEM_FILTER = new ItemFilterItem(new FabricItemSettings().group(ItemGroup.MISC));

	public static AbstractMachineBlockEntity getPacketBlockEntity(PlayerInventory playerInventory, BlockPos blockPos) {
		return (AbstractMachineBlockEntity) playerInventory.player.world.getBlockEntity(blockPos);
	}

	@Override
	public void onInitialize() { // modid is "infinitech"
		log(Level.INFO, "hi from infinitech!!");

		Milk.finiteMilkFluid();
		Milk.enableMilkFluid();
		Milk.enableMilkPlacing();
		Milk.enableCauldron();
		Milk.enableAllMilkBottles();

		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "conveyor_belt"), CONVEYOR_BELT_BLOCK);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "conveyor_belt"), new BlockItem(CONVEYOR_BELT_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
		CONVEYOR_BELT_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "conveyor_belt_entity"), FabricBlockEntityTypeBuilder.create(ConveyorBeltBlockEntity::new, CONVEYOR_BELT_BLOCK).build(null));

		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "item_grate"), ITEM_GRATE_BLOCK);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "item_grate"), new BlockItem(ITEM_GRATE_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
		ITEM_GRATE_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "item_grate_entity"), FabricBlockEntityTypeBuilder.create(ItemGrateBlockEntity::new, ITEM_GRATE_BLOCK).build(null));

		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "furnace_generator"), FURNACE_GENERATOR_BLOCK);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "furnace_generator"), new BlockItem(FURNACE_GENERATOR_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
		FURNACE_GENERATOR_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "furnace_generator_entity"), FabricBlockEntityTypeBuilder.create(FurnaceGeneratorBlockEntity::new, FURNACE_GENERATOR_BLOCK).build(null));

		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "power_bank"), POWER_BANK_BLOCK);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "power_bank"), new BlockItem(POWER_BANK_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
		POWER_BANK_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "power_bank_entity"), FabricBlockEntityTypeBuilder.create(PowerBankBlockEntity::new, POWER_BANK_BLOCK).build(null));

		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "fluid_tank"), FLUID_TANK_BLOCK);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "fluid_tank"), FLUID_TANK_BLOCK_ITEM);
		FLUID_TANK_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "fluid_tank_entity"), FabricBlockEntityTypeBuilder.create(FluidTankBlockEntity::new, FLUID_TANK_BLOCK).build(null));

		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "block_breaker"), BLOCK_BREAKER_BLOCK);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "block_breaker"), new BlockItem(BLOCK_BREAKER_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
		BLOCK_BREAKER_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "block_breaker_entity"), FabricBlockEntityTypeBuilder.create(BlockBreakerBlockEntity::new, BLOCK_BREAKER_BLOCK).build(null));

		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "energy_infuser"), ENERGY_INFUSER_BLOCK);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "energy_infuser"), new BlockItem(ENERGY_INFUSER_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
		ENERGY_INFUSER_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "energy_infuser_entity"), FabricBlockEntityTypeBuilder.create(EnergyInfuserBlockEntity::new, ENERGY_INFUSER_BLOCK).build(null));


		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "xp_drain"), XP_DRAIN_BLOCK);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "xp_drain"), new BlockItem(XP_DRAIN_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));

		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "item_pipe"), ITEM_PIPE_ITEM);
		ITEM_PIPE_PART.register();

		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "fluid_pipe"), FLUID_PIPE_ITEM);
		FLUID_PIPE_PART.register();

		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "energy_pipe"), ENERGY_PIPE_ITEM);
		ENERGY_PIPE_PART.register();

		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "wrench"), WRENCH_ITEM);

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
			if (blockEntity instanceof MultipartBlockEntity multipartBE) {
				var fluidPipe = multipartBE.getContainer().getFirstPart(FluidPipePart.class);
				if (fluidPipe != null) {
					return fluidPipe.getStorage(side);
				}
			}
			return null;
		});
		FluidStorage.ITEM.registerFallback((itemStack, context) -> {
			if (itemStack.getItem() instanceof FluidStoringBlockItem fluidItem) {
				return new FluidStoringBlockItemStorage(fluidItem, itemStack, context);
			}
			return null;
		});

		EnergyStorage.SIDED.registerFallback((world, pos, state, blockEntity, side) -> {
			if (blockEntity instanceof AbstractMachineBlockEntity machine) {
				return machine.energyStorage;
			} else if (blockEntity instanceof MultipartBlockEntity multipartBE) {
				var energyPipe = multipartBE.getContainer().getFirstPart(EnergyPipePart.class);
				if (energyPipe != null) {
					return EnergyPipeStorage.of(energyPipe, side);
				}
			}
			return null;
		});

		ServerPlayNetworking.registerGlobalReceiver(SIDE_CHOICE_UI_PACKET_ID,
				(server, player, handler, buffer, sender) -> {
					MachineUtil.Sides side = buffer.readEnumConstant(MachineUtil.Sides.class);
					MachineUtil.SideTypes side_id = buffer.readEnumConstant(MachineUtil.SideTypes.class);
					BlockPos blockPos = buffer.readBlockPos();
					server.execute(() -> {
						if(!player.world.isOutOfHeightLimit(blockPos)) {
							if (player.world.getBlockEntity(blockPos) instanceof AbstractMachineBlockEntity blockEntity) {
								blockEntity.sides.put(side,side_id);
								blockEntity.sync();
							}
						};
					});
				}
		);

		ServerPlayNetworking.registerGlobalReceiver(OPEN_PIPE_SCREEN_PACKET_ID,
				(server, player, handler, buffer, sender) -> {
					BlockPos blockPos = buffer.readBlockPos();
					long uniqueId = buffer.readLong();
					var lastDirection = buffer.readEnumConstant(Direction.class);
					server.execute(() -> {
						if (!player.world.isOutOfHeightLimit(blockPos)) {
							if (player.world.getBlockEntity(blockPos) instanceof MultipartBlockEntity multipartBlockEntity) {
								var container = multipartBlockEntity.getContainer();
								if (container.getPart(uniqueId) instanceof AbstractPipePart pipePart) {
									pipePart.lastDirection = lastDirection;
									player.openHandledScreen(pipePart);
								}
							}
						}
					});
				}
		);

		ServerPlayNetworking.registerGlobalReceiver(SWITCH_PIPE_MODE_PACKET_ID,
				(server, player, handler, buffer, sender) -> {
					AbstractIOPipePart.Mode mode = buffer.readEnumConstant(AbstractIOPipePart.Mode.class);
					Direction direction = buffer.readEnumConstant(Direction.class);
					BlockPos blockPos = buffer.readBlockPos();
					long uniqueId = buffer.readLong();
					server.execute(() -> {
						if(!player.world.isOutOfHeightLimit(blockPos)) {
							if (player.world.getBlockEntity(blockPos) instanceof MultipartBlockEntity multipartBlockEntity) {
								var container = multipartBlockEntity.getContainer();
								if (container.getPart(uniqueId) instanceof AbstractIOPipePart pipePart) {
									pipePart.setMode(direction, mode);
								}
							}
						}
					});
				}
		);

		ServerPlayNetworking.registerGlobalReceiver(TOGGLE_PIPE_SIDE_PACKET_ID,
				(server, player, handler, buffer, sender) -> {
					BlockPos blockPos = buffer.readBlockPos();
					long uniqueId = buffer.readLong();
					var direction = buffer.readEnumConstant(Direction.class);
					var value = buffer.readBoolean();
					server.execute(() -> {

						var world = player.world;

						if(!world.isOutOfHeightLimit(blockPos)) {
							if (world.getBlockEntity(blockPos) instanceof MultipartBlockEntity multipartBlockEntity) {
								var container = multipartBlockEntity.getContainer();
								if (container.getPart(uniqueId) instanceof AbstractPipePart pipePart) {
									pipePart.enabledSides.put(direction, value);
									pipePart.needsUpdate = true;

									world.markDirty(blockPos);

									var neighborPos = blockPos.offset(direction);
									if (!world.isOutOfHeightLimit(neighborPos) && world.getBlockEntity(neighborPos) instanceof MultipartBlockEntity neighborBlockEntity) {
										var neighborContainer = neighborBlockEntity.getContainer();

										AbstractPart neighborPart;
										if ((neighborPart = neighborContainer.getFirstPart(part -> part instanceof AbstractPipePart && pipePart.isValidPipe((AbstractPipePart)part, direction))) != null) {
											var neighborPipe = (AbstractPipePart)neighborPart;

											neighborPipe.enabledSides.put(direction.getOpposite(), value);
											neighborPipe.needsUpdate = true;

											world.markDirty(neighborPos);
										}
									}
								}
							}
						}
					});
				}
		);

		STILL_LIQUID_XP = Registry.register(Registry.FLUID, new Identifier(MOD_ID, "liquid_xp"), new LiquidXPFluid.Still());
		FLOWING_LIQUID_XP = Registry.register(Registry.FLUID, new Identifier(MOD_ID, "flowing_liquid_xp"), new LiquidXPFluid.Flowing());
		LIQUID_XP_BUCKET = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "liquid_xp_bucket"),
				new BucketItem(STILL_LIQUID_XP, new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1)));

		LIQUID_XP = Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "liquid_xp"), new FluidBlock(STILL_LIQUID_XP, FabricBlockSettings.copy(Blocks.WATER).luminance(state -> 15)){});

		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "staff_of_ender"), STAFF_OF_ENDER);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "item_filter"), ITEM_FILTER);
	}

	public static void log(Level level, String message) {
		LOGGER.log(level, message);
	}
}
