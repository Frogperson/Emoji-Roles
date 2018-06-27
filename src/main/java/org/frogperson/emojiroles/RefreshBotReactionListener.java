package org.frogperson.emojiroles;

import com.vdurmont.emoji.EmojiManager;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.*;

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

                        String[] messageRaw = message.getContentRaw().split("\\s+");
                        List<String> messageContents = Arrays.asList(messageRaw);
                        for (String word : messageContents) {
                            if (EmojiManager.isEmoji(word) && JsonDatabase.getLinkedRoleFromEmoji(word) != null) {
                                event.getChannel().addReactionById(event.getMessageId(), word).complete();
                                event.getChannel().removeReactionById(event.getMessageId(), word).queue();
                            }
                            else if (JsonDatabase.getLinkedRoleFromEmoji(word.replaceAll("[^0-9.]", "")) != null) {
                                event.getChannel().addReactionById(event.getMessageId(), jda.getEmoteById(word.replaceAll("[^0-9.]", ""))).complete();
                                event.getChannel().removeReactionById(event.getMessageId(), jda.getEmoteById(word.replaceAll("[^0-9.]", ""))).queue();
                            }
                        }
                    });
                }
            }, delay * 1000);
        }

    }

}




