package org.frogperson.emojiroles;

import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static org.frogperson.emojiroles.EmojiRoles.jda;
import static org.frogperson.emojiroles.EmojiRoles.jda1;

public class RefreshBotReactionListener extends ListenerAdapter {

    private Timer timer = new Timer();

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {

        boolean refreshEnabled = Settings.getRefreshReactions();
        boolean isRoleMessage = JsonDatabase.isMessageRoleMessage(event.getMessageId());
        boolean reactionIsFromThisBot = event.getMember().getUser().getId().equals(jda.getSelfUser().getId());
        boolean reactionIsFromReactionRefreshBot = event.getMember().getUser().getId().equals(jda1.getSelfUser().getId());
        int delay = Settings.getRefreshTimer();

        if (refreshEnabled && isRoleMessage && !reactionIsFromThisBot && !reactionIsFromReactionRefreshBot) {
            timer.cancel();
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    event.getChannel().getMessageById(event.getMessageId()).queue((Message message) -> {
                        for (Emote emote : message.getEmotes()) {
                            if (JsonDatabase.getLinkedRoleFromEmoji(emote.getId()) != null) {
                                event.getChannel().addReactionById(event.getMessageId(), emote).complete();
                                event.getChannel().removeReactionById(event.getMessageId(), emote).queue();
                            }
                        }
                    });
                }
            }, delay * 1000);
        }

    }

}




