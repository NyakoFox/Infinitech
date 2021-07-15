package gay.nyako.infinitech;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;

public class ConveyorBeltBlockEntityRenderer implements BlockEntityRenderer<ConveyorBeltBlockEntity> {

    public ConveyorBeltBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(ConveyorBeltBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();

        Direction dir = blockEntity.getCachedState().get(Properties.HORIZONTAL_FACING);

        ItemStack stack = blockEntity.getStack(0);

        if (!stack.isEmpty()) {
            // Move the item

            float offset = 0.25f;

            switch (dir) {
                case NORTH:
                    matrices.translate(0.5, 0.125, 1 - offset);
                    matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(0));
                    break;
                case SOUTH:
                    matrices.translate(0.5, 0.125, 0 + offset);
                    matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180));
                    break;
                case EAST:
                    matrices.translate(0 + offset, 0.125, 0.5);
                    matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(270));
                    break;
                case WEST:
                    matrices.translate(1 - offset, 0.125, 0.5);
                    matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90));
                    break;
            }

            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90)); // on its side (please work)

            int lightAbove = WorldRenderer.getLightmapCoordinates(blockEntity.getWorld(), blockEntity.getPos().up());
            MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Mode.GROUND, lightAbove, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, 42);
        }
        // Mandatory call after GL calls
        matrices.pop();
    }
}
