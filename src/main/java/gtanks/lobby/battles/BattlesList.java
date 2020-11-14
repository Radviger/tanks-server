package gtanks.lobby.battles;

import gtanks.StringUtils;
import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.commands.Type;
import gtanks.json.JsonUtils;
import gtanks.services.LobbyServices;
import gtanks.users.locations.UserLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BattlesList extends BattlesListCommandsConst {
    private static final List<BattleInfo> battles = new ArrayList<>();
    private static final LobbyServices lobbyServices = LobbyServices.INSTANCE;
    private static int countBattles = 0;

    public static boolean tryCreateBattle(BattleInfo btl) {
        btl.battleId = generateId(btl.name, btl.map.id);
        if (getBattleInfoById(btl.battleId) != null) {
            return false;
        } else {
            battles.add(btl);
            ++countBattles;
            lobbyServices.sendCommandToAllUsers(Type.LOBBY, UserLocation.BATTLESELECT, CREATE_BATTLE, JsonUtils.parseBattleInfo(btl));
            btl.model = new BattlefieldModel(btl);
            return true;
        }
    }

    public static void removeBattle(BattleInfo battle) {
        if (battle != null) {
            lobbyServices.sendCommandToAllUsers(Type.LOBBY, UserLocation.BATTLESELECT, StringUtils.concatStrings("remove_battle", ";", battle.battleId));
            if (battle.model != null && battle.model.players != null) {
                for (BattlefieldPlayerController player : battle.model.players.values()) {
                    player.parentLobby.kick();
                }
            }

            battle.model.destroy();
            battles.remove(battle);
        }
    }

    public static List<BattleInfo> getList() {
        return battles;
    }

    private static String generateId(String gameName, String mapId) {
        return (new Random()).nextInt(50000) + "@" + gameName + "@" + "#" + countBattles;
    }

    public static BattleInfo getBattleInfoById(String id) {
        for (BattleInfo battle : battles) {
            if (battle.battleId.equals(id)) {
                return battle;
            }
        }
        return null;
    }
}
