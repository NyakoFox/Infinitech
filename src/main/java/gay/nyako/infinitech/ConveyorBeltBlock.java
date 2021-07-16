package gay.nyako.infinitech;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.stream.Stream;

public class ConveyorBeltBlock extends BlockWithEntity {

    public ConveyorBeltBlock(Settings settings) {
        super(settings);
        setDefaultState(this.stateManager.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, InfinitechMod.CONVEYOR_BELT_BLOCK_ENTITY, ConveyorBeltBlockEntity::tick);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ConveyorBeltBlockEntity(pos, state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(Properties.HORIZONTAL_FACING);
    }

    // idk if we need this ,,
    @Override
    @SuppressWarnings("deprecation")
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
        Direction dir = state.get(Properties.HORIZONTAL_FACING);
        return switch (dir) {
            default -> Stream.of(
                    Block.createCuboidShape(1, 0, 0, 15, 2, 16),
                    Block.createCuboidShape(0, 0, 0, 1, 4, 16),
                    Block.createCuboidShape(15, 0, 0, 16, 4, 16)
            ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();
            case EAST, WEST -> Stream.of(
                    Block.createCuboidShape(0, 0, 1, 16, 2, 15),
                    Block.createCuboidShape(0, 0, 15, 16, 4, 16),
                    Block.createCuboidShape(0, 0, 0, 16, 4, 1)
            ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();
        };
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(Properties.HORIZONTAL_FACING, rotation.rotate(state.get(Properties.HORIZONTAL_FACING)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(Properties.HORIZONTAL_FACING)));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return this.getDefaultState().with(Properties.HORIZONTAL_FACING, context.getPlayerFacing());
    }

    //This method will drop all items onto the ground when the block is broken
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof ConveyorBeltBlockEntity) {
                ItemScatterer.spawn(world, pos, (ConveyorBeltBlockEntity)blockEntity);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public ActionResult onUse(BlockState blockState, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockHitResult blockHitResult) {
        if (world.isClient) return ActionResult.SUCCESS;
        ConveyorBeltBlockEntity blockEntity = (ConveyorBeltBlockEntity) world.getBlockEntity(blockPos);
        Inventory inventory = blockEntity;

        if (!player.getStackInHand(hand).isEmpty()) {
            // Put an item from the player's hand in
            if (inventory.getStack(0).isEmpty()) {
                // Put the stack the player is holding into the inventory
                ItemStack itemStack = player.getStackInHand(hand).copy();
                itemStack.setCount(1);
                inventory.setStack(0, itemStack);
                // Remove the stack from the player's hand
                player.getStackInHand(hand).setCount(player.getStackInHand(hand).getCount() - 1);
                blockEntity.progress = 0.0f;
            }
        } else {
            // If the player is not holding anything we'll get give them the item in the block entity

            // Give it to the player
            if (!inventory.getStack(0).isEmpty()) {
                player.getInventory().offerOrDrop(inventory.getStack(0));
                inventory.removeStack(0);
                blockEntity.progress = 0.0f;
            }
        }
        inventory.markDirty();

        return ActionResult.SUCCESS;
    }
}
