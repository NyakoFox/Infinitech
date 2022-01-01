package gay.nyako.infinitech.block.fluid_tank;

import gay.nyako.infinitech.InfinitechMod;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
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

        Storage ownStorage = FluidStorage.SIDED.find(world, pos, hit.getSide());

        ItemStack itemStack = player.getStackInHand(hand);
        if (itemStack.isOf(Items.GLASS_BOTTLE)) {
            try (Transaction transaction = Transaction.openOuter()) {
                long extractionAmount = 2700L * 10;
                if (ownStorage.simulateExtract(FluidVariant.of(InfinitechMod.STILL_LIQUID_XP), extractionAmount, transaction) == extractionAmount) {
                    ownStorage.extract(FluidVariant.of(InfinitechMod.STILL_LIQUID_XP), extractionAmount, transaction);
                    transaction.commit();
                    itemStack.decrement(1);
                    world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.NEUTRAL, 1.0f, 1.0f);
                    if (itemStack.isEmpty()) {
                        player.setStackInHand(hand, new ItemStack(Items.EXPERIENCE_BOTTLE));
                    } else if (!player.getInventory().insertStack(new ItemStack(Items.EXPERIENCE_BOTTLE))) {
                        player.dropItem(new ItemStack(Items.EXPERIENCE_BOTTLE), false);
                    }
                    player.incrementStat(Stats.USED.getOrCreateStat(itemStack.getItem()));
                    world.emitGameEvent(player, GameEvent.FLUID_PICKUP, pos);
                    return ActionResult.SUCCESS;
                }
            }
        } else if (itemStack.isOf(Items.EXPERIENCE_BOTTLE)) {
            try (Transaction transaction = Transaction.openOuter()) {
                long insertionAmount = 2700L * 10;
                if (ownStorage.simulateInsert(FluidVariant.of(InfinitechMod.STILL_LIQUID_XP), insertionAmount, transaction) == insertionAmount) {
                    ownStorage.insert(FluidVariant.of(InfinitechMod.STILL_LIQUID_XP), insertionAmount, transaction);
                    transaction.commit();
                    itemStack.decrement(1);
                    world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.NEUTRAL, 1.0f, 1.0f);
                    if (itemStack.isEmpty()) {
                        player.setStackInHand(hand, new ItemStack(Items.GLASS_BOTTLE));
                    } else if (!player.getInventory().insertStack(new ItemStack(Items.GLASS_BOTTLE))) {
                        player.dropItem(new ItemStack(Items.GLASS_BOTTLE), false);
                    }
                    player.incrementStat(Stats.USED.getOrCreateStat(itemStack.getItem()));
                    world.emitGameEvent(player, GameEvent.FLUID_PLACE, pos);
                    return ActionResult.SUCCESS;
                }
            }
        }

        ContainerItemContext context = ContainerItemContext.ofPlayerHand(player, hand);
        Storage itemStorage = context.find(FluidStorage.ITEM);
        if (itemStorage != null && ownStorage != null) {
            try (Transaction transaction = Transaction.openOuter()) {
                SoundEvent soundEvent;
                ResourceAmount resourceAmount = StorageUtil.findExtractableContent(itemStorage,transaction);
                if (resourceAmount != null) {
                    soundEvent = ((FluidVariant) resourceAmount.resource()).getFluid().isIn(FluidTags.LAVA) ? SoundEvents.ITEM_BUCKET_EMPTY_LAVA : SoundEvents.ITEM_BUCKET_EMPTY;
                    var inserted = StorageUtil.move(itemStorage, ownStorage, variant -> true, resourceAmount.amount(), transaction);

                    if (inserted > 0) {
                        if (soundEvent != null) {
                            player.playSound(soundEvent,SoundCategory.BLOCKS, 1f,1f);
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
                            player.playSound(soundEvent.get(),SoundCategory.BLOCKS,1f,1f);
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
