package com.arckenver.nations.serializer;

import java.lang.reflect.Type;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

import com.arckenver.nations.object.*;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.World;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class NationDeserializer implements JsonDeserializer<Nation>
{
	@Override
	public Nation deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) throws JsonParseException
	{
		JsonObject obj = json.getAsJsonObject();
		UUID uuid = UUID.fromString(obj.get("uuid").getAsString());
		String name = obj.get("name").getAsString();
		boolean isAdmin = obj.get("admin").getAsBoolean();
		Nation nation = new Nation(uuid, name, isAdmin);
		if (obj.has("tag"))
			nation.setTag(obj.get("tag").getAsString());
		for (Entry<String, JsonElement> e : obj.get("flags").getAsJsonObject().entrySet())
		{
			nation.setFlag(e.getKey(), e.getValue().getAsBoolean());
		}
		for (Entry<String, JsonElement> e : obj.get("perms").getAsJsonObject().entrySet())
		{
			for (Entry<String, JsonElement> en : e.getValue().getAsJsonObject().entrySet())
			{
				nation.setPerm(e.getKey(), en.getKey(), en.getValue().getAsBoolean());
			}
		}
		RegionCube region = new RegionCube();
		for (JsonElement e : obj.get("cubes").getAsJsonArray())
		{
			JsonObject cubeObj = e.getAsJsonObject();
			Cube cube = new Cube(
					UUID.fromString(cubeObj.get("world").getAsString()),
					cubeObj.get("minX").getAsInt(),
					cubeObj.get("maxX").getAsInt(),
					cubeObj.get("minY").getAsInt(),
					cubeObj.get("maxY").getAsInt(),
					cubeObj.get("minZ").getAsInt(),
					cubeObj.get("maxZ").getAsInt());
			region.addCube(cube);
		}
		nation.setRegionCube(region);

		if (obj.has("zones")) {
			for (JsonElement e : obj.get("zones").getAsJsonArray())
			{
				JsonObject zoneObj = e.getAsJsonObject();
				UUID zoneUUID = UUID.fromString(zoneObj.get("uuid").getAsString());
				String zoneName = null;
				if (zoneObj.has("name"))
					zoneName = zoneObj.get("name").getAsString();

				JsonObject cubeObj = zoneObj.get("cube").getAsJsonObject();
				Cube cube = new Cube(
						UUID.fromString(cubeObj.get("world").getAsString()),
						cubeObj.get("minX").getAsInt(),
						cubeObj.get("maxX").getAsInt(),
						cubeObj.get("minY").getAsInt(),
						cubeObj.get("maxY").getAsInt(),
						cubeObj.get("minZ").getAsInt(),
						cubeObj.get("maxZ").getAsInt());
				Zone zone = new Zone(zoneUUID, zoneName, cube);
				if (zoneObj.has("owner"))
				{
					zone.setOwner(UUID.fromString(zoneObj.get("owner").getAsString()));
				}
				if (zoneObj.has("playername"))
				{
					zone.setPlayerName(zoneObj.get("playername").getAsString());
				}
				for (JsonElement el : zoneObj.get("coowners").getAsJsonArray())
				{
					zone.addCoowner(UUID.fromString(el.getAsString()));
				}
				for (Entry<String, JsonElement> en : zoneObj.get("flags").getAsJsonObject().entrySet())
				{
					zone.setFlag(en.getKey(), en.getValue().getAsBoolean());
				}
				for (Entry<String, JsonElement> en : zoneObj.get("perms").getAsJsonObject().entrySet())
				{
					for (Entry<String, JsonElement> ent : en.getValue().getAsJsonObject().entrySet())
					{
						zone.setPerm(en.getKey(), ent.getKey(), ent.getValue().getAsBoolean());
					}
				}
				if (zoneObj.has("price"))
				{
					zone.setPrice(zoneObj.get("price").getAsBigDecimal());
				}
				nation.addZone(zone);
			}
		}

		if (obj.has("spawns"))
		{
			for (Entry<String, JsonElement> e : obj.get("spawns").getAsJsonObject().entrySet())
			{
				JsonObject spawnObj = e.getValue().getAsJsonObject();
				Optional<World> optWorld = Sponge.getServer().getWorld(UUID.fromString(spawnObj.get("world").getAsString()));
				if (optWorld.isPresent())
				{
					nation.addSpawn(e.getKey(), optWorld.get().getLocation(spawnObj.get("x").getAsDouble(), spawnObj.get("y").getAsDouble(), spawnObj.get("z").getAsDouble()));
				}
			}
		}

		if (!isAdmin)
		{
			nation.setPresident(UUID.fromString(obj.get("president").getAsString()));
			for (JsonElement element : obj.get("ministers").getAsJsonArray())
			{
				nation.addMinister(UUID.fromString(element.getAsString()));
			}
			for (JsonElement element : obj.get("citizens").getAsJsonArray())
			{
				nation.addCitizen(UUID.fromString(element.getAsString()));
			}
			if (obj.has("taxes"))
				nation.setTaxes(obj.get("taxes").getAsDouble());
			if (obj.has("extras"))
				nation.setExtras(obj.get("extras").getAsInt());
			if (obj.has("extraspawns"))
				nation.setExtraSpawns(obj.get("extraspawns").getAsInt());
		}
		return nation;
	}
}
