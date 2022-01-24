package gay.nyako.infinitech.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;

public abstract class AbstractGeneratorBlockEntity extends AbstractMachineBlockEntity {
    protected AbstractGeneratorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, long capacity, long transferRate) {
        super(type, pos, state, capacity, transferRate);
    }

    protected AbstractGeneratorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, long capacity) {
        super(type, pos, state, capacity);
    }
}
