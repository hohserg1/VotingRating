package hohserg.voting.rating;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.config.Config;

import java.util.List;

@Config(modid = Main.modid)
public class Configuration {
    @Config.Comment({
            "List of locations of rating panels",
            "Each must be string of three numbers separated by commas"
    })
    public static String[] ratingPanelLocations = {"0, 70, 0", "20, 70, 0"};

    @Config.Comment("How far panel can visible ")
    public static int panelVisibleDistance = 256;

    @Config.Comment("How many elements have panel")
    @Config.RangeInt(min = 3, max = 30)
    public static int panelSize = 10;
}
