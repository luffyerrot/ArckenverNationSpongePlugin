package com.arckenver.nations.cmdexecutor.nation;

import java.math.BigDecimal;
import java.util.Optional;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.arckenver.nations.ConfigHandler;
import com.arckenver.nations.DataHandler;
import com.arckenver.nations.LanguageHandler;
import com.arckenver.nations.NationsPlugin;
import com.arckenver.nations.Utils;
import com.arckenver.nations.object.Cube;
import com.arckenver.nations.object.Nation;
import com.arckenver.nations.object.PointCube;
import com.arckenver.nations.object.RegionCube;
import com.arckenver.nations.object.Zone;
import com.flowpowered.math.vector.Vector3i;

public class NationUnclaimExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("nations.command.nation.unclaim")
				.arguments()
				.executor(new NationUnclaimExecutor())
				.build(), "unclaim");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (src instanceof Player)
		{
			Player player = (Player) src;
			Nation nation = DataHandler.getNationOfPlayer(player.getUniqueId());
			if (nation == null)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NONATION));
				return CommandResult.success();
			}
			if (!nation.isStaff(player.getUniqueId()))
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PERM_NATIONSTAFF));
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
			int toUnclaim = nation.getRegionCube().size() - claimed.size();
			
			BigDecimal refund = BigDecimal.valueOf(0);
			if (ConfigHandler.getNode("prices", "unclaimRefundPercentage").getInt() != 0)
			{
				if (NationsPlugin.getEcoService() == null)
				{
					src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOECO));
					return CommandResult.success();
				}
				Optional<Account> optAccount = NationsPlugin.getEcoService().getOrCreateAccount("nation-" + nation.getUUID().toString());
				if (!optAccount.isPresent())
				{
					src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ECONONATION));
					return CommandResult.success();
				}
				refund = BigDecimal.valueOf(toUnclaim * ConfigHandler.getNode("prices", "blockClaimPrice").getInt() * (ConfigHandler.getNode("prices", "unclaimRefundPercentage").getInt() / 100));
				TransactionResult result = optAccount.get().deposit(NationsPlugin.getEcoService().getDefaultCurrency(), refund, NationsPlugin.getCause());
				if (result.getResult() != ResultType.SUCCESS)
				{
					src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ECOTRANSACTION));
					return CommandResult.success();
				}
			}
			
			
			nation.setRegionCube(claimed);
			DataHandler.addToWorldChunks(nation);
			DataHandler.saveNation(nation.getUUID());
			if (!refund.equals(BigDecimal.ZERO))
			{
				String str = LanguageHandler.INFO_UNCLAIMREFUND.replaceAll("\\{NUM\\}", Integer.toString(toUnclaim)).replaceAll("\\{PRECENT\\}", ConfigHandler.getNode("prices", "blockClaimPrice").getString());
				src.sendMessage(Text.builder()
						.append(Text.of(TextColors.AQUA, str.split("\\{AMOUNT\\}")[0]))
						.append(Utils.formatPrice(TextColors.AQUA, refund))
						.append(Text.of(TextColors.AQUA, str.split("\\{AMOUNT\\}")[1])).build());
			}
			else
			{
				src.sendMessage(Text.of(TextColors.AQUA, LanguageHandler.SUCCESS_UNCLAIM));
			}
		}
		else
		{
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
