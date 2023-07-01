package net.fabricmc.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.example.entity.LuoFireWorkRocket1Entity;
import net.fabricmc.example.entity.LuoFireWorkRocket2Entity;
import net.fabricmc.example.entity.LuoFireWorkRocket3Entity;
import net.fabricmc.example.entity.LuoFireWorkRocket4Entity;
import net.fabricmc.example.item.LuoFireWorkRocket1Item;
import net.fabricmc.example.item.LuoFireWorkRocket2Item;
import net.fabricmc.example.item.LuoFireWorkRocket3Item;
import net.fabricmc.example.item.LuoFireWorkRocket4Item;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleMod implements ModInitializer {
	public static final String ModID = "luotianyi"; // This is just so we can refer to our ModID easier.
	public static final Logger LOGGER = LoggerFactory.getLogger(ModID);
	public static final EntityType<LuoFireWorkRocket1Entity> LuoFireWorkRocket1EntityType = Registry.register(
			Registries.ENTITY_TYPE,
			new Identifier(ModID, "luo_firework_rocket1"),
			FabricEntityTypeBuilder.<LuoFireWorkRocket1Entity>create(SpawnGroup.MISC, LuoFireWorkRocket1Entity::new)
					.dimensions(EntityDimensions.fixed(0.25F, 0.25F)) // dimensions in Minecraft units of the projectile
					.trackRangeBlocks(4).trackedUpdateRate(10) // necessary for all thrown projectiles (as it prevents it from breaking, lol)
					.build() // VERY IMPORTANT DONT DELETE FOR THE LOVE OF GOD PSLSSSSSS
	);
	public static final EntityType<LuoFireWorkRocket2Entity> LuoFireWorkRocket2EntityType = Registry.register(
			Registries.ENTITY_TYPE,
			new Identifier(ModID, "luo_firework_rocket2"),
			FabricEntityTypeBuilder.<LuoFireWorkRocket2Entity>create(SpawnGroup.MISC, LuoFireWorkRocket2Entity::new)
					.dimensions(EntityDimensions.fixed(0.25F, 0.25F)) // dimensions in Minecraft units of the projectile
					.trackRangeBlocks(4).trackedUpdateRate(10) // necessary for all thrown projectiles (as it prevents it from breaking, lol)
					.build() // VERY IMPORTANT DONT DELETE FOR THE LOVE OF GOD PSLSSSSSS
	);
	public static final EntityType<LuoFireWorkRocket3Entity> LuoFireWorkRocket3EntityType = Registry.register(
			Registries.ENTITY_TYPE,
			new Identifier(ModID, "luo_firework_rocket3"),
			FabricEntityTypeBuilder.<LuoFireWorkRocket3Entity>create(SpawnGroup.MISC, LuoFireWorkRocket3Entity::new)
					.dimensions(EntityDimensions.fixed(0.25F, 0.25F)) // dimensions in Minecraft units of the projectile
					.trackRangeBlocks(4).trackedUpdateRate(10) // necessary for all thrown projectiles (as it prevents it from breaking, lol)
					.build() // VERY IMPORTANT DONT DELETE FOR THE LOVE OF GOD PSLSSSSSS
	);
	public static final EntityType<LuoFireWorkRocket4Entity> LuoFireWorkRocket4EntityType = Registry.register(
			Registries.ENTITY_TYPE,
			new Identifier(ModID, "luo_firework_rocket4"),
			FabricEntityTypeBuilder.<LuoFireWorkRocket4Entity>create(SpawnGroup.MISC, LuoFireWorkRocket4Entity::new)
					.dimensions(EntityDimensions.fixed(0.25F, 0.25F)) // dimensions in Minecraft units of the projectile
					.trackRangeBlocks(4).trackedUpdateRate(10) // necessary for all thrown projectiles (as it prevents it from breaking, lol)
					.build() // VERY IMPORTANT DONT DELETE FOR THE LOVE OF GOD PSLSSSSSS
	);
	public static final Item LuoFireWorkRocket4Item = new LuoFireWorkRocket4Item(new Item.Settings());
	public static final Item LuoFireWorkRocket1Item = new LuoFireWorkRocket1Item(new Item.Settings());
	public static final Item LuoFireWorkRocket2Item = new LuoFireWorkRocket2Item(new Item.Settings());
	public static final Item LuoFireWorkRocket3Item = new LuoFireWorkRocket3Item(new Item.Settings());
	@Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world!");
		Registry.register(Registries.ITEM, new Identifier(ModID, "luo_firework_rocket1"), LuoFireWorkRocket1Item);
		Registry.register(Registries.ITEM, new Identifier(ModID, "luo_firework_rocket2"), LuoFireWorkRocket2Item);
		Registry.register(Registries.ITEM, new Identifier(ModID, "luo_firework_rocket3"), LuoFireWorkRocket3Item);
		Registry.register(Registries.ITEM, new Identifier(ModID, "luo_firework_rocket4"), LuoFireWorkRocket4Item);

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(content -> {
			content.addAfter(Items.FIREWORK_ROCKET, LuoFireWorkRocket4Item);
			content.addAfter(Items.FIREWORK_ROCKET, LuoFireWorkRocket3Item);
			content.addAfter(Items.FIREWORK_ROCKET, LuoFireWorkRocket2Item);
			content.addAfter(Items.FIREWORK_ROCKET, LuoFireWorkRocket1Item);
		});

		DispenserBlock.registerBehavior(LuoFireWorkRocket1Item, new ItemDispenserBehavior() {
			public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
				Direction direction = (Direction)pointer.getBlockState().get(DispenserBlock.FACING);
				LuoFireWorkRocket1Entity fireworkRocketEntity = new LuoFireWorkRocket1Entity(pointer.getWorld(), stack, pointer.getX(), pointer.getY(), pointer.getX(), true);
				DispenserBehavior.setEntityPosition(pointer, fireworkRocketEntity, direction);
				fireworkRocketEntity.setVelocity((double)direction.getOffsetX(), (double)direction.getOffsetY(), (double)direction.getOffsetZ(), 0.5F, 1.0F);
				pointer.getWorld().spawnEntity(fireworkRocketEntity);
				stack.decrement(1);
				return stack;
			}

			protected void playSound(BlockPointer pointer) {
				pointer.getWorld().syncWorldEvent(WorldEvents.FIREWORK_ROCKET_SHOOTS, pointer.getPos(), 0);
			}
		});
		DispenserBlock.registerBehavior(LuoFireWorkRocket2Item, new ItemDispenserBehavior() {
			public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
				Direction direction = (Direction)pointer.getBlockState().get(DispenserBlock.FACING);
				LuoFireWorkRocket2Entity fireworkRocketEntity = new LuoFireWorkRocket2Entity(pointer.getWorld(), stack, pointer.getX(), pointer.getY(), pointer.getX(), true);
				DispenserBehavior.setEntityPosition(pointer, fireworkRocketEntity, direction);
				fireworkRocketEntity.setVelocity((double)direction.getOffsetX(), (double)direction.getOffsetY(), (double)direction.getOffsetZ(), 0.5F, 1.0F);
				pointer.getWorld().spawnEntity(fireworkRocketEntity);
				stack.decrement(1);
				return stack;
			}

			protected void playSound(BlockPointer pointer) {
				pointer.getWorld().syncWorldEvent(WorldEvents.FIREWORK_ROCKET_SHOOTS, pointer.getPos(), 0);
			}
		});
		DispenserBlock.registerBehavior(LuoFireWorkRocket3Item, new ItemDispenserBehavior() {
			public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
				Direction direction = (Direction)pointer.getBlockState().get(DispenserBlock.FACING);
				LuoFireWorkRocket3Entity fireworkRocketEntity = new LuoFireWorkRocket3Entity(pointer.getWorld(), stack, pointer.getX(), pointer.getY(), pointer.getX(), true);
				DispenserBehavior.setEntityPosition(pointer, fireworkRocketEntity, direction);
				fireworkRocketEntity.setVelocity((double)direction.getOffsetX(), (double)direction.getOffsetY(), (double)direction.getOffsetZ(), 0.5F, 1.0F);
				pointer.getWorld().spawnEntity(fireworkRocketEntity);
				stack.decrement(1);
				return stack;
			}

			protected void playSound(BlockPointer pointer) {
				pointer.getWorld().syncWorldEvent(WorldEvents.FIREWORK_ROCKET_SHOOTS, pointer.getPos(), 0);
			}
		});
		DispenserBlock.registerBehavior(LuoFireWorkRocket4Item, new ItemDispenserBehavior() {
			public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
				Direction direction = (Direction)pointer.getBlockState().get(DispenserBlock.FACING);
				LuoFireWorkRocket4Entity fireworkRocketEntity = new LuoFireWorkRocket4Entity(pointer.getWorld(), stack, pointer.getX(), pointer.getY(), pointer.getX(), true);
				DispenserBehavior.setEntityPosition(pointer, fireworkRocketEntity, direction);
				fireworkRocketEntity.setVelocity((double)direction.getOffsetX(), (double)direction.getOffsetY(), (double)direction.getOffsetZ(), 0.5F, 1.0F);
				pointer.getWorld().spawnEntity(fireworkRocketEntity);
				stack.decrement(1);
				return stack;
			}

			protected void playSound(BlockPointer pointer) {
				pointer.getWorld().syncWorldEvent(WorldEvents.FIREWORK_ROCKET_SHOOTS, pointer.getPos(), 0);
			}
		});
	}
}
