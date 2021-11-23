package net.auoeke.soulboundarmory;

import net.auoeke.cell.client.gui.CellElement;
import net.auoeke.soulboundarmory.client.keyboard.ExperienceBarKeyBinding;
import net.auoeke.soulboundarmory.client.keyboard.GUIKeyBinding;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SoulboundArmoryClient {
    public static final Minecraft client = CellElement.client;

    public static final KeyBinding guiKeyBinding = new GUIKeyBinding();
    public static final KeyBinding toggleXPBarKeyBinding = new ExperienceBarKeyBinding();

    public static PlayerEntity player() {
        return client.player;
    }
}
