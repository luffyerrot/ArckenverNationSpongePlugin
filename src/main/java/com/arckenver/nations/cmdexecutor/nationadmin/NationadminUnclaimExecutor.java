package com.arckenver.nations.cmdexecutor.nationadmin;

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
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.arckenver.nations.ConfigHandler;
import com.arckenver.nations.DataHandler;
import com.arckenver.nations.LanguageHandler;
import com.arckenver.nations.object.Cube;
import com.arckenver.nations.object.Nation;
import com.arckenver.nations.object.PointCube;
import com.arckenver.nations.object.RegionCube;
import com.arckenver.nations.object.Zone;
import com.flowpowered.math.vector.Vector3i;

public class NationadminUnclaimExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("nations.command.nationadmin.unclaim")
				.arguments(GenericArguments.optional(GenericArguments.string(Text.of("nation"))))
				.executor(new NationadminUnclaimExecutor())
				.build(), "unclaim");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (src instanceof Player)
		{
			if (!ctx.<String>getOne("nation").isPresent())
			{
				src.sendMessage(Text.of(TextColors.YELLOW, "/na unclaim <nation>"));
				return CommandResult.success();
			}
			Player player = (Player) src;
			String nationName = ctx.<String>getOne("nation").get();
			Nation nation = DataHandler.getNation(nationName);
			if (nation == null)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_BADNATIONNAME));
				return CommandResult.success();
			}
			PointCube a = DataHandler.getFirstPointCube(player.getUniqueId());
			PointCube b = DataHandler.getSecondPointCube(player.getUniqueId());
			if (a == null || b == null)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDAXESELECT));
				return CommandResult.success();
			}
			if (!ConfigHandler.getNode("worlds").getNode(a.getWorld().getName()).getNode("enabled").getBoolean())
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PLUGINDISABLEDINWORLD));
				return CommandResult.success();
			}
			Cube cube = new Cube(a, b);
			if (!nation.getRegionCube().intersects(cube))
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDINTERSECT));
				return CommandResult.success();
			}
			for (Location<World> spawn : nation.getSpawns().values())
			{
				if (cube.isInside(new Vector3i(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ())))
				{
					src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_AREACONTAINSPAWN));
					return CommandResult.success();
				}
			}
			for (Zone zone : nation.getZones().values())
			{
				if (zone.getCube().intersects(cube))
				{
					src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_SELECTIONCONTAINZONE));
					return CommandResult.success();
				}
			}
			RegionCube claimed = nation.getRegionCube().copy();
			claimed.removeCube(cube);

			nation.setRegionCube(claimed);
			DataHandler.addToWorldChunks(nation);
			DataHandler.saveNation(nation.getUUID());
			src.sendMessage(Text.of(TextColors.AQUA, LanguageHandler.SUCCESS_GENERAL));
		}
		else
		{
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
