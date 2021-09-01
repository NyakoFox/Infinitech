package gay.nyako.infinitech.storage;

import net.minecraft.util.math.Direction;

public record SidedFluidStorageContext(FluidInventory inventory, Direction direction) {
}
