package me.hasunemiku2015.signpic.HelpKeybind;

import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import me.hasunemiku2015.signpic.App;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;

/**
 * The class that listens to the display help keybind.
 */
@OnlyIn(Dist.CLIENT)
public class HelpKeybindListener {
  static Minecraft mc = Minecraft.getInstance();

  @SubscribeEvent
  public void onKeyPress(KeyInputEvent event){
    if(event.getKey() == App.helpSignPic.getKey().getKeyCode()){
      mc.player.sendStatusMessage(new TranslationTextComponent("info.signpic.help"), false);
    }
  }
}