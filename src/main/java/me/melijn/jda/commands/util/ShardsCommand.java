package me.melijn.jda.commands.util;

import me.melijn.jda.Helpers;
import me.melijn.jda.blub.Category;
import me.melijn.jda.blub.Command;
import me.melijn.jda.blub.CommandEvent;
import me.melijn.jda.utils.TableBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.JDA;

import java.util.Arrays;

import static me.melijn.jda.Melijn.PREFIX;

public class ShardsCommand extends Command {

    public ShardsCommand() {
        this.commandName = "shards";
        this.usage = PREFIX + commandName;
        this.description = "Shows you nerdy information \uD83E\uDDD0";
        this.category = Category.UTILS;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getGuild() == null || Helpers.hasPerm(event.getMember(), this.commandName, 0)) {
            ShardManager shardManager = event.getJDA().asBot().getShardManager();
            TableBuilder tableBuilder = new TableBuilder().setColumns(Arrays.asList("Shard ID", "Ping", "Users", "Guilds", "VCs"));
            int shardCount = 1;
            int avgping = 0;
            int vcs = 0;
            for (JDA jda : shardManager.getShards()) {
                avgping += jda.getPing();
                long jvcs = jda.getGuilds().stream().filter(guild -> guild.getSelfMember().getVoiceState().getChannel() != null).count();
                vcs += jvcs;
                tableBuilder.addRow(Arrays.asList(String.valueOf(shardCount++), String.valueOf(jda.getPing()), String.valueOf(jda.getUsers().size()), String.valueOf(jda.getGuilds().size()), String.valueOf(jvcs)));
            }
            avgping = avgping/(shardCount-1);
            tableBuilder.setFooterRow(Arrays.asList("Sum/Avg", String.valueOf(avgping), String.valueOf(shardManager.getUsers().size()), String.valueOf(shardManager.getGuilds().size()), String.valueOf(vcs)));

            for (String part : tableBuilder.build()) {
                event.reply(part);
            }
        }
    }
}
