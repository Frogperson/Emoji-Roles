package org.frogperson.emojiroles;

import com.google.gson.*;

import java.io.*;

import static org.frogperson.emojiroles.EmojiRoles.jda;

public class JsonDatabase {

    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static BufferedReader bufferedReader;

    private static void loadBR() {
        try {
            bufferedReader = new BufferedReader(new FileReader("LinkedEmoji.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void closeBR() {
        try {
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static {
        try {
            bufferedReader = new BufferedReader(new FileReader("LinkedEmoji.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static JsonObject json = gson.fromJson(bufferedReader, JsonObject.class);

    public static boolean addRoleMessage(String messageId) {
        loadBR();
        JsonElement msgId = gson.fromJson(messageId, JsonElement.class);
        JsonArray msgIds = new JsonArray();
        if (!json.has("messageIds"))
            json.add("messageIds", msgIds);
        if (!json.getAsJsonArray("messageIds").contains(msgId)) {
            json.getAsJsonArray("messageIds").add(msgId);
            try (Writer writer = new FileWriter("LinkedEmoji.json")) {
                gson.toJson(json, writer);
            } catch (java.io.IOException e) {
                e.printStackTrace();
                closeBR();
                return false;
            }
            System.out.println("Added " + messageId + " as a Role Message");
            closeBR();
            return true;
        }
        System.out.println("AddRoleMessage Error: Message was already a Role Message");
        closeBR();
        return false;
    }

    public static boolean removeRoleMessage(String messageId) {
        loadBR();
        JsonElement msgId = gson.fromJson(messageId, JsonElement.class);
        if (!json.has("messageIds")) {
            System.out.println("RemoveRoleMessage Error: No Rol eMessages were in LinkedEmoji.json");
            return false;
        }
        if (!json.getAsJsonArray("messageIds").contains(msgId)) {
            System.out.println("RemoveRoleMessage Error: Message was not a Role Message");
            return false;
        }
        json.getAsJsonArray("messageIds").remove(msgId);
        try (Writer writer = new FileWriter("LinkedEmoji.json")) {
            gson.toJson(json, writer);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            closeBR();
            return false;
        }
        System.out.println("Removed " + messageId + " from being a Role Message");
        closeBR();
        return true;
    }

    public static boolean addEmojiRole(String emojiId, String roleId) {
        loadBR();
        JsonElement emojiRoles = new JsonObject();
        if (!json.has("emojiRoles")) {
            json.add("emojiRoles", emojiRoles);
        }
        json.getAsJsonObject("emojiRoles").addProperty(emojiId, roleId);

        try (Writer writer = new FileWriter("LinkedEmoji.json")) {
            gson.toJson(json, writer);
            //System.out.println("writer");
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return false;
        }
        System.out.println("Linked emoji: " + jda.getEmoteById(emojiId).getName() + " with role: " + jda.getRoleById(roleId).getName() );
        closeBR();
        return true;
    }

    public static boolean removeEmojiRole(String emojiId) {
        loadBR();
        if (!json.has("emojiRoles")) {
            System.out.println("RemoveEmojiRole Error: Emoji was not previously linked");
            closeBR();
            return false;
        }
        json.getAsJsonObject("emojiRoles").remove(emojiId);
        try (Writer writer = new FileWriter("LinkedEmoji.json")) {
            gson.toJson(json, writer);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return false;
        }
        System.out.println("Unlinked emoji:" + jda.getEmoteById(emojiId).getName());
        closeBR();
        return true;
    }


    public static boolean isMessageRoleMessage(String messageId) {
        loadBR();
        JsonElement msgId = gson.fromJson(messageId, JsonElement.class);
        boolean isMessageRoleMessage = json.has("messageIds") && json.getAsJsonArray("messageIds").contains(msgId);
        closeBR();
        return isMessageRoleMessage;
    }

    public static String getLinkedRoleFromEmoji(String emojiId) {
        loadBR();
        if (json.has("emojiRoles")) {
            try {
                String role = json.getAsJsonObject("emojiRoles").get(emojiId).getAsString();
                closeBR();
                return role;
            } catch (NullPointerException n) {
                closeBR();
                return null;
            }
        }
        closeBR();
        return null;
    }
}

