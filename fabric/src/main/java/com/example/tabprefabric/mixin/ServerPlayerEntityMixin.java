package com.example.tabprefabric.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.example.tabprefabric.GameModeChangeCallback;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Inject(
        method = "method_7336(Lnet/minecraft/world/GameMode;)Z",
        at = @At("HEAD"),
        cancellable = true,
        remap = true
    )
    private void onGameModeChange(GameMode gameMode, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        boolean success = GameModeChangeCallback.EVENT.invoker().onChange(player, gameMode);
        if (!success) {
            cir.setReturnValue(false);
        }
    }
} 