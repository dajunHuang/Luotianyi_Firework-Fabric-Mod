package net.fabricmc.example.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.example.ExampleMod;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

public class ExampleModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ExampleMod.LuoFireWorkRocket1EntityType, (context) ->
                new FlyingItemEntityRenderer(context));
        EntityRendererRegistry.register(ExampleMod.LuoFireWorkRocket2EntityType, (context) ->
                new FlyingItemEntityRenderer(context));
        EntityRendererRegistry.register(ExampleMod.LuoFireWorkRocket3EntityType, (context) ->
                new FlyingItemEntityRenderer(context));
        EntityRendererRegistry.register(ExampleMod.LuoFireWorkRocket4EntityType, (context) ->
                new FlyingItemEntityRenderer(context));
    }
}
