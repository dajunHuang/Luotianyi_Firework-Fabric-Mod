package net.fabricmc.example.item;

import net.fabricmc.example.entity.LuoFireWorkRocket4Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class LuoFireWorkRocket4Item extends Item {
    public LuoFireWorkRocket4Item(Settings settings) {
        super(settings);
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (user.isFallFlying()) {
            ItemStack itemStack = user.getStackInHand(hand);
            if (!world.isClient) {
                LuoFireWorkRocket4Entity fireworkRocketEntity = new LuoFireWorkRocket4Entity(world, itemStack, user);
                world.spawnEntity(fireworkRocketEntity);
                if (!user.getAbilities().creativeMode) {
                    itemStack.decrement(1);
                }
                user.incrementStat(Stats.USED.getOrCreateStat(this));
            }

            return TypedActionResult.success(user.getStackInHand(hand), world.isClient());
        } else {
            return TypedActionResult.pass(user.getStackInHand(hand));
        }
    }

    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (!world.isClient) {
            ItemStack itemStack = context.getStack();
            Vec3d vec3d = context.getHitPos();
            Direction direction = context.getSide();
            LuoFireWorkRocket4Entity fireworkRocketEntity = new LuoFireWorkRocket4Entity(world, context.getPlayer(), vec3d.x + (double)direction.getOffsetX() * 0.15, vec3d.y + (double)direction.getOffsetY() * 0.15, vec3d.z + (double)direction.getOffsetZ() * 0.15, itemStack);
            world.spawnEntity(fireworkRocketEntity);
            itemStack.decrement(1);
        }
        return ActionResult.success(world.isClient);
    }

    public ItemStack getDefaultStack() {
        ItemStack itemStack = new ItemStack(this);
        itemStack.getOrCreateSubNbt("Firework").putBoolean("enabled", true);
        return itemStack;
    }
}
