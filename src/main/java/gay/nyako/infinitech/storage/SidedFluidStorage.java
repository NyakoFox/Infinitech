package gay.nyako.infinitech.storage;

import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SidedFluidStorage extends CombinedStorage<FluidVariant, SidedFluidSlotWrapper> {
    private static final Map<SidedFluidStorageContext, SidedFluidStorage> WRAPPERS = new MapMaker().weakValues().makeMap();

    public static SidedFluidStorage of(FluidInventory inventory, @Nullable Direction direction) {
        var context = new SidedFluidStorageContext(inventory, direction);
        return WRAPPERS.computeIfAbsent(context, ctx -> new SidedFluidStorage(inventory, direction));
    }

    public SidedFluidStorage(FluidInventory inventory, Direction direction) {
        super(createWrappers(inventory, direction));
    }

    private static List<SidedFluidSlotWrapper> createWrappers(FluidInventory inventory, Direction direction) {
        var slots = inventory.getAvailableFluidSlots(direction);
        var wrappers = Lists.<SidedFluidSlotWrapper>newArrayList();
        for (int i = 0; i < slots.length; i++) {
            wrappers.add(new SidedFluidSlotWrapper(inventory, i, direction));
        }
        return wrappers;
    }
}
