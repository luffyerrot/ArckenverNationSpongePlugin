package com.arckenver.nations.cmdexecutor.nationadmin;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;

import com.arckenver.nations.ConfigHandler;
import com.arckenver.nations.DataHandler;
import com.arckenver.nations.LanguageHandler;
import com.arckenver.nations.cmdelement.NationNameElement;
import com.arckenver.nations.object.Nation;

public class NationadminSettagExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("nations.command.nationadmin.settag")
				.arguments(
						GenericArguments.optional(new NationNameElement(Text.of("nation"))),
						GenericArguments.optional(GenericArguments.string(Text.of("tag"))))
				.executor(new NationadminSettagExecutor())
				.build(), "settag", "tag");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (!ctx.<String> getOne("nation").isPresent())
		{
			src.sendMessage(Text.of(TextColors.YELLOW, "/na settag <nation> [tag]"));
			return CommandResult.success();
		}
		String newTag = null;
		if (ctx.<String> getOne("tag").isPresent())
			newTag = ctx.<String> getOne("tag").get();
		Nation nation = DataHandler.getNation(ctx.<String> getOne("nation").get());
		if (nation == null)
		{
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NONATION));
			return CommandResult.success();
		}
		if (newTag != null && DataHandler.getNation(newTag) != null)
		{
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NAMETAKEN));
			return CommandResult.success();
		}
		if (newTag != null && DataHandler.getNationByTag(newTag) != null)
		{
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_TAGTAKEN));
			return CommandResult.success();
		}
		if (newTag != null && !newTag.matches("[\\p{Alnum}\\p{IsIdeographic}\\p{IsLetter}\"_\"]*"))
		{
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_TAGALPHA));
			return CommandResult.success();
		}
		if (newTag != null && (newTag.length() < ConfigHandler.getNode("others", "minNationTagLength").getInt()
				|| newTag.length() > ConfigHandler.getNode("others", "maxNationTagLength").getInt()))
		{
			src.sendMessage(Text.of(TextColors.RED,
					LanguageHandler.ERROR_TAGLENGTH
							.replaceAll("\\{MIN\\}",
									ConfigHandler.getNode("others", "minNationTagLength").getString())
							.replaceAll("\\{MAX\\}",
									ConfigHandler.getNode("others", "maxNationTagLength").getString())));
			return CommandResult.success();
		}
		String oldTag = nation.getTag();
		nation.setTag(newTag);
		DataHandler.saveNation(nation.getUUID());
		MessageChannel.TO_ALL.send(Text.of(TextColors.RED,
				LanguageHandler.INFO_TAG.replaceAll("\\{NAME\\}", nation.getName()).replaceAll("\\{OLDTAG\\}", oldTag).replaceAll("\\{NEWTAG\\}", nation.getTag())));
		return CommandResult.success();
	}
}

