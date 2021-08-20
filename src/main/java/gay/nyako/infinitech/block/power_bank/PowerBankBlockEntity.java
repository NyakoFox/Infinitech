package gay.nyako.infinitech.block.power_bank;

import gay.nyako.infinitech.InfinitechMod;
import gay.nyako.infinitech.block.AbstractMachineBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class PowerBankBlockEntity extends AbstractMachineBlockEntity {
    public PowerBankBlockEntity(BlockPos pos, BlockState state) {
        super(InfinitechMod.POWER_BANK_BLOCK_ENTITY, pos, state, 2_000_000, 10_000);
        canInsert = true;
        canExtract = true;
    }
}
