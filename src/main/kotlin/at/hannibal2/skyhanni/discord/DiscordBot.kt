package at.hannibal2.skyhanni.discord

import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent

val botCommandChannelId = "1343359770920222872"

class DiscordBot : ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent) {
        val message = event.message.contentRaw.trim()
        val args = message.split(" ", limit = 3)

        if (event.author.isBot) return
        if (event.channel.id != botCommandChannelId) return

        when {
            args[0] == "!add" && args.size == 3 -> {
                val keyword = args[1]
                val response = args[2]
                if (Database.listKeywords().contains(keyword.lowercase())) {
                    event.channel.sendMessage("❌ Already exists use `!edit` instead.").queue()
                    return
                }
                if (Database.addKeyword(keyword, response)) {
                    event.channel.sendMessage("✅ Keyword '$keyword' added!").queue()
                } else {
                    event.channel.sendMessage("❌ Failed to add keyword.").queue()
                }
                return
            }
            args[0] == "!add" && args.size == 3 -> {
                val keyword = args[1]
                val response = args[2]
                if (!Database.listKeywords().contains(keyword.lowercase())) {
                    event.channel.sendMessage("❌ Keyword does not exist!`!add` instead.").queue()
                    return
                }
                if (Database.addKeyword(keyword, response)) {
                    event.channel.sendMessage("✅ Keyword '$keyword' added!").queue()
                } else {
                    event.channel.sendMessage("❌ Failed to add keyword.").queue()
                }
                return
            }

            args[0] == "!delete" && args.size == 2 -> {
                val keyword = args[1]
                if (Database.deleteKeyword(keyword)) {
                    event.channel.sendMessage("🗑️ Keyword '$keyword' deleted!").queue()
                } else {
                    event.channel.sendMessage("❌ Keyword '$keyword' not found.").queue()
                }
                return
            }

            message == "!list" -> {
                val keywords = Database.listKeywords().joinToString(", ")
                val response = if (keywords.isNotEmpty()) "📌 Keywords: $keywords" else "No keywords set."
                event.channel.sendMessage(response).queue()
                return
            }

            message == "!help" -> {
                val commands = listOf("add", "remove", "list")
                val response = "The bot currently supports those commands: ${commands.joinToString(", ")}"
                event.channel.sendMessage(response).queue()
                return
            }

            message.startsWith("!") -> {
                val keyword = message.substring(1)
                val response = Database.getResponse(keyword)
                if (response != null) {
                    event.channel.sendMessage(response).queue()
                } else {
                    event.channel.sendMessage("Unknown command \uD83E\uDD7A Type `!help` for help.")
                        .queue()
                }
            }
        }
    }
}

fun main() {
    val config = ConfigLoader.load("config.json")
    val token = config.token
    val jda =
        JDABuilder.createDefault(token).addEventListeners(DiscordBot()).enableIntents(GatewayIntent.MESSAGE_CONTENT)
            .build()
    jda.awaitReady()
    jda.getPrivateChannelById(botCommandChannelId)?.sendMessage("I'm Back!")?.queue()
}