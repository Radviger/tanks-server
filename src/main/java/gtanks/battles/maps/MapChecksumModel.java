package gtanks.battles.maps;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.anticheats.AntiCheatModel;

@AntiCheatModel(
    name = "MapChecksumModel",
    actionInfo = "Проверяет чек-суму(md5) карты на клиенте"
)
public class MapChecksumModel {
    private BattlefieldModel bfModel;

    public MapChecksumModel(BattlefieldModel bfModel) {
        this.bfModel = bfModel;
    }

    public void check(BattlefieldPlayerController player, String hashSum) {
        this.bfModel.battleInfo.map.md5Hash.equals(hashSum);
    }
}
