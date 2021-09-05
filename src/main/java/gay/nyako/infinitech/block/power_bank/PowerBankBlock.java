package gay.nyako.infinitech.block.power_bank;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PowerBankBlock extends BlockWithEntity {

    public static final IntProperty PERCENTAGE = IntProperty.of("percentage", 0, 10);

    public PowerBankBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(PERCENTAGE, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(PERCENTAGE);
    }

    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PowerBankBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
        return ActionResult.SUCCESS;
    }
}
