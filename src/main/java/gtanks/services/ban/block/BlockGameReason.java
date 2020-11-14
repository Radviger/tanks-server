package gtanks.services.ban.block;

public enum BlockGameReason {
    DEFAULT("Аккаунт был заблокирован за нарушение правил игры\nЕсли вы уверены, что мы ошиблись - пишите на help@gtanksonline.com"),
    UNACCEPTABLE_NICK("Аккаунт был заблокирован за использование недопустимого игрового имени\nЕсли вы уверены, что мы ошиблись - пишите на help@gtanksonline.com "),
    USING_CHEATS("Аккаунт был заблокирован за использование читов\nЕсли вы уверены, что мы ошиблись - пишите на help@gtanksonline.com "),
    TRANSFER_OF_ACCOUNT("Аккаунт был заблокирован за попытку передачи аккаунта стороннему игроку\nЗаявки на восстановление аккаунта приниматься не будут. "),
    PUMPING("Прокачка (набор очков за счет бездействия другого игрока)\nЕсли вы уверены, что мы ошиблись - пишите на help@gtanksonline.com "),
    USING_BAGS("Злонамерное использование програмной ошибки \nЕсли вы уверены, что мы ошиблись - пишите на help@gtanksonline.com "),
    SABOTAGE("Саботаж (создание помех чужой команде через бездействующих «фальшивых» игроков)\nЕсли вы уверены, что мы ошиблись - пишите на help@gtanksonline.com ");

    private final String reason;

    BlockGameReason(String reason) {
        this.reason = reason;
    }

    public static BlockGameReason getReasonById(int i) {
        return values()[i % values().length];
    }

    public String getReason() {
        return this.reason;
    }
}
