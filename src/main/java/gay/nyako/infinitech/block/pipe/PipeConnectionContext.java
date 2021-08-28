package gay.nyako.infinitech.block.pipe;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public record PipeConnectionContext(AbstractPipePart pipe, World world, BlockPos pos, Direction direction) {
    public <T> T lookup(BlockApiLookup<T, Direction> lookup) {
        return lookup.find(world(), pos(), direction().getOpposite());
    }
}
