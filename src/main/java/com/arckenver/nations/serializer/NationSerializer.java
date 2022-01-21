package com.arckenver.nations.serializer;

import java.lang.reflect.Type;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.UUID;

import com.arckenver.nations.object.*;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class NationSerializer implements JsonSerializer<Nation>
{
	@Override
	public JsonElement serialize(Nation nation, Type type, JsonSerializationContext ctx)
	{
		JsonObject json = new JsonObject();

		json.add("uuid", new JsonPrimitive(nation.getUUID().toString()));
		json.add("name", new JsonPrimitive(nation.getRealName()));
		json.add("admin", new JsonPrimitive(nation.isAdmin()));

		if (!nation.isAdmin())
		{
			json.add("taxes", new JsonPrimitive(nation.getTaxes()));
			json.add("extras", new JsonPrimitive(nation.getExtras()));
			json.add("extraspawns", new JsonPrimitive(nation.getExtraSpawns()));

		}
		
		if (nation.hasTag())
			json.add("tag", new JsonPrimitive(nation.getTag()));

		JsonObject flags = new JsonObject();
		for (Entry<String, Boolean> e : nation.getFlags().entrySet())
		{
			flags.add(e.getKey(), new JsonPrimitive(e.getValue()));
		}
		json.add("flags", flags);

		JsonObject perms = new JsonObject();
		for (Entry<String, Hashtable<String, Boolean>> e : nation.getPerms().entrySet())
		{
			JsonObject obj = new JsonObject();
			for (Entry<String, Boolean> en : e.getValue().entrySet())
			{
				obj.add(en.getKey(), new JsonPrimitive(en.getValue()));
			}
			perms.add(e.getKey(), obj);
		}
		json.add("perms", perms);

		JsonArray cubeArray = new JsonArray();
		for (Cube c : nation.getRegionCube().getCubes())
		{
			JsonObject cubeJson = new JsonObject();
			cubeJson.add("world", new JsonPrimitive(c.getWorld().toString()));
			cubeJson.add("minX", new JsonPrimitive(c.getMinX()));
			cubeJson.add("maxX", new JsonPrimitive(c.getMaxX()));
			cubeJson.add("minY", new JsonPrimitive(c.getMinY()));
			cubeJson.add("maxY", new JsonPrimitive(c.getMaxY()));
			cubeJson.add("minZ", new JsonPrimitive(c.getMinZ()));
			cubeJson.add("maxZ", new JsonPrimitive(c.getMaxZ()));
			cubeArray.add(cubeJson);
		}
		json.add("cubes", cubeArray);

		JsonArray zonesArray = new JsonArray();
		for (Zone zone : nation.getZones().values())
		{
			JsonObject zoneObj = new JsonObject();

			zoneObj.add("uuid", new JsonPrimitive(zone.getUUID().toString()));
			if (zone.isNamed())
				zoneObj.add("name", new JsonPrimitive(zone.getRealName()));

			JsonObject cubeJson = new JsonObject();
			cubeJson.add("world", new JsonPrimitive(zone.getCube().getWorld().toString()));
			cubeJson.add("minX", new JsonPrimitive(zone.getCube().getMinX()));
			cubeJson.add("maxX", new JsonPrimitive(zone.getCube().getMaxX()));
			cubeJson.add("minY", new JsonPrimitive(zone.getCube().getMinY()));
			cubeJson.add("maxY", new JsonPrimitive(zone.getCube().getMaxY()));
			cubeJson.add("minZ", new JsonPrimitive(zone.getCube().getMinZ()));
			cubeJson.add("maxZ", new JsonPrimitive(zone.getCube().getMaxZ()));
			zoneObj.add("cube", cubeJson);

			if (zone.getOwner() != null)
			{
				zoneObj.add("owner", new JsonPrimitive(zone.getOwner().toString()));
			}
			
			if (zone.getPlayerName() != null)
			{
				zoneObj.add("playername", new JsonPrimitive(zone.getPlayerName().toString()));
			}

			JsonArray coownersArray = new JsonArray();
			for (UUID coowner : zone.getCoowners())
			{
				coownersArray.add(new JsonPrimitive(coowner.toString()));
			}
			zoneObj.add("coowners", coownersArray);

			JsonObject zoneFlags = new JsonObject();
			for (Entry<String, Boolean> e : zone.getFlags().entrySet())
			{
				zoneFlags.add(e.getKey(), new JsonPrimitive(e.getValue()));
			}
			zoneObj.add("flags", zoneFlags);

			JsonObject zonePerms = new JsonObject();
			for (Entry<String, Hashtable<String, Boolean>> e : zone.getPerms().entrySet())
			{
				JsonObject obj = new JsonObject();
				for (Entry<String, Boolean> en : e.getValue().entrySet())
				{
					obj.add(en.getKey(), new JsonPrimitive(en.getValue()));
				}
				zonePerms.add(e.getKey(), obj);
			}
			zoneObj.add("perms", zonePerms);

			if (zone.isForSale())
			{
				zoneObj.add("price", new JsonPrimitive(zone.getPrice()));
			}

			zonesArray.add(zoneObj);
		}
		json.add("zones", zonesArray);

		JsonObject spawns = new JsonObject();
		for (Entry<String, Location<World>> e : nation.getSpawns().entrySet())
		{
			JsonObject loc = new JsonObject();
			loc.add("world", new JsonPrimitive(e.getValue().getExtent().getUniqueId().toString()));
			loc.add("x", new JsonPrimitive(e.getValue().getX()));
			loc.add("y", new JsonPrimitive(e.getValue().getY()));
			loc.add("z", new JsonPrimitive(e.getValue().getZ()));

			spawns.add(e.getKey(), loc);
		}
		json.add("spawns", spawns);
		
		if (nation.getPresident() != null)
			json.add("president", new JsonPrimitive(nation.getPresident().toString()));

		JsonArray ministersArray = new JsonArray();
		for (UUID minister : nation.getMinisters())
		{
			ministersArray.add(new JsonPrimitive(minister.toString()));
		}
		json.add("ministers", ministersArray);

		JsonArray citizensArray = new JsonArray();
		for (UUID citizen : nation.getCitizens())
		{
			citizensArray.add(new JsonPrimitive(citizen.toString()));
		}
		json.add("citizens", citizensArray);

		return json;
	}
}
