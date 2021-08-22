package me.hasunemiku2015.signpic;

import org.lwjgl.glfw.GLFW;

import me.hasunemiku2015.signpic.HelpKeybind.HelpKeybindListener;
import me.hasunemiku2015.signpic.TileEntityHandler.ModController;
import me.hasunemiku2015.signpic.TileEntityHandler.RenderEvent;
import me.hasunemiku2015.signpic.TileEntityHandler.SignTileEntitySpecialRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * This is the main class of the plugin.
 */
@Mod(App.MODID)
public class App {
    public static final String MODID = "signpic";
    public static final KeyBinding toggleSignPic = new KeyBinding(I18n.format("signpic.key.toggle"), GLFW.GLFW_KEY_V,
            I18n.format("key.categories.signpic"));
    public static final KeyBinding helpSignPic = new KeyBinding(I18n.format("signpic.key.help"), GLFW.GLFW_KEY_F12,
            I18n.format("key.categories.signpic"));

    public App() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onClientSetup(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new ModController());
        MinecraftForge.EVENT_BUS.register(new RenderEvent());
        MinecraftForge.EVENT_BUS.register(new HelpKeybindListener());

        ClientRegistry.bindTileEntityRenderer(TileEntityType.SIGN,
                (TileEntityRendererDispatcher t) -> new SignTileEntitySpecialRenderer(t));
        ClientRegistry.registerKeyBinding(toggleSignPic);
        ClientRegistry.registerKeyBinding(helpSignPic);
    }
}
