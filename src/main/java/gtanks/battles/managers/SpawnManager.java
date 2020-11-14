package gtanks.battles.managers;

import gtanks.battles.maps.Map;
import gtanks.battles.tanks.PlayerTeamType;
import gtanks.battles.tanks.math.Vector3;

import java.util.Random;

public class SpawnManager {
    private static Random rand = new Random();

    public static Vector3 getSpawnState(Map map, PlayerTeamType forTeam) {
        Vector3 pos;

        try {
            switch (forTeam) {
                case BLUE:
                    pos = map.spawnPositonsBlue.get(rand.nextInt(map.spawnPositonsBlue.size()));
                    break;
                case RED:
                    pos = map.spawnPositonsRed.get(rand.nextInt(map.spawnPositonsRed.size()));
                    break;
                default:
                    pos = map.spawnPositonsDM.get(rand.nextInt(map.spawnPositonsDM.size()));
                    break;
            }

            if (pos == null) {
                pos = map.spawnPositonsDM.get(rand.nextInt(map.spawnPositonsDM.size()));
            }
        } catch (Exception e) {
            pos = map.spawnPositonsDM.get(rand.nextInt(map.spawnPositonsDM.size()));
        }

        return pos;
    }
}
