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

public class LuoFireWorkRocket2Entity
        extends ProjectileEntity
        implements FlyingItemEntity {
    private static final TrackedData<ItemStack> ITEM = DataTracker.registerData(LuoFireWorkRocket2Entity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    private static final TrackedData<OptionalInt> SHOOTER_ENTITY_ID = DataTracker.registerData(LuoFireWorkRocket2Entity.class, TrackedDataHandlerRegistry.OPTIONAL_INT);
    private static final TrackedData<Boolean> SHOT_AT_ANGLE = DataTracker.registerData(LuoFireWorkRocket2Entity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private int life;
    @Nullable
    private LivingEntity shooter;
    public static final byte LUO_EXPLODE_FIREWORK_CLIENT2 = 72;

    public LuoFireWorkRocket2Entity(EntityType<? extends LuoFireWorkRocket2Entity> entityType, World world) {
        super((EntityType<? extends ProjectileEntity>)entityType, world);
    }

    public LuoFireWorkRocket2Entity(World world, double x, double y, double z, ItemStack stack) {
        super((EntityType<? extends ProjectileEntity>) ExampleMod.LuoFireWorkRocket2EntityType, world);
        this.life = 0;
        this.setPosition(x, y, z);
        if (!stack.isEmpty() && stack.hasNbt()) {
            this.dataTracker.set(ITEM, stack.copy());
        }
        this.setVelocity(this.random.nextTriangular(0.0, 0.002297), 0.05, this.random.nextTriangular(0.0, 0.002297));
    }

    public LuoFireWorkRocket2Entity(World world, @Nullable Entity entity, double x, double y, double z, ItemStack stack) {
        this(world, x, y, z, stack);
        this.setOwner(entity);
    }
    public LuoFireWorkRocket2Entity(World world, ItemStack stack, LivingEntity shooter) {
        this(world, shooter, shooter.getX(), shooter.getY(), shooter.getZ(), stack);
        this.dataTracker.set(SHOOTER_ENTITY_ID, OptionalInt.of(shooter.getId()));
        this.shooter = shooter;
    }

    public LuoFireWorkRocket2Entity(World world, ItemStack stack, double x, double y, double z, boolean shotAtAngle) {
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
        return itemStack.isEmpty() ? new ItemStack(ExampleMod.LuoFireWorkRocket2Item) : itemStack;
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
        this.world.sendEntityStatus(this, LUO_EXPLODE_FIREWORK_CLIENT2);
        this.emitGameEvent(GameEvent.EXPLODE, this.getOwner());
        this.discard();
    }

    @Override
    public void handleStatus(byte status) {
        if (status == LUO_EXPLODE_FIREWORK_CLIENT2 && this.world.isClient) {
            double pattern [][][] = {{{-0.6291666666666667, -0.010416666666666666}, {-0.6729166666666667, -0.03125}, {-0.7354166666666667, -0.041666666666666664}, {-0.7958333333333333, -0.04375}, {-0.8479166666666667, -0.0375}, {-0.8979166666666667, -0.01875}, {-0.9333333333333333, 0.008333333333333333}, {-0.9604166666666667, 0.04375}, {-0.9770833333333333, 0.09166666666666666}, {-0.98125, 0.14166666666666666}, {-0.975, 0.18958333333333333}, {-0.9625, 0.22916666666666666}, {-0.94375, 0.28541666666666665}, {-0.9229166666666667, 0.3333333333333333}, {-0.8895833333333333, 0.38333333333333336}, {-0.8583333333333333, 0.42916666666666664}, {-0.825, 0.46875}, {-0.7875, 0.5083333333333333}, {-0.75, 0.5583333333333333}, {-0.69375, 0.6020833333333333}, {-0.6333333333333333, 0.65}, {-0.5645833333333333, 0.6833333333333333}, {-0.5, 0.71875}, {-0.43125, 0.7375}, {-0.3625, 0.74375}, {-0.28958333333333336, 0.7354166666666667}, {-0.2375, 0.7166666666666667}, {-0.1875, 0.6708333333333333}, {-0.15625, 0.6166666666666667}, {-0.14791666666666667, 0.5604166666666667}, {-0.15625, 0.5020833333333333}, {-0.17291666666666666, 0.4395833333333333},
                    {-0.2, 0.39375}, {-0.22291666666666668, 0.3458333333333333}, {-0.2520833333333333, 0.30625}, {-0.2875, 0.27291666666666664}, {-0.31666666666666665, 0.2375}, {-0.35625, 0.19791666666666666}, {-0.39791666666666664, 0.16458333333333333}, {-0.35833333333333334, 0.13125}, {-0.31666666666666665, 0.09791666666666667}, {-0.28541666666666665, 0.07083333333333333}, {-0.25833333333333336, 0.03333333333333333}, {-0.24791666666666667, -0.014583333333333334}, {-0.24791666666666667, -0.075}, {-0.27708333333333335, -0.13333333333333333}, {-0.3020833333333333, -0.16875}, {-0.33125, -0.2}, {-0.3729166666666667, -0.24166666666666667}, {-0.4083333333333333, -0.275}, {-0.44166666666666665, -0.3104166666666667}, {-0.49375, -0.3416666666666667}, {-0.5270833333333333, -0.375}, {-0.5666666666666667, -0.40208333333333335}, {-0.51875, -0.44375}, {-0.4875, -0.4875}, {-0.47291666666666665, -0.5333333333333333}, {-0.47708333333333336, -0.5875}, {-0.5, -0.625}, {-0.5395833333333333, -0.6583333333333333}, {-0.5916666666666667, -0.6770833333333334}, {-0.64375, -0.6875}, {-0.6958333333333333, -0.6770833333333334}, {-0.7375, -0.66875}, {-0.7583333333333333, -0.6354166666666666}, {-0.7604166666666666, -0.5958333333333333}, {-0.7333333333333333, -0.55}, {-0.6666666666666666, -0.5479166666666667}, {-0.625, -0.5666666666666667}, {-0.6291666666666667, -0.6125}, {-0.675, -0.6208333333333333}, {-0.6958333333333333, -0.6020833333333333}}, {{-0.8708333333333333, 0.4125}, {-0.9083333333333333, 0.4125}, {-0.9479166666666666, 0.4270833333333333}, {-0.9770833333333333, 0.46041666666666664}, {-0.98125, 0.5145833333333333}, {-0.9541666666666667, 0.5541666666666667}, {-0.8791666666666667, 0.5729166666666666}, {-0.8354166666666667, 0.5458333333333333}, {-0.8229166666666666, 0.5125}, {-0.81875, 0.5770833333333333}, {-0.8104166666666667, 0.6208333333333333}, {-0.7854166666666667, 0.6625}, {-0.7458333333333333, 0.6541666666666667}, {-0.7333333333333333, 0.6166666666666667}, {-0.7479166666666667, 0.5729166666666666}}, {{-0.8145833333333333, 0.3}, {-0.7604166666666666, 0.30625}, {-0.7291666666666666, 0.30625}, {-0.7604166666666666, 0.26458333333333334}, {-0.7770833333333333, 0.23125}}, {{-0.5020833333333333, 0.47291666666666665}, {-0.51875, 0.45}, {-0.5541666666666667, 0.41041666666666665}, {-0.5125, 0.41041666666666665}, {-0.45208333333333334, 0.40625}}, {{-0.6583333333333333, 0.29583333333333334}, {-0.6083333333333333, 0.31666666666666665}, {-0.5604166666666667, 0.3333333333333333}, {-0.5666666666666667, 0.28125}, {-0.56875, 0.21875}, {-0.6041666666666666, 0.2604166666666667}, {-0.6541666666666667, 0.29375}}, {{-0.3729166666666667, -0.4791666666666667}, {-0.36666666666666664, -0.44166666666666665}, {-0.35, -0.4}, {-0.32083333333333336, -0.34791666666666665}, {-0.2833333333333333, -0.30416666666666664}, {-0.23125, -0.2625}, {-0.16458333333333333, -0.24375}, {-0.10208333333333333, -0.2625}, {-0.07291666666666667, -0.3125}, {-0.07083333333333333, -0.3770833333333333}, {-0.08958333333333333, -0.4354166666666667}, {-0.1125, -0.48125}, {-0.1375, -0.525}, {-0.17916666666666667, -0.56875}, {-0.21458333333333332, -0.60625}, {-0.26458333333333334, -0.6229166666666667}, {-0.3229166666666667, -0.6333333333333333}, {-0.35833333333333334, -0.5958333333333333}, {-0.3645833333333333, -0.55625}, {-0.34375, -0.50625}, {-0.25416666666666665, -0.49583333333333335}, {-0.1875, -0.5041666666666667}, {-0.13541666666666666, -0.525}, {-0.0875, -0.5479166666666667}, {-0.06041666666666667, -0.5895833333333333}, {-0.04791666666666667, -0.6416666666666667}, {-0.03958333333333333, -0.6875}}, {{-0.24166666666666667, -0.6291666666666667}, {-0.18333333333333332, -0.61875}, {-0.14583333333333334, -0.63125}, {-0.12708333333333333, -0.6645833333333333}, {-0.12916666666666668, -0.7104166666666667}, {-0.16875, -0.7520833333333333}, {-0.24166666666666667, -0.75}, {-0.2604166666666667, -0.71875}, {-0.25416666666666665, -0.65625}, {-0.2375, -0.6354166666666666}}, {{0.041666666666666664, -0.24583333333333332}, {0.10208333333333333, -0.24791666666666667}, {0.15, -0.24791666666666667}, {0.20416666666666666, -0.2520833333333333}, {0.16458333333333333, -0.28958333333333336}, {0.10833333333333334, -0.32916666666666666}, {0.04791666666666667, -0.3770833333333333}, {0.004166666666666667, -0.43125}, {0.06458333333333334, -0.44166666666666665}, {0.125, -0.42083333333333334}, {0.18333333333333332, -0.39166666666666666}}, {{0.12083333333333333, -0.34791666666666665}, {0.11041666666666666, -0.38958333333333334}, {0.10208333333333333, -0.43125}, {0.0875, -0.49583333333333335}, {0.08333333333333333, -0.5520833333333334}, {0.08958333333333333, -0.5895833333333333}}, {{0.17291666666666666, -0.4708333333333333}, {0.20625, -0.49583333333333335}, {0.24791666666666667, -0.54375}}, {{0.2604166666666667, -0.35625}, {0.29583333333333334, -0.325}, {0.32916666666666666, -0.3020833333333333}, {0.36041666666666666, -0.3020833333333333}, {0.375, -0.35625}, {0.375, -0.4}, {0.3729166666666667, -0.44166666666666665}}, {{0.42916666666666664, -0.08333333333333333}, {0.44583333333333336, -0.1125}, {0.46458333333333335, -0.15416666666666667}}, {{0.3958333333333333, -0.2708333333333333}, {0.42291666666666666, -0.25416666666666665}, {0.46875, -0.24166666666666667}, {0.4979166666666667, -0.27291666666666664}, {0.47291666666666665, -0.3125}, {0.4583333333333333, -0.35833333333333334}, {0.4395833333333333, -0.4}, {0.42083333333333334, -0.4354166666666667}, {0.4, -0.48333333333333334}}, {{0.4708333333333333, -0.3770833333333333}, {0.47291666666666665, -0.4270833333333333}, {0.49166666666666664, -0.46458333333333335}, {0.53125, -0.4666666666666667}, {0.5791666666666667, -0.4395833333333333}, {0.60625, -0.40625}, {0.6520833333333333, -0.3625}, {0.66875, -0.3125}, {0.6770833333333334, -0.2625}, {0.6645833333333333, -0.23125}, {0.6166666666666667, -0.23125}, {0.59375, -0.26666666666666666}, {0.5895833333333333, -0.32083333333333336}, {0.6083333333333333, -0.35625}, {0.6666666666666666, -0.3875}, {0.7583333333333333, -0.3854166666666667}, {0.8125, -0.35}, {0.86875, -0.29791666666666666}, {0.90625, -0.25}, {0.9354166666666667, -0.20625}, {0.9625, -0.14791666666666667}, {0.98125, -0.09791666666666667}, {0.9875, -0.025}, {0.9625, 0.025}, {0.93125, 0.058333333333333334}, {0.8666666666666667, 0.0625}, {0.8041666666666667, 0.03958333333333333}, {0.7645833333333333, -0.0}, {0.73125, -0.03958333333333333}, {0.7041666666666667, -0.09166666666666666}, {0.6916666666666667, -0.04375}, {0.675, 0.01875}, {0.6395833333333333, 0.06041666666666667}, {0.5979166666666667, 0.12708333333333333}, {0.5583333333333333, 0.16041666666666668}, {0.5, 0.17291666666666666}, {0.425, 0.175}, {0.375, 0.14791666666666667}, {0.32916666666666666, 0.10625}, {0.30833333333333335, 0.05416666666666667}, {0.3, 0.004166666666666667}, {0.30625, -0.06666666666666667}, {0.33541666666666664, -0.1125}}};
            this.explodePattern(0.5, pattern);
        }
        super.handleStatus(status);
    }

    private void explodePattern(double size, double [][][] pattern) {
        this.world.addParticle(ParticleTypes.POOF, this.getX(), this.getY(), this.getZ(), 0.1 * size, 0.1 * size, 0.0);
        float f = this.random.nextFloat() * 3.1415927F;
        double g = 0.034;

        for(int line = 0; line < pattern.length; ++ line) {
            double d = pattern[line][0][0];
            double e = pattern[line][0][1];
            for(int i = 0; i < 1; ++i) {
                double h = (double)f + (double)((float)i * 3.1415927F) * g;
                double j = d;
                double k = e;

                for(int l = 1; l < pattern[line].length; ++l) {
                    double m = pattern[line][l][0];
                    double n = pattern[line][l][1];

                    for(double o = 0; o <= 1.0; o += 0.5) {
                        double p = MathHelper.lerp(o, j, m) * size;
                        double q = MathHelper.lerp(o, k, n) * size;
                        double r = p * Math.sin(h);
                        p *= Math.cos(h);
                        double s = 1;
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

}
