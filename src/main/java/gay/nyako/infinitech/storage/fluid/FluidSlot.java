package gay.nyako.infinitech.storage.fluid;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;

public class FluidSlot {
    public FluidVariant fluid;
    public long amount;
    public long capacity;

    public FluidSlot(FluidVariant fluid, long amount, long capacity) {
        this.fluid = fluid;
        this.amount = amount;
        this.capacity = capacity;
    }

    public static FluidSlot blank(long capacity) {
        return new FluidSlot(FluidVariant.blank(), 0, capacity);
    }
}
