package gay.nyako.infinitech.block.xp_drain;

import gay.nyako.infinitech.InfinitechMod;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.Random;

public class XPDrainBlock extends Block {

    private final Random random = new Random();

    public XPDrainBlock(Settings settings) {
        super(settings);
    }

    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        if (world.isClient()) return;
        if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) entity;

            Storage<FluidVariant> below = FluidStorage.SIDED.find(world, pos.offset(Direction.DOWN), Direction.UP);
            if (below == null) return;
            if (!below.supportsInsertion()) return;

            try (Transaction transaction = Transaction.openOuter()) {
                long insertionAmount = Math.min(player.totalExperience, 10) * 2700L;
                long inserted = below.simulateInsert(FluidVariant.of(InfinitechMod.STILL_LIQUID_XP), insertionAmount, transaction);
                if (inserted < 2700) return; // It can't hold 1 XP, so don't do anything!!

                below.insert(FluidVariant.of(InfinitechMod.STILL_LIQUID_XP), insertionAmount, transaction);
                player.addExperience((int) -(Math.ceil(inserted / 2700L)));

                transaction.commit();
            }
            player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 0.1f, (this.random.nextFloat() - this.random.nextFloat()) * 0.35f + 0.9f);
            world.playSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 0.1f, (this.random.nextFloat() - this.random.nextFloat()) * 0.35f + 0.9f, true);
        }

        super.onSteppedOn(world, pos, state, entity);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
        return Block.createCuboidShape(0, 0, 0, 16, 4, 16);
    }
}