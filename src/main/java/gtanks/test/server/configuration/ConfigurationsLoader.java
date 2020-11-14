package gtanks.test.server.configuration;

import com.google.gson.*;
import gtanks.test.osgi.OSGi;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class ConfigurationsLoader {
    private static final String DEFAULT_PATH = "configurations/runner/";
    private static final String FORMAT_CONFIG = ".json";
    private static final String PARSER_CLASS_NAME = "class_name";
    private static final String PARSER_PARAMS_ARRAY = "params";
    private static final String PARSER_VALUE = "value";
    private static final String PARSER_VAR_NAME = "var";
    private static final Gson GSON = new Gson();

    public static void load(String pathToAllConfigs) {
        if (pathToAllConfigs == null || pathToAllConfigs.isEmpty()) {
            pathToAllConfigs = DEFAULT_PATH;
            System.out.println("WARNING! Path to all configs is null! Use default: configurations/runner/");
        }

        File path = new File(pathToAllConfigs);

        for (File file : path.listFiles()) {
            if (file.getPath().endsWith(FORMAT_CONFIG)) {
                parseAndLoadClass(file);
            }
        }
    }

    private static void parseAndLoadClass(File config) {
        try {
            JsonObject json = GSON.fromJson(new FileReader(config), JsonObject.class);
            String className = json.get(PARSER_CLASS_NAME).getAsString();
            JsonArray params = json.getAsJsonArray(PARSER_PARAMS_ARRAY);
            Class<?> clazz = Class.forName(className);
            Object entity = clazz.newInstance();

            for (JsonElement param : params) {
                JsonObject p = param.getAsJsonObject();
                Field field = clazz.getDeclaredField(p.get(PARSER_VAR_NAME).getAsString());
                field.setAccessible(true);
                String cName = p.get(PARSER_CLASS_NAME).getAsString();
                Class<?> c = Class.forName(cName);
                if (c.getSuperclass() == Number.class || c == String.class) {
                    field.set(entity, c.getConstructor(String.class).newInstance(p.get(PARSER_VALUE).getAsString()));
                }
            }

            OSGi.registerModel(entity);
        } catch (JsonParseException | IOException | ClassNotFoundException | IllegalAccessException | IllegalArgumentException | SecurityException | InstantiationException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
