package atomicstryker.battletowers.common;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class AS_TowerDestroyer {

    public Entity player;
    private final int xGolem;
    private final int yGolem;
    private final int zGolem;
    private final World world;
    private long triggerTime;
    private long lastExplosionSoundTime;
    private final int maxfloor = 6;
    private int floor = maxfloor;

    private boolean deleteMe = false;

    public AS_TowerDestroyer(World worldObj, ChunkCoordinates coords, long time, Entity golemkiller) {
        this.world = worldObj;
        this.player = golemkiller;
        this.xGolem = coords.posX;
        this.yGolem = coords.posY;
        this.zGolem = coords.posZ;
        this.triggerTime = time;
        this.lastExplosionSoundTime = time;

        world.playSoundEffect(xGolem, yGolem, zGolem, "towerbreakstart", 4F, 1.0F);
    }

    public void update() {
        if (deleteMe) {
            return;
        } else if (yCoord() < 70) {
            finishByDeletingSpawners();
            return;
        }

        float explosionPower = 10F;
        long initialExplosionDelay = 15000L;
        long perFloorExplosionDelay = 5000L;
        if (floor == maxfloor && System.currentTimeMillis() > triggerTime + initialExplosionDelay) {
            triggerTime = System.currentTimeMillis();

            // kaboom baby
            if (!world.isRemote) {
                world.createExplosion(player, xGolem, yCoord(), zGolem, explosionPower, true);
                cleanUpStragglerBlocks();
            }

            floor--;
        } else if (floor < maxfloor && System.currentTimeMillis() > triggerTime + perFloorExplosionDelay) // each floor
                                                                                                          // bursts 10
                                                                                                          // seconds
                                                                                                          // after that
        {
            if (floor < 1) {
                finishByDeletingSpawners();
                return;
            }
            triggerTime = System.currentTimeMillis();

            // kaboom baby
            if (!world.isRemote) {
                world.createExplosion(player, xGolem, yCoord(), zGolem, explosionPower, true);
                cleanUpStragglerBlocks();
            }

            floor--;
        } else {
            createSFX(randomTowerCoord(xGolem), (int) yCoord(), randomTowerCoord(zGolem));
        }
    }

    private void finishByDeletingSpawners() {
        deleteMe = true;

        if (AS_BattleTowersCore.instance.towerFallDestroysMobSpawners) {
            Block spawnerid = Blocks.mob_spawner;
            int minYdeletion = Math.max(yGolem - 80, 8);
            for (int xIterator = xGolem - 8; xIterator < xGolem + 8; xIterator++) // do each X
            {
                for (int zIterator = zGolem - 8; zIterator < zGolem + 8; zIterator++) // do each Z
                {
                    for (int yIterator = yGolem; yIterator >= minYdeletion; yIterator--) // go down the tower
                    {
                        if (world.getBlock(xIterator, yIterator, zIterator) == spawnerid) {
                            // destroy all present mobspawners
                            world.setBlock(xIterator, yIterator, zIterator, Blocks.air, 0, 3);
                        }
                    }
                }
            }
        }
    }

    public boolean isFinished() {
        return deleteMe;
    }

    private double yCoord() {
        int floorDistance = 7;
        return yGolem - (floorDistance * Math.abs(maxfloor - floor));
    }

    private int randomTowerCoord(int i) {
        return i - 7 + world.rand.nextInt(15);
    }

    private void cleanUpStragglerBlocks() {
        int ytemp = (int) yCoord();
        for (int xIterator = -8; xIterator < 8; xIterator++) // do each X
        {
            for (int zIterator = -8; zIterator < 8; zIterator++) // do each Z
            {
                for (int yIterator = 1; yIterator < 9; yIterator++) // do Y 8 blocks high
                {
                    if (world.getBlock(xGolem + xIterator, ytemp + yIterator, zGolem + zIterator) != Blocks.air) {
                        world.setBlock(xGolem + xIterator, ytemp + yIterator, zGolem + zIterator, Blocks.air, 0, 3);
                    }
                }
            }
        }
    }

    private void createSFX(int i, int j, int k) {
        if (System.currentTimeMillis() > lastExplosionSoundTime + 4000L) {
            switch (world.rand.nextInt(4)) {
                case 0: {
                    world.playSoundEffect(
                        i,
                        j,
                        k,
                        "random.fizz",
                        4F,
                        (1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F);
                    break;
                }
                case 1: {
                    world.playSoundEffect(
                        i,
                        j,
                        k,
                        "towercrumble",
                        4F,
                        (1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F);
                    break;
                }
            }
            lastExplosionSoundTime = System.currentTimeMillis();
        }

        double d = (float) i + world.rand.nextFloat();
        double d1 = (float) j + world.rand.nextFloat();
        double d2 = (float) k + world.rand.nextFloat();
        double d3 = d - i;
        double d4 = d1 - j;
        double d5 = d2 - k;
        double d6 = MathHelper.sqrt_double(d3 * d3 + d4 * d4 + d5 * d5);
        d3 /= d6;
        d4 /= d6;
        d5 /= d6;
        double d7 = 0.5D / (d6 / 1D + 0.10000000000000001D);
        d7 *= world.rand.nextFloat() * world.rand.nextFloat() + 0.3F;
        d3 *= d7;
        d4 *= d7;
        d5 *= d7;

        switch (world.rand.nextInt(4)) {
            case 0: {
                world.spawnParticle(
                    "explode",
                    (d + i * 1.0D) / 2D,
                    (d1 + j * 1.0D) / 2D,
                    (d2 + k * 1.0D) / 2D,
                    d3,
                    d4,
                    d5);
                break;
            }
            case 1: {
                world.spawnParticle("smoke", d, d1, d2, d3, d4, d5);
                break;
            }
            case 2: {
                world.spawnParticle("lava", d, d1, d2, 0.0D, 0.0D, 0.0D);
                break;
            }
            case 4: {
                world.spawnParticle(
                    "largesmoke",
                    (double) i + Math.random(),
                    (double) j + 1.2D,
                    (double) k + Math.random(),
                    0.0D,
                    0.0D,
                    0.0D);
                break;
            }
        }
    }
}
