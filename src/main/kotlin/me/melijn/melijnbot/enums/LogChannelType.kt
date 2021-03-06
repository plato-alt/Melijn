package me.melijn.melijnbot.enums

enum class LogChannelType(val text: String = this.toString(), val parentNodes: Array<String> = emptyArray()) {

    //Punishments
    PERMANENT_BAN("PermanentBan", arrayOf("punishment", "punishments", "ban", "permban", "pblc")),
    TEMP_BAN("TemporaryBan", arrayOf("punishment", "punishments", "ban", "tempban", "tblc")),
    SOFT_BAN("SoftBan", arrayOf("punishment", "punishments", "ban", "sblc")),
    UNBAN("Unban", arrayOf("punishment", "punishments", "ban", "pardon", "ublc")),

    PERMANENT_MUTE("PermanentMute", arrayOf("punishment", "punishments", "mute", "permmute", "pmlc")),
    TEMP_MUTE("TempMute", arrayOf("punishment", "punishments", "mute", "tmlc")),
    UNMUTE("Unmute", arrayOf("punishment", "punishments", "mute", "umlc")),

    KICK("Kick", arrayOf("punishment", "punishments", "klc")),
    WARN("Warn", arrayOf("punishment", "punishments", "wlc")),

    //Deleted Messages
    OTHER_DELETED_MESSAGE(
        "Other-Deleted-Message",
        arrayOf("deleted-messages", "deleted-message", "odm", "other-deleted-messages", "odmlc")
    ),
    SELF_DELETED_MESSAGE(
        "Self-Deleted-Message",
        arrayOf("deleted-messages", "deleted-message", "sdm", "self-deleted-messages", "sdmlc")
    ),
    PURGED_MESSAGE("Purged-Message", arrayOf("deleted-messages", "deleted-message", "pm", "purged-messages", "pmlc")),
    FILTERED_MESSAGE(
        "Filtered-Message",
        arrayOf("deleted-messages", "deleted-message", "fm", "filtered-messages", "fmlc")
    ),

    //Others
    VERIFICATION("Verification", arrayOf("v", "uv", "vlc", "uvlc")),
    EDITED_MESSAGE("Edited-Message", arrayOf("em", "edited-messages", "emlc")),
    REACTION("Reaction", arrayOf("r", "reactions", "r", "rlc")),
    ATTACHMENT("Attachment", arrayOf("a", "alc")),
    // OTHER_ROLES("Other-Roles", arrayOf("or", "orlc")),

    MUSIC("Music", arrayOf("m", "mlc")),
    BOT("Bot", arrayOf("b", "blc")),
    PUNISHMENT_POINTS("PunishmentPoints", arrayOf("punishment", "punishments", "pp", "pplc"));


    //Messages
    companion object {

        fun getMatchingTypesFromNode(node: String): List<LogChannelType> {
            return values().filter { channel ->
                channel.parentNodes.contains(node.toLowerCase())
                    || node.equals("all", true)
                    || channel.text.equals(node, true)
                    || channel.toString().equals(node, true)
            }
        }
    }
}
