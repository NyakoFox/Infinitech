package gay.nyako.infinitech.block.block_breaker;

import gay.nyako.infinitech.InfinitechMod;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockBreakerBlockEntity extends BlockEntity {
    public BlockBreakerBlockEntity(BlockPos pos, BlockState state) {
        super(InfinitechMod.BLOCK_BREAKER_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, BlockBreakerBlockEntity blockEntity) {

    }
}