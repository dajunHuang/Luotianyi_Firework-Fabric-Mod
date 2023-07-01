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

public class LuoFireWorkRocket3Entity
        extends ProjectileEntity
        implements FlyingItemEntity {
    private static final TrackedData<ItemStack> ITEM = DataTracker.registerData(LuoFireWorkRocket3Entity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    private static final TrackedData<OptionalInt> SHOOTER_ENTITY_ID = DataTracker.registerData(LuoFireWorkRocket3Entity.class, TrackedDataHandlerRegistry.OPTIONAL_INT);
    private static final TrackedData<Boolean> SHOT_AT_ANGLE = DataTracker.registerData(LuoFireWorkRocket3Entity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private int life;
    @Nullable
    private LivingEntity shooter;
    public static final byte LUO_EXPLODE_FIREWORK_CLIENT3 = 72;

    public LuoFireWorkRocket3Entity(EntityType<? extends LuoFireWorkRocket3Entity> entityType, World world) {
        super((EntityType<? extends ProjectileEntity>)entityType, world);
    }

    public LuoFireWorkRocket3Entity(World world, double x, double y, double z, ItemStack stack) {
        super((EntityType<? extends ProjectileEntity>) ExampleMod.LuoFireWorkRocket3EntityType, world);
        this.life = 0;
        this.setPosition(x, y, z);
        if (!stack.isEmpty() && stack.hasNbt()) {
            this.dataTracker.set(ITEM, stack.copy());
        }
        this.setVelocity(this.random.nextTriangular(0.0, 0.002297), 0.05, this.random.nextTriangular(0.0, 0.002297));
    }

    public LuoFireWorkRocket3Entity(World world, @Nullable Entity entity, double x, double y, double z, ItemStack stack) {
        this(world, x, y, z, stack);
        this.setOwner(entity);
    }
    public LuoFireWorkRocket3Entity(World world, ItemStack stack, LivingEntity shooter) {
        this(world, shooter, shooter.getX(), shooter.getY(), shooter.getZ(), stack);
        this.dataTracker.set(SHOOTER_ENTITY_ID, OptionalInt.of(shooter.getId()));
        this.shooter = shooter;
    }

    public LuoFireWorkRocket3Entity(World world, ItemStack stack, double x, double y, double z, boolean shotAtAngle) {
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
        return itemStack.isEmpty() ? new ItemStack(ExampleMod.LuoFireWorkRocket3Item) : itemStack;
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
        this.world.sendEntityStatus(this, LUO_EXPLODE_FIREWORK_CLIENT3);
        this.emitGameEvent(GameEvent.EXPLODE, this.getOwner());
        this.discard();
    }

    @Override
    public void handleStatus(byte status) {
        if (status == LUO_EXPLODE_FIREWORK_CLIENT3 && this.world.isClient) {
            double pattern [][][] = {{{-0.361, 0.463}, {-0.404, 0.448}, {-0.441, 0.432}, {-0.469, 0.395}, {-0.485, 0.352}, {-0.488, 0.324}, {-0.454, 0.312}, {-0.395, 0.321}, {-0.358, 0.352}, {-0.333, 0.389}, {-0.324, 0.407}, {-0.321, 0.361}, {-0.302, 0.330}, {-0.259, 0.296}, {-0.210, 0.287}, {-0.167, 0.275}, {-0.160, 0.299}, {-0.136, 0.340}, {-0.139, 0.296}, {-0.117, 0.278}, {-0.093, 0.269}, {-0.071, 0.287}, {-0.068, 0.318}, {-0.099,
                    0.358}, {-0.031, 0.349}, {0.006, 0.370}, {0.019, 0.389}, {-0.015, 0.414}, {-0.059, 0.414}, {-0.099, 0.383}, {-0.117, 0.395}, {-0.117, 0.438}, {-0.142, 0.469}, {-0.185, 0.485}, {-0.198, 0.448}, {-0.191, 0.407}, {-0.145, 0.389}, {-0.185, 0.373}, {-0.216, 0.333}, {-0.176, 0.315}, {-0.136, 0.330}}, {{-0.139, 0.466}, {-0.093, 0.503}, {-0.077, 0.540}, {-0.059, 0.574}, {-0.127, 0.583}, {-0.194, 0.574}, {-0.253, 0.534}, {-0.299, 0.497}, {-0.324, 0.549}, {-0.367, 0.602}, {-0.429, 0.627}, {-0.497, 0.630}, {-0.481, 0.556}, {-0.460, 0.497}, {-0.417, 0.469}, {-0.364, 0.463}}, {{0.049, 0.580}, {0.071, 0.596},
                    {0.083, 0.577}, {0.102, 0.512}, {0.157, 0.488}, {0.201, 0.491}, {0.228, 0.469}, {0.210, 0.444}, {0.179, 0.457}, {0.167, 0.472}, {0.099, 0.491}, {0.071, 0.540}, {0.049, 0.574}, {0.068, 0.599}}, {{0.031, 0.522}, {0.049, 0.534}, {0.074, 0.519}, {0.062, 0.503}, {0.096, 0.451}, {0.160, 0.435}, {0.222, 0.414}, {0.244, 0.423}, {0.259, 0.401}, {0.247, 0.386}, {0.216, 0.401}, {0.133, 0.423}, {0.080, 0.435},
                    {0.056, 0.478}, {0.028, 0.522}}, {{0.299, 0.466}, {0.299, 0.491}, {0.336, 0.481}, {0.349, 0.463}, {0.315, 0.444}, {0.299, 0.463}}, {{0.386, 0.463}, {0.407, 0.488}, {0.426, 0.481}, {0.407, 0.457}, {0.380, 0.457}}, {{0.380, 0.420}, {0.404, 0.410}, {0.404, 0.392}, {0.383, 0.398}, {0.377, 0.420}}, {{0.312, 0.410}, {0.336, 0.423}, {0.333, 0.401}, {0.306, 0.386}, {0.306, 0.414}}, {{0.435, 0.380}, {0.478,
                    0.361}, {0.537, 0.333}, {0.586, 0.309}, {0.623, 0.361}, {0.657, 0.429}, {0.691, 0.497}, {0.725, 0.546}, {0.765, 0.611}, {0.793, 0.660}, {0.827, 0.713}, {0.781, 0.750}, {0.741, 0.781}, {0.685, 0.806}, {0.614, 0.833}, {0.580, 0.759}, {0.559, 0.698}, {0.528, 0.630}, {0.503, 0.562}, {0.478, 0.500}, {0.457, 0.448}, {0.432, 0.383}}, {{0.651, 0.265}, {0.688, 0.235}, {0.722, 0.201}, {0.753, 0.173}, {0.812,
                    0.219}, {0.867, 0.256}, {0.901, 0.296}, {0.954, 0.340}, {0.985, 0.367}, {0.957, 0.432}, {0.929, 0.488}, {0.895, 0.519}, {0.861, 0.543}, {0.824, 0.491}, {0.784, 0.438}, {0.750, 0.389}, {0.710, 0.340}, {0.682, 0.302}, {0.651, 0.269}}, {{0.633, 0.383}, {0.664, 0.349}, {0.682, 0.306}}, {{0.451, 0.373}, {0.389, 0.355}, {0.324, 0.336}, {0.247, 0.343}, {0.164, 0.346}, {0.093, 0.352}, {0.052, 0.358}, {-0.003, 0.361}}, {{-0.472, 0.389}, {-0.537, 0.398}, {-0.577, 0.438}, {-0.602, 0.488}, {-0.620, 0.562}, {-0.611, 0.599}, {-0.562, 0.654}, {-0.491, 0.698}, {-0.417, 0.728}, {-0.352, 0.772}, {-0.253, 0.793}, {-0.170, 0.806}, {-0.071, 0.809}, {0.015, 0.796}, {0.111, 0.769}, {0.188, 0.747}, {0.256, 0.722}, {0.336, 0.685}, {0.417, 0.636}, {0.469, 0.605}, {0.509, 0.577}}, {{-0.546, 0.654}, {-0.414, 0.685}, {-0.296, 0.707}, {-0.148, 0.728}, {-0.031, 0.725}, {0.093, 0.725}, {0.207, 0.704}, {0.306, 0.667}, {0.407, 0.623}, {0.503, 0.571}}, {{-0.580, 0.444}, {-0.636, 0.395}, {-0.679, 0.346}, {-0.716, 0.287}, {-0.741, 0.235}, {-0.765, 0.160}, {-0.790, 0.096}, {-0.799, 0.003}, {-0.806, -0.077}, {-0.806, -0.154}, {-0.802, -0.216}, {-0.790, -0.293}, {-0.772, -0.364}, {-0.781, -0.429}, {-0.781, -0.503}, {-0.775, -0.577}, {-0.756, -0.645}, {-0.738, -0.698}, {-0.719, -0.728}, {-0.694, -0.660}, {-0.682, -0.608}, {-0.660, -0.549}, {-0.651, -0.500}, {-0.651, -0.457}}, {{-0.802, -0.114}, {-0.833, -0.077}, {-0.821, -0.022}, {-0.796, 0.028}}, {{-0.744, 0.241}, {-0.824, 0.198}, {-0.889, 0.133}, {-0.938, 0.074}, {-0.972, -0.012}, {-0.981, -0.099}, {-0.960, -0.167}, {-0.904, -0.231}, {-0.843, -0.269}, {-0.787, -0.299}}, {{-0.676, -0.623}, {-0.620, -0.673}, {-0.577, -0.694}, {-0.522, -0.710}, {-0.454, -0.682}, {-0.401, -0.713}, {-0.299, -0.738}, {-0.216, -0.753}, {-0.123, -0.744}, {-0.182, -0.719}, {-0.238, -0.660}, {-0.278, -0.580}, {-0.306, -0.506}, {-0.330, -0.420}, {-0.346, -0.343}, {-0.361, -0.253}, {-0.367, -0.173}, {-0.377, -0.080}, {-0.377, -0.006}, {-0.377, 0.049}, {-0.361, 0.059}, {-0.395, 0.093}, {-0.475, 0.111}, {-0.417, 0.154}, {-0.401, 0.225}, {-0.398, 0.275}, {-0.481, 0.247}, {-0.522, 0.188}, {-0.525, 0.111}, {-0.574, 0.151}, {-0.654, 0.157}, {-0.728, 0.114}, {-0.688, 0.062}, {-0.642, 0.028}, {-0.565, 0.065}, {-0.608, 0.003}, {-0.580, -0.056}, {-0.528, -0.046}, {-0.503, 0.006}, {-0.515, 0.059}, {-0.441, 0.019}, {-0.370, 0.052}}, {{-0.414, -0.707}, {-0.444, -0.762}, {-0.466, -0.818}, {-0.497, -0.892}, {-0.519, -0.948}, {-0.549, -0.994}}, {{-0.231, -0.747}, {-0.231, -0.809}, {-0.228, -0.870}, {-0.228, -0.929}, {-0.238, -0.997}}, {{-0.188, -0.701}, {-0.120, -0.673}, {-0.062, -0.682}, {-0.009, -0.707}, {0.037, -0.691}}, {{-0.256, -0.608}, {-0.173, -0.620}, {-0.077, -0.639}, {0.025, -0.636}, {0.133, -0.623}}, {{0.185, -0.738}, {0.160, -0.694}, {0.145, -0.654}, {0.154, -0.608}, {0.207, -0.617}, {0.231, -0.627}, {0.265, -0.602}, {0.309, -0.599}, {0.330, -0.611}, {0.312, -0.657}, {0.336, -0.701}, {0.346, -0.747}, {0.327, -0.781}, {0.346, -0.806}, {0.361, -0.846}, {0.327, -0.873}, {0.262, -0.880}, {0.247, -0.901}, {0.201, -0.917}, {0.182, -0.883}, {0.176, -0.818}, {0.160, -0.787}, {0.182, -0.735}}, {{0.142, -0.664}, {0.099, -0.707}, {0.062, -0.769}, {0.046, -0.830}, {0.037, -0.877}, {0.074,
                    -0.901}, {0.151, -0.898}, {0.185, -0.895}}, {{0.065, -0.633}, {0.046, -0.688}, {0.000, -0.750}, {-0.046, -0.821}, {-0.090, -0.889}, {-0.117, -0.944}, {-0.145, -0.988}}, {{-0.006, -0.784}, {0.015, -0.796}, {0.019, -0.735}}, {{-0.049, -0.836}, {0.000, -0.867}, {-0.012, -0.907}, {-0.012, -0.991}, {-0.059, -0.969}, {-0.114, -0.951}}, {{-0.009, -0.910}, {0.083, -0.935}, {0.179, -0.944}, {0.296, -0.944},
                    {0.392, -0.923}, {0.466, -0.901}}, {{0.457, -0.787}, {0.444, -0.815}, {0.460, -0.855}, {0.460, -0.907}, {0.472, -0.957}, {0.494, -0.994}}, {{0.488, -0.781}, {0.503, -0.873}, {0.509, -0.923}, {0.522, -0.997}}, {{0.315, -0.948}, {0.336, -0.997}, {0.404, -0.978}, {0.395, -0.917}}, {{0.204, -0.769}, {0.262, -0.799}, {0.290, -0.772}, {0.309, -0.775}, {0.290, -0.806}, {0.312, -0.840}, {0.269, -0.830}, {0.225, -0.861}, {0.238, -0.824}, {0.210, -0.772}}, {{0.327, -0.602}, {0.404, -0.577}, {0.481, -0.552}, {0.537, -0.525}, {0.574, -0.485}, {0.602, -0.457}, {0.642, -0.494}, {0.667, -0.451}, {0.654, -0.370}, {0.654, -0.287}, {0.654, -0.219}, {0.698, -0.204}, {0.728, -0.241}, {0.769, -0.299}, {0.769, -0.318}}, {{0.744, -0.352}, {0.799, -0.318}, {0.867, -0.306}, {0.910, -0.340}, {0.929, -0.389}, {0.938, -0.451}, {0.941, -0.528}, {0.920, -0.586}, {0.870, -0.627}, {0.787, -0.651}, {0.719, -0.651}, {0.698, -0.627}, {0.670, -0.559}, {0.667, -0.460}}, {{0.790, -0.407}, {0.830, -0.386}, {0.914, -0.395}}, {{0.719, -0.648}, {0.676, -0.691}, {0.608, -0.725}, {0.549, -0.756}, {0.481, -0.778}, {0.429, -0.793}, {0.355, -0.815}}, {{0.466, -0.556}, {0.463, -0.611}, {0.478, -0.664}, {0.506, -0.710}, {0.543, -0.759}}, {{0.546, -0.537}, {0.543, -0.586}, {0.562, -0.657}, {0.593, -0.698}, {0.608, -0.719}}, {{0.574, -0.503}, {0.577, -0.571}, {0.593, -0.623}, {0.627, -0.676}, {0.648, -0.707}}, {{0.630, -0.725}, {0.679, -0.806}, {0.725, -0.855}, {0.762, -0.932}, {0.787, -0.981}}, {{0.287, -0.593}, {0.377, -0.556}, {0.466, -0.512}, {0.540, -0.460}, {0.596, -0.407}, {0.630, -0.336}, {0.614, -0.256}, {0.583, -0.222}, {0.571, -0.154}, {0.562, -0.083}, {0.549, -0.031}}, {{0.552, 0.201}, {0.577, 0.130}, {0.580, 0.034}, {0.556, -0.031}}, {{0.590, 0.312}, {0.620, 0.262}, {0.636, 0.182}, {0.636, 0.093}, {0.691, 0.006}, {0.759, -0.068}, {0.799, -0.151}, {0.830, -0.231}}, {{0.645, -0.099}, {0.704, -0.139}, {0.747, -0.160}, {0.827, -0.225}}, {{0.756, -0.160}, {0.762, -0.222}, {0.756, -0.275}}, {{0.691, 0.222}, {0.679, 0.160}, {0.645, 0.093}}, {{0.164, 0.346}, {0.210, 0.272}, {0.238, 0.170}, {0.256, 0.074}, {0.262, -0.022}, {0.262, -0.117}, {0.179, -0.105}, {0.123, -0.080}, {0.062, -0.065}, {0.065, -0.127}, {-0.022, -0.093}, {-0.083, -0.046}, {-0.136, 0.006}, {-0.176, 0.052}, {-0.216, 0.117}}, {{-0.176, 0.056}, {-0.194, -0.019}, {-0.228, -0.090}, {-0.265, -0.167}, {-0.318, -0.244}, {-0.346, -0.281}}, {{-0.281, -0.049}, {-0.238, 0.009}, {-0.179, 0.046}, {-0.090, 0.043}}, {{0.194, 0.136}, {0.275, 0.170}, {0.315, 0.170}, {0.361, 0.154}}, {{0.136, -0.401}, {0.231, -0.389}, {0.321, -0.389}, {0.315, -0.448}, {0.293, -0.512}, {0.247, -0.568}, {0.201, -0.574}, {0.154, -0.552}, {0.136, -0.503}, {0.136, -0.432}, {0.142, -0.398}}, {{-0.333, -0.327}, {-0.299, -0.247}, {-0.253, -0.194}, {-0.182, -0.145}, {-0.164, -0.077}, {-0.154, -0.127}, {-0.099, -0.130}, {-0.015, -0.148}, {-0.117, -0.145}, {-0.191, -0.185}, {-0.253, -0.247}, {-0.290, -0.327}, {-0.315, -0.389}}, {{0.265, -0.065}, {0.293, -0.009}, {0.349, 0.022}, {0.358, 0.068}, {0.364, 0.028}, {0.432, 0.022}, {0.506, -0.009}, {0.543, -0.040}, {0.571, -0.117}, {0.497, -0.031}, {0.395, 0.003}, {0.306, -0.034}, {0.269, -0.086}}, {{-0.231, -0.284}, {-0.228, -0.370}, {-0.182, -0.435}, {-0.105, -0.463}, {0.025, -0.407}, {0.028, -0.315}, {-0.003, -0.228}, {-0.037, -0.179}, {-0.117, -0.167}, {-0.176, -0.191}, {-0.228, -0.256}, {-0.225, -0.284}}, {{0.278, -0.136}, {0.306, -0.059}, {0.380, -0.037}, {0.460, -0.090}, {0.500, -0.154}, {0.500, -0.225}, {0.448, -0.290}, {0.336, -0.278}, {0.306, -0.213}, {0.278, -0.133}}, {{-0.309, -0.398}, {-0.256, -0.451}, {-0.157, -0.469}, {-0.096, -0.466}}, {{0.460, -0.296}, {0.522, -0.262}, {0.556, -0.207}, {0.571, -0.151}}, {{-0.148, -0.176}, {-0.108, -0.225}, {-0.111, -0.265}, {-0.160, -0.302}, {-0.201, -0.284}, {-0.216, -0.256}}, {{0.293, -0.117}, {0.315, -0.157}, {0.364, -0.142}, {0.380, -0.099}, {0.377, -0.059}, {0.358, -0.046}}, {{-0.154, -0.306}, {-0.142, -0.352}, {-0.093, -0.373}, {-0.049, -0.340}, {-0.046, -0.306}, {-0.065, -0.284}}, {{0.333, -0.154}, {0.343, -0.198}, {0.367, -0.225}, {0.407, -0.219}, {0.423, -0.182}, {0.420, -0.157}}, {{0.420, -0.154}, {0.395, -0.139}, {0.417, -0.108}, {0.438, -0.133}, {0.426, -0.154}}, {{-0.102, -0.281}, {-0.083, -0.259}, {-0.056, -0.275}, {-0.077, -0.296}, {-0.090, -0.287}}, {{0.370, -0.136}, {0.392, -0.136}, {0.401, -0.164}, {0.401, -0.204}, {0.367, -0.191}, {0.358, -0.170}, {0.373, -0.154}, {0.386, -0.173}, {0.389, -0.188}}, {{-0.111, -0.284}, {-0.133, -0.302}, {-0.133, -0.330}, {-0.102, -0.349}, {-0.074, -0.340}, {-0.065, -0.315}, {-0.090, -0.309}, {-0.105, -0.309}, {-0.105, -0.327}, {-0.086, -0.330}}};
            this.explodePattern(.75, pattern);
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
