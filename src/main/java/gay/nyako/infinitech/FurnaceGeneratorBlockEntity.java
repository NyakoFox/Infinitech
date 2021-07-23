package gay.nyako.infinitech;

import dev.technici4n.fasttransferlib.api.Simulation;
import dev.technici4n.fasttransferlib.api.energy.EnergyIo;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.FurnaceScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;

public class FurnaceGeneratorBlockEntity extends LockableContainerBlockEntity implements NamedScreenHandlerFactory, ImplementedInventory, EnergyIo {
    private static final int[] TOP_SLOTS = new int[]{0};
    private static final int[] BOTTOM_SLOTS = new int[]{2, 1};
    private static final int[] SIDE_SLOTS = new int[]{1};
    protected DefaultedList<ItemStack> inventory;
    int burnTime;
    int fuelTime;
    protected final PropertyDelegate propertyDelegate;

    private final double maxPower = 2_000_000; // heck it . lots
    private final double energyRate = 20; // 20 energy per tick maybe??
    private final double transferRate = 100_000;
    private double power = 0;

    public FurnaceGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(InfinitechMod.FURNACE_GENERATOR_BLOCK_ENTITY, pos, state);
        this.inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);

        this.propertyDelegate = new PropertyDelegate() {
            public int get(int index) {
                switch(index) {
                    case 0:
                        return FurnaceGeneratorBlockEntity.this.burnTime;
                    case 1:
                        return FurnaceGeneratorBlockEntity.this.fuelTime;
                    case 2:
                        return (int) FurnaceGeneratorBlockEntity.this.power;
                    default:
                        return 0;
                }
            }

            public void set(int index, int value) {
                switch(index) {
                    case 0:
                        FurnaceGeneratorBlockEntity.this.burnTime = value;
                        break;
                    case 1:
                        FurnaceGeneratorBlockEntity.this.fuelTime = value;
                        break;
                    case 2:
                        FurnaceGeneratorBlockEntity.this.power = (double) value;
                }

            }

            public int size() {
                return 3;
            }
        };
    }

    protected Text getContainerName() {
        return new TranslatableText("container.furnace");
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText(getCachedState().getBlock().getTranslationKey());
    }

    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new FurnaceScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
    }

    public static Map<Item, Integer> createFuelTimeMap() {
        return AbstractFurnaceBlockEntity.createFuelTimeMap();
    }

    private boolean isBurning() {
        return this.burnTime > 0;
    }

    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        Inventories.readNbt(nbt, this.inventory);
        this.burnTime = nbt.getShort("BurnTime");
        this.fuelTime = this.getFuelTime((ItemStack)this.inventory.get(0));
    }

    public NbtCompound writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putShort("BurnTime", (short)this.burnTime);
        Inventories.writeNbt(nbt, this.inventory);
        return nbt;
    }

    public static void tick(World world, BlockPos pos, BlockState state, FurnaceGeneratorBlockEntity blockEntity) {
        boolean bl = blockEntity.isBurning();
        boolean bl2 = false;
        if (blockEntity.isBurning()) {
            blockEntity.power += blockEntity.energyRate;
            --blockEntity.burnTime;
        }

        ItemStack itemStack = blockEntity.inventory.get(0); // Fuel
        if (blockEntity.isBurning() || !itemStack.isEmpty()) {
            if (!blockEntity.isBurning()) {
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

    public int[] getAvailableSlots(Direction side) {
        if (side == Direction.DOWN) {
            return BOTTOM_SLOTS;
        } else {
            return side == Direction.UP ? TOP_SLOTS : SIDE_SLOTS;
        }
    }

    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return this.isValid(slot, stack);
    }

    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        if (dir == Direction.DOWN && slot == 1) {
            return stack.isOf(Items.WATER_BUCKET) || stack.isOf(Items.BUCKET);
        } else {
            return true;
        }
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    public int size() {
        return this.inventory.size();
    }

    public boolean isEmpty() {
        Iterator var1 = this.inventory.iterator();

        ItemStack itemStack;
        do {
            if (!var1.hasNext()) {
                return true;
            }

            itemStack = (ItemStack)var1.next();
        } while(itemStack.isEmpty());

        return false;
    }

    public ItemStack getStack(int slot) {
        return this.inventory.get(slot);
    }

    public ItemStack removeStack(int slot, int amount) {
        return Inventories.splitStack(this.inventory, slot, amount);
    }

    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(this.inventory, slot);
    }

    public void setStack(int slot, ItemStack stack) {
        ItemStack itemStack = this.inventory.get(slot);
        this.inventory.set(slot, stack);
        if (stack.getCount() > this.getMaxCountPerStack()) {
            stack.setCount(this.getMaxCountPerStack());
        }
        this.markDirty();

    }

    public boolean canPlayerUse(PlayerEntity player) {
        if (this.world.getBlockEntity(this.pos) != this) {
            return false;
        } else {
            return player.squaredDistanceTo((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
        }
    }

    public boolean isValid(int slot, ItemStack stack) {
        return canUseAsFuel(stack);
    }

    public void clear() {
        this.inventory.clear();
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        //We provide *this* to the screenHandler as our class Implements Inventory
        //Only the Server has the Inventory at the start, this will be synced to the client in the ScreenHandler
        return new FurnaceGeneratorScreenHandler(syncId, playerInventory, this, propertyDelegate);
    }

    public double getTransferRate() {
        return transferRate;
    }

    @Override
    public double getEnergy() {
        return this.power;
    }

    @Override
    public double getEnergyCapacity() {
        return maxPower;
    }

    @Override
    public boolean supportsInsertion() {
        return false;
    }

    @Override
    public boolean supportsExtraction() {
        return true;
    }

    @Override
    public double extract(double maxAmount, Simulation simulation) {
        if(simulation == Simulation.SIMULATE) {
            return Math.min(power, maxAmount);
        }
        double drain = Math.min(power, maxAmount);
        power -= drain;
        markDirty();
        if(world != null && !world.isClient()) {
            //sync();
        }
        return drain;
    }

}

