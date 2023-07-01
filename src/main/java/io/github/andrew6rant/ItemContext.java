package io.github.andrew6rant;

import io.github.andrew6rant.api.IItemContextOverride;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static net.minecraft.item.ItemStack.areItemsEqual;

// Based on https://github.com/VazkiiMods/Quark/blob/master/src/main/java/vazkii/quark/content/client/module/UsageTickerModule.java
public class ItemContext {

    public static List<TickerElement> elements = new ArrayList<>();

    //@Config(description = "Switch the armor display to the off hand side and the hand display to the main hand side")
    public static boolean invert = false;

    //@Config
    public static int shiftLeft = 0;
    //Config
    public static int shiftRight = 0;

    //@Config
    public static boolean enableMainHand = true;
    //@Config
    public static boolean enableOffHand = true;
    //@Config
    public static boolean enableArmor = true;

    //@Override (wasn't static before)
    public static void configChanged() {
        elements = new ArrayList<>();

        if (enableMainHand) {
            elements.add(new TickerElement(EquipmentSlot.MAINHAND));
        }
        if (enableOffHand) {
            elements.add(new TickerElement(EquipmentSlot.OFFHAND));
        }
        if (enableArmor) {
            elements.add(new TickerElement(EquipmentSlot.HEAD));
            elements.add(new TickerElement(EquipmentSlot.CHEST));
            elements.add(new TickerElement(EquipmentSlot.LEGS));
            elements.add(new TickerElement(EquipmentSlot.FEET));
        }
    }

    // clientTick(ClientTickEvent event) and renderHUD(RenderGuiOverlayEvent.Post event)
    // reimplemented as a HudRenderCallback event in ItemContextClient

    public static class TickerElement {

        private static final int MAX_TIME = 60; //Integer.MAX_VALUE
        private static final int ANIM_TIME = 9; //5

        public int liveTicks;
        public final EquipmentSlot slot;
        public ItemStack currStack = ItemStack.EMPTY;
        public ItemStack currRealStack = ItemStack.EMPTY;
        public int currCount;

        public TickerElement(EquipmentSlot slot) {
            this.slot = slot;
        }

        public void tick(PlayerEntity player) {
            ItemStack realStack = getStack(player);
            int count = getStackCount(player, realStack, realStack, false);

            ItemStack displayedStack = getLogicalStack(realStack, count, player, false);

            if (displayedStack.isEmpty()) {
                liveTicks = 0;
            } else if (shouldChange(realStack, currRealStack, count, currCount) || shouldChange(displayedStack, currStack, count, currCount)) {
                boolean done = liveTicks == 0;
                boolean animatingIn = liveTicks > MAX_TIME - ANIM_TIME;
                boolean animatingOut = liveTicks < ANIM_TIME && !done;
                if (animatingOut) {
                    liveTicks = MAX_TIME - liveTicks;
                }
                else if (!animatingIn) {
                    if (!done) {
                        liveTicks = MAX_TIME - ANIM_TIME;
                    } else {
                        liveTicks = MAX_TIME;
                    }
                }
            } else if (liveTicks > 0) {
                liveTicks--;
            }

            currCount = count;
            currStack = displayedStack;
            currRealStack = realStack;
        }

        public void render(MinecraftClient client, DrawContext context, Window window, PlayerEntity player, boolean invert, float partialTicks) {
            if(liveTicks > 0) {
                float animProgress;

                if (liveTicks < ANIM_TIME) {
                    animProgress = Math.max(0, liveTicks - partialTicks) / ANIM_TIME;
                } else {
                    animProgress = Math.min(ANIM_TIME, (MAX_TIME - liveTicks) + partialTicks) / ANIM_TIME;
                }

                float anim = -animProgress * (animProgress - 2) * 20F;

                float x = window.getScaledWidth() / 2f;
                float y = window.getScaledHeight() - anim;

                int barWidth = 190;
                boolean armor = slot.getType() == EquipmentSlot.Type.ARMOR;

                Arm primary = player.getMainArm();
                Arm ourSide = (armor != invert) ? primary : primary.getOpposite();

                int slots = armor ? 4 : 2;
                int index = slots - slot.getEntitySlotId() - 1;
                float mul = ourSide == Arm.LEFT ? -1 : 1;

                if (ourSide != primary && !player.getStackInHand(Hand.OFF_HAND).isEmpty()) {
                    barWidth += 58;
                }

                x += (barWidth / 2f) * mul + index * 20;
                if (ourSide == Arm.LEFT) {
                    x -= slots * 20;
                    x += shiftLeft;
                } else {
                    x += shiftRight;
                }

                ItemStack stack = getRenderedStack(player);

                //client.getItemRenderer().renderAndDecorateItem(stack, (int) x, (int) y);
                //client.getItemRenderer().renderGuiItemDecorations(Minecraft.getInstance().font, stack, (int) x, (int) y);
                context.drawItem(stack, (int)x, (int)y);
                context.drawItemInSlot(client.textRenderer, stack, (int) x, (int) y);
            }
        }

        public boolean shouldChange(ItemStack currStack, ItemStack prevStack, int currentTotal, int pastTotal) {
            return !areItemsEqual(prevStack, currStack) || (currStack.isDamageable() && currStack.getDamage() != prevStack.getDamage()) || currentTotal != pastTotal;
            //return !prevStack.sameItem(currStack) || (currStack.isDamageableItem() && currStack.getDamageValue() != prevStack.getDamageValue()) || currentTotal != pastTotal;
        }

        public ItemStack getStack(PlayerEntity player) {
            return player.getEquippedStack(slot);
        }

        public ItemStack getLogicalStack(ItemStack stack, int count, PlayerEntity player, boolean renderPass) {
            boolean verifySize = true;
            ItemStack returnStack = stack;
            boolean logicLock = false;

            if (stack.getItem() instanceof IItemContextOverride override) {
                stack = override.getUsageTickerItem(stack);
                returnStack = stack;
                verifySize = override.shouldUsageTickerCheckMatchSize(currStack);
            } else if (isProjectileWeapon(stack)) {
                returnStack = player.getProjectileType(stack);
                logicLock = true;
            }

            if(!logicLock) {
                if (!stack.isStackable() && slot.getType() == EquipmentSlot.Type.HAND) {
                returnStack = ItemStack.EMPTY;
                } else if (verifySize && stack.isStackable() && count == stack.getCount()) {
                    returnStack = ItemStack.EMPTY;
                }
            }

            //UsageTickerEvent.GetStack event = new GetStack(slot, returnStack, stack, count, renderPass, player);
            //MinecraftForge.EVENT_BUS.post(event);
            //return event.isCanceled() ? ItemStack.EMPTY : event.getResultStack();
            return returnStack;
        }

        public int getStackCount(PlayerEntity player, ItemStack displayStack, ItemStack original, boolean renderPass) {
            int val = 1;

            if (displayStack.isStackable()) {
                Predicate<ItemStack> predicate = (stackAt) -> ItemStack.areItemsEqual(stackAt, displayStack);

                int total = 0;
                Inventory inventory = player.getInventory();
                for (int i = 0; i < inventory.size(); i++) {
                    ItemStack stackAt = inventory.getStack(i);
                    if (predicate.test(stackAt)) {
                        total += stackAt.getCount();
                    }

                    else if (stackAt.getItem() instanceof IItemContextOverride override) {
                        total += override.getUsageTickerCountForItem(stackAt, predicate);
                    }
                }

                val = Math.max(total, displayStack.getCount());
            }

            //UsageTickerEvent.GetCount event = new GetCount(slot, displayStack, original, val, renderPass, player);
            //MinecraftForge.EVENT_BUS.post(event);
            //return event.isCanceled() ? 0 : event.getResultCount();
            return val;
        }

        private static boolean isProjectileWeapon(ItemStack stack) {
            return (stack.getItem() instanceof RangedWeaponItem) && EnchantmentHelper.getLevel(Enchantments.INFINITY, stack) == 0;
        }

        public ItemStack getRenderedStack(PlayerEntity player) {
            ItemStack stack = getStack(player);
            int count = getStackCount(player, stack, stack, true);
            ItemStack logicalStack = getLogicalStack(stack, count, player, true).copy();
            if (logicalStack != stack) {
                count = getStackCount(player, logicalStack, stack, true);
            }
            logicalStack.setCount(count);

            if (logicalStack.isEmpty()) {
                return ItemStack.EMPTY;
            }

            return logicalStack;
        }
    }

}