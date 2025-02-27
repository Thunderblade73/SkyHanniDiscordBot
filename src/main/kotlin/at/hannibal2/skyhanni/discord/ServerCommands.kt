package at.hannibal2.skyhanni.discord

import at.hannibal2.skyhanni.discord.Utils.logAction
import at.hannibal2.skyhanni.discord.Utils.messageDeleteAndThen
import at.hannibal2.skyhanni.discord.Utils.reply
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

@Suppress("UNUSED_PARAMETER")
class ServerCommands(private val config: BotConfig, commands: Commands) {
    init {
        commands.add(Command("server", userCommand = true) { event, args -> event.serverCommand(args) })
        commands.add(Command("serverlist") { event, args -> event.serverList(args) })
        commands.add(Command("serveradd") { event, args -> event.serverAdd(args) })
        commands.add(Command("serveredit") { event, args -> event.serverEdit(args) })
        commands.add(Command("serveraddalias") { event, args -> event.serverAddAlias(args) })
        commands.add(Command("serveraliasdelete") { event, args -> event.serverAliasDelete(args) })
        commands.add(Command("serverdelete") { event, args -> event.serverDelete(args) })
    }

    private fun MessageReceivedEvent.serverCommand(args: List<String>) {
        if (args.size !in 2..3) {
            reply("Usage: !server <keyword>")
            return
        }
        val keyword = args[1]
        val debug = args.getOrNull(2) == "-d"
        val server = Database.getServer(keyword)
        if (server != null) {
            if (debug) {
                reply(server.printDebug())
            } else {
                reply(server.print())
            }
        } else {
            reply("Server with keyword '$keyword' not found.")
        }
    }

    private fun Server.print(): String = with(this) {
        buildString {
            append("**$displayName**\n")
            if (description.isNotEmpty()) {
                append(description)
                append("\n")
            }
            append("\n")
            append(inviteLink)
        }
    }

    private fun Server.printDebug(): String = with(this) {
        buildString {
            append("keyword: '$keyword'\n")
            append("displayName: '$displayName'\n")
            append("description: '$description'\n")
            append("inviteLink: '<$inviteLink>'\n")
            val aliases = Database.getServerAliases(keyword)
            append("aliases: $aliases\n")
            append("edit command:\n")
            append("`!serveredit $keyword ${displayName.replace(" ", "_")} $inviteLink $description`")
        }
    }

    private fun MessageReceivedEvent.serverAdd(args: List<String>) {
        if (args.size < 4) {
            reply("Usage: !serveradd <keyword> <displayName> <invite link> <description>")
            return
        }
        val keyword = args[1]
        if (Database.getServer(keyword) != null) {
            reply("❌ Server already exists. Use `!serveredit` instead.")
            return
        }
        val server = createServer(keyword, args)
        if (Database.addServer(server)) {
            message.messageDeleteAndThen {
                reply("✅ Server '$keyword' added!")
                reply(server.print())
                logAction("added server '$keyword'")
            }
        } else {
            reply("❌ Failed to add server.")
        }
    }

    private fun MessageReceivedEvent.serverEdit(args: List<String>) {
        if (args.size < 4) {
            reply("Usage: !serveredit <keyword> <displayName> <invite link> <description>")
            return
        }
        val keyword = args[1]
        if (Database.getServer(keyword) == null) {
            reply("❌ Server does not exist. Use `!serveradd` instead.")
            return
        }
        val server = createServer(keyword, args)
        if (Database.addServer(server)) {
            message.messageDeleteAndThen {
                reply("✅ Server '$keyword' edited!")
                reply(server.print())
                logAction("edited server '$keyword'")
            }
        } else {
            reply("❌ Failed to edit server.")
        }
    }

    private fun createServer(keyword: String, args: List<String>): Server {
        val displayName = args[2].replace("_", " ")
        val inviteLink = args[3]
        val description = args.drop(4).joinToString(" ")
        return Server(keyword = keyword, displayName = displayName, inviteLink = inviteLink, description = description)
    }

    private fun MessageReceivedEvent.serverAddAlias(args: List<String>) {
        if (args.size < 3) {
            reply("Usage: !serveraddalias <keyword> <alias>")
            return
        }
        val keyword = args[1]
        val alias = args[2]
        if (Database.getServer(alias) != null) {
            reply("❌ Alias already exists.")
            return
        }
        if (Database.getServer(keyword) == null) {
            reply("❌ Server with keyword '$keyword' does not exist.")
            return
        }
        if (Database.addServerAlias(keyword, alias)) {
            message.messageDeleteAndThen {
                reply("✅ Alias '$alias' added for server '$keyword'")
                logAction("added alias '$alias' for server '$keyword'")
            }
        } else {
            reply("❌ Failed to add alias.")
        }
    }

    private fun MessageReceivedEvent.serverAliasDelete(args: List<String>) {
        if (args.size < 3) {
            reply("Usage: !serveraliasdelete <keyword> <alias>")
            return
        }
        val keyword = args[1]
        val alias = args[2]
        if (Database.deleteServerAlias(keyword, alias)) {
            message.messageDeleteAndThen {
                reply("✅ Alias '$alias' deleted from server '$keyword'")
                logAction("deleted alias '$alias' for server '$keyword'")
            }
        } else {
            reply("❌ Failed to delete alias '$alias' for server '$keyword'.")
        }
    }

    private fun MessageReceivedEvent.serverDelete(args: List<String>) {
        if (args.size != 2) {
            reply("Usage: !serverdelete <keyword>")
            return
        }
        val keyword = args[1]
        if (Database.deleteServer(keyword)) {
            message.messageDeleteAndThen {
                reply("✅ Server '$keyword' deleted!")
                logAction("deleted server '$keyword'")
            }
        } else {
            reply("❌ Server with keyword '$keyword' not found or deletion failed.")
        }
    }

    private fun MessageReceivedEvent.serverList(args: List<String>) {
        val servers = Database.listServers()
        if (servers.isEmpty()) {
            reply("No servers found.")
            return
        }
        val list = servers.joinToString("\n") { server ->
            val aliases = Database.getServerAliases(server.keyword)
            if (aliases.isNotEmpty())
                "${server.keyword} [${aliases.joinToString(", ")}]"
            else
                server.keyword
        }
        reply("Server list:\n$list")
    }
}
