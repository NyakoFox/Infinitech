package gay.nyako.infinitech.storage;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.util.math.Direction;

public class SidedFluidSlotWrapper extends SnapshotParticipant<ResourceAmount<FluidVariant>> implements SingleSlotStorage<FluidVariant> {
    private final FluidInventory inventory;
    private final int slotIndex;
    private final FluidSlot slot;
    private final Direction direction;

    public SidedFluidSlotWrapper(FluidInventory inventory, int slot, Direction direction) {
        this.inventory = inventory;
        this.slotIndex = slot;
        this.slot = inventory.getFluidSlot(slot);
        this.direction = direction;
    }

    @Override
    public long insert(FluidVariant insertedVariant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(insertedVariant, maxAmount);

        if ((insertedVariant.equals(slot.fluid) || slot.fluid.isBlank()) && inventory.canInsert(slotIndex, insertedVariant, direction)) {
            long insertedAmount = Math.min(maxAmount, slot.capacity - slot.amount);

            if (insertedAmount > 0) {
                updateSnapshots(transaction);

                if (slot.fluid.isBlank()) {
                    slot.fluid = insertedVariant;
                    slot.amount = insertedAmount;
                } else {
                    slot.amount += insertedAmount;
                }
            }

            return insertedAmount;
        }

        return 0;
    }

    @Override
    public long extract(FluidVariant extractedVariant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(extractedVariant, maxAmount);

        if (extractedVariant.equals(slot.fluid) && inventory.canExtract(slotIndex, extractedVariant, direction)) {
            long extractedAmount = Math.min(maxAmount, slot.amount);

            if (extractedAmount > 0) {
                updateSnapshots(transaction);
                slot.amount -= extractedAmount;

                if (slot.amount == 0) {
                    slot.fluid = FluidVariant.blank();
                }
            }

            return extractedAmount;
        }

        return 0;
    }

    @Override
    public boolean isResourceBlank() {
        return slot.fluid.isBlank();
    }

    @Override
    public FluidVariant getResource() {
        return slot.fluid;
    }

    @Override
    public long getAmount() {
        return slot.amount;
    }

    @Override
    public long getCapacity() {
        return slot.capacity;
    }

    @Override
    protected ResourceAmount<FluidVariant> createSnapshot() {
        return new ResourceAmount<>(slot.fluid, slot.amount);
    }

    @Override
    protected void readSnapshot(ResourceAmount<FluidVariant> snapshot) {
        slot.fluid = snapshot.resource();
        slot.amount = snapshot.amount();
    }

    @Override
    protected void onFinalCommit() {
        inventory.markDirty();
    }
}
