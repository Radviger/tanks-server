package gtanks.battles.tanks.loaders;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import gtanks.StringUtils;
import gtanks.battles.tanks.hulls.Hull;
import gtanks.exceptions.GTanksServerException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HullsFactory {
    private static final Gson GSON = new Gson();
    private static final Map<String, Hull> hulls = new HashMap<>();

    public static void init(String path2configs) {
        hulls.clear();

        File file = new File(path2configs);

        for (File config : file.listFiles()) {
            try {
                parse(config);
            } catch (Exception e) {
                throw new RuntimeException("Error reading config " + config, e);
            }
        }
    }

    private static void parse(File config) throws IOException, JsonParseException {
        JsonObject obj = GSON.fromJson(new FileReader(config), JsonObject.class);
        String type = obj.get("type").getAsString();

        for (JsonElement e : obj.getAsJsonArray("modifications")) {
            JsonObject mod = e.getAsJsonObject();
            Hull hull = new Hull(mod.get("mass").getAsFloat(), mod.get("power").getAsFloat(), mod.get("speed").getAsFloat(), mod.get("turn_speed").getAsFloat(), mod.get("hp").getAsFloat());
            hulls.put(StringUtils.concatStrings(type, "_", mod.get("modification").getAsString()), hull);
        }

    }

    public static Hull getHull(String id) {
        Hull hull = hulls.get(id);
        if (hull == null) {
            throw new GTanksServerException("Hull with id " + id + " is null!");
        } else {
            return hull;
        }
    }
}
