package gay.nyako.infinitech.mixin;

import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ExperienceBottleEntity.class)
public abstract class ExperienceBottleEntityMixin {
    @ModifyArg(method= "onCollision(Lnet/minecraft/util/hit/HitResult;)V", at=@At(value="INVOKE", target="Lnet/minecraft/entity/ExperienceOrbEntity;spawn(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/Vec3d;I)V"), index=2)
    private int modify( int amount ) {
        return 10;
    }
}
