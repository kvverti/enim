package kvverti.enim.entity;

import java.util.Arrays;

import net.minecraft.block.properties.IProperty;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;

import kvverti.enim.entity.properties.PropertyArmor;
import kvverti.enim.entity.state.RenderState;

/** Base for entities that may wear armor. */
public class ArmorLivingRender<T extends EntityLivingBase> extends LivingRender<T> {

	public static final IProperty<ArmorMaterial> ARMOR_HEAD = PropertyArmor.create("armor_head");
	public static final IProperty<ArmorMaterial> ARMOR_CHEST = PropertyArmor.create("armor_chest");
	public static final IProperty<ArmorMaterial> ARMOR_LEGS = PropertyArmor.create("armor_legs");
	public static final IProperty<ArmorMaterial> ARMOR_FEET = PropertyArmor.create("armor_feet");

	/** Construct an instance with the given additional properties. Do not include the armor properties in the array. */
	public ArmorLivingRender(RenderManager manager, IProperty<?>... properties) {

		super(manager, plusArmor(properties));
	}

	private static IProperty<?>[] plusArmor(IProperty<?>[] init) {

		int len = init.length;
		IProperty<?>[] res = Arrays.copyOf(init, len + 4);
		res[len] = ARMOR_HEAD;
		res[len + 1] = ARMOR_CHEST;
		res[len + 2] = ARMOR_LEGS;
		res[len + 3] = ARMOR_FEET;
		return res;
	}

	/**
	 * Calculates the armor states for the given entity. Subclasses with additional properties should override this method to apply
	 * additional properties to the returned result by first calling {@code super.getStateFromEntity(entity)}.
	 */
	@Override
	public RenderState getStateFromEntity(T entity) {

		return getStateManager().getDefaultState()
			.withProperty(ARMOR_HEAD, getMaterial(entity, EntityEquipmentSlot.HEAD))
			.withProperty(ARMOR_CHEST, getMaterial(entity, EntityEquipmentSlot.CHEST))
			.withProperty(ARMOR_LEGS, getMaterial(entity, EntityEquipmentSlot.LEGS))
			.withProperty(ARMOR_FEET, getMaterial(entity, EntityEquipmentSlot.FEET));
	}

	private static final ArmorMaterial NONE = ArmorMaterial.valueOf("ENIM_NONE");

	private ArmorMaterial getMaterial(T entity, EntityEquipmentSlot slot) {

		ItemStack stack = entity.getItemStackFromSlot(slot);
		if(!stack.isEmpty() && stack.getItem() instanceof ItemArmor) {

			ItemArmor armor = (ItemArmor) stack.getItem();
			if(armor.getEquipmentSlot() == slot)
				return armor.getArmorMaterial();
		}
		return NONE;
	}
}