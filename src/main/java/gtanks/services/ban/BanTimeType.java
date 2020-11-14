package gtanks.services.ban;

public enum BanTimeType {
    FIVE_MINUTES("НА 5 МИНУТ.", 12, 5),
    ONE_HOUR("НА 1 ЧАС.", 10, 1),
    ONE_DAY("НА 1 ДЕНЬ.", 5, 1),
    ONE_WEEK("НА 1 НЕДЕЛЮ.", 4, 1),
    ONE_MONTH("НА 1 МЕСЯЦ.", 2, 1),
    HALF_YEAR("НА ПОЛ ГОДА.", 2, 6),
    FOREVER("НАВСЕГДА.", 1, 2);

    private final String nameType;
    private final int field, amount;

    BanTimeType(String nameType, int field, int amount) {
        this.nameType = nameType;
        this.field = field;
        this.amount = amount;
    }

    public String getNameType() {
        return this.nameType;
    }

    public int getField() {
        return this.field;
    }

    public int getAmount() {
        return this.amount;
    }

    @Override
    public String toString() {
        return "BanTimeType [" + this.nameType + "]";
    }
}
