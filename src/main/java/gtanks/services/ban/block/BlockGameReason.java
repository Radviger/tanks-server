package gtanks.services.ban.block;

import java.lang.reflect.Field;

public class BlockGameReason {
    public static final BlockGameReason DEFAULT = new BlockGameReason(0, "Аккаунт был заблокирован за нарушение правил игры\nЕсли вы уверены, что мы ошиблись - пишите на help@gtanksonline.com");
    public static final BlockGameReason UNACCEPTABLE_NICK = new BlockGameReason(1, "Аккаунт был заблокирован за использование недопустимого игрового имени\nЕсли вы уверены, что мы ошиблись - пишите на help@gtanksonline.com ");
    public static final BlockGameReason USING_CHEATS = new BlockGameReason(2, "Аккаунт был заблокирован за использование читов\nЕсли вы уверены, что мы ошиблись - пишите на help@gtanksonline.com ");
    public static final BlockGameReason TRANSFER_OF_ACCOUNT = new BlockGameReason(3, "Аккаунт был заблокирован за попытку передачи аккаунта стороннему игроку\nЗаявки на восстановление аккаунта приниматься не будут. ");
    public static final BlockGameReason PUMPING = new BlockGameReason(4, "Прокачка (набор очков за счет бездействия другого игрока)\nЕсли вы уверены, что мы ошиблись - пишите на help@gtanksonline.com ");
    public static final BlockGameReason USING_BAGS = new BlockGameReason(5, "Злонамерное использование програмной ошибки \nЕсли вы уверены, что мы ошиблись - пишите на help@gtanksonline.com ");
    public static final BlockGameReason SABOTAGE = new BlockGameReason(6, "Саботаж (создание помех чужой команде через бездействующих «фальшивых» игроков)\nЕсли вы уверены, что мы ошиблись - пишите на help@gtanksonline.com ");
    private final String reason;
    private final int reasonId;

    private BlockGameReason(int reasonId, String reason) {
        this.reason = reason;
        this.reasonId = reasonId;
    }

    public static BlockGameReason getReasonById(int i) {
        Class clazz = BlockGameReason.class;
        BlockGameReason reason = DEFAULT;

        try {
            Field[] var6;
            int var5 = (var6 = clazz.getFields()).length;

            for (int var4 = 0; var4 < var5; ++var4) {
                Field field = var6[var4];
                BlockGameReason temp = (BlockGameReason) field.get((Object) null);
                if (temp.reasonId == i) {
                    reason = temp;
                    break;
                }
            }
        } catch (Exception var8) {
            var8.printStackTrace();
        }

        return reason;
    }

    public String getReason() {
        return this.reason;
    }

    public int getReasonId() {
        return this.reasonId;
    }
}
