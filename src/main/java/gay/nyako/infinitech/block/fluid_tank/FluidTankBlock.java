package gay.nyako.infinitech.block.fluid_tank;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class FluidTankBlock extends BlockWithEntity {
    public final long capacity;

    public FluidTankBlock(long capacity, Settings settings) {
        super(settings);
        this.capacity = capacity;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new FluidTankBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        var context = ContainerItemContext.ofPlayerHand(player, hand);
        var itemStorage = context.find(FluidStorage.ITEM);
        var ownStorage = FluidStorage.SIDED.find(world, pos, hit.getSide());
        if (itemStorage != null && ownStorage != null) {
            try (Transaction transaction = Transaction.openOuter()) {
                var inserted = StorageUtil.move(itemStorage, ownStorage, variant -> true, FluidConstants.BUCKET, transaction);

                if (inserted > 0) {
                    transaction.commit();
                    return ActionResult.SUCCESS;
                }
            }

            try (Transaction transaction = Transaction.openOuter()) {
                var extracted = StorageUtil.move(ownStorage, itemStorage, variant -> true, FluidConstants.BUCKET, transaction);

                if (extracted > 0) {
                    transaction.commit();
                    return ActionResult.SUCCESS;
                }
            }
        }

        return ActionResult.PASS;
    }
}
