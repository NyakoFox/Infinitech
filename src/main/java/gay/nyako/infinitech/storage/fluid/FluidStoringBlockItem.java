package gay.nyako.infinitech.storage.fluid;

import net.minecraft.item.ItemStack;

import java.util.List;

public interface FluidStoringBlockItem {
    List<FluidSlot> getFluidInventory(ItemStack stack);

    ItemStack fromFluidInventory(List<FluidSlot> inventory);
}
