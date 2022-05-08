package gay.nyako.infinitech.block.pipe;

import alexiil.mc.lib.multipart.api.AbstractPart;
import alexiil.mc.lib.multipart.api.MultipartContainer.PartOffer;
import alexiil.mc.lib.multipart.api.MultipartHolder;
import alexiil.mc.lib.multipart.api.MultipartUtil;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.function.Function;

public class PipePartItem extends Item {
    private final Function<MultipartHolder, AbstractPart> factory;

    public PipePartItem(Settings settings, Function<MultipartHolder, AbstractPart> factory) {
        super(settings);
        this.factory = factory;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var world = context.getWorld();
        if (world.isClient) {
            return ActionResult.PASS;
        }

        var pos = context.getBlockPos().offset(context.getSide());
        var offer = MultipartUtil.offerNewPart(world, pos, h -> factory.apply(h));
        if (offer == null) {
            return ActionResult.FAIL;
        }
        offer.apply();
        offer.getHolder().getPart().onPlacedBy(context.getPlayer(), context.getHand());
        context.getStack().increment(-1);

        var state = world.getBlockState(pos);
        var soundType = state.getSoundGroup();
        world.playSound(null, pos, soundType.getPlaceSound(), SoundCategory.BLOCKS, soundType.getVolume(), soundType.getPitch());

        return ActionResult.SUCCESS;
    }
}
