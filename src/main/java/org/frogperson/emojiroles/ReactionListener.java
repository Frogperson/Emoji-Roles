package org.frogperson.emojiroles;

import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.GuildController;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.frogperson.emojiroles.EmojiRoles.jda;
import static org.frogperson.emojiroles.EmojiRoles.jda1;
import static org.frogperson.emojiroles.EmojiRoles.refreshBotActive;

public class ReactionListener extends ListenerAdapter {

    Timer timer = new Timer();

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        GuildController guildController = new GuildController(jda.getGuildById(event.getGuild().getId()));
        int delay = Settings.getRefreshTimer();
        boolean isRefreshBotReaction = false;
        boolean force = true;

        if (refreshBotActive)
            isRefreshBotReaction = event.getMember().getUser().getId().equals(jda1.getSelfUser().getId());

        //Message is role message, reaction is not from this bot or refresh bot
        if (JsonDatabase.isMessageRoleMessage(event.getMessageId()) && !event.getMember().getUser().getId().equals(jda.getSelfUser().getId()) && !isRefreshBotReaction) {
            event.getReaction().removeReaction(event.getUser()).complete();

            event.getChannel().getMessageById(event.getMessageId()).queue((Message message) -> {
                List<Emote> emojiList = new ArrayList<>();
                for (Emote emoji : message.getEmotes()) {
                    if (JsonDatabase.getLinkedRoleFromEmoji(emoji.getId()) != null)
                        emojiList.add(emoji);
                }

                //Only do things if the reacted emoji is in the Role Message
                if (emojiList.contains(event.getReaction().getReactionEmote().getEmote())) {
                    System.out.println("test");
                    if (!refreshBotActive) { //Don't do this if we have another bot reacting
                        timer.cancel();
                        timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                event.getChannel().getMessageById(event.getMessageId()).queue((Message message) -> {
                                    message.clearReactions().complete();
                                    for (Emote emoji : emojiList) {
                                        event.getChannel().addReactionById(event.getMessageId(), emoji).queue();
                                    }
                                });
                            }
                        }, delay * 1000);
                    }

                    try {
                        String roleId = JsonDatabase.getLinkedRoleFromEmoji(event.getReactionEmote().getId());
                        String roleName = jda.getRoleById(roleId).getName();
                        Role role = jda.getRoleById(roleId);

                        //Add the role to the member
                        if (!Commands.doesMemberHaveRole(event.getMember(), roleName)) {
                            guildController.addSingleRoleToMember(event.getMember(), role).queue();
                            System.out.println("Gave " + role.getName() + " to " + event.getMember().getUser().getName());
                            if (!Settings.getRoleAddedEmoji().equals("")) {
                                event.getChannel().addReactionById(event.getMessageId(), jda.getEmoteById(Settings.getRoleAddedEmoji())).complete();
                                event.getChannel().removeReactionById(event.getMessageId(), jda.getEmoteById(Settings.getRoleAddedEmoji())).queueAfter(3, TimeUnit.SECONDS);
                            }
                        }
                        //Remove the role from the member
                        else {
                            guildController.removeSingleRoleFromMember(event.getMember(), role).queue();
                            System.out.println("Removed " + role.getName() + " from " + event.getMember().getUser().getName());
                            if (!Settings.getRoleRemovedEmoji().equals("")) {
                                event.getChannel().addReactionById(event.getMessageId(), jda.getEmoteById(Settings.getRoleRemovedEmoji())).complete();
                                event.getChannel().removeReactionById(event.getMessageId(), jda.getEmoteById(Settings.getRoleRemovedEmoji())).queueAfter(3, TimeUnit.SECONDS);
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        System.out.println("Emoji not linked");
                    }
                }
            });
        }
    }
}