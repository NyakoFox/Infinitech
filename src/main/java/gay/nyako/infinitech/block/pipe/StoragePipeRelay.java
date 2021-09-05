package gay.nyako.infinitech.block.pipe;

import com.google.common.collect.MapMaker;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.util.math.Direction;

import java.util.Map;

public class StoragePipeRelay<T> extends SnapshotParticipant<Integer> implements SingleSlotStorage<T> {
    private static final Map<PipeSideContext, StoragePipeRelay> CACHE = new MapMaker().weakValues().makeMap();

    public static <O> StoragePipeRelay<O> of(AbstractStoragePipePart<O> pipe, Direction side, O blankVariant) {
        return CACHE.computeIfAbsent(new PipeSideContext(pipe, side), p -> new StoragePipeRelay(pipe, side, blankVariant));
    }

    private final AbstractStoragePipePart<T> pipe;
    private final Direction side;
    private final T blankVariant;
    private int nextInsertion;

    public StoragePipeRelay(AbstractStoragePipePart<T> pipe, Direction side, T blankVariant) {
        this.pipe = pipe;
        this.side = side;
        this.blankVariant = blankVariant;
    }

    @Override
    public long insert(T resource, long maxAmount, TransactionContext transaction) {
        var storages = pipe.getConnectedStorages(side);

        if (storages.size() == 0) {
            return 0;
        }

        updateSnapshots(transaction);

        var toInsert = maxAmount;
        var failedAttempts = 0;
        while (toInsert > 0) {
            long inserted = storages.get(nextInsertion).insert(resource, 1, transaction);
            toInsert -= inserted;
            if (inserted > 0) {
                failedAttempts = 0;
            } else if (failedAttempts++ >= storages.size()) {
                break;
            }
            nextInsertion = (nextInsertion + 1) % storages.size();
        }

        return maxAmount - toInsert;
    }

    @Override
    public long extract(T resource, long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public boolean supportsExtraction() {
        return false;
    }

    @Override
    public boolean isResourceBlank() {
        return true;
    }

    @Override
    public T getResource() {
        return this.blankVariant;
    }

    @Override
    public long getAmount() {
        return 0;
    }

    @Override
    public long getCapacity() {
        return Long.MAX_VALUE;
    }

    @Override
    protected Integer createSnapshot() {
        return this.nextInsertion;
    }

    @Override
    protected void readSnapshot(Integer snapshot) {
        this.nextInsertion = snapshot;
    }
}
