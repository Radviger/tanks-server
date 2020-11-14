package gtanks.main;

import com.google.gson.JsonParseException;
import gtanks.RankUtils;
import gtanks.battles.maps.MapsLoader;
import gtanks.battles.tanks.loaders.HullsFactory;
import gtanks.battles.tanks.loaders.WeaponsFactory;
import gtanks.groups.UserGroupsLoader;
import gtanks.gui.console.ConsoleWindow;
import gtanks.logger.Logger;
import gtanks.main.netty.NettyService;
import gtanks.rmi.payments.kit.loader.KitsLoader;
import gtanks.rmi.server.RMIServer;
import gtanks.services.hibernate.Database;
import gtanks.system.SystemConsoleHandler;
import gtanks.test.server.configuration.ConfigurationsLoader;
import gtanks.users.garage.GarageItemsLoader;
import org.hibernate.query.NativeQuery;

import java.io.IOException;

public class Main {
    public static ConsoleWindow console;

    public static void main(String[] args) {
        try {
            ConfigurationsLoader.load("config/core/");
            initFactories();
            SystemConsoleHandler sch = SystemConsoleHandler.INSTANCE;
            sch.start();
            UserGroupsLoader.load("config/groups/");
            Logger.log("Connecting to DB...");
            Database.execute(session -> {
                NativeQuery<?> query = session.createSQLQuery("SET NAMES 'utf8' COLLATE 'utf8_general_ci';");
                System.out.println("Setting UTF-8 charset on DB: " + query.executeUpdate());
            });
            NettyService.INSTANCE.bind();
            RMIServer.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void initFactories() throws IOException, JsonParseException {
        GarageItemsLoader.loadFromConfig(
            "config/turrets.json",
            "config/hulls.json",
            "config/color_maps.json",
            "config/inventory.json",
            "config/effects.json"
        );
        WeaponsFactory.init("config/weapons/");
        HullsFactory.init("config/hulls/");
        RankUtils.init();
        MapsLoader.initFactoryMaps();
        KitsLoader.load("config/kits.json");
    }
}
