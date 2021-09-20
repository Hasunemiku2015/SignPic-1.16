package me.hasunemiku2015.signpic.TileEntityHandler;

import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.BlockState;
import net.minecraft.block.StandingSignBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.tileentity.SignTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.Color;

/**
 * The custom SignTileEntityRender of this mod, most of the Magic happen here.
 */
@OnlyIn(Dist.CLIENT)
public class SignTileEntitySpecialRenderer extends SignTileEntityRenderer {
  // Variables
  static Minecraft mc = Minecraft.getInstance();
  static List<String> invalidLinks = new ArrayList<String>();

  static HashMap<String, Thread> threadPool = new HashMap<>();

  @Override
  public void render(SignTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn,
      IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {

    // Use Default Renderer
    if (!ModController.isEnabled) {
      super.render(tileEntityIn, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
      return;
    }

    String lines;
    try{
      lines = String.join("", tileEntityIn.getText(0).getString(), tileEntityIn.getText(1).getString(),
        tileEntityIn.getText(2).getString(), tileEntityIn.getText(3).getString()).replace(System.lineSeparator(), "");
    } catch (Exception ex) {
      return;
    }

    // If not SignPicture Sign use Normal Renderer
    if(!(lines.startsWith("#") && lines.contains("{") && lines.endsWith("}") )){
      super.render(tileEntityIn, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
      return;
    }
    lines = lines.replaceFirst("#", "");

    boolean isHttps = lines.startsWith("$");
    if(isHttps) lines = lines.replaceFirst("\\$", "");
    String header = isHttps ? "https://" : "http://";

    String[] var = lines.split("\\{");
    String link = var[0];
    String dimInfo = var[1].replace("}", "");

    double[] dimArr;
    try{
       dimArr = parseDimensions(dimInfo);
    } catch (Exception ex){
      super.render(tileEntityIn, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
      return;
    }

    //No need to create new Object if sign is already created
    if (RenderEvent.renderInfoMap.containsKey(tileEntityIn.getPos())) return;

    //State Calculation
    int state = 0;
    boolean isWallSign = false;

    BlockState blkState = tileEntityIn.getBlockState();
    if(blkState.getBlock() instanceof StandingSignBlock){
      double facingAngle = (blkState.get(StandingSignBlock.ROTATION) * 360) / 16.0F;

      if(dimArr[4] == 1){
        int[] shiftArr = new int[] {1, 2, 0, 3};
        state = shiftArr[(int) (facingAngle / 90)];
      } else
        state = (int) (facingAngle / 90) + 4;
    } else {
      Direction direction = blkState.get(WallSignBlock.FACING);
      state = direction.ordinal() - 2;
      isWallSign = true;
    }

    //Create RenderInfo Object and download image if missing
    RenderInfo info = new RenderInfo(tileEntityIn.getPos().getX(), tileEntityIn.getPos().getY(),
        tileEntityIn.getPos().getZ(), dimArr[0], dimArr[1], dimArr[2], dimArr[3], state, link, isWallSign);
    info.register();

    if(!RenderEvent.textureMap.containsKey(link))
      downloadImage(header, link, tileEntityIn.getPos());
    super.render(tileEntityIn, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
  }

  /**
   * Downloads the image through HTTP on another thread
   * @param url
   */
  private void downloadImage(String header, String url, BlockPos pos) {
    if(invalidLinks.contains(url) || threadPool.containsKey(url)) return;

    Thread thread = new Thread(() -> {
      HttpURLConnection connection = null;
      try {
        connection = (HttpURLConnection) (new URL(header.concat(url))).openConnection(mc.getProxy());
        connection.setDoInput(true);
        connection.setDoOutput(false);
        connection.connect();

        if (connection.getResponseCode() == 200) {
          BufferedImage bi = ImageIO.read(connection.getInputStream());
          RenderEvent.textureMap.put(url, new DynamicTexture(convertToNativeImage(bi)));
        }
      } catch (Exception exception) {
        RenderEvent.renderInfoMap.remove(pos);
        invalidLinks.add(url);
      } finally {
        if (connection != null) {
          connection.disconnect();
        }
        threadPool.remove(url);
      }
    });

    thread.setName(url);
    threadPool.put(url, thread);
    thread.setDaemon(true);
    thread.start();
  }

  /**
   * Converts a BufferedImage from java.awt to NativeImage in net.minecraft.client
   *
   * @param bufferedImag: Buffered Image
   * @return NativeImage
   */
  private NativeImage convertToNativeImage(BufferedImage bufferedImage) {
    NativeImage image = new NativeImage(bufferedImage.getWidth(), bufferedImage.getHeight(),
        bufferedImage.getColorModel().hasAlpha());
    for (int x = 0; x < bufferedImage.getWidth(); x++) {
      for (int y = 0; y < bufferedImage.getHeight(); y++) {
        int px = bufferedImage.getRGB(x, y);
        int alpha = (px >> 24) & 0xff;
        int red = (px >> 16) & 0xff;
        int green = (px >> 8) & 0xff;
        int blue = (px >> 0) & 0xff;

        // argb -> rgba
        image.setPixelRGBA(x, y, new Color(blue, green, red, alpha).getRGB());
      }
    }
    return image;
  }

  /**
   * Private method to parse dimension of image. Separated for clean code.
   * @param dimInfo
   * @return dimArray
   */
  private double[] parseDimensions(String dimInfo){
    //Example Dimension: {4x1,0x0R}, {4x1,0x0}
    //height, width, offsetH, offsetW, rot90 (0 or 1)
    double[] dimArray = new double[]{1, 1, 0, 0, 0};

    if(dimInfo.endsWith("R")){
      dimArray[4] = 1;
      dimInfo = dimInfo.replace("R", "");
    }

    String argsDim,argsOffset = null;
    if(dimInfo.contains(",")){
      String[] var = dimInfo.split(",");
      argsDim = var[0];
      argsOffset = var[1];
    } else {
      argsDim = dimInfo;
    }


    String[] argsDimArr = argsDim.split("x");
    try{
      dimArray[0] = Double.parseDouble(argsDimArr[0]);
      dimArray[1] = Double.parseDouble(argsDimArr[1]);
    } catch (Exception ignored){}

    if(argsOffset == null) return dimArray;

    String[] argsOffsetArr = argsOffset.split("x");
    try{
      dimArray[2] = Double.parseDouble(argsOffsetArr[0]);
      dimArray[3] = Double.parseDouble(argsOffsetArr[1]);
    } catch (Exception ignored){}

    return dimArray;
  }

  // Constructor: You shouldn't be constructing this class!
  public SignTileEntitySpecialRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
    super(rendererDispatcherIn);
  }
}