package org.frogperson.emojiroles;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageUpdateEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.entities.Emote;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class EditMessageListener extends ListenerAdapter{

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {

        List<Emote> emojis = event.getMessage().getEmotes();
        Message msg = event.getMessage();

        if (JsonDatabase.isMessageRoleMessage(event.getMessageId())) {
            msg.clearReactions().complete();
            for(Emote emoji : emojis) {
                if(JsonDatabase.getLinkedRoleFromEmoji(emoji.getId()) != null)
                    msg.addReaction(emoji).queue();
            }
        }
    }
}
