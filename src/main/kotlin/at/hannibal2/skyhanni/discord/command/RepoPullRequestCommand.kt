package at.hannibal2.skyhanni.discord.command

import at.hannibal2.skyhanni.discord.*
import at.hannibal2.skyhanni.discord.github.GitHubClient
import at.hannibal2.skyhanni.discord.json.discord.PullRequestJson

object RepoPullRequestCommand : PullRequestCommand() {
    override val repo get() = "SkyHanni-REPO"
    override var disableBuildInfo: Boolean = true
    override val github get() = GitHubClient(user, repo, BOT.config.githubToken)

    override val labelTypes: Map<String, Set<String>> get() = mapOf(
        Pair("Misc", setOf("Merge Conflicts", "Part of SkyHanni PR", "Wait on Hypixel"))
    )

    override fun StringBuilder.appendLabelCategories(labels: Set<String>, pr: PullRequestJson) {
        appendLabelCategory("Misc", labels, this)
    }

    override val name: String = "repopr"
    override val description: String = "Displays useful information about a repo pull request on Github."
    override val options: List<Option> = listOf(
        Option("number", "Number of the repo pull request you want to display.")
    )
}
