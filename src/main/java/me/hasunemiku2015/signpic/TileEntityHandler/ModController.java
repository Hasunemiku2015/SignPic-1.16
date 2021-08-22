package me.hasunemiku2015.signpic.TileEntityHandler;

import me.hasunemiku2015.signpic.App;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Variable and Event handlers for SignTileEntitySpecialRenderer.
 */
@OnlyIn(Dist.CLIENT)
public class ModController {
  static boolean isEnabled = true;
  static Minecraft mc = Minecraft.getInstance();

  @SubscribeEvent
  public void onKeyPress(KeyInputEvent event) {
    if (App.toggleSignPic.isPressed()){
      isEnabled = !isEnabled;

      if(isEnabled)
        mc.player.sendStatusMessage(new TranslationTextComponent("info.signpic.enable"), true);
      else
        mc.player.sendStatusMessage(new TranslationTextComponent("info.signpic.disable"), true);
    }
  }
}