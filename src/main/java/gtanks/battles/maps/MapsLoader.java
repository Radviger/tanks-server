package gtanks.battles.maps;

import com.google.gson.*;
import gtanks.battles.maps.parser.Parser;
import gtanks.battles.maps.parser.map.bonus.BonusRegion;
import gtanks.battles.maps.parser.map.bonus.BonusType;
import gtanks.battles.maps.parser.map.spawn.SpawnPosition;
import gtanks.battles.maps.parser.map.spawn.SpawnPositionType;
import gtanks.battles.maps.themes.MapThemeFactory;
import gtanks.battles.tanks.math.Vector3;
import gtanks.logger.Logger;
import org.apache.commons.codec.digest.DigestUtils;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsLoader {
    private static final Gson GSON = new Gson();
    public static java.util.Map<String, Map> maps = new HashMap<>();
    private static List<MapConfigItem> configItems = new ArrayList<>();
    private static Parser parser;

    public static void initFactoryMaps() {
        Logger.log("Maps Loader Factory inited. Loading maps...");

        try {
            parser = new Parser();
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        loadConfig();
    }

    private static void loadConfig() {
        File file = new File("config/maps/config.json");
        try {
            JsonObject obj = GSON.fromJson(new FileReader(file), JsonObject.class);
            JsonArray maps = obj.getAsJsonArray("maps");

            for (JsonElement m : maps) {
                JsonObject item = m.getAsJsonObject();
                String id = item.get("id").getAsString();
                String name = item.get("name").getAsString();
                String skyboxId = item.get("skybox_id").getAsString();
                JsonElement ambientSoundId = item.get("ambient_sound_id");
                JsonElement gameModeId = item.get("gamemode_id");
                int minRank = Integer.parseInt(item.get("min_rank").getAsString());
                int maxRank = Integer.parseInt(item.get("max_rank").getAsString());
                int maxPlayers = Integer.parseInt(item.get("max_players").getAsString());
                boolean tdm = item.get("tdm").getAsBoolean();
                boolean ctf = item.get("ctf").getAsBoolean();
                JsonElement themeId = item.get("theme_id");
                MapConfigItem __item = ambientSoundId != null && gameModeId != null ? new MapConfigItem(id, name, skyboxId, minRank, maxRank, maxPlayers, tdm, ctf, ambientSoundId.getAsString(), gameModeId.getAsString()) : new MapConfigItem(id, name, skyboxId, minRank, maxRank, maxPlayers, tdm, ctf);
                if (themeId != null) {
                    __item.themeName = themeId.getAsString();
                }
                configItems.add(__item);
            }

            parseMaps();
        } catch (Exception e) {
            throw new RuntimeException("Error loading config " + file, e);
        }
    }

    private static void parseMaps() {
        File[] maps = (new File("config/maps")).listFiles();

        for (File file : maps) {
            if (!file.isDirectory() && file.getName().endsWith(".xml")) {
                parse(file);
            }
        }

        Logger.log("Loaded all maps!\n");
    }

    private static void parse(File file) {
        Logger.log("Loading " + file.getName() + "...");
        MapConfigItem temp = getMapItem(file.getName().substring(0, file.getName().length() - 4));
        if (temp != null) {
            Map map = new Map();

            try {
                map.name = temp.name;
                map.id = temp.id;
                map.skyboxId = temp.skyboxId;
                map.minRank = temp.minRank;
                map.maxRank = temp.maxRank;
                map.maxPlayers = temp.maxPlayers;
                map.tdm = temp.tdm;
                map.ctf = temp.ctf;
                map.md5Hash = DigestUtils.md5Hex(new FileInputStream(file));
                map.mapTheme = temp.ambientSoundId != null && temp.gameMode != null ? MapThemeFactory.getMapTheme(temp.ambientSoundId, temp.gameMode) : MapThemeFactory.getDefaultMapTheme();
                map.themeId = temp.themeName;
            } catch (IOException e) {
                e.printStackTrace();
            }

            gtanks.battles.maps.parser.map.Map parsedMap = null;

            try {
                parsedMap = parser.parseMap(file);
            } catch (JAXBException var8) {
                var8.printStackTrace();
            }

            for (SpawnPosition sp : parsedMap.getSpawnPositions()) {
                if (sp.getSpawnPositionType() == SpawnPositionType.NONE) {
                    map.spawnPositonsDM.add(sp.getVector3());
                }

                if (sp.getSpawnPositionType() == SpawnPositionType.RED) {
                    map.spawnPositonsRed.add(sp.getVector3());
                }

                if (sp.getSpawnPositionType() == SpawnPositionType.BLUE) {
                    map.spawnPositonsBlue.add(sp.getVector3());
                }
            }

            if (parsedMap.getBonusesRegion() != null) {

                for (BonusRegion br : parsedMap.getBonusesRegion()) {
                    for (BonusType type : br.getType()) {
                        switch (type) {
                            case CRYSTALL:
                                map.crystallsRegions.add(br.toServerBonusRegion());
                                break;
                            case CRYSTALL_100:
                                map.goldsRegions.add(br.toServerBonusRegion());
                                break;
                            case ARMOR:
                                map.armorsRegions.add(br.toServerBonusRegion());
                                break;
                            case DAMAGE:
                                map.damagesRegions.add(br.toServerBonusRegion());
                                break;
                            case HEAL:
                                map.healthsRegions.add(br.toServerBonusRegion());
                                break;
                            case NITRO:
                                map.nitrosRegions.add(br.toServerBonusRegion());
                                break;
                        }
                    }
                }
            }

            map.flagBluePosition = parsedMap.getPositionBlueFlag() != null ? parsedMap.getPositionBlueFlag().toVector3() : null;
            map.flagRedPosition = parsedMap.getPositionRedFlag() != null ? parsedMap.getPositionRedFlag().toVector3() : null;
            if (map.flagBluePosition != null) {
                Vector3 var10000 = map.flagBluePosition;
                var10000.z += 50.0F;
                var10000 = map.flagRedPosition;
                var10000.z += 50.0F;
            }

            maps.put(map.id, map);
        }
    }

    private static MapConfigItem getMapItem(String id) {
        for (Object configItem : configItems) {
            MapConfigItem item = (MapConfigItem) configItem;
            if (item.id.equals(id)) {
                return item;
            }
        }

        return null;
    }
}
