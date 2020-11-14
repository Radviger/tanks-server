package gtanks.battles.tanks.statistic;

public class PlayerStatistic implements Comparable<PlayerStatistic> {
    private long kills;
    private int deaths;
    private int prize;
    private long score;

    public PlayerStatistic(int kills, int deaths, int score) {
        this.kills = (long) kills;
        this.deaths = deaths;
        this.score = (long) score;
    }

    public void addKills(boolean killsEqualsScore) {
        ++this.kills;
        if (killsEqualsScore) {
            this.score = this.kills;
        }

    }

    public void addDeaths() {
        ++this.deaths;
    }

    public void addScore(int value) {
        this.score += (long) value;
    }

    public long getScore() {
        return this.score;
    }

    public void setScore(long value) {
        this.score = value;
    }

    public long getKills() {
        return this.kills;
    }

    public void setKills(long kills) {
        this.kills = kills;
    }

    public int getDeaths() {
        return this.deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public int getPrize() {
        return this.prize;
    }

    public void setPrize(int prize) {
        this.prize = prize;
    }

    public void clear() {
        this.kills = 0L;
        this.deaths = 0;
        this.prize = 0;
        this.score = 0L;
    }

    public float getKD() {
        return (float) (this.kills / (long) this.deaths);
    }

    @Override
    public String toString() {
        return "score: " + this.score + " kills: " + this.kills + " deaths: " + this.deaths + " prize: " + this.prize;
    }

    @Override
    public int compareTo(PlayerStatistic other) {
        return (int) (other.getScore() - this.score);
    }
}
