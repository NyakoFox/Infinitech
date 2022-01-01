package gay.nyako.infinitech.block.block_breaker;

import gay.nyako.infinitech.InfinitechMod;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BlockBreakerBlock extends BlockWithEntity {

    public BlockBreakerBlock(Settings settings) {
        super(settings);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, InfinitechMod.BLOCK_BREAKER_BLOCK_ENTITY, BlockBreakerBlockEntity::tick);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BlockBreakerBlockEntity(pos, state);
    }
}