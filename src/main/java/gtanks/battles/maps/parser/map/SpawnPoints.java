package gtanks.battles.maps.parser.map;

import gtanks.battles.maps.parser.map.spawn.SpawnPosition;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(
    name = "spawn-points"
)
class SpawnPoints {
    private List<SpawnPosition> spawnPositions;

    public List<SpawnPosition> getSpawnPositions() {
        return this.spawnPositions;
    }

    @XmlElement(
        name = "spawn-point"
    )
    public void setSpawnPositions(List<SpawnPosition> spawnPositions) {
        this.spawnPositions = spawnPositions;
    }
}
