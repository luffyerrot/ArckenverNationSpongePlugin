package com.arckenver.nations.cmdexecutor.zone;

import java.util.Map.Entry;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.arckenver.nations.DataHandler;
import com.arckenver.nations.LanguageHandler;
import com.arckenver.nations.cmdelement.PlayerNameElement;
import com.arckenver.nations.object.Cube;
import com.arckenver.nations.object.Nation;
import com.arckenver.nations.object.PointCube;
import com.arckenver.nations.object.Zone;

public class ZoneCreateExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("nations.command.zone.create")
				.arguments(
						GenericArguments.optional(GenericArguments.string(Text.of("name"))),
						GenericArguments.optional(new PlayerNameElement(Text.of("owner"))))
				.executor(new ZoneCreateExecutor())
				.build(), "create", "add");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (src instanceof Player)
		{
			Player player = (Player) src;
			Nation nation = DataHandler.getNation(player.getLocation());
			if (nation == null)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDSTANDNATION));
				return CommandResult.success();
			}
			if (!nation.isStaff(player.getUniqueId()))
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PERM_NATIONSTAFF));
				return CommandResult.success();
			}
			String zoneName = null;
			if (ctx.<String>getOne("name").isPresent())
			{
				zoneName = ctx.<String>getOne("name").get();
			}
			if (zoneName != null && !zoneName.matches("[\\p{Alnum}\\p{IsIdeographic}\\p{IsLetter}\"_\"]{1,30}"))
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ALPHASPAWN
						.replaceAll("\\{MIN\\}", "1")
						.replaceAll("\\{MAX\\}", "30")));
				return CommandResult.success();
			}
			UUID owner = null;
			if (ctx.<String>getOne("owner").isPresent())
			{
				owner = DataHandler.getPlayerUUID(ctx.<String>getOne("owner").get());
				if (owner == null)
				{
					src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_BADPLAYERNAME));
					return CommandResult.success();
				}
			}
			PointCube a = DataHandler.getFirstPointCube(player.getUniqueId());
			PointCube b = DataHandler.getSecondPointCube(player.getUniqueId());
			if (a == null || b == null)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDAXESELECT));
				return CommandResult.success();
			}
			Cube cube = new Cube(a, b);
			if (!nation.getRegionCube().isInside(cube))
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ZONENOTINNATION));
				return CommandResult.success();
			}
			for (Zone zone : nation.getZones().values())
			{
				if (zoneName != null && zone.isNamed() && zone.getName().equalsIgnoreCase(zoneName))
				{
					src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ZONENAME));
					return CommandResult.success();
				}
				if (cube.intersects(zone.getCube()))
				{
					src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ZONEINTERSECT));
					return CommandResult.success();
				}
			}
			Zone zone = new Zone(UUID.randomUUID(), zoneName, cube, owner, player.getName());
			for (Entry<String, Boolean> e : nation.getFlags().entrySet())
			{
				zone.setFlag(e.getKey(), e.getValue());
			}
			nation.addZone(zone);
			nation.addCitizen(player.getUniqueId());
			zone.setOwner(player.getUniqueId());
			zone.setPlayerName(player.getName());
			DataHandler.saveNation(nation.getUUID());
			src.sendMessage(Text.of(TextColors.GREEN, LanguageHandler.SUCCESS_ZONECREATE.replaceAll("\\{ZONE\\}", zone.getName())));
			Sponge.getServer().getPlayer(owner).ifPresent(
					p -> p.sendMessage(Text.of(TextColors.AQUA, LanguageHandler.SUCCESS_SETOWNER.replaceAll("\\{ZONE\\}", zone.getName()))));
		}
		else
		{
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
