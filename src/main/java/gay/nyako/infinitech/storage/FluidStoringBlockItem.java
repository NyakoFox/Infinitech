package gay.nyako.infinitech.storage;

import net.minecraft.item.ItemStack;

import java.util.List;

public interface FluidStoringBlockItem {
    List<FluidSlot> getFluidInventory(ItemStack stack);

    ItemStack fromFluidInventory(List<FluidSlot> inventory);
}
