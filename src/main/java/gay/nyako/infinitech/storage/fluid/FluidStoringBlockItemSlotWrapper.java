package gay.nyako.infinitech.storage.fluid;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

public class FluidStoringBlockItemSlotWrapper extends SnapshotParticipant<ResourceAmount<FluidVariant>> implements SingleSlotStorage<FluidVariant> {
    public final FluidStoringBlockItemStorage storage;
    public final FluidSlot slot;

    public FluidStoringBlockItemSlotWrapper(FluidStoringBlockItemStorage storage, FluidSlot slot) {
        this.storage = storage;
        this.slot = slot;
    }

    @Override
    public FluidVariant getResource() {
        return slot.fluid;
    }

    @Override
    public boolean isResourceBlank() {
        return slot.fluid.isBlank();
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
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);

        if (resource.equals(slot.fluid) || slot.fluid.isBlank()) {
            var inserted = Math.min(maxAmount, slot.capacity - slot.amount);

            if (inserted > 0) {
                updateSnapshots(transaction);

                if (slot.fluid.isBlank()) {
                    slot.fluid = resource;
                    slot.amount = inserted;
                } else {
                    slot.amount += inserted;
                }

                storage.replaceItem(transaction);
            }

            return inserted;
        }

        return 0;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);

        if (resource.equals(slot.fluid)) {
            var extracted = Math.min(maxAmount, slot.amount);

            if (extracted > 0) {
                updateSnapshots(transaction);
                slot.amount -= extracted;

                if (slot.amount == 0) {
                    slot.fluid = FluidVariant.blank();
                }

                storage.replaceItem(transaction);
            }

            return extracted;
        }

        return 0;
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
}
