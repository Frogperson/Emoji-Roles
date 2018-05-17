package org.frogperson.emojiroles;

import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.GuildController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.frogperson.emojiroles.EmojiRoles.jda;

public class EmojiListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        String roleName;
        Role role;
        String roleId;
        GuildController guildController = new GuildController(jda.getGuildById(event.getGuild().getId()));


        if (JsonDatabase.isMessageRoleMessage(event.getMessageId()) && !event.getMember().getUser().getId().equals(jda.getSelfUser().getId())) {
            event.getReaction().removeReaction(event.getUser()).queue();

            try {
                roleId = JsonDatabase.getLinkedRoleFromEmoji(event.getReactionEmote().getId());
                roleName = jda.getRoleById(roleId).getName();
                role = jda.getRoleById(roleId);

                //Add the role to the member
                if (!Commands.doesMemberHaveRole(event.getMember(), roleName)) {
                    guildController.addSingleRoleToMember(event.getMember(), role).queue();
                    System.out.println("Gave " + role.getName() + " to " + event.getMember().getUser().getName());
                    if (!Settings.getRoleAddedEmoji().equals("")) {
                        event.getChannel().addReactionById(event.getMessageId(), jda.getEmoteById(Settings.getRoleAddedEmoji())).queue();
                        event.getChannel().removeReactionById(event.getMessageId(), jda.getEmoteById(Settings.getRoleAddedEmoji())).queueAfter(3, TimeUnit.SECONDS);
                    }

                //Remove the role from the member
                } else {
                    guildController.removeSingleRoleFromMember(event.getMember(), role).queue();
                    System.out.println("Removed " + role.getName() + " from " + event.getMember().getUser().getName());
                    if (!Settings.getRoleRemovedEmoji().equals("")) {
                        event.getChannel().addReactionById(event.getMessageId(), jda.getEmoteById(Settings.getRoleRemovedEmoji())).queue();
                        event.getChannel().removeReactionById(event.getMessageId(), jda.getEmoteById(Settings.getRoleRemovedEmoji())).queueAfter(3, TimeUnit.SECONDS);
                    }
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Emoji not linked");
            }
        }

        //for adding the reactions to a role message when the message was added via the `!addrolemessage <messageId>` command. This is a giant mess and can probably be cleaned up but it works
        event.getChannel().getMessageById(event.getMessageId()).queue(message ->
        {
            List<Emote> messageEmojis = message.getEmotes();
            List<Emote> linkedMessageEmojis = new ArrayList<>();
            List<Emote> reactionEmojis = new ArrayList<>();
            for (Emote messageEmoji : messageEmojis) {
                if (JsonDatabase.getLinkedRoleFromEmoji(messageEmoji.getId()) != null)
                    linkedMessageEmojis.add(messageEmoji);
            }
            event.getChannel().getMessageById(event.getMessageId()).queue(message1 ->
            {
                for (MessageReaction reaction : message1.getReactions()) {
                    reactionEmojis.add(reaction.getReactionEmote().getEmote());
                }
                if (!reactionEmojis.containsAll(linkedMessageEmojis)) {
                    event.getChannel().getMessageById(event.getMessageId()).queue(message2 ->
                    {
                        message2.clearReactions().complete();
                        for (Emote roleMessageEmoji : linkedMessageEmojis) {
                            event.getChannel().getMessageById(event.getMessageId()).queue(message3 -> message3.addReaction(roleMessageEmoji).queue());
                        }
                    });
                }
            });
        });
    }
}