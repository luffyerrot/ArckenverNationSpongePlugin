package com.arckenver.nations.object;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.UUID;

public class Cube
{
	private UUID world;
	private int minX;
	private int maxX;
	private int minY;
	private int maxY;
	private int minZ;
	private int maxZ;
	private boolean useless = false;

	public Cube(UUID world, Vector3i point)
	{
		this(world, point.getX(), point.getX(), point.getY(), point.getY(), point.getZ(), point.getZ());
	}

	public Cube(PointCube a, PointCube b)
	{
		this(a.getWorld().getUniqueId(), Math.min(a.getX(), b.getX()), Math.max(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.max(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()), Math.max(a.getZ(), b.getZ()));
	}

	public Cube(UUID world, int minX, int maxX, int minY, int maxY, int minZ, int maxZ)
	{
		this.world = world;
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
		this.minZ = minZ;
		this.maxZ = maxZ;
	}

	public UUID getWorld()
	{
		return world;
	}

	public void setWorld(UUID world)
	{
		this.world = world;
	}

	public int getMinX()
	{
		return minX;
	}

	public void setMinX(int minX)
	{
		this.minX = minX;
	}

	public int getMaxX()
	{
		return maxX;
	}

	public void setMaxX(int maxX)
	{
		this.maxX = maxX;
	}

	public int getMinY()
	{
		return minY;
	}

	public void setMinY(int minY)
	{
		this.minY = minY;
	}

	public int getMaxY()
	{
		return maxY;
	}

	public void setMaxY(int maxY)
	{
		this.maxY = maxY;
	}

	public int getMinZ()
	{
		return minZ;
	}

	public void setMinZ(int minZ)
	{
		this.minZ = minZ;
	}

	public int getMaxZ()
	{
		return maxZ;
	}

	public void setMaxZ(int maxZ)
	{
		this.maxZ = maxZ;
	}

	public int width()
	{
		return maxX - minX + 1;
	}
	
	public int height()
	{
		return maxY - minY + 1;
	}

	public int lenght()
	{ 
		return maxZ - minZ + 1; 
	}
	
	public int size()
	{
		return width()*height()*lenght();
	}
	

	public boolean useless() {
		return useless;
	}
	
	public boolean isInside(Vector3i point)
	{
		return minX <= point.getX() && point.getX() <= maxX && minY <= point.getY() && point.getY() <= maxY && minZ <= point.getZ() && point.getZ() <= maxZ;
	}

	public boolean isInside(Location<World> loc)
	{
		return loc.getExtent().getUniqueId().equals(world) && isInside(new Vector3i(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
	}
	
	public boolean intersects(Cube cube)
	{
		return this.minX <= cube.maxX && cube.minX <= this.maxX && this.minY <= cube.maxY && cube.minY <= this.maxY && this.minZ <= cube.maxZ && cube.minZ <= this.maxZ;
	}

	public ArrayList<Vector3i> pointsInside(Cube cube)
	{
		ArrayList<Vector3i> points = new ArrayList<Vector3i>();
		Vector3i A = new Vector3i(minX, minY, minZ);
		if (cube.isInside(A))
			points.add(A);
		Vector3i B = new Vector3i(minX, minY, maxZ);
		if (cube.isInside(B))
			points.add(B);
		Vector3i C = new Vector3i(minX, maxY, minZ);
		if (cube.isInside(C))
			points.add(C);
		Vector3i D = new Vector3i(minX, maxY, maxZ);
		if (cube.isInside(D))
			points.add(D);
		Vector3i E = new Vector3i(maxX, minY, minZ);
		if (cube.isInside(E))
			points.add(E);
		Vector3i F = new Vector3i(maxX, minY, maxZ);
		if (cube.isInside(F))
			points.add(F);
		Vector3i G = new Vector3i(maxX, maxY, minZ);
		if (cube.isInside(G))
			points.add(G);
		Vector3i H = new Vector3i(maxX, maxY, maxZ);
		if (cube.isInside(H))
			points.add(H);
		return points;
	}

	public float distance(Vector3i point)
	{
		if (isInside(point))
		{
			return 0;
		}
		return Math.min(
				Math.min(
						Math.min(
								point.distance(new Vector3i(minX, minY, minZ)),
								point.distance(new Vector3i(minX, minY, maxZ))),
						Math.min(
								point.distance(new Vector3i(minX, maxY, minZ)),
								point.distance(new Vector3i(minX, maxY, maxZ)))),
				Math.min(
						Math.min(
								point.distance(new Vector3i(maxX, minY, minZ)),
								point.distance(new Vector3i(maxX, minY, maxZ))),
						Math.min(
								point.distance(new Vector3i(maxX, maxY, minZ)),
								point.distance(new Vector3i(maxX, maxY, maxZ)))));
	}

	public boolean isAdjacent(Cube cube)
	{
		return (new Cube(world, minX - 1, maxX + 1, minY - 1, maxY + 1, minZ - 1, maxZ + 1)).intersects(cube);
	}

	public boolean isAdjacent(Vector3i point)
	{
		return this.minX - 1 <= point.getX() && point.getX() <= this.maxX + 1 && this.minY - 1 <= point.getY() && point.getY() <= this.maxY + 1 && this.minZ - 1 <= point.getZ() && point.getZ() <= this.maxZ + 1;
	}
	
	public ArrayList<Cube> cutBy(Cube cube)
	{
		ArrayList<Cube> result = new ArrayList<Cube>();
		ArrayList<Vector3i> points = this.pointsInside(cube);
		if (points.size() == 8)
		{
			return result;
		}
		else if (points.size() == 4 || points.size() == 6 || points.size() == 2)
		{
			Cube r = new Cube(world, minX, maxX, minY, maxY, minZ, maxZ);
			int x0 = points.get(0).getX();
			int y0 = points.get(0).getY();
			int z0 = points.get(0).getZ();
			int x1 = points.get(1).getX();
			int y1 = points.get(1).getY();
			int z1 = points.get(1).getZ();

			if (x0 == minX && x1 == minX && ((y0 == minY && y1 == maxY) || (y0 == maxY && y1 == minY)) && ((z0 == minZ && z1 == maxZ) || (z0 == maxZ && z1 == minZ)))
			{
				r.setMinX(cube.getMaxX() + 1);
			}
			else if (x0 == maxX && x1 == maxX && ((y0 == minY && y1 == maxY) || (y0 == maxY && y1 == minY)) && ((z0 == minZ && z1 == maxZ) || (z0 == maxZ && z1 == minZ)))
			{
				r.setMaxX(cube.getMinX() - 1);
			}
			else if (y0 == minY && y1 == minY && ((x0 == minX && x1 == maxX) || (x0 == maxX && x1 == minX)) && ((z0 == minZ && z1 == maxZ) || (z0 == maxZ && z1 == minZ)))
			{
				r.setMinY(cube.getMaxY() + 1);
			}
			else if (y0 == maxY && y1 == maxY && ((x0 == minX && x1 == maxX) || (x0 == maxX && x1 == minX)) && ((z0 == minZ && z1 == maxZ) || (z0 == maxZ && z1 == minZ)))
			{
				r.setMaxY(cube.getMinY() - 1);
			}
			else if (z0 == minZ && z1 == minZ && ((x0 == minX && x1 == maxX) || (x0 == maxX && x1 == minX)) && ((y0 == minY && y1 == maxY) || (y0 == maxY && y1 == minY)))
			{
				r.setMinZ(cube.getMaxZ() + 1);
			}
			else if (z0 == maxZ && z1 == maxZ && ((x0 == minX && x1 == maxX) || (x0 == maxX && x1 == minX)) && ((y0 == minY && y1 == maxY) || (y0 == maxY && y1 == minY)))
			{
				r.setMaxZ(cube.getMinZ() - 1);
			}
			result.add(r);
		}
		else if (points.size() == 1)
		{
            if (points.get(0).getX() == minX && points.get(0).getY() == minY && points.get(0).getZ() == minZ)
            {
                result.add(new Cube(world, cube.getMaxX() + 1, maxX, minY, cube.getMaxY(), minZ, cube.getMaxZ()));
                result.add(new Cube(world, minX, maxX, cube.getMaxY() + 1, maxY, minZ, maxZ));
                result.add(new Cube(world, minX, maxX, minY, maxY, cube.getMaxZ() + 1, maxZ));
            }
            else if (points.get(0).getX() == minX && points.get(0).getY() == minY && points.get(0).getZ() == maxZ)
            {
                result.add(new Cube(world, cube.getMaxX() + 1, maxX, minY, cube.getMaxY(), cube.getMinZ(), maxZ));
                result.add(new Cube(world, minX, maxX, cube.getMaxY() + 1, maxY, minZ, maxZ));
                result.add(new Cube(world, minX, maxX, minY, maxY, minZ, cube.getMinZ() - 1));
            }
			else if (points.get(0).getX() == minX && points.get(0).getY() == maxY && points.get(0).getZ() == minZ)
			{
				result.add(new Cube(world, minX, cube.getMaxX(), minY, cube.getMinY() - 1, minZ, cube.getMaxZ()));
				result.add(new Cube(world, cube.getMaxX() + 1, maxX, minY, maxY, minZ, maxZ));
                result.add(new Cube(world, minX, maxX, minY, maxY, cube.getMaxZ() + 1, maxZ));
			}
            else if (points.get(0).getX() == minX && points.get(0).getY() == maxY && points.get(0).getZ() == maxZ)
            {
                result.add(new Cube(world, minX, cube.getMaxX(), minY, cube.getMinY() - 1, cube.getMinZ(), maxZ));
                result.add(new Cube(world, cube.getMaxX() + 1, maxX, minY, maxY, minZ, maxZ));
                result.add(new Cube(world, minX, maxX, minY, maxY, minZ, cube.getMinZ() - 1));
            }
			else if (points.get(0).getX() == maxX && points.get(0).getY() == minY && points.get(0).getZ() == minZ)
            {
                result.add(new Cube(world, minX, cube.getMinX() - 1, minZ, cube.getMaxY(), minZ, cube.getMaxZ()));
                result.add(new Cube(world, minX, maxX, cube.getMaxY() + 1, maxY, minZ, maxZ));
                result.add(new Cube(world, minX, maxX, minY, maxY, cube.getMaxZ() + 1, maxZ));
            }
            else if (points.get(0).getX() == maxX && points.get(0).getY() == minY && points.get(0).getZ() == maxZ)
            {
                result.add(new Cube(world, minX, cube.getMinX() - 1, minZ, cube.getMaxY(), cube.getMinZ(), maxZ));
                result.add(new Cube(world, minX, maxX, cube.getMaxY() + 1, maxY, minZ, maxZ));
                result.add(new Cube(world, minX, maxX, minY, maxY, minZ, cube.getMinZ() - 1));
            }
            else if (points.get(0).getX() == maxX && points.get(0).getY() == maxY && points.get(0).getZ() == minZ)
            {
                result.add(new Cube(world, minX, cube.getMinX() - 1, minZ, cube.getMaxY(), minZ, cube.getMaxZ()));
                result.add(new Cube(world, minX, maxX, minY, cube.getMinY() - 1, minZ, maxZ));
                result.add(new Cube(world, minX, maxX, minY, maxY, minZ, cube.getMinZ() + 1));
            }
			else if (points.get(0).getX() == maxX && points.get(0).getY() == maxY && points.get(0).getZ() == maxZ)
			{
				result.add(new Cube(world, cube.getMinX(), maxX, minY, cube.getMinY() - 1, cube.getMinZ(), maxZ));
				result.add(new Cube(world, minX, cube.getMinX() - 1, minY, maxY, minZ, maxZ));
                result.add(new Cube(world, minX, maxX, minY, maxY, minZ, cube.getMinZ() - 1));
			}
		}
		else if (points.size() == 0)
		{
			if (!intersects(cube))
			{
				result.add(new Cube(world, minX, maxX, minY, maxY, minZ, maxZ));
			}
			else if (cube.getMinX() > minX && cube.getMaxX() < maxX && cube.getMinY() > minY && cube.getMaxY() < maxY && cube.getMinZ() > minZ && cube.getMaxZ() < maxZ) {
				result.add(new Cube(world, minX, maxX, minY, maxY, minZ, maxZ));
                cube.useless = true;
			}
			else if (cube.getMinX() > minX && cube.getMaxX() < maxX)
			{
				result.add(new Cube(world, minX, cube.getMinX() - 1, minY, maxY, minZ, maxZ));
				result.add(new Cube(world, cube.getMaxX() + 1, maxX, minY, maxY, minZ, maxZ));
				if (cube.getMinY() <= minY)
				{
					if (cube.getMaxY() < maxY)
                        {
						result.add(new Cube(world, cube.getMinX(), cube.getMaxX(), cube.getMaxY() + 1, maxY, minZ, maxZ));
					}
				}
				else
				{
					result.add(new Cube(world, cube.getMinX(), cube.getMaxX(), minY, cube.getMinY() - 1, minZ, maxZ));
					if (cube.getMaxY() < maxY)
					{
						result.add(new Cube(world, cube.getMinX(), cube.getMaxX(), cube.getMaxY() + 1, maxY, minZ, maxZ));
					}
				}
                if (cube.getMinZ() <= minZ)
                {
                    if (cube.getMaxZ() < maxZ)
                    {
                        result.add(new Cube(world, cube.getMinX(), cube.getMaxX(), minY, maxY, cube.getMaxZ() + 1, maxZ));
                    }
                }
                else
                {
                    result.add(new Cube(world, cube.getMinX(), cube.getMaxX(), minY, maxY, minZ, cube.getMinZ() - 1));
                    if (cube.getMaxY() < maxY)
                    {
                        result.add(new Cube(world, cube.getMinX(), cube.getMaxX(), minY, maxY, cube.getMaxZ() + 1, maxZ));
                    }
                }
			}
			else if (cube.getMinY() > minY && cube.getMaxY() < maxY)
			{
				result.add(new Cube(world, minX, maxX, minY, cube.getMinY() - 1, minZ, maxZ));
				result.add(new Cube(world, minX, maxX, cube.getMaxY() + 1, maxY, minZ, maxZ));
				if (cube.getMinX() <= minX)
				{
					if (cube.getMaxX() < maxX)
					{
						result.add(new Cube(world, cube.getMaxX() + 1, maxX, cube.getMinY(), cube.getMaxY(), minZ, maxZ));
					}
				}
				else
				{
					result.add(new Cube(world, minX, cube.getMinX() - 1, cube.getMinY(), cube.getMaxY(), minZ, maxZ));
					if (cube.getMaxY() < maxY)
					{
						result.add(new Cube(world, cube.getMaxX() + 1, maxX, cube.getMinY(), cube.getMaxY(), minZ, maxZ));
					}
				}
                if (cube.getMinZ() <= minZ)
                {
                    if (cube.getMaxZ() < maxZ)
                    {
                        result.add(new Cube(world, cube.getMinX(), cube.getMaxX(), minY, maxY, cube.getMaxZ() + 1, maxZ));
                    }
                }
                else
                {
                    result.add(new Cube(world, cube.getMinX(), cube.getMaxX(), minY, maxY, minZ, cube.getMinZ() - 1));
                    if (cube.getMaxY() < maxY)
                    {
                        result.add(new Cube(world, cube.getMinX(), cube.getMaxX(), minY, maxY, cube.getMaxZ() + 1, maxZ));
                    }
                }
            }
		}
        else if (cube.getMinZ() > minZ && cube.getMaxZ() < maxZ)
        {
            result.add(new Cube(world, minX, maxX, minY, maxY, minZ, cube.getMinZ() - 1));
            result.add(new Cube(world, minX, maxX, minY, maxY, cube.getMaxZ() + 1, maxZ));
            if (cube.getMinX() <= minX)
            {
                if (cube.getMaxX() < maxX)
                {
                    result.add(new Cube(world, cube.getMaxX() + 1, maxX, cube.getMinY(), cube.getMaxY(), minZ, maxZ));
                }
            }
            else
            {
                result.add(new Cube(world, minX, cube.getMinX() - 1, cube.getMinY(), cube.getMaxY(), minZ, maxZ));
                if (cube.getMaxY() < maxY)
                {
                    result.add(new Cube(world, cube.getMaxX() + 1, maxX, cube.getMinY(), cube.getMaxY(), minZ, maxZ));
                }
            }
            if (cube.getMinY() <= minY)
            {
                if (cube.getMaxY() < maxY)
                {
                    result.add(new Cube(world, cube.getMinX(), cube.getMaxX(), cube.getMaxY() + 1, maxY, minZ, maxZ));
                }
            }
            else
            {
                result.add(new Cube(world, cube.getMinX(), cube.getMaxX(), minY, cube.getMinY() - 1, minZ, maxZ));
                if (cube.getMaxY() < maxY)
                {
                    result.add(new Cube(world, cube.getMinX(), cube.getMaxX(), cube.getMaxY() + 1, maxY, minZ, maxZ));
                }
            }
        }
		return result;
	}
}
