package soulboundarmory.component.soulbound.item;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import soulboundarmory.client.gui.screen.SoulboundTab;
import soulboundarmory.client.gui.screen.StatisticEntry;
import soulboundarmory.component.Components;
import soulboundarmory.component.soulbound.player.SoulboundComponent;
import soulboundarmory.component.statistics.Category;
import soulboundarmory.component.statistics.EnchantmentStorage;
import soulboundarmory.component.statistics.SkillStorage;
import soulboundarmory.component.statistics.Statistic;
import soulboundarmory.component.statistics.StatisticType;
import soulboundarmory.component.statistics.Statistics;
import soulboundarmory.config.Configuration;
import soulboundarmory.item.SoulboundItem;
import soulboundarmory.network.ExtendedPacketBuffer;
import soulboundarmory.network.Packets;
import soulboundarmory.serial.CompoundSerializable;
import soulboundarmory.skill.Skill;
import soulboundarmory.skill.SkillContainer;
import soulboundarmory.util.ItemUtil;

public abstract class ItemStorage<T extends ItemStorage<T>> implements CompoundSerializable {
    protected static final NumberFormat statisticFormat = DecimalFormat.getInstance();

    public final SoulboundComponent component;
    public final PlayerEntity player;
    public final Item item;
    public EnchantmentStorage enchantments;

    protected final boolean client;
    protected SkillStorage skills;
    protected Statistics statistics;
    protected ItemStack itemStack;
    protected boolean unlocked;
    protected int boundSlot;

    public ItemStorage(SoulboundComponent component, Item item) {
        this.component = component;
        this.player = component.player;
        this.client = component.player.world.isClient;
        this.item = item;
        this.itemStack = this.newItemStack();
    }

    public abstract Text name();

    public abstract List<StatisticEntry> screenAttributes();

    public abstract List<Text> tooltip();

    public abstract Item consumableItem();

    public abstract double increase(StatisticType statistic);

    public abstract int getLevelXP(int level);

    public abstract StorageType<T> type();

    public abstract Class<? extends SoulboundItem> itemClass();

    public abstract Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers(Multimap<EntityAttribute, EntityAttributeModifier> modifiers, EquipmentSlot slot);

    public static Stream<ItemStorage<?>> all(Entity entity) {
        return Components.soulbound(entity).flatMap(component -> component.storages.values().stream());
    }

    public static Optional<ItemStorage<?>> get(Entity entity, Item item) {
        return all(entity).filter(storage -> storage.player == entity && storage.item == item).findAny();
    }

    public static ItemStorage<?> get(Entity entity, StorageType<?> type) {
        return all(entity).filter(storage -> storage.type() == type).findAny().orElse(null);
    }

    public static Optional<ItemStorage<?>> get(Entity entity, ItemStack stack) {
        return all(entity).filter(storage -> storage.accepts(stack)).findAny();
    }

    public static Optional<ItemStorage<?>> firstEquipped(Entity entity, boolean consumable) {
        if (entity == null) {
            return Optional.empty();
        }

        var storages = all(entity).toList();

        for (var itemStack : entity.getItemsHand()) {
            for (var storage : storages) {
                if (storage.accepts(itemStack) || consumable && storage.canConsume(itemStack)) {
                    return Optional.of(storage);
                }
            }
        }

        return Optional.empty();
    }

    public boolean accepts(ItemStack stack) {
        return stack.getItem() == this.item;
    }

    public ItemStack equippedMenuStack() {
        for (var itemStack : this.player.getItemsHand()) {
            if (this.accepts(itemStack) || this.canConsume(itemStack)) {
                return itemStack;
            }
        }

        return null;
    }

    public boolean isUnlocked() {
        return this.unlocked;
    }

    public void unlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public int size(Category category) {
        return this.statistics.size(category);
    }

    public Statistic statistic(StatisticType statistic) {
        return this.statistics.get(statistic);
    }

    public Statistic statistic(Category category, StatisticType statistic) {
        return this.statistics.get(category, statistic);
    }

    public double attributeTotal(StatisticType statistic) {
        if (statistic == StatisticType.attackDamage) {
            var attackDamage = this.doubleValue(StatisticType.attackDamage);

            for (var entry : this.enchantments.entrySet()) {
                var enchantment = entry.getKey();

                if (entry.getValue() > 0) {
                    attackDamage += enchantment.getAttackDamage(this.enchantment(enchantment), EntityGroup.DEFAULT);
                }
            }

            return attackDamage;
        }

        return this.doubleValue(statistic);
    }

    public double attributeRelative(StatisticType attribute) {
        if (attribute == StatisticType.attackSpeed) return this.doubleValue(StatisticType.attackSpeed) - 4;
        if (attribute == StatisticType.attackDamage) return this.doubleValue(StatisticType.attackDamage) - 1;
        if (attribute == StatisticType.reach) return this.doubleValue(StatisticType.reach) - 3;

        return this.doubleValue(attribute);
    }

    public int intValue(StatisticType type) {
        return this.statistic(type).intValue();
    }

    public float floatValue(StatisticType type) {
        return this.statistic(type).floatValue();
    }

    public double doubleValue(StatisticType type) {
        return this.statistic(type).doubleValue();
    }

    public boolean incrementStatistic(StatisticType type, double amount) {
        var leveledUp = false;

        if (type == StatisticType.experience) {
            var statistic = this.statistic(StatisticType.experience);
            statistic.add(amount);
            var xp = statistic.intValue();

            if (xp >= this.nextLevelXP() && this.canLevelUp()) {
                var nextLevelXP = this.nextLevelXP();

                this.incrementStatistic(StatisticType.level, 1);
                this.incrementStatistic(StatisticType.experience, -nextLevelXP);

                leveledUp = true;
            }

            if (xp < 0) {
                var currentLevelXP = this.getLevelXP(this.intValue(StatisticType.level) - 1);

                this.incrementStatistic(StatisticType.level, -1);
                this.incrementStatistic(StatisticType.experience, currentLevelXP);
            }
        } else if (type == StatisticType.level) {
            var sign = (int) Math.signum(amount);

            for (var i = 0; i < Math.abs(amount); i++) {
                this.onLevelup(sign);
            }
        } else {
            this.statistics.add(type, amount);
        }

        this.updateItemStack();
        this.sync();

        return leveledUp;
    }

    public void incrementPoints(StatisticType type, int points) {
        if (points == 0) {
            return;
        }

        var statistic = this.statistic(type);
        BigDecimal change;

        if (points > 0) {
            change = BigDecimal.valueOf(statistic.max()).subtract(statistic.value());
            var newPoints = change.divide(BigDecimal.valueOf(this.increase(type)), RoundingMode.UP);
            var bigPoints = BigDecimal.valueOf(points);

            if (newPoints.compareTo(bigPoints) < 0) {
                points = newPoints.intValue();
            } else {
                change = BigDecimal.valueOf(this.increase(type)).multiply(bigPoints);
            }
        } else {
            change = BigDecimal.valueOf(statistic.min()).subtract(statistic.value());
            var newPoints = change.divide(BigDecimal.valueOf(this.increase(type)), RoundingMode.UP);
            var bigPoints = BigDecimal.valueOf(points);

            if (newPoints.compareTo(bigPoints) > 0) {
                points = newPoints.intValue();
            } else {
                change = BigDecimal.valueOf(this.increase(type)).multiply(bigPoints);
            }
        }

        var attributePoints = this.statistic(StatisticType.attributePoints);
        var spentAttributePoints = this.statistic(StatisticType.spentAttributePoints);

        attributePoints.add(-points);
        spentAttributePoints.add(points);
        statistic.add(change);
        statistic.incrementPoints(points);

        this.updateItemStack();
        this.sync();
    }

    public void set(StatisticType statistic, Number value) {
        this.statistics.put(statistic, value);
        this.sync();
    }

    public boolean canLevelUp() {
        var configuration = Configuration.instance();
        return this.intValue(StatisticType.level) < configuration.maxLevel || configuration.maxLevel < 0;
    }

    public void onLevelup(int sign) {
        var statistic = this.statistic(StatisticType.level);
        statistic.add(sign);
        var level = statistic.intValue();
        var configuration = Configuration.instance();

        if (level % configuration.levelsPerEnchantment == 0) {
            this.incrementStatistic(StatisticType.enchantmentPoints, sign);
        }

        if (level % configuration.levelsPerSkillPoint == 0) {
            this.incrementStatistic(StatisticType.skillPoints, sign);
        }

        this.incrementStatistic(StatisticType.attributePoints, sign);
    }

    public Collection<SkillContainer> skills() {
        return this.skills.values();
    }

    public SkillContainer skill(Identifier identifier) {
        return this.skill(Skill.registry.getValue(identifier));
    }

    public SkillContainer skill(Skill skill) {
        return this.skills.get(skill);
    }

    public boolean hasSkill(Identifier identifier) {
        return this.hasSkill(Skill.registry.getValue(identifier));
    }

    public boolean hasSkill(Skill skill) {
        return this.skills.contains(skill);
    }

    public boolean hasSkill(Skill skill, int level) {
        return this.skills.contains(skill, level);
    }

    public void upgrade(SkillContainer skill) {
        if (this.client) {
            Packets.serverSkill.send(new ExtendedPacketBuffer().writeIdentifier(skill.skill().getRegistryName()));
        } else {
            var points = this.intValue(StatisticType.skillPoints);
            var cost = skill.cost();

            // Synchronization is handled by incrementStatistic.
            if (skill.canLearn(points)) {
                skill.learn();
                this.incrementStatistic(StatisticType.skillPoints, -cost);
            } else if (skill.canUpgrade(points)) {
                skill.upgrade();
                this.incrementStatistic(StatisticType.skillPoints, -cost);
            }
        }
    }

    public int nextLevelXP() {
        return this.getLevelXP(this.intValue(StatisticType.level));
    }

    public int enchantment(Enchantment enchantment) {
        return this.enchantments.get(enchantment);
    }

    public void addEnchantment(Enchantment enchantment, int levels) {
        var current = this.enchantment(enchantment);
        var change = Math.max(0, current + levels) - current;

        this.statistics.add(StatisticType.enchantmentPoints, -change);
        this.statistics.add(StatisticType.spentEnchantmentPoints, change);

        this.enchantments.add(enchantment, change);
        this.updateItemStack();

        this.sync();
    }

    public void reset() {
        for (var category : Category.registry) {
            this.reset(category);
        }

        this.unlocked = false;
        this.boundSlot = -1;

        this.sync();
    }

    public void reset(Category category) {
        if (category == Category.datum) {
            this.statistics.reset(Category.datum);
        } else if (category == Category.attribute) {
            for (var type : this.statistics.get(Category.attribute).values()) {
                this.incrementStatistic(StatisticType.attributePoints, type.points());
                type.reset();
            }

            this.set(StatisticType.spentAttributePoints, 0);
            this.updateItemStack();
        } else if (category == Category.enchantment) {
            for (var enchantment : this.enchantments) {
                this.incrementStatistic(StatisticType.enchantmentPoints, this.enchantments.get(enchantment));
                this.enchantments.put(enchantment, 0);
            }

            this.statistic(StatisticType.spentEnchantmentPoints).setToMin();
            this.updateItemStack();
        } else if (category == Category.skill) {
            this.skills.reset();

            this.statistic(StatisticType.skillPoints).setToMin();
        }

        this.sync();
    }

    public boolean canConsume(ItemStack item) {
        return this.consumableItem() == item.getItem();
    }

    public boolean canUnlock() {
        return !this.unlocked && ItemUtil.handStacks(this.player).anyMatch(this::canConsume);
    }

    public boolean isMenuItem(ItemStack stack) {
        return this.accepts(stack) || this.canConsume(stack);
    }

    public int boundSlot() {
        return this.boundSlot;
    }

    public void bindSlot(int boundSlot) {
        this.boundSlot = boundSlot;
    }

    public void unbindSlot() {
        this.boundSlot = -1;
    }

/*
    public void refresh() {
        if (this.client) {
            if (CellElement.minecraft.currentScreen instanceof SoulboundScreen) {
                if (ItemUtil.handStacks(this.player).anyMatch(this::accepts)) {
                    this.openGUI(this.component.tab());
                } else if (ItemUtil.handStacks(this.player).anyMatch(this::canConsume)) {
                    this.openGUI(0);
                }
            }
        } else {
            Packets.clientRefresh.send(this.player, new ExtendedPacketBuffer(this));
        }
    }

    public boolean tryOpenGUI() {
        var handStacks = ItemUtil.handStacks(this.player).toList().listIterator();

        while (handStacks.hasNext()) {
            var stack = handStacks.next();

            if (this.accepts(stack)) {
                this.openGUI(this.component.tab(), handStacks.previousIndex());
            } else if (this.canConsume(stack)) {
                this.openGUI(0, handStacks.previousIndex());
            } else {
                continue;
            }

            return true;
        }

        return false;
    }

    public void openGUI() {
        this.openGUI(ItemUtil.equippedStack(this.player.inventory, this.itemClass()) == null ? 0 : this.component.tab());
    }

    public void openGUI(int tab, int slot) {
        if (this.client) {
            if (SoulboundArmoryClient.client.currentScreen instanceof SoulboundScreen screen && this.component.tab() == tab) {
                screen.refresh();
            } else {
                SoulboundArmoryClient.client.openScreen(new SoulboundScreen(this.component, this.tabs(), tab, slot));
            }
        } else {
            Packets.clientOpenGUI.send(this.player, new ExtendedPacketBuffer(this).writeInt(tab));
        }
    }
*/

    public boolean isItemEquipped() {
        return ItemUtil.handStacks(this.player).anyMatch(this::accepts);
    }

    public boolean isMenuItemEquipped() {
        return this.equippedMenuStack() != null;
    }

    public void removeOtherItems(int slot) {
        for (var storage : this.component.storages.values()) {
            if (storage != this) {
                var inventory = ItemUtil.inventory(this.player).toList().listIterator();

                while (inventory.hasNext()) {
                    var stack = inventory.next();

                    if (storage.accepts(stack) && (storage != this || inventory.previousIndex() != slot)) {
                        this.player.inventory.removeOne(stack);
                    }
                }
            }
        }
    }

    public ItemStack stack() {
        return this.itemStack;
    }

    protected ItemStack newItemStack() {
        return new ItemStack(this.item);
    }

    protected void updateItemStack() {
        this.itemStack = this.newItemStack();

        for (var slot : EquipmentSlot.values()) {
            var attributeModifiers = this.attributeModifiers(slot);

            for (var attribute : attributeModifiers.keySet()) {
                for (var modifier : attributeModifiers.get(attribute)) {
                    this.itemStack.addAttributeModifier(attribute, modifier, EquipmentSlot.MAINHAND);
                }
            }
        }

        for (var entry : this.enchantments.entrySet()) {
            int level = entry.getValue();

            if (level > 0) {
                this.itemStack.addEnchantment(entry.getKey(), level);
            }
        }
    }

    public final Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers(EquipmentSlot slot) {
        return this.attributeModifiers(LinkedHashMultimap.create(), slot);
    }

    protected String formatStatistic(StatisticType statistic) {
        var value = this.attributeTotal(statistic);
        return statisticFormat.format(statistic == StatisticType.criticalStrikeRate ? value * 100 : value);
    }

    public abstract List<SoulboundTab> tabs();

    public void tick() {}

    @Override
    public void serializeNBT(NbtCompound tag) {
        tag.put("statistics", this.statistics.serializeNBT());
        tag.put("enchantments", this.enchantments.serializeNBT());
        tag.put("skills", this.skills.serializeNBT());
        tag.putBoolean("unlocked", this.unlocked);
        tag.putInt("slot", this.boundSlot());
    }

    @Override
    public void deserializeNBT(NbtCompound tag) {
        this.statistics.deserializeNBT(tag.getCompound("statistics"));
        this.enchantments.deserializeNBT(tag.getCompound("enchantments"));
        this.skills.deserializeNBT(tag.getCompound("skills"));
        this.unlocked(tag.getBoolean("unlocked"));
        this.bindSlot(tag.getInt("slot"));

        this.updateItemStack();
    }

    /**
     Synchronize information to the client.
     */
    public void sync() {
        Packets.clientSync.sendIfServer(this.player, new ExtendedPacketBuffer(this).writeNbt(this.serializeNBT()));
    }
}
