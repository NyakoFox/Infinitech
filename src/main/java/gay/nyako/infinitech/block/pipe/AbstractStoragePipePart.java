package gay.nyako.infinitech.block.pipe;

import alexiil.mc.lib.multipart.api.MultipartHolder;
import alexiil.mc.lib.multipart.api.PartDefinition;
import alexiil.mc.lib.net.*;
import gay.nyako.infinitech.InfinitechMod;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;

public abstract class AbstractStoragePipePart<T> extends AbstractPipePart {
    public Mode mode;

    public AbstractStoragePipePart(PartDefinition definition, MultipartHolder holder) {
        super(definition, holder);
        mode = Mode.INSERT;
    }

    public abstract Storage<T> getStorage(Direction side);

    protected abstract BlockApiLookup<Storage<T>, Direction> getLookup();

    public List<Storage<T>> getConnectedStorages(@Nullable Direction ignoreSide) {
        var checked = new HashSet<BlockPos>();
        if (ignoreSide != null) {
            checked.add(holder.getContainer().getMultipartPos().offset(ignoreSide));
        }
        return getNetworkConnections(checked).stream()
                .map((ctx) -> ctx.lookup(getLookup()))
                .filter((storage) -> storage != null && storage.supportsInsertion()).toList();
    }

    @Override
    public void tick() {
        if (!holder.getContainer().isClientWorld() && mode == Mode.EXTRACT) {
            var connections = getConnections();
            var ignorePositions = new HashSet<BlockPos>();
            for (PipeConnectionContext context : connections) {
                ignorePositions.add(context.pos());
            }
            for (PipeConnectionContext context : connections) {
                var storage = context.lookup(getLookup());
                var ownStorage = getStorage(context.direction());
                if (storage.supportsExtraction()) {
                    try (Transaction transaction = Transaction.openOuter()) {
                        for (StorageView<T> view : storage.iterable(transaction)) {
                            if (!view.isResourceBlank()) {
                                var resource = view.getResource();

                                long extracted;
                                try (Transaction testExtraction = transaction.openNested()) {
                                    extracted = view.extract(resource, 1, testExtraction);
                                }

                                long inserted = ownStorage.insert(resource, extracted, transaction);

                                if (inserted > 0) {
                                    view.extract(resource, inserted, transaction);
                                    transaction.commit();
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

    @Override
    public ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!player.world.isClient) {
            switch (this.mode) {
                case INSERT: this.mode = Mode.EXTRACT; break;
                case EXTRACT: this.mode = Mode.INSERT; break;
            }
            player.sendMessage(new LiteralText("Switched mode: " + mode.toString()), true);
        }
        updateConnections();
        return ActionResult.SUCCESS;
    }

    public enum Mode {
        INSERT,
        EXTRACT
    }
}
