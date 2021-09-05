package gay.nyako.infinitech.block.fluid_tank;

import gay.nyako.infinitech.storage.fluid.ImplementedFluidStoringBlockItem;
import net.minecraft.item.BlockItem;

public class FluidTankBlockItem extends BlockItem implements ImplementedFluidStoringBlockItem {
    private final FluidTankBlock tankBlock;

    public FluidTankBlockItem(FluidTankBlock block, Settings settings) {
        super(block, settings);
        this.tankBlock = block;
    }

    @Override
    public long getFluidCapacity() {
        return tankBlock.capacity;
    }
}
