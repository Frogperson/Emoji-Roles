# Emoji Roles
A discord bot to allow members to toggle certain roles by clicking on reactions


## Commands
  
 ##### Link Emoji
`!emojilink :Emoji: <Role Name>  `
Used to link an Emoji with a Role



  ##### Unlink Emoji
`!emojiunlink :Emoji:`
Unlinks the Emoji with whatever role it was linked to

  ##### Add Role Message
`^rolemessage  `
Marks the previous message as a Role Message

or

 `!addrolemessage <messageId>`
 Marks the message with message ID \<messageId> as a Role Message

  ##### Remove Role Message
`!removerolemessage <messageId>  `
Removes the message from being a Role Message

  ##### Refresh Settings
`!emojireload  `
Reloads the settings.conf


  ## Usage

1. Link your desired Emojis with your roles using `!emojilink :Emoji:  <Role Name>`

2. Create a message with your linked Emojis in it. (It's usually nice to have a short description, and it's required for the bot to add the reactions later)

3. Add that message as a Role Message either via `^rolemessage` or `!addrolemessage <messageId>`
  
4. The bot will then react on the message with every Linked Emoji in that message  

5.  Test it out by clicking on one of the reactions.
  
  
## Optional
  
In the settings.conf, add emoji IDs for roleAddedEmoji and roleRemovedEmoji  
These are reacted on the message after another reaction is clicked to let the user know it worked  
(I use a green + and a red - on my server)  
  
In the settings.conf, configure the refreshReactions if you want the bot to keep multiple reactions in the same order as they are in the message