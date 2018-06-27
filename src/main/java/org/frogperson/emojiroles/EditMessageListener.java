package org.frogperson.emojiroles;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import jdk.nashorn.internal.runtime.regexp.joni.Regex;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageUpdateEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.entities.Emote;

import javax.annotation.RegEx;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.frogperson.emojiroles.EmojiRoles.jda;

public class EditMessageListener extends ListenerAdapter{

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {

        Message msg = event.getMessage();
        String[] message = event.getMessage().getContentRaw().split("\\s+");
        List<String> messageContents = Arrays.asList(message);

        if (JsonDatabase.isMessageRoleMessage(event.getMessageId())) {
            msg.clearReactions().complete();
            for (String word : messageContents) {
                if (EmojiManager.isEmoji(word) && JsonDatabase.getLinkedRoleFromEmoji(word) != null)
                    msg.addReaction(word).complete();
                else if (JsonDatabase.getLinkedRoleFromEmoji(word.replaceAll("[^0-9.]", "")) != null)
                    msg.addReaction(jda.getEmoteById(word.replaceAll("[^0-9.]", ""))).complete();
            }
        }
    }
}
