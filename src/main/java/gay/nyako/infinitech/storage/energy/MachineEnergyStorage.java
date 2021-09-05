package gay.nyako.infinitech.storage.energy;

import gay.nyako.infinitech.block.AbstractMachineBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import team.reborn.energy.api.EnergyStorage;

public class MachineEnergyStorage extends SnapshotParticipant<Long> implements EnergyStorage {
    private final AbstractMachineBlockEntity machine;

    public MachineEnergyStorage(AbstractMachineBlockEntity machine) {
        this.machine = machine;
    }

    @Override
    public long getAmount() {
        return machine.energy;
    }

    @Override
    public long getCapacity() {
        return machine.capacity;
    }

    @Override
    public boolean supportsInsertion() {
        return machine.canInsert;
    }

    @Override
    public boolean supportsExtraction() {
        return machine.canExtract;
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notNegative(maxAmount);

        if (machine.canInsert) {
            maxAmount = Math.min(maxAmount, machine.transferRate);
            var inserted = Math.min(maxAmount, machine.capacity - machine.energy);

            if (inserted > 0) {
                updateSnapshots(transaction);
                machine.energy += inserted;

                return inserted;
            }
        }

        return 0;
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notNegative(maxAmount);

        if (machine.canExtract) {
            maxAmount = Math.min(maxAmount, machine.transferRate);
            var extracted = Math.min(maxAmount, machine.energy);

            if (extracted > 0) {
                updateSnapshots(transaction);
                machine.energy -= extracted;

                return extracted;
            }
        }

        return 0;
    }

    @Override
    protected Long createSnapshot() {
        return machine.energy;
    }

    @Override
    protected void readSnapshot(Long snapshot) {
        machine.energy = snapshot;
    }

    @Override
    protected void onFinalCommit() {
        machine.markDirty();
    }
}
