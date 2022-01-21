package com.arckenver.nations.listener;

import java.util.Optional;

import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.arckenver.nations.ConfigHandler;
import com.arckenver.nations.DataHandler;
import com.arckenver.nations.LanguageHandler;
import com.arckenver.nations.object.Cube;
import com.arckenver.nations.object.PointCube;

public class GoldenAxeListener
{
	@Listener
	public void onPlayerRightClick(InteractBlockEvent.Secondary.MainHand event, @First Player player)
	{
		if (ConfigHandler.getNode("others", "enableGoldenAxe").getBoolean(true) == false)
		{
			return ;
		}
		Optional<ItemStack> optItem = player.getItemInHand(HandTypes.MAIN_HAND);
		if (!optItem.isPresent())
		{
			return;
		}
		if (optItem.get().getItem().equals(ItemTypes.GOLDEN_AXE))
		{
			event.setCancelled(true);
			Optional<Location<World>> optLoc = event.getTargetBlock().getLocation();
			if (!optLoc.isPresent())
			{
				return;
			}
			PointCube secondPointCube = new PointCube(optLoc.get().getExtent(), optLoc.get().getBlockX(), optLoc.get().getBlockY(), optLoc.get().getBlockZ());
			DataHandler.setSecondPointCube(player.getUniqueId(), secondPointCube);
			PointCube firstPointCube = DataHandler.getFirstPointCube(player.getUniqueId());
			if (firstPointCube != null && !firstPointCube.getWorld().equals(secondPointCube.getWorld()))
			{
				firstPointCube = null;
				DataHandler.setFirstPointCube(player.getUniqueId(), firstPointCube);
			}
			
			String coord = secondPointCube.getX() + " " + secondPointCube.getY() + " " + secondPointCube.getZ() + ")" + ((firstPointCube != null) ? " (" + new Cube(firstPointCube, secondPointCube).size() + ")" : "");
			player.sendMessage(Text.of(TextColors.AQUA, LanguageHandler.AXE_SECOND.replaceAll("\\{COORD\\}", coord)));
		}
	}
	
	@Listener
	public void onPlayerLeftClick(InteractBlockEvent.Primary.MainHand event, @First Player player)
	{
		if (ConfigHandler.getNode("others", "enableGoldenAxe").getBoolean(true) == false)
		{
			return ;
		}
		Optional<ItemStack> optItem = player.getItemInHand(HandTypes.MAIN_HAND);
		if (!optItem.isPresent())
		{
			return;
		}
		if (optItem.get().getItem().equals(ItemTypes.GOLDEN_AXE))
		{
			event.setCancelled(true);
			Optional<Location<World>> optLoc = event.getTargetBlock().getLocation();
			if (!optLoc.isPresent())
			{
				return;
			}
			PointCube firstPointCube = new PointCube(optLoc.get().getExtent(), optLoc.get().getBlockX(), optLoc.get().getBlockY(), optLoc.get().getBlockZ());
			DataHandler.setFirstPointCube(player.getUniqueId(), firstPointCube);
			PointCube secondPointCube = DataHandler.getSecondPointCube(player.getUniqueId());
			if (secondPointCube != null && !secondPointCube.getWorld().equals(firstPointCube.getWorld()))
			{
				secondPointCube = null;
				DataHandler.setSecondPointCube(player.getUniqueId(), secondPointCube);
			}
			
			String coord = firstPointCube.getX() + " " + firstPointCube.getY() + " " + firstPointCube.getZ() + ")" + ((secondPointCube != null) ? " (" + new Cube(secondPointCube, firstPointCube).size() + ")" : "");
			player.sendMessage(Text.of(TextColors.AQUA, LanguageHandler.AXE_FIRST.replaceAll("\\{COORD\\}", coord)));
		}
	}
}
