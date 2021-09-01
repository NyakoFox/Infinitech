package gay.nyako.infinitech.storage;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface FluidInventory {
    List<FluidSlot> getFluidSlots();

    default FluidSlot getFluidSlot(int slot) {
        return getFluidSlots().get(slot);
    }

    int[] getAvailableFluidSlots(Direction side);

    boolean canInsert(int slot, FluidVariant fluid, @Nullable Direction dir);

    boolean canExtract(int slot, FluidVariant fluid, Direction dir);

    default void markDirty() { }

    static NbtCompound writeNbt(NbtCompound nbt, List<FluidSlot> slots) {
        NbtList nbtList = new NbtList();

        for(int i = 0; i < slots.size(); ++i) {
            FluidSlot slot = slots.get(i);

            var identifier = Registry.FLUID.getId(slot.fluid.getFluid());

            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.putByte("Slot", (byte)i);
            nbtCompound.putString("id", identifier.toString());
            nbtCompound.putLong("amount", slot.amount);
            if (slot.fluid.hasNbt()) {
                nbtCompound.put("tag", slot.fluid.copyNbt());
            }

            nbtList.add(nbtCompound);
        }

        nbt.put("Fluids", nbtList);

        return nbt;
    }

    static void readNbt(NbtCompound nbt, List<FluidSlot> slots) {
        NbtList nbtList = nbt.getList("Fluids", 10);

        for(int i = 0; i < nbtList.size(); ++i) {
            NbtCompound nbtCompound = nbtList.getCompound(i);
            int j = nbtCompound.getByte("Slot") & 255;
            if (j >= 0 && j < slots.size()) {
                var id = nbtCompound.getString("id");
                var amount = nbtCompound.getLong("amount");

                var fluid = Registry.FLUID.get(new Identifier(id));
                FluidVariant variant;
                if (nbtCompound.contains("tag")) {
                    variant = FluidVariant.of(fluid, nbtCompound.getCompound("tag"));
                } else {
                    variant = FluidVariant.of(fluid);
                }

                var slot = slots.get(j);
                slot.fluid = variant;
                slot.amount = amount;
            }
        }

    }
}
