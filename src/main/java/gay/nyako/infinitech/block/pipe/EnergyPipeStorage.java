package gay.nyako.infinitech.block.pipe;

import com.google.common.collect.MapMaker;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.util.math.Direction;
import team.reborn.energy.api.EnergyStorage;

import java.util.List;
import java.util.Map;

public class EnergyPipeStorage implements EnergyStorage, Transaction.CloseCallback, Transaction.OuterCloseCallback {
    private static final Map<PipeSideContext, EnergyPipeStorage> CACHE = new MapMaker().weakValues().makeMap();

    public static EnergyPipeStorage of(EnergyPipePart pipe, Direction side) {
        return CACHE.computeIfAbsent(new PipeSideContext(pipe, side), p -> new EnergyPipeStorage(pipe, side));
    }

    private final EnergyPipePart pipe;
    private final Direction side;

    public EnergyPipeStorage(EnergyPipePart pipe, Direction side) {
        this.pipe = pipe;
        this.side = side;
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notNegative(maxAmount);

        var storages = pipe.getConnectedStorages(side);

        if (storages.size() > 0) {
            List<EnergyStorage> validStorages;
            try (Transaction nested = transaction.openNested()) {
                validStorages = storages.stream().filter((storage) -> storage.insert(pipe.transferRate, nested) > 0).toList();
            }

            if (validStorages.size() > 0) {
                var perStorage = maxAmount / validStorages.size();

                var inserted = 0;
                for (EnergyStorage storage : validStorages) {
                    inserted += storage.insert(perStorage, transaction);
                }

                if (inserted > 0) {
                    transaction.addCloseCallback(this);
                }

                return inserted;
            }
        }

        return 0;
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        return 0;
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
    public void onClose(TransactionContext transaction, TransactionContext.Result result) {
        if (result.wasCommitted()) {
            if (transaction.nestingDepth() > 0) {
                transaction.getOpenTransaction(transaction.nestingDepth() - 1).addCloseCallback(this);
            } else {
                transaction.addOuterCloseCallback(this);
            }
        }
    }

    @Override
    public void afterOuterClose(TransactionContext.Result result) {
        pipe.transferCountdown = EnergyPipePart.TRANSFER_TIMER;
    }
}
