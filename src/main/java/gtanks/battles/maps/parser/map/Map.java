package gtanks.battles.maps.parser.map;

import gtanks.battles.maps.parser.Vector3d;
import gtanks.battles.maps.parser.map.bonus.BonusRegion;
import gtanks.battles.maps.parser.map.spawn.SpawnPosition;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(
    name = "map"
)
public class Map {
    private SpawnPoints spawnPoints;
    private BonusRegions bonusRegions;
    private FlagsPositions flagPositions;

    public SpawnPoints getSpawnPoints() {
        return this.spawnPoints;
    }

    @XmlElement(
        name = "spawn-points"
    )
    public void setSpawnPoints(SpawnPoints spawnPoints) {
        this.spawnPoints = spawnPoints;
    }

    public BonusRegions getBonusRegions() {
        return this.bonusRegions;
    }

    @XmlElement(
        name = "bonus-regions"
    )
    public void setBonusRegions(BonusRegions bonusRegions) {
        this.bonusRegions = bonusRegions;
    }

    public FlagsPositions getFlagPositions() {
        return this.flagPositions;
    }

    @XmlElement(
        name = "ctf-flags"
    )
    public void setFlagPositions(FlagsPositions flagPositions) {
        this.flagPositions = flagPositions;
    }

    public Vector3d getPositionBlueFlag() {
        return this.getFlagPositions() != null ? this.getFlagPositions().getBlueFlag() : null;
    }

    public Vector3d getPositionRedFlag() {
        return this.getFlagPositions() != null ? this.getFlagPositions().getRedFlag() : null;
    }

    public List<SpawnPosition> getSpawnPositions() {
        return this.spawnPoints.getSpawnPositions();
    }

    public List<BonusRegion> getBonusesRegion() {
        return this.bonusRegions.getBonusRegions();
    }
}
