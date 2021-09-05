package gay.nyako.infinitech.storage.fluid;

import com.google.common.collect.Lists;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.item.ItemStack;

import java.util.List;

public class FluidStoringBlockItemStorage extends CombinedStorage<FluidVariant, FluidStoringBlockItemSlotWrapper> {
    private FluidStoringBlockItem item;
    private ContainerItemContext context;
    private List<FluidSlot> slots;

    public FluidStoringBlockItemStorage(FluidStoringBlockItem item, ItemStack stack, ContainerItemContext context) {
        super(Lists.newArrayList());
        this.item = item;
        this.context = context;
        this.slots = item.getFluidInventory(stack);
        for (var slot : slots) {
            parts.add(new FluidStoringBlockItemSlotWrapper(this, slot));
        }
    }

    public void replaceItem(TransactionContext transaction) {
        var newStack = item.fromFluidInventory(slots);
        context.exchange(ItemVariant.of(newStack), 1, transaction);
    }
}
