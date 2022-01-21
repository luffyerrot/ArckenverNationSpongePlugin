package com.arckenver.nations.listener;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.hanging.ItemFrame;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.monster.Monster;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.arckenver.nations.ConfigHandler;
import com.arckenver.nations.DataHandler;
import com.arckenver.nations.LanguageHandler;
import com.arckenver.nations.cmdexecutor.zone.ZoneBuyExecutor;
import com.arckenver.nations.cmdexecutor.zone.ZoneSellExecutor;
import com.arckenver.nations.object.Nation;
import com.arckenver.nations.object.Zone;

public class InteractPermListener
{
	@Listener(order=Order.FIRST, beforeModifications = true)
	public void onInteract(InteractBlockEvent event, @First Player player)
	{
		if (!ConfigHandler.getNode("worlds").getNode(player.getWorld().getName()).getNode("enabled").getBoolean())
		{
			return;
		}
		if (player.hasPermission("nations.admin.bypass.perm.interact"))
		{
			
			return;
		}
		Optional<ItemStack> optItem = player.getItemInHand(HandTypes.MAIN_HAND);
		if (optItem.isPresent() && (ConfigHandler.isWhitelisted("use", optItem.get().getItem().getId()) || optItem.get().getItem().equals(ItemTypes.GOLDEN_AXE) && ConfigHandler.getNode("others", "enableGoldenAxe").getBoolean(true)))
			return;
		event.getTargetBlock().getLocation().ifPresent(loc -> {
			if (!DataHandler.getPerm("interact", player.getUniqueId(), loc))
			{
				event.setCancelled(true);
				if (loc.getBlockType() != BlockTypes.STANDING_SIGN && loc.getBlockType() != BlockTypes.WALL_SIGN)
					player.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PERM_INTERACT));
			}
		});
	}
	
	@Listener(order=Order.FIRST, beforeModifications = true)
	public void InteractBlockEventGauche(InteractBlockEvent.Primary event, @First Player player) throws InterruptedException, ExecutionException {
		if (!ConfigHandler.getNode("worlds").getNode(player.getWorld().getName()).getNode("enabled").getBoolean())
		{
			return;
		}
		if (player.hasPermission("nations.command.zone.leftsign")) {
			if (event.getCause().first(Player.class).isPresent()) {
				if (event.getTargetBlock().getLocation().isPresent()) {
					Location<World> location = event.getTargetBlock().getLocation().get();
					if((location.getBlock().getType() == BlockTypes.STANDING_SIGN) || (location.getBlock().getType() == BlockTypes.WALL_SIGN)) {
						List<Text> line = location.getValue(Keys.SIGN_LINES).get().get();
						if(!line.get(0).isEmpty() && line.get(0).toPlain().trim().equals("[Maison]") && !line.get(1).isEmpty() && !line.get(2).isEmpty() && !line.get(3).isEmpty()) {
							Nation nation = DataHandler.getNation(player.getLocation());
							Zone zone = nation.getZone(player.getLocation());
							if (zone == null){
								player.sendMessage(Text.of(TextColors.RED, "Vous devez vous tenir dans la zone de la maison pour l'acheter."));
							} else {
								if (zone.isForSale()) {
									String targetName = line.get(3).toPlainSingle();
									UserStorageService storage = Sponge.getServiceManager().provide(UserStorageService.class).get();
									User target = storage.get(targetName).get();
									ZoneBuyExecutor zoneBE = new ZoneBuyExecutor();
									zoneBE.zoneBuy(player, target);
									TileEntity tile = location.getTileEntity().get();
									SignData sign = tile.getOrCreate(SignData.class).get();
									Text ligne3 = Text.builder(line.get(2).toPlain()).color(TextColors.RED).build();
									sign.set(sign.lines().set(2, ligne3));
									Text ligne4 = Text.builder(zone.getPlayerName()).color(TextColors.DARK_GRAY).build();
									sign.set(sign.lines().set(3, ligne4));
									tile.offer(sign);
								} else {
									player.sendMessage(Text.of(TextColors.RED, "La zone n'est pas à vendre"));
								}
							}
						} 
					}
				}
			}
			return;
		}
	}
	
	@Listener(order=Order.FIRST, beforeModifications = true)
	public void onInteract(InteractBlockEvent.Secondary event, @First Player player)
	{
		if (!ConfigHandler.getNode("worlds").getNode(player.getWorld().getName()).getNode("enabled").getBoolean())
		{
			return;
		}
		if (player.hasPermission("nations.command.zone.rightsign"))
		{
			if (event.getCause().first(Player.class).isPresent()) {
				if (event.getTargetBlock().getLocation().isPresent()) {
					Location<World> location = event.getTargetBlock().getLocation().get();
					if((location.getBlock().getType() == BlockTypes.STANDING_SIGN) || (location.getBlock().getType() == BlockTypes.WALL_SIGN)) {
						List<Text> line = location.getValue(Keys.SIGN_LINES).get().get();
						if(!line.get(0).isEmpty() && line.get(0).toPlain().trim().equals("[Maison]") && !line.get(1).isEmpty() && !line.get(2).isEmpty() && !line.get(3).isEmpty()) {
							if(line.get(3).toPlain().trim().equals(player.getName())) {
								Nation nation = DataHandler.getNation(player.getLocation());
								Zone zone = nation.getZone(player.getLocation());
								if (zone == null){
									player.sendMessage(Text.of(TextColors.RED, "Vous devez vous tenir dans la zone de la maison pour la vendre."));
								} else {
									if (!zone.isForSale()) {
										String linePrix = line.get(2).toPlain();
										BigDecimal bigDecimal = new BigDecimal(linePrix);
										ZoneSellExecutor.zoneSell(bigDecimal, player);
										TileEntity tile = location.getTileEntity().get();
										SignData sign = tile.getOrCreate(SignData.class).get();
										Text ligne3 = Text.builder(line.get(2).toPlain()).color(TextColors.GREEN).build();
										sign.set(sign.lines().set(2, ligne3));
										Text ligne4 = Text.builder(line.get(3).toPlain()).color(TextColors.DARK_GRAY).build();
										sign.set(sign.lines().set(3, ligne4));
										tile.offer(sign);
									} else {
										player.sendMessage(Text.of(TextColors.RED, "Cette maison est déjà à vendre."));
									}
								}
							} else {
								player.sendMessage(Text.of(TextColors.RED, "Cette maison appartient à " + line.get(3).toPlain() + ", tu ne peut pas la vendre."));
							}
						} 
					}
				}
			}
			return;
		}
		Optional<ItemStack> optItem = player.getItemInHand(HandTypes.MAIN_HAND);
		if (optItem.isPresent() && (ConfigHandler.isWhitelisted("use", optItem.get().getItem().getId()) || optItem.get().getItem().equals(ItemTypes.GOLDEN_AXE) && ConfigHandler.getNode("others", "enableGoldenAxe").getBoolean(true)))
			return;
		event.getTargetBlock().getLocation().ifPresent(loc -> {
			if (!DataHandler.getPerm("interact", player.getUniqueId(), loc))
			{
				event.setCancelled(true);
				if (loc.getBlockType() != BlockTypes.STANDING_SIGN && loc.getBlockType() != BlockTypes.WALL_SIGN)
					player.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PERM_INTERACT));
			}
		});
	}

	@Listener(order=Order.FIRST, beforeModifications = true)
	public void onInteract(InteractEntityEvent event, @First Player player)
	{
		if (!ConfigHandler.getNode("worlds").getNode(player.getWorld().getName()).getNode("enabled").getBoolean())
		{
			return;
		}
		if (player.hasPermission("nations.admin.bypass.perm.interact"))
		{
			return;
		}
		Entity target = event.getTargetEntity();
		if (target instanceof Player || target instanceof Monster)
		{
			return;
		}
		if (target instanceof ItemFrame || target instanceof ArmorStand)
		{
			if (player.hasPermission("nations.admin.bypass.perm.build"))
			{
				return;
			}
			if (!DataHandler.getPerm("build", player.getUniqueId(), event.getTargetEntity().getLocation()))
			{
				event.setCancelled(true);
				player.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PERM_BUILD));
			}
			return;
		}
		if (!DataHandler.getPerm("interact", player.getUniqueId(), event.getTargetEntity().getLocation()))
		{
			event.setCancelled(true);
			player.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PERM_INTERACT));
		}
	}
}
