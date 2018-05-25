package org.frogperson.emojiroles;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class Settings {

    private static String token;
    private static String ownerId;
    private static String adminRole;
    private static String commandPrefix;
    private static String roleAddedEmoji;
    private static String roleRemovedEmoji;
    private static Boolean refreshReactions;
    private static Integer refreshTimer;
    private static String refreshBotToken;

    public static void loadSettings() {


        URL inputUrl = Settings.class.getResource("/settings.conf");
        File settingsFile = new File("settings.conf");
        if (!settingsFile.exists()) {
            try {
                System.out.println(inputUrl + settingsFile.toString());
                FileUtils.copyURLToFile(inputUrl, settingsFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Config config = ConfigFactory.parseFile(settingsFile);

        System.out.println("Loading Settings");

        token = config.getString("token");
        ownerId = config.getString("ownerId");
        adminRole = config.getString("adminRole");
        commandPrefix = config.getString("commandPrefix");
        roleAddedEmoji = config.getString("roleAddedEmoji");
        roleRemovedEmoji = config.getString("roleRemovedEmoji");
        refreshReactions = config.getBoolean("refreshReactions");
        refreshTimer = config.getInt("refreshTimer");
        refreshBotToken = config.getString("refreshBotToken");

    }


    public static String getToken() {
        return token;
    }

    public static String getOwnerId() { //not actually used right now
        return ownerId;
    }

    public static String getAdminRole() {
        return adminRole;
    }

    public static String getCommandPrefix() {
        return commandPrefix;
    }

    public static String getRoleAddedEmoji() {
        return roleAddedEmoji;
    }

    public static String getRoleRemovedEmoji() {
        return roleRemovedEmoji;
    }

    public static Boolean getRefreshReactions() { return refreshReactions; }

    public static Integer getRefreshTimer() { return refreshTimer; }

    public static String getRefreshBotToken() { return refreshBotToken; }

}
