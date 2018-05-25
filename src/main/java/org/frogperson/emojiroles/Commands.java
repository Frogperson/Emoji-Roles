package org.frogperson.emojiroles;

import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.frogperson.emojiroles.EmojiRoles.jda;

public class Commands extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        String prefix = Settings.getCommandPrefix();
        String msg = event.getMessage().getContentDisplay();
        List<Emote> emojis = event.getMessage().getEmotes();
        String emoji;
        String role;

        if (msg.toLowerCase().startsWith("^rolemessage") && doesMemberHaveRole(event.getMember(), Settings.getAdminRole())) { //command to mark the previous message as a Role Message
            event.getChannel().getHistoryBefore(event.getMessage(), 1).queue(messageHistory -> {
                Message previousMessage = messageHistory.getRetrievedHistory().get(0);
                JsonDatabase.addRoleMessage(previousMessage.getId());
                List<Emote> previousEmojis = previousMessage.getEmotes();
                previousMessage.clearReactions().complete(); //clear previous reactions just in case
                for (Emote previousEmoji : previousEmojis) {
                    if (JsonDatabase.getLinkedRoleFromEmoji(previousEmoji.getId()) != null)
                        previousMessage.addReaction(previousEmoji).queue();
                }
            });
            event.getMessage().delete().queue();
        }

        if (msg.startsWith(prefix)) {


            //add role message command. Syntax: !addrolemessage <messageId>
            if (msg.toLowerCase().startsWith("addrolemessage ", prefix.length()) && doesMemberHaveRole(event.getMember(), Settings.getAdminRole())) {
                String messageId = msg.replace(prefix + "addrolemessage ", "");
                if (JsonDatabase.addRoleMessage(messageId)) {
                    event.getTextChannel().sendMessage("**Message is now a Role Message.**").queue();
                    List<Emote> linkedMessageEmojis = new ArrayList<>();
                    List<Emote> reactionEmojis = new ArrayList<>();
                    for (TextChannel textChannel : jda.getTextChannels()) {
                        try {
                            textChannel.getMessageById(messageId).queue((Message message) -> {
                                for (Emote emote : message.getEmotes()) {
                                    if (JsonDatabase.getLinkedRoleFromEmoji(emote.getId()) != null)
                                        linkedMessageEmojis.add(emote);
                                }
                                for (MessageReaction reaction : message.getReactions())
                                    reactionEmojis.add(reaction.getReactionEmote().getEmote());
                                if (!reactionEmojis.containsAll(linkedMessageEmojis)) {
                                    message.clearReactions().complete();
                                    System.out.println(linkedMessageEmojis);
                                    for (Emote emote : linkedMessageEmojis) {
                                        textChannel.addReactionById(messageId, emote).queue();
                                    }
                                }
                            });
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        } catch (InsufficientPermissionException e) {
                            System.out.println("Insufficient permissions to read channel");
                        }
                    }
                }
            }

            //remove role message command. Syntax: !removerolemessage <messageId>
            if (msg.toLowerCase().startsWith("removerolemessage ", prefix.length()) && doesMemberHaveRole(event.getMember(), Settings.getAdminRole())) {
                if (JsonDatabase.removeRoleMessage(msg.replace(prefix + "removerolemessage ", "")))
                    event.getTextChannel().sendMessage("**Message is no longer a Role Message**").queue();
            }

            //link command. Syntax: !emojilink :Emoji: Role Name
            if (msg.toLowerCase().startsWith("emojilink ", prefix.length()) && doesMemberHaveRole(event.getMember(), Settings.getAdminRole())) {
                if (emojis.size() == 1) {
                    emoji = emojis.get(0).getId();
                    role = getRoleIdFromName(getRoleFromMessage(msg));
                    if (role != null && jda.getEmoteById(emoji) != null) {
                        if (JsonDatabase.addEmojiRole(emoji, role))
                            event.getTextChannel().sendMessage("**Linked sucessfully**").queue();
                    } else {
                        event.getTextChannel().sendMessage("**Please use an emoji uploaded to this server**").queue();
                    }
                }
            }

            //unlink command. Syntax: !emojiunlink :Emoji:
            if (msg.toLowerCase().startsWith("emojiunlink ", prefix.length()) && doesMemberHaveRole(event.getMember(), Settings.getAdminRole())) {
                if (emojis.size() == 1) {
                    emoji = emojis.get(0).getId();
                    if (JsonDatabase.getLinkedRoleFromEmoji(emojis.get(0).getId()) != null)
                        if (JsonDatabase.removeEmojiRole(emoji))
                            event.getTextChannel().sendMessage("**Unlinked sucessfully**").queue();
                }
            }

            //reload settings
            if (msg.toLowerCase().startsWith("emojireload", prefix.length()) && doesMemberHaveRole(event.getMember(), Settings.getAdminRole())) {
                event.getTextChannel().sendMessage("**Settings reloaded**").queue();
                Settings.loadSettings();
            }
        }
    }

    private String getRoleFromMessage(String msg) { //magic regex stuff to get grab the role name from the message. Thanks to Benji for helping me with this
        String regex = "(?:.*? ){2}(.*)";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(msg);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    public String getRoleIdFromName(String roleName) {
        for (Role role : jda.getRoles())
            if (roleName.equalsIgnoreCase(role.getName())) return role.getId();
        return null;
    }

    public static boolean doesMemberHaveRole(Member member, String roleName) {
        for (Role role : member.getRoles())
            if (roleName.equalsIgnoreCase(role.getName())) return true;
        return false;
    }
}

