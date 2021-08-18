package gay.nyako.infinitech.block.power_bank;

import gay.nyako.infinitech.block.furnace_generator.FurnaceGeneratorBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PowerBankBlock extends BlockWithEntity {
    public PowerBankBlock(Settings settings) {
        super(settings);
    }

    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PowerBankBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        PowerBankBlockEntity blockEntity = (PowerBankBlockEntity) world.getBlockEntity(pos);
        String heck = blockEntity.getEnergy() + "/" + blockEntity.getEnergyCapacity();
        player.sendMessage(new LiteralText(heck), false);
        return ActionResult.SUCCESS;
    }
}
