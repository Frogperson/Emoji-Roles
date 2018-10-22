package org.frogperson.emojiroles;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import org.apache.commons.io.FileUtils;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class EmojiRoles {

    public static JDA jda;
    public static JDA jda1;

    public static boolean refreshBotActive = false;

    public static void main(String[] args) {

        Settings.loadSettings();

        URL inputUrl = EmojiRoles.class.getResource("/LinkedEmoji.json");
        File linkedChannelsFile = new File("LinkedEmoji.json");

        if (!linkedChannelsFile.exists()) {
            System.out.println(inputUrl.toString() + linkedChannelsFile.toString());
            try {
                FileUtils.copyURLToFile(inputUrl, linkedChannelsFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        JDABuilder builder = new JDABuilder(AccountType.BOT);
        builder.setToken(Settings.getToken());
        builder.setAutoReconnect(true);
        builder.addEventListener(new org.frogperson.emojiroles.Bot());
        builder.addEventListener(new Commands());
        builder.addEventListener(new ReactionListener());
        builder.addEventListener(new EditMessageListener());

        JDABuilder builder1 = new JDABuilder(AccountType.BOT);
        builder1.setToken(Settings.getRefreshBotToken());
        builder1.setAutoReconnect(true);
        builder1.addEventListener(new RefreshBotReactionListener());


        try {
            jda = builder.buildBlocking();
        } catch (LoginException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            jda1 = builder1.buildBlocking();
            refreshBotActive = true;
        } catch (LoginException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
