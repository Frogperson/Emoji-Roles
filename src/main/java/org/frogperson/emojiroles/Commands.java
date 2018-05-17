package org.frogperson.emojiroles;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.entities.Emote;

import java.util.List;
import java.util.concurrent.TimeUnit;
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

            if (msg.toLowerCase().startsWith("addrolemessage ", prefix.length()) && doesMemberHaveRole(event.getMember(), Settings.getAdminRole())) { //add role message command. Syntax: !addrolemessage <messageId>
                if (JsonDatabase.addRoleMessage(msg.replace(prefix + "addrolemessage ", "")))
                    event.getTextChannel().sendMessage("**Message is now a Role Message.** Please add a random reaction to your new Role Message").queue();
            }

            if (msg.toLowerCase().startsWith("removerolemessage ", prefix.length()) && doesMemberHaveRole(event.getMember(), Settings.getAdminRole())) { //remove role message command. Syntax: !removerolemessage <messageId>
                if (JsonDatabase.removeRoleMessage(msg.replace(prefix + "removerolemessage ", "")))
                    event.getTextChannel().sendMessage("**Message is no longer a Role Message**").queue();
            }

            if (msg.toLowerCase().startsWith("emojilink ", prefix.length()) && doesMemberHaveRole(event.getMember(), Settings.getAdminRole())) { //link command. Syntax: !emojilink RoleName :Emoji:      Note: Roles with spaces not supported
                if (emojis.size() == 1) {
                    emoji = emojis.get(0).getId();
                    role = getRoleIdFromName(getRoleFromMessage(msg));
                    if (role != null) {
                        if (JsonDatabase.addEmojiRole(emoji, role))
                            event.getTextChannel().sendMessage("**Linked sucessfully**").queue();
                    }
                }
            }

            if (msg.toLowerCase().startsWith("emojiunlink ", prefix.length()) && doesMemberHaveRole(event.getMember(), Settings.getAdminRole())) { //unlink command. Syntax: !emojiunlink :Emoji:
                if (emojis.size() == 1) {
                    emoji = emojis.get(0).getId();
                    if (JsonDatabase.getLinkedRoleFromEmoji(emojis.get(0).getId()) != null)
                        if (JsonDatabase.removeEmojiRole(emoji))
                            event.getTextChannel().sendMessage("**Unlinked sucessfully**").queue();
                }
            }

            if (msg.toLowerCase().startsWith("emojireload", prefix.length()) && doesMemberHaveRole(event.getMember(), Settings.getAdminRole())) { //reload settings
                event.getTextChannel().sendMessage("**Settings reloaded**").queue();
                Settings.loadSettings();
            }
        }
    }

    private String getRoleFromMessage(String msg) { //magic regex stuff to get grab the role name from the message. Thanks to Benji

        String regex = "^!emojilink (.+) [^ ]+$";
        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(msg);
        return m.group(1);
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

