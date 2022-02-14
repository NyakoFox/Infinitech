package gay.nyako.infinitech.block;

import gay.nyako.infinitech.ImplementedInventory;
import gay.nyako.infinitech.block.furnace_generator.FurnaceGeneratorBlockEntity;
import gay.nyako.infinitech.item.StaffOfEnderItem;
import gay.nyako.infinitech.storage.energy.MachineEnergyStorage;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.Properties;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;
import team.reborn.energy.api.base.SimpleBatteryItem;

import java.util.HashMap;

public abstract class AbstractMachineBlockEntity extends SyncingBlockEntity implements ImplementedInventory, SidedInventory, InventoryProvider {
    public long energy = 0;
    public long oldEnergy = -1;
    public long difference = -1;
    public long capacity;
    public long transferRate;
    public boolean canInsert = true;
    public boolean canExtract = true;
    public MachineEnergyStorage energyStorage;
    public InventoryStorage storage;
    public HashMap<MachineUtil.Sides, MachineUtil.SideTypes> sides = new HashMap<>();
    public DefaultedList<ItemStack> inventory;
    private int[] slots;
    public int slotOffset = 0;

    protected AbstractMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, long capacity, long transferRate) {
        super(type, pos, state);
        this.capacity = capacity;
        this.transferRate = transferRate;
        this.energyStorage = new MachineEnergyStorage(this);
        sides.put(MachineUtil.Sides.FRONT,  MachineUtil.SideTypes.UNSET);
        sides.put(MachineUtil.Sides.BACK,   MachineUtil.SideTypes.UNSET);
        sides.put(MachineUtil.Sides.LEFT,   MachineUtil.SideTypes.UNSET);
        sides.put(MachineUtil.Sides.RIGHT,  MachineUtil.SideTypes.UNSET);
        sides.put(MachineUtil.Sides.TOP,    MachineUtil.SideTypes.UNSET);
        sides.put(MachineUtil.Sides.BOTTOM, MachineUtil.SideTypes.UNSET);

        int slots = getSlotAmount();
        if (hasBatterySlot()) {
            slotOffset++;
            slots++;
        }
        this.slots = new int[Math.max(slots - 1, 0)];
        this.inventory = DefaultedList.ofSize(slots, ItemStack.EMPTY);
        this.storage = InventoryStorage.of(this, null);
    }

    protected AbstractMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, long capacity) {
        this(type, pos, state, capacity, 1_000_000_000);
    }

    /**
     * The amount of slots the machine should have, minus any
     * slots that all machines have (for example, the battery slot).
     *
     * @return      the base amount of slots the inventory should have
     */
    public abstract int getSlotAmount();

    /**
     * Should the machine have a charging slot?
     *
     * @return      the battery slot
     */
    public abstract boolean hasBatterySlot();

    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.energy = nbt.getLong("Energy");
        this.difference = nbt.getLong("Difference");

        NbtCompound directionCompound = nbt.getCompound("SideConfiguration");
        sides.put(MachineUtil.Sides.FRONT,  MachineUtil.SideTypes.values()[directionCompound.getInt("FRONT" )]);
        sides.put(MachineUtil.Sides.BACK,   MachineUtil.SideTypes.values()[directionCompound.getInt("BACK"  )]);
        sides.put(MachineUtil.Sides.LEFT,   MachineUtil.SideTypes.values()[directionCompound.getInt("LEFT"  )]);
        sides.put(MachineUtil.Sides.RIGHT,  MachineUtil.SideTypes.values()[directionCompound.getInt("RIGHT" )]);
        sides.put(MachineUtil.Sides.TOP,    MachineUtil.SideTypes.values()[directionCompound.getInt("TOP"   )]);
        sides.put(MachineUtil.Sides.BOTTOM, MachineUtil.SideTypes.values()[directionCompound.getInt("BOTTOM")]);

        clear();
        Inventories.readNbt(nbt,inventory);
    }

    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putLong("Energy", this.energy);
        nbt.putLong("Difference", this.difference);

        NbtCompound directionCompound = new NbtCompound();
        directionCompound.putInt("FRONT",  sides.get(MachineUtil.Sides.FRONT ).ordinal());
        directionCompound.putInt("BACK",   sides.get(MachineUtil.Sides.BACK  ).ordinal());
        directionCompound.putInt("LEFT",   sides.get(MachineUtil.Sides.LEFT  ).ordinal());
        directionCompound.putInt("RIGHT",  sides.get(MachineUtil.Sides.RIGHT ).ordinal());
        directionCompound.putInt("TOP",    sides.get(MachineUtil.Sides.TOP   ).ordinal());
        directionCompound.putInt("BOTTOM", sides.get(MachineUtil.Sides.BOTTOM).ordinal());

        nbt.put("SideConfiguration", directionCompound);

        Inventories.writeNbt(nbt,inventory);
    }

    /*
        Can something insert to this direction?
     */
    public boolean canTransfer(Direction direction) {
        MachineUtil.Sides side = MachineUtil.DirectionToSide(getCachedState().get(Properties.FACING), direction);
        return (sides.get(side) != MachineUtil.SideTypes.OFF);
    }

    /**
     * Is this side disabled?
     *
     * @return      if a side is disabled
     */
    public boolean isSideDisabled(Direction direction) {
        Direction baseDir = getCachedState().get(Properties.HORIZONTAL_FACING);
        MachineUtil.Sides side = MachineUtil.DirectionToSide(baseDir, direction);
        MachineUtil.SideTypes sideType = sides.get(side);
        return (sideType == MachineUtil.SideTypes.OFF);
    }

    /**
     * Attempt transfers based on all sides.
     */
    public void attemptSideTransfers(InventoryStorage storage) {
        if (world.isClient()) return;
        Direction baseDir = getCachedState().get(Properties.HORIZONTAL_FACING);
        for (MachineUtil.Sides side : MachineUtil.Sides.values()) {
            Direction direction = MachineUtil.SideToRelativeDirection(side, baseDir);
            MachineUtil.SideTypes sideType = sides.get(side);

            if (sideType == MachineUtil.SideTypes.INPUT || sideType == MachineUtil.SideTypes.BOTH) {
                // Attempt to input!
                Storage<ItemVariant> front = ItemStorage.SIDED.find(world, pos.offset(direction), direction.getOpposite());
                long output = StorageUtil.move(front, storage, item -> true, 1, null);
            }
            if (sideType == MachineUtil.SideTypes.OUTPUT || sideType == MachineUtil.SideTypes.BOTH) {
                // Attempt to output
                Storage<ItemVariant> back = ItemStorage.SIDED.find(world, pos.offset(direction), direction.getOpposite());
                StorageUtil.move(storage, back, item -> true, 1, null);
            }
        }
    }

    /**
     * Attempt energy transfers based on all sides.
     */
    protected void attemptEnergyTransfers() {
        for (Direction dir : Direction.values()) {
            if (world.getBlockEntity(pos.offset(dir)) instanceof AbstractGeneratorBlockEntity) continue;
            EnergyStorage storage = EnergyStorage.SIDED.find(world,pos.offset(dir),dir.getOpposite());
            if (storage != null) {
                EnergyStorageUtil.move(energyStorage, storage, transferRate, null);
            }
        }
    }

    /**
     * Attempt to charge a battery.
     */
    public void processChargeSlot() {
        if (world.isClient()) return;
        if (!hasBatterySlot()) return;
        ItemStack itemStack = inventory.get(getBatteryIndex());
        if (!itemStack.isEmpty() && EnergyStorageUtil.isEnergyStorage(itemStack)) {
            NbtCompound stackNbt = itemStack.getOrCreateNbt();
            stackNbt.putInt("oldEnergy", stackNbt.getInt("energy"));
            itemStack.setNbt(stackNbt);
            // Grab the energy storage
            EnergyStorage itemEnergyStorage = ContainerItemContext.ofSingleSlot(InventoryStorage.of(this, null).getSlot(getBatteryIndex())).find(EnergyStorage.ITEM);
            // Move between our block and the item
            EnergyStorageUtil.move(energyStorage, itemEnergyStorage, Long.MAX_VALUE, null);
        }
    }

    public void calculateEnergyDifference() {
        difference = energy - oldEnergy;
        oldEnergy = energy;
    }

    public int getBatteryIndex() {
        if (hasBatterySlot()) return size() - 1;
        return -1;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        if (slot == getBatteryIndex()) return EnergyStorageUtil.isEnergyStorage(stack);
        return true;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        if (slot == getBatteryIndex()) return EnergyStorageUtil.isEnergyStorage(stack);
        return true;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return false;
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public SidedInventory getInventory(BlockState state, WorldAccess world, BlockPos pos) {
        return this;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return slots;
    }
}
