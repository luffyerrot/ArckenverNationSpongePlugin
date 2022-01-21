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

import com.arckenver.nations.ConfigHandler;
import com.arckenver.nations.DataHandler;
import com.arckenver.nations.LanguageHandler;
import com.arckenver.nations.NationsPlugin;
import com.arckenver.nations.Utils;
import com.arckenver.nations.object.Cube;
import com.arckenver.nations.object.Nation;
import com.arckenver.nations.object.PointCube;
import com.arckenver.nations.object.RegionCube;

public class NationClaimExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		CommandSpec subCmd = CommandSpec.builder()
		.description(Text.of(""))
		.permission("nations.command.nation.claim.outpost")
		.arguments()
		.executor(new NationClaimOutpostExecutor())
		.build();
		
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("nations.command.nation.claim")
				.arguments()
				.executor(new NationClaimExecutor())
				.child(subCmd, "outpost", "o")
				.build(), "claim");
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
			if (nation.getRegionCube().size() > 0 && !nation.getRegionCube().isAdjacent(cube))
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDADJACENT));
				return CommandResult.success();
			}
			if (!DataHandler.canClaim(cube, false, nation.getUUID()))
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_TOOCLOSE));
				return CommandResult.success();
			}
			RegionCube claimed = nation.getRegionCube().copy();
			claimed.addCube(cube);
			
			if (claimed.size() > nation.maxBlockSize())
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOENOUGHBLOCKS));
				return CommandResult.success();
			}
			
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
			BigDecimal price = BigDecimal.valueOf((claimed.size() - nation.getRegionCube().size()) * ConfigHandler.getNode("prices", "blockClaimPrice").getDouble());
			TransactionResult result = optAccount.get().withdraw(NationsPlugin.getEcoService().getDefaultCurrency(), price, NationsPlugin.getCause());
			if (result.getResult() == ResultType.ACCOUNT_NO_FUNDS)
			{
				src.sendMessage(Text.builder()
						.append(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDMONEYNATION.split("\\{AMOUNT\\}")[0]))
						.append(Utils.formatPrice(TextColors.RED, price))
						.append(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDMONEYNATION.split("\\{AMOUNT\\}")[1])).build());
				return CommandResult.success();
			}
			else if (result.getResult() != ResultType.SUCCESS)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ECOTRANSACTION));
				return CommandResult.success();
			}
			
			nation.setRegionCube(claimed);
			DataHandler.addToWorldChunks(nation);
			DataHandler.saveNation(nation.getUUID());
			src.sendMessage(Text.of(TextColors.AQUA, LanguageHandler.SUCCESS_CLAIM));
		}
		else
		{
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
