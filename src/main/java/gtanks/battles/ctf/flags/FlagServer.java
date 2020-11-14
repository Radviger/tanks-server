package gtanks.battles.ctf.flags;

import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.ctf.FlagReturnTimer;
import gtanks.battles.tanks.PlayerTeamType;
import gtanks.battles.tanks.math.Vector3;

public class FlagServer {
    public PlayerTeamType flagTeamType;
    public BattlefieldPlayerController owner;
    public Vector3 position;
    public Vector3 basePosition;
    public FlagState state;
    public FlagReturnTimer returnTimer;
}
