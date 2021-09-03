package gay.nyako.infinitech.block.fluid_tank;

import gay.nyako.infinitech.InfinitechMod;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class FluidTankBlock extends BlockWithEntity {
    public static IntProperty LUMINANCE = IntProperty.of("luminance", 0, 15);

    public final long capacity;

    public FluidTankBlock(long capacity, Settings settings) {
        super(settings);
        this.setDefaultState(getStateManager().getDefaultState().with(LUMINANCE, 0));
        this.capacity = capacity;
    }

    public static int getLuminance(BlockState state) {
        return state.get(LUMINANCE);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new FluidTankBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        var context = ContainerItemContext.ofPlayerHand(player, hand);
        var itemStorage = context.find(FluidStorage.ITEM);
        var ownStorage = FluidStorage.SIDED.find(world, pos, hit.getSide());
        if (itemStorage != null && ownStorage != null) {
            try (Transaction transaction = Transaction.openOuter()) {
                Optional<SoundEvent> soundEvent = Optional.empty();
                ResourceAmount resourceAmount = StorageUtil.findExtractableContent(itemStorage,transaction);
                if (resourceAmount != null) {
                    soundEvent = ((FluidVariant) resourceAmount.resource()).getFluid().getBucketFillSound();

                    var inserted = StorageUtil.move(itemStorage, ownStorage, variant -> true, resourceAmount.amount(), transaction);

                    if (inserted > 0) {
                        if (soundEvent.isPresent()) {
                            world.playSound(pos.getX(), pos.getY(), pos.getZ(), soundEvent.get(), SoundCategory.BLOCKS, 1f, 1f, true);
                        }
                        transaction.commit();
                        return ActionResult.SUCCESS;
                    }
                }
            }

            try (Transaction transaction = Transaction.openOuter()) {
                Optional<SoundEvent> soundEvent = Optional.empty();
                ResourceAmount resourceAmount = StorageUtil.findExtractableContent(ownStorage,transaction);
                if (resourceAmount != null) {
                    soundEvent = ((FluidVariant) resourceAmount.resource()).getFluid().getBucketFillSound();

                    var extracted = StorageUtil.move(ownStorage, itemStorage, variant -> true, resourceAmount.amount(), transaction);

                    if (extracted > 0) {
                        if (soundEvent.isPresent()) {
                            world.playSound(pos.getX(), pos.getY(), pos.getZ(), soundEvent.get(), SoundCategory.BLOCKS, 1f, 1f, true);
                        }
                        transaction.commit();
                        return ActionResult.SUCCESS;
                    }
                }
            }
        }

        return ActionResult.SUCCESS;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LUMINANCE);
    }
}
