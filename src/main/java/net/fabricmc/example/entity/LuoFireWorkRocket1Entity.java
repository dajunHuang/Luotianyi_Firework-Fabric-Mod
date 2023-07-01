package net.fabricmc.example.entity;

import net.fabricmc.example.ExampleMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.*;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

public class LuoFireWorkRocket1Entity
        extends ProjectileEntity
        implements FlyingItemEntity {
    private static final TrackedData<ItemStack> ITEM = DataTracker.registerData(LuoFireWorkRocket1Entity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    private static final TrackedData<OptionalInt> SHOOTER_ENTITY_ID = DataTracker.registerData(LuoFireWorkRocket1Entity.class, TrackedDataHandlerRegistry.OPTIONAL_INT);
    private static final TrackedData<Boolean> SHOT_AT_ANGLE = DataTracker.registerData(LuoFireWorkRocket1Entity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private int life;
    @Nullable
    private LivingEntity shooter;
    public static final byte LUO_EXPLODE_FIREWORK_CLIENT1 = 72;


    public LuoFireWorkRocket1Entity(EntityType<? extends LuoFireWorkRocket1Entity> entityType, World world) {
        super((EntityType<? extends ProjectileEntity>)entityType, world);
    }

    public LuoFireWorkRocket1Entity(World world, double x, double y, double z, ItemStack stack) {
        super((EntityType<? extends ProjectileEntity>) ExampleMod.LuoFireWorkRocket1EntityType, world);
        this.life = 0;
        this.setPosition(x, y, z);
        if (!stack.isEmpty() && stack.hasNbt()) {
            this.dataTracker.set(ITEM, stack.copy());
        }
        this.setVelocity(this.random.nextTriangular(0.0, 0.002297), 0.05, this.random.nextTriangular(0.0, 0.002297));
    }

    public LuoFireWorkRocket1Entity(World world, @Nullable Entity entity, double x, double y, double z, ItemStack stack) {
        this(world, x, y, z, stack);
        this.setOwner(entity);
    }
    public LuoFireWorkRocket1Entity(World world, ItemStack stack, LivingEntity shooter) {
        this(world, shooter, shooter.getX(), shooter.getY(), shooter.getZ(), stack);
        this.dataTracker.set(SHOOTER_ENTITY_ID, OptionalInt.of(shooter.getId()));
        this.shooter = shooter;
    }

    public LuoFireWorkRocket1Entity(World world, ItemStack stack, double x, double y, double z, boolean shotAtAngle) {
        this(world, x, y, z, stack);
        this.dataTracker.set(SHOT_AT_ANGLE, shotAtAngle);
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(ITEM, ItemStack.EMPTY);
        this.dataTracker.startTracking(SHOOTER_ENTITY_ID, OptionalInt.empty());
        this.dataTracker.startTracking(SHOT_AT_ANGLE, false);
    }

    @Override
    public ItemStack getStack() {
        ItemStack itemStack = this.dataTracker.get(ITEM);
        return itemStack.isEmpty() ? new ItemStack(ExampleMod.LuoFireWorkRocket1Item) : itemStack;
    }

    private boolean wasShotByEntity() {
        return ((OptionalInt)this.dataTracker.get(SHOOTER_ENTITY_ID)).isPresent();
    }

    public boolean wasShotAtAngle() {
        return (Boolean)this.dataTracker.get(SHOT_AT_ANGLE);
    }

    public void tick() {
        super.tick();
        Vec3d vec3d3;
        if (this.wasShotByEntity()) {
            if (this.shooter == null) {
                ((OptionalInt)this.dataTracker.get(SHOOTER_ENTITY_ID)).ifPresent((id) -> {
                    Entity entity = this.world.getEntityById(id);
                    if (entity instanceof LivingEntity) {
                        this.shooter = (LivingEntity)entity;
                    }

                });
            }

            if (this.shooter != null) {
                if (this.shooter.isFallFlying()) {
                    Vec3d vec3d = this.shooter.getRotationVector();
                    double d = 1.5;
                    double e = 0.1;
                    Vec3d vec3d2 = this.shooter.getVelocity();
                    this.shooter.setVelocity(vec3d2.add(vec3d.x * 0.1 + (vec3d.x * 1.5 - vec3d2.x) * 0.5, vec3d.y * 0.1 + (vec3d.y * 1.5 - vec3d2.y) * 0.5, vec3d.z * 0.1 + (vec3d.z * 1.5 - vec3d2.z) * 0.5));
                    vec3d3 = this.shooter.getHandPosOffset(Items.FIREWORK_ROCKET);
                } else {
                    vec3d3 = Vec3d.ZERO;
                }

                this.setPosition(this.shooter.getX() + vec3d3.x, this.shooter.getY() + vec3d3.y, this.shooter.getZ() + vec3d3.z);
                this.setVelocity(this.shooter.getVelocity());
            }
        } else {
            if (!this.wasShotAtAngle()) {
                double f = this.horizontalCollision ? 1.0 : 1.15;
                this.setVelocity(this.getVelocity().multiply(f, 1.0, f).add(0.0, 0.04, 0.0));
            }

            vec3d3 = this.getVelocity();
            this.move(MovementType.SELF, vec3d3);
            this.setVelocity(vec3d3);
        }

        HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
        if (!this.noClip) {
            this.onCollision(hitResult);
            this.velocityDirty = true;
        }

        this.updateRotation();
        if (this.life == 0 && !this.isSilent()) {
            this.world.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.AMBIENT, 3.0F, 1.0F);
        }

        ++this.life;
        if (this.world.isClient && this.life % 2 < 2) {
            this.world.addParticle(ParticleTypes.FIREWORK, this.getX(), this.getY(), this.getZ(), this.random.nextGaussian() * 0.05, -this.getVelocity().y * 0.5, this.random.nextGaussian() * 0.05);
        }

        if (!this.world.isClient && this.life > 30) {
            explodeAndRemove();
        }

    }
    private void explodeAndRemove() {
        this.world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, SoundCategory.AMBIENT, 3.0f, 1.0f);
        this.world.sendEntityStatus(this, LUO_EXPLODE_FIREWORK_CLIENT1);
        this.emitGameEvent(GameEvent.EXPLODE, this.getOwner());
        this.discard();
    }


    @Override
    public void handleStatus(byte status) {
        if (status == LUO_EXPLODE_FIREWORK_CLIENT1 && this.world.isClient) {
            double pattern [][] = {{-0.25, 0.17}, {0.25, 0.17}, {0.08, 0.17}, {0.025, -0.05}, {-0.03, -0.27}, {0.045, -0.01}, {-0.15, -0.01}, {-0.09, 0.17}};
            this.explodeStar(1, pattern);
        }
        super.handleStatus(status);
    }

    private void explodeStar(double size, double[][] pattern) {
        double d = pattern[0][0];
        double e = pattern[0][1];
        this.world.addParticle(ParticleTypes.POOF, this.getX(), this.getY(), this.getZ(), d * size, e * size, 0.0);
        float f = this.random.nextFloat() * 3.1415927F;
        double g = 0.034;

        for(int i = 0; i < 2; ++i) {
            double h = (double)f + (double)((float)i * 3.1415927F) * g;
            double j = d;
            double k = e;

            for(int l = 1; l < pattern.length; ++l) {
                double m = pattern[l][0];
                double n = pattern[l][1];

                for(double o = 0; o <= 1.0; o += 0.1) {
                    double p = MathHelper.lerp(o, j, m) * size;
                    double q = MathHelper.lerp(o, k, n) * size;
                    double r = p * Math.sin(h);
                    p *= Math.cos(h);
                    double s = 1;
                    if(this.world.isClient()) {
                        Particle particle = MinecraftClient.getInstance().particleManager.addParticle(ParticleTypes.FIREWORK, this.getX(), this.getY(), this.getZ(), p * s, q, r * s);
                        particle.setColor(0, 100, 25);
                    }
                }
                for(float a = 0; a < 2 * 3.1415927F; a += 0.1) {
                    double p = size * Math.sin(a)*0.3;
                    double q = size * Math.cos(a)*0.3;
                    double r = p * Math.sin(h);
                    p *= Math.cos(h);
                    if(this.world.isClient()) {
                        Particle particle = MinecraftClient.getInstance().particleManager.addParticle(ParticleTypes.FIREWORK, this.getX(), this.getY(), this.getZ(), p, q, r);
                        particle.setColor(0, 100, 25);
                    }
                }
                j = m;
                k = n;
            }
        }

    }
}
