package gay.nyako.infinitech;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.UUID;

public class FakePlayerEntity extends PlayerEntity {
    public static final UUID uuid = UUID.fromString("3e1776b0-499d-447a-9831-abbabc1f6dd5"); // uuidgenerator.net
    public static final String name = "InfinitechFakePlayer";
    public static final GameProfile profile = new GameProfile(uuid, name);

    public FakePlayerEntity(World world, BlockPos pos) {
        super(world, pos, 0.0f, profile);
    }

    public FakePlayerEntity(World world) {
        super(world, BlockPos.ORIGIN, 0.0f, profile);
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }

    @Override
    public boolean isPartOfGame() {
        return false;
    }
}
