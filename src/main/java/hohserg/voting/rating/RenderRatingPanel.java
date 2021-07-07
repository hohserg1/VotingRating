package hohserg.voting.rating;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Mod.EventBusSubscriber(modid = Main.modid, value = Side.CLIENT)
public class RenderRatingPanel {

    @SubscribeEvent
    public static void onRenderPanel(RenderWorldLastEvent event) {
        for (BlockPos location : Main.locations())
            if (mc().player.getDistanceSq(location) < Configuration.panelVisibleDistance * Configuration.panelVisibleDistance)
                drawPanel(location);
    }

    private static void drawPanel(BlockPos location) {
        GlStateManager.pushMatrix();

        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        translateToZeroCoord(mc().getRenderPartialTicks());

        GlStateManager.translate(location.getX(), location.getY(), location.getZ());
        GlStateManager.rotate(-mc().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(-1, -1, 1);
        double scale = 0.05;
        GlStateManager.scale(scale, scale, scale);

        FontRenderer font = mc().fontRenderer;

        List<Pair<String, Integer>> data = rating.get();

        for (int i = 0; i < Math.min(data.size(), Configuration.panelSize); i++) {
            Pair<String, Integer> nickCount = data.get(i);
            String line = prepareLine(nickCount, i);
            int stringWidth = font.getStringWidth(line);
            drawBackLine(stringWidth + font.FONT_HEIGHT, i, 50, 50, 50, 100);
            font.drawString(line, -stringWidth / 2, (i - Configuration.panelSize) * (font.FONT_HEIGHT + 2), 0xffffff);
        }

        GlStateManager.scale(1.5, 1.5, 1);
        String title = TextFormatting.LIGHT_PURPLE + "Рейтинг голосования";
        int stringWidth = font.getStringWidth(title);
        int y = 2;
        drawBackLine(stringWidth + +font.FONT_HEIGHT, y, 0, 0, 0, 100);
        font.drawString(title, -stringWidth / 2, (y - Configuration.panelSize) * (font.FONT_HEIGHT + 2), 0xffffff);

        GlStateManager.popMatrix();

    }

    private static void drawBackLine(int stringWidth, int i, int r, int g, int b, int a) {

        FontRenderer font = mc().fontRenderer;
        GlStateManager.disableTexture2D();
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(-stringWidth / 2, -1 + (i - Configuration.panelSize) * (font.FONT_HEIGHT + 2), 0.1).color(r, g, b, a).endVertex();
        buffer.pos(-stringWidth / 2, font.FONT_HEIGHT + (i - Configuration.panelSize) * (font.FONT_HEIGHT + 2), 0.1).color(r, g, b, a).endVertex();
        buffer.pos(stringWidth / 2, font.FONT_HEIGHT + (i - Configuration.panelSize) * (font.FONT_HEIGHT + 2), 0.1).color(r, g, b, a).endVertex();
        buffer.pos(stringWidth / 2, -1 + (i - Configuration.panelSize) * (font.FONT_HEIGHT + 2), 0.1).color(r, g, b, a).endVertex();
        Tessellator.getInstance().draw();

        GlStateManager.enableTexture2D();
    }

    private static String prepareLine(Pair<String, Integer> nickCount, int i) {
        if (i < 3)
            return TextFormatting.GOLD + nickCount.getLeft() + TextFormatting.GREEN + " > " + TextFormatting.AQUA + nickCount.getRight();
        else
            return TextFormatting.WHITE + nickCount.getLeft() + TextFormatting.GREEN + " > " + TextFormatting.AQUA + nickCount.getRight();
    }

    private static Minecraft mc() {
        return Minecraft.getMinecraft();
    }

    private static void translateToZeroCoord(float partialTicks) {
        Entity player = mc().player;
        double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
        GlStateManager.translate(-x, -y, -z);
    }

    private static AtomicReference<List<Pair<String, Integer>>> rating = new AtomicReference<>();

    static void preInit() {
        if (rating.get() == null)
            updateRating();
    }

    private static long ticks = 0;

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        ticks++;

        if (ticks >= 20 * 60 * 60) {
            updateRating();
            ticks = 0;
        }
    }

    private static void updateRating() {
        new Thread(() -> {
            List<Pair<String, Integer>> newRating = Main.queryRating();
            rating.set(newRating);
        }).start();
    }
}
