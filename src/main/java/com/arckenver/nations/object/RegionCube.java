package com.arckenver.nations.object;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.UUID;

public class RegionCube {
    private ArrayList<Cube> cubes;
    private Hashtable<UUID, Cube> extrema;

    public RegionCube()
    {
        this(new ArrayList<Cube>());
    }

    public RegionCube(ArrayList<Cube> cubes)
    {
        this.cubes = cubes;
        this.extrema = new Hashtable<UUID, Cube>();
        calculateExtr();
    }

    private void calculateExtr()
    {
        for (Cube c : cubes)
        {
            if (!extrema.containsKey(c.getWorld()))
            {
                extrema.put(c.getWorld(), new Cube(c.getWorld(), Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE));
            }
            Cube cube = extrema.get(c.getWorld());
            if (c.getMaxX() > cube.getMaxX()) cube.setMaxX(c.getMaxX());
            if (c.getMinX() < cube.getMinX()) cube.setMinX(c.getMinX());
            if (c.getMaxY() > cube.getMaxY()) cube.setMaxY(c.getMaxY());
            if (c.getMinY() < cube.getMinY()) cube.setMinY(c.getMinY());
            if (c.getMaxZ() > cube.getMaxZ()) cube.setMaxZ(c.getMaxZ());
            if (c.getMinZ() < cube.getMinZ()) cube.setMinZ(c.getMinZ());
        }
    }

    public ArrayList<Cube> getCubes()
    {
        return cubes;
    }

    public void addCube(Cube cube)
    {
        ArrayList<Cube> toAdd = new ArrayList<Cube>();
        toAdd.add(cube);
        for (Iterator<Cube> it = cubes.iterator(); it.hasNext();)
        {
            Cube c = it.next();
            ArrayList<Cube> futureToAdd = new ArrayList<Cube>();
            for (Cube cubeToAdd : toAdd)
            {
                futureToAdd.addAll(cubeToAdd.cutBy(c));
            }
            if (c.useless())
                it.remove();
            toAdd = futureToAdd;
        }
        cubes.addAll(toAdd);
        calculateExtr();
    }

    public void removeCube(Cube cube)
    {
        ArrayList<Cube> futureCubes = new ArrayList<Cube>();
        for (Cube c : cubes)
        {
            futureCubes.addAll(c.cutBy(cube));
        }
        cubes = futureCubes;
        calculateExtr();
    }

    public int size()
    {
        int s = 0;
        for (Cube c : cubes)
        {
            s += c.size();
        }
        return s;
    }

    public boolean isInside(Location<World> loc)
    {
        Cube extr = extrema.get(loc.getExtent().getUniqueId());
        Vector3i p = new Vector3i(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        if (p.getX() < extr.getMinX() || p.getX() > extr.getMaxX() || p.getY() < extr.getMinY() || p.getY() > extr.getMaxY() || p.getZ() < extr.getMinZ() || p.getZ() > extr.getMaxZ())
        {
            return false;
        }
        for (Cube c : cubes)
        {
            if (c.isInside(p))
            {
                return true;
            }
        }
        return false;
    }

    public boolean isInside(Cube cube)
    {
        Cube extr = extrema.get(cube.getWorld());
        if (cube.getMaxX() < extr.getMinX() || cube.getMinX() > extr.getMaxX() || cube.getMaxY() < extr.getMinY() || cube.getMinY() > extr.getMaxY() || cube.getMaxZ() < extr.getMinZ() || cube.getMinZ() > extr.getMaxZ())
        {
            return false;
        }
        ArrayList<Cube> cut = new ArrayList<Cube>();
        cut.add(cube);
        for (Cube c : cubes)
        {
            ArrayList<Cube> newCut = new ArrayList<Cube>();
            for (Cube cu : cut)
            {
                newCut.addAll(cu.cutBy(c));
            }
            cut = newCut;
        }
        return cut.isEmpty();
    }

    public boolean isAdjacent(Vector3i point)
    {
        for (Cube c : cubes)
        {
            if (c.isAdjacent(point))
            {
                return true;
            }
        }
        return false;
    }

    public boolean isAdjacent(Cube cube)
    {
        for (Cube c : cubes)
        {
            if (c.isAdjacent(cube))
            {
                return true;
            }
        }
        return false;
    }

    public boolean intersects(Cube cube)
    {
        for (Cube c : cubes)
        {
            if (c.intersects(cube))
            {
                return true;
            }
        }
        return false;
    }

    public float distance(Location<World> loc)
    {
        float dist = Float.MAX_VALUE;
        for (Cube c : cubes)
        {
            if (loc.getExtent().getUniqueId().equals(c.getWorld()))
            {
                dist = Math.min(dist, c.distance(new Vector3i(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())));
            }
        }
        return dist;
    }

    public RegionCube copy()
    {
        ArrayList<Cube> rectsCopy = new ArrayList<Cube>();
        for (Cube c : cubes)
        {
            rectsCopy.add(new Cube(c.getWorld(), c.getMinX(), c.getMaxX(), c.getMinY(), c.getMaxY(), c.getMinZ(), c.getMaxZ()));
        }
        return new RegionCube(rectsCopy);
    }
}
