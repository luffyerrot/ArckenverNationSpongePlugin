package com.arckenver.nations.listener;

import org.spongepowered.api.entity.living.monster.Monster;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.SpawnEntityEvent;

import com.arckenver.nations.DataHandler;


public class MobSpawningListener
{
	@Listener
	public void onEntitySpawn(SpawnEntityEvent event)
	{
		event.filterEntities(e -> !(e instanceof Monster) || DataHandler.getFlag("mobs", e.getLocation()));
	}
}
