package gtanks.battles.maps;

import gtanks.battles.bonuses.BonusRegion;
import gtanks.battles.maps.themes.MapTheme;
import gtanks.battles.tanks.math.Vector3;

import java.util.ArrayList;
import java.util.List;

public class Map {
    public String name;
    public String id;
    public String skyboxId;
    public String themeId;
    public MapTheme mapTheme;
    public int minRank;
    public int maxRank;
    public int maxPlayers;
    public boolean tdm = false;
    public boolean ctf = false;
    public List<Vector3> spawnPositonsDM = new ArrayList<>();
    public List<Vector3> spawnPositonsBlue = new ArrayList<>();
    public List<Vector3> spawnPositonsRed = new ArrayList<>();
    public List<BonusRegion> goldsRegions = new ArrayList<>();
    public List<BonusRegion> crystallsRegions = new ArrayList<>();
    public List<BonusRegion> healthsRegions = new ArrayList<>();
    public List<BonusRegion> armorsRegions = new ArrayList<>();
    public List<BonusRegion> damagesRegions = new ArrayList<>();
    public List<BonusRegion> nitrosRegions = new ArrayList<>();
    public int totalCountDrops;
    public Vector3 flagRedPosition;
    public Vector3 flagBluePosition;
    public String md5Hash;

    public Map() {
    }

    public Map(String name, String id, String skyboxId, List<Vector3> spawnPositionsDM, List<Vector3> spawnPositionsBlue, List<Vector3> spawnPositionsRed, List<BonusRegion> goldsRegions, List<BonusRegion> crystallsRegions, List<BonusRegion> dropRegions, int min, int max, int maxPlayers, boolean tdm, boolean ctf) {
        this.name = name;
        this.id = id;
        this.skyboxId = skyboxId;
        this.spawnPositonsDM = spawnPositionsDM;
        this.spawnPositonsBlue = spawnPositionsBlue;
        this.spawnPositonsRed = spawnPositionsRed;
        this.goldsRegions = goldsRegions;
        this.crystallsRegions = crystallsRegions;
        this.minRank = min;
        this.maxRank = max;
        this.tdm = tdm;
        this.ctf = ctf;
        this.maxPlayers = maxPlayers;
    }
}
