package hohserg.voting.rating;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Mod(modid = Main.modid, name = "VotingRating")
public class Main {

    public static final String modid = "voting_rating";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        RenderRatingPanel.preInit();
    }

    public static List<Pair<String, Integer>> queryRating() {
        try {
            Document doc = Jsoup.connect("https://monitoringminecraft.ru/top/tfc-survival").get();

            return doc.body().getElementsByClass("popcols").get(0)
                    .children().stream()
                    .filter(e -> e.select("h3").get(0).text().equals("Топ голосующих"))
                    .map(e -> e.select("tbody").get(0))
                    .map(e -> e.children().stream().map(line -> Pair.of(line.child(0).child(0).text(), Integer.parseInt(line.child(1).text()))).collect(toList()))
                    .findAny()
                    .get();
        } catch (Throwable e) {
            e.printStackTrace();
            return ImmutableList.of();
        }
    }


    private static List<BlockPos> locations;

    public static List<BlockPos> locations() {
        if (locations == null)
            locations = Arrays.stream(Configuration.ratingPanelLocations).flatMap(loc -> toPos(loc.replaceAll("\\s+", "").split(","))).collect(toList());
        return locations;
    }

    private static Stream<BlockPos> toPos(String[] coords) {
        try {
            return Stream.of(new BlockPos(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2])));
        } catch (Throwable e) {
            System.out.println("Exception while parsing rating panel locations");
            e.printStackTrace();
            return Stream.empty();
        }
    }
}
