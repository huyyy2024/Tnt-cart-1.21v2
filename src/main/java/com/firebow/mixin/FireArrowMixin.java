package com.firebow.mixin;

import com.firebow.event.FireBowRuntime;
import com.firebow.util.PlacementCalc;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(PersistentProjectileEntity.class)
public class FireArrowMixin {

    @Inject(method = "onBlockHit", at = @At("HEAD"))
    private void onBlockHit(BlockHitResult hit, CallbackInfo ci) {
        PersistentProjectileEntity self = (PersistentProjectileEntity)(Object) this;
        MinecraftClient mc = MinecraftClient.getInstance();

        if (!isOwnArrow(self, mc)) return;
        if (!self.isOnFire()) return;

        World world = self.getWorld();
        BlockPos railPos = PlacementCalc.fromBlockHit(world, hit.getBlockPos(), hit.getSide());
        if (railPos == null) return;

        FireBowRuntime.get().enqueue(railPos);
    }

    @Inject(method = "onEntityHit", at = @At("HEAD"))
    private void onEntityHit(EntityHitResult hit, CallbackInfo ci) {
        PersistentProjectileEntity self = (PersistentProjectileEntity)(Object) this;
        MinecraftClient mc = MinecraftClient.getInstance();

        if (!isOwnArrow(self, mc)) return;
        if (!self.isOnFire()) return;

        Entity target = hit.getEntity();
        World world = self.getWorld();

        BlockPos railPos = PlacementCalc.fromEntityHit(world, target, self.getVelocity());
        if (railPos == null) return;

        FireBowRuntime.get().enqueue(railPos);
    }

    private boolean isOwnArrow(PersistentProjectileEntity arrow, MinecraftClient mc) {
        if (mc.player == null) return false;
        Entity owner = arrow.getOwner();
        return owner != null && owner.getId() == mc.player.getId();
    }
}
