package gay.nyako.infinitech.storage;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface ImplementedFluidStoringBlockItem extends FluidStoringBlockItem {
    long getFluidCapacity();

    ItemStack getDefaultStack();

    @Override
    default List<FluidSlot> getFluidInventory(ItemStack stack) {
        if (stack.hasNbt() && stack.getNbt().contains("BlockEntityTag")) {
            var nbt = stack.getNbt().getCompound("BlockEntityTag");
            var slots = new ArrayList<FluidSlot>();
            FluidInventory.readNbt(nbt, slots);
            return slots;
        } else {
            return Arrays.asList(FluidSlot.blank(getFluidCapacity()));
        }
    }

    @Override
    default ItemStack fromFluidInventory(List<FluidSlot> inventory) {
        var stack = getDefaultStack();
        var allEmpty = true;
        for (var slot : inventory) {
            if (slot.amount > 0) {
                allEmpty = false;
                break;
            }
        }
        if (!allEmpty) {
            var nbt = stack.getOrCreateSubNbt("BlockEntityTag");
            FluidInventory.writeNbt(nbt, inventory);
        }
        return stack;
    }
}
