package gay.nyako.infinitech.block.pipe;

import alexiil.mc.lib.multipart.api.MultipartHolder;
import alexiil.mc.lib.multipart.api.PartDefinition;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractStoragePipePart<T> extends AbstractIOPipePart {
    public AbstractStoragePipePart(PartDefinition definition, MultipartHolder holder) {
        super(definition, holder);
    }

    public abstract long getTransferRate();

    public abstract Storage<T> getStorage(Direction side);

    protected abstract BlockApiLookup<Storage<T>, Direction> getLookup();

    public List<Storage<T>> getConnectedStorages(@Nullable Direction ignoreSide) {
        var checked = new HashSet<BlockPos>();
        if (ignoreSide != null) {
            checked.add(holder.getContainer().getMultipartPos().offset(ignoreSide));
        }
        return getNetworkConnections(checked).stream()
                .filter((ctx) -> ((AbstractIOPipePart)ctx.pipe()).getMode(ctx.direction()).isInsert())
                .map((ctx) -> ctx.lookup(getLookup()))
                .filter((storage) -> storage != null && storage.supportsInsertion()).toList();
    }

    @Override
    public void tick() {
        super.tick();

        if (holder.getContainer().isClientWorld()) return;

        var connections = getConnections();
        var ignorePositions = new HashSet<BlockPos>();
        for (PipeConnectionContext context : connections) {
            ignorePositions.add(context.pos());
        }
        for (PipeConnectionContext context : connections) {
            if (!getMode(context.direction()).isExtract()) continue;

            var storage = context.lookup(getLookup());
            var ownStorage = getStorage(context.direction());
            if (storage.supportsExtraction()) {
                try (Transaction transaction = Transaction.openOuter()) {
                    var toExtract = getTransferRate();

                    var iterator = storage.iterator();
                    while (iterator.hasNext()) {
                        var view = iterator.next();
                        if (!view.isResourceBlank()) {
                            var resource = view.getResource();

                            long extracted;
                            try (Transaction testExtraction = transaction.openNested()) {
                                extracted = view.extract(resource, toExtract, testExtraction);
                            }

                            if (extracted > 0) {
                                long inserted = ownStorage.insert(resource, extracted, transaction);

                                if (inserted > 0) {
                                    view.extract(resource, inserted, transaction);
                                    transaction.commit();

                                    toExtract -= inserted;
                                    if (toExtract == 0) {
                                        break;
                                    }
                                } else {
                                    transaction.abort();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean canConnectTo(PipeConnectionContext context) {
        var storage = context.lookup(getLookup());
        if (storage != null) {
            return storage.supportsInsertion() || storage.supportsExtraction();
        }
        return false;
    }
}
