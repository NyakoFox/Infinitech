package gay.nyako.infinitech.block.furnace_generator;

import gay.nyako.infinitech.ImplementedInventory;
import gay.nyako.infinitech.InfinitechMod;
import gay.nyako.infinitech.block.AbstractGeneratorBlockEntity;
import gay.nyako.infinitech.block.AbstractMachineBlockEntity;
import io.github.cottonmc.cotton.gui.PropertyDelegateHolder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;

import java.util.Iterator;
import java.util.Map;

public class FurnaceGeneratorBlockEntity extends AbstractGeneratorBlockEntity implements PropertyDelegateHolder, NamedScreenHandlerFactory, ExtendedScreenHandlerFactory {
    int burnTime;
    int fuelTime;
    protected final PropertyDelegate propertyDelegate;

    // 1 coal burns for 1600,
    // And 1 coal is supposed to equal 4000 energy according to the api.
    // 4000 / 1600 = 2.5, so we use 2.5.
    // That's sad.
    private final double energyRate = 2.5;
    // Multiply the speed by 4, so we're actually giving 10 E/t.
    // The API can't do doubles or floats, and well, 2 or 3 E/t is *way* too slow.
    private final int speed = 4;

    public FurnaceGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(InfinitechMod.FURNACE_GENERATOR_BLOCK_ENTITY, pos, state, 200_000, 1_000);
        this.canInsert = false;
        this.canExtract = true;

        this.propertyDelegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                switch(index) {
                    case 0: return burnTime;
                    case 1: return fuelTime;
                    case 2: return (int) energy;
                    case 3: return (int) difference;
                    default: return 0;
                }
            }

            @Override
            public void set(int index, int value) {
                switch(index) {
                    case 0: burnTime = value; break;
                    case 1: fuelTime = value; break;
                    case 2: energy = value; break;
                    case 3: difference = value; break;
                }

            }

            @Override
            public int size() {
                return 4;
            }
        };
    }

    @Override
    public int getSlotAmount() {
        // It should have one slot for the output.
        return 1;
    }

    @Override
    public boolean hasBatterySlot() {
        // It should have a battery slot.
        return true;
    }

    protected Text getContainerName() {
        return Text.translatable("container.furnace");
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }

    public static Map<Item, Integer> createFuelTimeMap() {
        return AbstractFurnaceBlockEntity.createFuelTimeMap();
    }

    private boolean isBurning() {
        return this.burnTime > 0;
    }

    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.burnTime = nbt.getShort("BurnTime");
        this.fuelTime = this.getFuelTime((ItemStack) inventory.get(0));
    }

    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putShort("BurnTime", (short)this.burnTime);
    }

    public static void tick(World world, BlockPos pos, BlockState state, FurnaceGeneratorBlockEntity blockEntity) {
        boolean bl = blockEntity.isBurning();
        boolean bl2 = false;
        if (blockEntity.isBurning()) {
            blockEntity.energy += blockEntity.energyRate * blockEntity.speed;
            if (blockEntity.energy > blockEntity.capacity) {
                blockEntity.energy = blockEntity.capacity;
            }

            blockEntity.burnTime -= blockEntity.speed;
        }

        // First, let's try to charge an item.
        blockEntity.processChargeSlot();

        // Now let's try to push energy in anything beside us.
        blockEntity.attemptEnergyTransfers();

        // Now we should attempt to pull in or push out items, depending on the side configuration.
        blockEntity.attemptSideTransfers(blockEntity.storage);

        ItemStack itemStack = blockEntity.inventory.get(0); // Fuel
        if (blockEntity.isBurning() || !itemStack.isEmpty()) {
            if (!blockEntity.isBurning() && blockEntity.energy < blockEntity.capacity) {
                blockEntity.burnTime = blockEntity.getFuelTime(itemStack);
                blockEntity.fuelTime = blockEntity.burnTime;
                if (blockEntity.isBurning()) {
                    bl2 = true;
                    if (!itemStack.isEmpty()) {
                        Item item = itemStack.getItem();
                        itemStack.decrement(1);
                        if (itemStack.isEmpty()) {
                            Item item2 = item.getRecipeRemainder();
                            blockEntity.inventory.set(0, item2 == null ? ItemStack.EMPTY : new ItemStack(item2));
                        }
                    }
                }
            }
        }

        if (bl != blockEntity.isBurning()) {
            bl2 = true;
            state = state.with(AbstractFurnaceBlock.LIT, blockEntity.isBurning());
            world.setBlockState(pos, state, 3);
        }

        if (bl2) {
            markDirty(world, pos, state);
        }
        blockEntity.calculateEnergyDifference();
    }

    protected int getFuelTime(ItemStack fuel) {
        if (fuel.isEmpty()) {
            return 0;
        } else {
            Item item = fuel.getItem();
            return (Integer)createFuelTimeMap().getOrDefault(item, 0);
        }
    }

    public static boolean canUseAsFuel(ItemStack stack) {
        return createFuelTimeMap().containsKey(stack.getItem());
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        if (isSideDisabled(dir)) return false;
        if (slot == 0) return isValid(slot, stack);
        return super.canInsert(slot, stack, dir);
    }

    public boolean isValid(int slot, ItemStack stack) {
        if (slot == 0) return canUseAsFuel(stack);
        return super.isValid(slot, stack);
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        //We provide *this* to the screenHandler as our class Implements Inventory
        //Only the Server has the Inventory at the start, this will be synced to the client in the ScreenHandler
        //return new FurnaceGeneratorScreenHandler(syncId, playerInventory, this, propertyDelegate);
        return new FurnaceGeneratorGuiDescription(syncId, playerInventory, ScreenHandlerContext.create(world, pos), this);
    }

    @Override
    public PropertyDelegate getPropertyDelegate() {
        return this.propertyDelegate;
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(pos);
    }
}
