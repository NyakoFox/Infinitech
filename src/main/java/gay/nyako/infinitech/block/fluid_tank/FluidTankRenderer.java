package gay.nyako.infinitech.block.fluid_tank;

import com.google.common.collect.Lists;
import gay.nyako.infinitech.InfinitechModelProvider;
import gay.nyako.infinitech.storage.fluid.FluidInventory;
import gay.nyako.infinitech.storage.fluid.FluidSlot;
import java.util.ArrayList;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.ColorHelper;
import net.fabricmc.fabric.impl.renderer.RendererAccessImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

public class FluidTankRenderer implements BlockEntityRenderer<FluidTankBlockEntity>, BuiltinItemRendererRegistry.DynamicItemRenderer {
    private static final float EDGE_SIZE = 3f / 16f;
    private static final float INNER_SIZE = 1f - (EDGE_SIZE * 2f);

    @Override
    public void render(FluidTankBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        render(entity.getStoredVariant(), entity.getFillPercent(), entity.getWorld(), entity.getPos(), matrices, vertexConsumers, light, overlay);
    }

    @Override
    public void render(ItemStack stack, ModelTransformation.Mode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        /*var transformation = InfinitechModelProvider.FLUID_TANK_MODEL.getTransformation();
        transformation.getTransformation(mode).apply(false, matrices);*/

        var mesh = InfinitechModelProvider.FLUID_TANK_MODEL.mesh;
        renderMesh(mesh, matrices, vertexConsumers.getBuffer(RenderLayers.getItemLayer(stack, true)), light, overlay);

        if (stack.hasNbt()) {
            var nbt = stack.getNbt();
            if (nbt.contains("BlockEntityTag")) {
                var slots = Lists.<FluidSlot>newArrayList();
                FluidInventory.readNbt(nbt.getCompound("BlockEntityTag"), slots);
                var slot = slots.get(0);

                var world = MinecraftClient.getInstance().world;
                var pos = MinecraftClient.getInstance().player.getBlockPos();
                render(slot.fluid, (float) slot.amount / slot.capacity, world, pos, matrices, vertexConsumers, light, overlay);
            }
        }

        matrices.pop();
    }

    private void render(FluidVariant variant, float percent, @Nullable BlockRenderView view, @Nullable BlockPos pos, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (percent == 0f || variant.isBlank()) {
            return;
        }

        matrices.push();

        var handler = FluidVariantRendering.getHandlerOrDefault(variant.getFluid());

        var sprites = handler.getSprites(variant);
        var sprite = sprites[0]; // Um?

        var color = handler.getColor(variant, view, pos);
        var flipped = FluidVariantAttributes.isLighterThanAir(variant);
        var luminance = variant.getFluid().getDefaultState().getBlockState().getLuminance();

        var renderer = RendererAccessImpl.INSTANCE.getRenderer();
        var consumer = vertexConsumers.getBuffer(RenderLayer.getTranslucent());

        var builder = renderer.meshBuilder();
        var emitter = builder.getEmitter();

        var newColor = ColorHelper.swapRedBlueIfNeeded(color);

        emitFluidFace(emitter, sprite, newColor, flipped, Direction.UP, 1f, flipped ? 0f : (1f - percent));
        emitFluidFace(emitter, sprite, newColor, flipped, Direction.DOWN, 1f, flipped ? (1f - percent) : 0f);
        emitFluidFace(emitter, sprite, newColor, flipped, Direction.NORTH, percent, 0f);
        emitFluidFace(emitter, sprite, newColor, flipped, Direction.EAST, percent, 0f);
        emitFluidFace(emitter, sprite, newColor, flipped, Direction.SOUTH, percent, 0f);
        emitFluidFace(emitter, sprite, newColor, flipped, Direction.WEST, percent, 0f);

        var mesh = builder.build();

        var newLight = (light & 0xFFFF_0000) | (Math.max((light >> 4) & 0xF, luminance) << 4);
        renderMesh(mesh, matrices, consumer, newLight, overlay);

        matrices.pop();
    }

    private void emitFluidFace(QuadEmitter emitter, Sprite sprite, int color, boolean flipped, Direction direction, float height, float depth) {
        var minU = sprite.getMinU();
        var minV = sprite.getMinV();

        var uMult = sprite.getMaxU() - minU;
        var vMult = sprite.getMaxV() - minV;

        var bottomleft = flipped ? (1f - EDGE_SIZE - (height * INNER_SIZE)) : EDGE_SIZE;
        var right = 1f - EDGE_SIZE;
        var top = flipped ? (1f - EDGE_SIZE) : (EDGE_SIZE + (height * INNER_SIZE));
        var deep = EDGE_SIZE + (depth * INNER_SIZE);

        emitter.square(direction, bottomleft, bottomleft, right, top, deep);
        emitter.spriteBake(0, sprite, MutableQuadView.BAKE_ROTATE_NONE);
        emitter.spriteColor(0, color, color, color, color);
        emitter.sprite(0, 0, minU + bottomleft * uMult, minV + (1f - top) * vMult);
        emitter.sprite(1, 0, minU + bottomleft * uMult, minV + (1f - bottomleft) * vMult);
        emitter.sprite(2, 0, minU + right * uMult, minV + (1f - bottomleft) * vMult);
        emitter.sprite(3, 0, minU + right * uMult, minV + (1f - top) * vMult);
        emitter.emit();
    }

    private void renderMesh(Mesh mesh, MatrixStack matrices, VertexConsumer consumer, int light, int overlay) {
        var quadList = ModelHelper.toQuadLists(mesh);
        for (int x = 0; x < quadList.length; x++) {
            for (BakedQuad bq : quadList[x]) {
                float[] brightness = new float[] {1f, 1f, 1f, 1f};
                int[] lights = new int[]{light, light, light, light};
                consumer.quad(matrices.peek(), bq, brightness, 1f, 1f, 1f, lights, overlay, true);
            }
        }
    }
}
