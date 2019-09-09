package me.melijn.melijnbot.objects.utils

import me.melijn.melijnbot.objects.command.CommandContext
import me.melijn.melijnbot.objects.translation.MESSAGE_UNKNOWN_USER
import me.melijn.melijnbot.objects.translation.PLACEHOLDER_ARG
import me.melijn.melijnbot.objects.translation.Translateable
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.sharding.ShardManager
import net.dv8tion.jda.api.utils.data.DataObject
import net.dv8tion.jda.internal.JDAImpl
import java.awt.Color
import java.util.regex.Pattern
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


val USER_MENTION_PATTERN: Pattern = Pattern.compile("<@(\\d+)>")

val Member.asTag: String
    get() = this.user.asTag

val TextChannel.asTag: String
    get() = "#${this.name}"

suspend fun <T> RestAction<T>.await() = suspendCoroutine<T> {
    queue(
            { success -> it.resume(success) },
            { failure -> it.resumeWithException(failure) }
    )
}

fun getUserByArgs(context: CommandContext, index: Int): User {
    var user = getUserByArgsN(context, index)
    if (user == null) user = context.getAuthor()
    return user
}


fun getUserByArgsN(context: CommandContext, index: Int): User? {//With null
    val shardManager = context.getShardManager() ?: return null
    return if (context.args.size > index)
        getUserByArgsN(shardManager, context.getGuildN(), context.args[index])
    else null
}

fun getUserByArgsN(shardManager: ShardManager, guild: Guild?, arg: String): User? {
    var user: User? = null
    val argMentionMatcher = USER_MENTION_PATTERN.matcher(arg)

    user = if (arg.matches(Regex("\\d+")) && shardManager.getUserById(arg) != null)
        shardManager.getUserById(arg)
    else if (guild != null && guild.getMembersByName(arg, true).size > 0)
        guild.getMembersByName(arg, true)[0].user
    else if (guild != null && guild.getMembersByNickname(arg, true).size > 0)
        guild.getMembersByNickname(arg, true)[0].user
    else if (argMentionMatcher.matches())
        shardManager.getUserById(argMentionMatcher.group(1))
    else user

    return user
}

suspend fun retrieveUserByArgsN(context: CommandContext, index: Int): User? = suspendCoroutine {
    val user1: User? = getUserByArgsN(context, index)
    if (user1 != null) {
        it.resume(user1)
    } else if (context.args.size > index) {
        val arg = context.args[index]

        when {
            arg.matches(Regex("\\d+")) -> context.jda.shardManager?.retrieveUserById(arg)
            arg.matches(Regex("<@\\d+>")) -> {
                val id = arg.substring(2, arg.lastIndex - 1).toLong()
                context.jda.shardManager?.retrieveUserById(id)
            }
            else -> null
        }?.queue({ user ->
            it.resume(user)
        }, { _ ->
            it.resume(null)
        })
    }
}

suspend fun retrieveUserByArgsNMessage(context: CommandContext, index: Int): User? {
    val possibleUser = retrieveUserByArgsN(context, index)
    if (possibleUser == null) {
        val msg = Translateable(MESSAGE_UNKNOWN_USER)
                .string(context)
                .replace(PLACEHOLDER_ARG, context.args[index])
        sendMsg(context, msg)
    }
    return possibleUser
}

fun getUserByArgsNMessage(context: CommandContext, index: Int): User? {
    val user = getUserByArgsN(context, index)
    if (user == null) {
        val msg = Translateable(MESSAGE_UNKNOWN_USER)
                .string(context)
                .replace(PLACEHOLDER_ARG, context.args[index])
        sendMsg(context, msg, null)
    }
    return user
}

fun getRoleByArgsN(context: CommandContext, index: Int, sameGuildAsContext: Boolean = true): Role? {
    var role: Role? = null
    if (!context.isFromGuild && sameGuildAsContext) return role
    if (context.args.size > index) {
        val arg = context.args[index]

        role = if (arg.matches(Regex("\\d+")) && context.jda.shardManager?.getRoleById(arg) != null)
            context.jda.shardManager?.getRoleById(arg)
        else if (context.isFromGuild && context.getGuild().getRolesByName(arg, true).size > 0)
            context.getGuild().getRolesByName(arg, true)[0]
        else if (arg.matches(Regex("<@&\\d+>"))) {
            var role2: Role? = null
            val pattern = Pattern.compile("<@&(\\d+)>")
            val matcher = pattern.matcher(arg)
            while (matcher.find()) {
                val id = matcher.group(1)
                val role3 = context.jda.shardManager?.getRoleById(id)
                if (role2 != null && role3 == null) continue
                role2 = role3
            }
            role2
        } else role
    }
    if (sameGuildAsContext && !context.getGuild().roles.contains(role)) return null
    return role
}

fun getRoleByArgsNMessage(context: CommandContext, index: Int, sameGuildAsContext: Boolean = true): Role? {
    val role = getRoleByArgsN(context, index, sameGuildAsContext)
    if (role == null) {
        val msg = Translateable("message.unknown.role")
                .string(context)
                .replace(PLACEHOLDER_ARG, context.args[index])
        sendMsg(context, msg, null)
    }
    return role
}

fun getColorFromArgNMessage(context: CommandContext, index: Int): Color? {
    val arg = context.args[index]
    when {
        arg.matches("(?i)#([a-f]|\\d){6}".toRegex()) -> {

            var red: Int = Integer.valueOf(arg.substring(1, 3), 16)
            var green: Int = Integer.valueOf(arg.substring(3, 5), 16)
            var blue: Int = Integer.valueOf(arg.substring(5, 7), 16)
            red = red shl 16 and 0xFF0000
            green = green shl 8 and 0x00FF00
            blue = blue and 0x0000FF
            return Color.getColor((red or green or blue).toString())
        }
        else -> {
            val color: Color? = Color.getColor(arg)
            if (color == null) {
                val msg = Translateable("message.unknown.color")
                        .string(context)
                        .replace(PLACEHOLDER_ARG, arg)
                sendMsg(context, msg, null)
            }
            return color
        }
    }
}

fun JDA.messageByJSONNMessage(context: CommandContext, json: String): MessageEmbed? {
    val jdaImpl = (this as JDAImpl)

    return try {
        jdaImpl.entityBuilder.createMessageEmbed(DataObject.fromJson(json))
    } catch (e: Exception) {
        val msg = Translateable("message.invalidJSONStruct").string(context)
                .replace("%cause%", e.message ?: "unknown")
        sendMsg(context, msg, null)
        null
    }
}

fun getTextChannelByArgsN(context: CommandContext, index: Int, sameGuildAsContext: Boolean = true): TextChannel? {
    var channel: TextChannel? = null
    if (!context.isFromGuild && sameGuildAsContext) return channel
    if (context.args.size > index && context.isFromGuild) {
        val arg = context.args[index]

        channel = if (arg.matches(Regex("\\d+"))) {
            context.jda.shardManager?.getTextChannelById(arg)
        } else if (context.isFromGuild && context.getGuild().getTextChannelsByName(arg, true).size > 0) {
            context.getGuild().getTextChannelsByName(arg, true)[0]
        } else if (arg.matches(Regex("<#\\d+>"))) {
            var textChannel1: TextChannel? = null
            val pattern = Pattern.compile("<#(\\d+)>")
            val matcher = pattern.matcher(arg)
            while (matcher.find()) {
                val id = matcher.group(1)
                val textChannel2 = context.jda.shardManager?.getTextChannelById(id)
                if (textChannel1 != null && textChannel2 == null) continue
                textChannel1 = textChannel2
            }
            textChannel1
        } else channel
    }
    if (sameGuildAsContext && !context.getGuild().textChannels.contains(channel)) return null
    return channel
}

fun getTextChannelByArgsNMessage(context: CommandContext, index: Int, sameGuildAsContext: Boolean = true): TextChannel? {
    val textChannel = getTextChannelByArgsN(context, index, sameGuildAsContext)
    if (textChannel == null) {
        val msg = Translateable("message.unknown.textchannel")
                .string(context)
                .replace(PLACEHOLDER_ARG, context.args[index])
        sendMsg(context, msg, null)
    }
    return textChannel
}

fun getMemberByArgsNMessage(context: CommandContext, index: Int): Member? {
    val user = getUserByArgsN(context, index)
    val member =
            if (user == null) null
            else context.getGuild().getMember(user)

    if (member == null) {
        val msg = Translateable("message.unknown.member")
                .string(context)
                .replace(PLACEHOLDER_ARG, context.args[index])
        sendMsg(context, msg, null)
    }

    return member
}

fun getMemberByArgsN(guild: Guild, arg: String): Member? {
    val shardManager = guild.jda.shardManager ?: return null
    val user = getUserByArgsN(shardManager, guild, arg)

    return if (user == null) null
    else guild.getMember(user)
}