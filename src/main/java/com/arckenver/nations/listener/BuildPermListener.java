package com.arckenver.nations.listener;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.arckenver.nations.ConfigHandler;
import com.arckenver.nations.DataHandler;
import com.arckenver.nations.LanguageHandler;
import com.arckenver.nations.Utils;
import com.arckenver.nations.object.Nation;
import com.arckenver.nations.object.Zone;

public class BuildPermListener
{
	@Listener(order=Order.FIRST, beforeModifications = true)
	public void onPlayerModifyBlock(ChangeBlockEvent.Modify event, @First Player player)
	{
		if (player.hasPermission("nations.admin.bypass.perm.build"))
		{
			return;
		}
		event
		.getTransactions()
		.stream()
		.forEach(trans -> trans.getOriginal().getLocation().ifPresent(loc -> {
			if (ConfigHandler.getNode("worlds").getNode(trans.getFinal().getLocation().get().getExtent().getName()).getNode("enabled").getBoolean()
					&& !ConfigHandler.isWhitelisted("build", trans.getFinal().getState().getType().getId())
					&& !DataHandler.getPerm("build", player.getUniqueId(), loc))
			{
				trans.setValid(false);
				try {
					player.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PERM_BUILD));
				} catch (Exception e) {}
			}
		}));
	}

	@Listener(order=Order.FIRST, beforeModifications = true)
	public void onPlayerChangeBlock(ChangeBlockEvent.Pre event, @First Player player)
	{
		if (player.hasPermission("nations.admin.bypass.perm.build"))
		{
			return;
		}
		if (Utils.isFakePlayer(event)) {
			return;
		}
		for (Location<World> loc : event.getLocations()) {
			if (ConfigHandler.getNode("worlds").getNode(loc.getExtent().getName()).getNode("enabled").getBoolean()
					&& !ConfigHandler.isWhitelisted("break", loc.getBlock().getId())
					&& !DataHandler.getPerm("build", player.getUniqueId(), loc))
			{
				event.setCancelled(true);
				try {
					player.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PERM_BUILD));
				} catch (Exception e) {}
				return;
			}
		}
	}

	@Listener(order=Order.FIRST, beforeModifications = true)
	public void onPlayerPlacesBlock(ChangeBlockEvent.Place event, @First Player player)
	{
		if (player.hasPermission("nations.admin.bypass.perm.build"))
		{
			return;
		}
		event
		.getTransactions()
		.stream()
		.forEach(trans -> trans.getOriginal().getLocation().ifPresent(loc -> {
			if (ConfigHandler.getNode("worlds").getNode(trans.getFinal().getLocation().get().getExtent().getName()).getNode("enabled").getBoolean()
					&& !ConfigHandler.isWhitelisted("build", trans.getFinal().getState().getType().getId())
					&& !DataHandler.getPerm("build", player.getUniqueId(), loc))
			{
				trans.setValid(false);
				try {
					player.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PERM_BUILD));
				} catch (Exception e) {}
			}
		}));
	}

	@Listener(order=Order.FIRST, beforeModifications = true)
	public void onPlayerBreaksBlock(ChangeBlockEvent.Break event)
	{
		User user = Utils.getUser(event);

		if (user != null && user.hasPermission("nations.admin.bypass.perm.build"))
		{
			return;
		}
		event
		.getTransactions()
		.stream()
		.forEach(trans -> trans.getOriginal().getLocation().ifPresent(loc -> {
			if (!ConfigHandler.isWhitelisted("break", trans.getFinal().getState().getType().getId())
					&& ConfigHandler.getNode("worlds").getNode(trans.getFinal().getLocation().get().getExtent().getName()).getNode("enabled").getBoolean())
			{
				if (user == null || !DataHandler.getPerm("build", user.getUniqueId(), loc))
				{
					trans.setValid(false);
					if (user != null && user instanceof Player) {
						try {
							((Player) user).sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PERM_BUILD));
						} catch (Exception e) {}
					}
				}
			}
		}));
	}

	@Listener(order = Order.FIRST, beforeModifications = true)
	public void onSignChanged(ChangeSignEvent event)
	{
		Player player = event.getCause().first(Player.class).get();
		if (!ConfigHandler.getNode("worlds").getNode(event.getTargetTile().getLocation().getExtent().getName()).getNode("enabled").getBoolean())
		{
			return;
		}
		if (player.hasPermission("nations.admin.bypass.perm.build"))
		{
			if(event.getCause().first(Player.class).isPresent()) {
				Nation nation = DataHandler.getNation(player.getLocation());
				Zone zone = nation.getZone(player.getLocation());
				if(!event.getText().lines().get(0).isEmpty() && event.getText().lines().get(0).toPlain().trim().equals("Maison") && !event.getText().lines().get(1).isEmpty() && event.getText().lines().get(1).toPlain().trim().equals(zone.getName())
						&& !event.getText().lines().get(2).isEmpty()) {
					Text ligne1 = Text.builder("[Maison]").color(TextColors.DARK_PURPLE).build();
					event.getText().set(event.getText().lines().set(0, ligne1));
					Text ligne2 = Text.builder(event.getText().lines().get(1).toPlain()).color(TextColors.LIGHT_PURPLE).build();
					event.getText().set(event.getText().lines().set(1, ligne2));
					Text ligne3 = Text.builder(event.getText().lines().get(2).toPlain()).color(TextColors.GRAY).build();
					event.getText().set(event.getText().lines().set(2, ligne3));
					Text ligne4 = Text.builder(zone.getPlayerName()).color(TextColors.DARK_GRAY).build();
					event.getText().set(event.getText().lines().set(3, ligne4));
				}
			}
			return;
		}
		if (!DataHandler.getPerm("build", player.getUniqueId(), event.getTargetTile().getLocation()))
		{
			event.setCancelled(true);
		}
	}
	
	public static Text color(String message) {
		return TextSerializers.formattingCode('&').deserialize(message);
	}

	@Listener(order=Order.FIRST, beforeModifications = true)
	public void onEntitySpawn(SpawnEntityEvent event, @First Player player)
	{
		if (player.hasPermission("nations.admin.bypass.perm.build"))
		{
			return;
		}
		if (event.getCause().contains(SpawnTypes.PLACEMENT))
		{
			try {
				if (!ConfigHandler.getNode("worlds").getNode(event.getEntities().get(0).getWorld().getName()).getNode("enabled").getBoolean())
					return;
				if (!ConfigHandler.isWhitelisted("spawn", event.getEntities().get(0).getType().getId())
						&& !DataHandler.getPerm("build", player.getUniqueId(), event.getEntities().get(0).getLocation()))
					event.setCancelled(true);
			} catch (IndexOutOfBoundsException e) {}
		}
	}
}
