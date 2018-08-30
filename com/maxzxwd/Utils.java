package com.maxzxwd;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class Utils {
    public static Class<?> craftPlayerClass = null;
    public static Class<?> craftWorldClass = null;
    public static Class<?> entityRabbitClass = null;
    public static Class<?> packetPlayOutMountClass = null;
    public static Class<?> packetPlayOutSpawnEntityLivingClass = null;
    public static Class<?> worldServerClass = null;
    public static Class<?> entityPlayerClass = null;
    public static Class<?> playerConnectionClass = null;

    public static Constructor<?> entityRabbitConstructor = null;
    public static Constructor<?> packetPlayOutSpawnEntityLivingConstructor = null;

    public static Method craftPlayerGetHandler = null;
    public static Method craftWorldGetHandler = null;
    public static Method entitySetLocation = null;
    public static Method entitySetInvisible = null;
    public static Method entityGetId = null;
    public static Method sendPacket = null;

    private Utils() {
    }

    public static void init() {
        try {
            String netMinecraftServer = "net.minecraft.server." + getBukkitVersion() + '.';
            String orgBukkitCraftbukkit = "org.bukkit.craftbukkit." + getBukkitVersion() + '.';

            craftPlayerClass = Class.forName(orgBukkitCraftbukkit+ "entity.CraftPlayer");
            craftWorldClass = Class.forName(orgBukkitCraftbukkit + "CraftWorld");

            entityPlayerClass = Class.forName(netMinecraftServer + "EntityPlayer");
            entityRabbitClass = Class.forName(netMinecraftServer + "EntityRabbit");
            packetPlayOutMountClass = Class.forName(netMinecraftServer + "PacketPlayOutMount");
            packetPlayOutSpawnEntityLivingClass = Class.forName(netMinecraftServer + "PacketPlayOutSpawnEntityLiving");
            worldServerClass = Class.forName(netMinecraftServer + "WorldServer");
            playerConnectionClass = Class.forName(netMinecraftServer + "PlayerConnection");

            entityRabbitConstructor = entityRabbitClass.getDeclaredConstructor(Class.forName(netMinecraftServer + "World"));
            packetPlayOutSpawnEntityLivingConstructor =
                packetPlayOutSpawnEntityLivingClass.getDeclaredConstructor(Class.forName(netMinecraftServer + "EntityLiving"));

            craftPlayerGetHandler = craftPlayerClass.getDeclaredMethod("getHandle");
            craftWorldGetHandler = craftWorldClass.getDeclaredMethod("getHandle");
            entitySetLocation = entityRabbitClass.getMethod("setLocation", double.class, double.class, double.class, float.class, float.class);
            entitySetInvisible = entityRabbitClass.getMethod("setInvisible", boolean.class);
            entitySetInvisible = entityRabbitClass.getMethod("setInvisible", boolean.class);
            entityGetId = entityRabbitClass.getMethod("getId");
            sendPacket = playerConnectionClass.getDeclaredMethod("sendPacket", Class.forName(netMinecraftServer + "Packet"));
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static String getBukkitVersion() {
        String temp = Bukkit.getServer().getClass().getPackage().getName();
        return temp.substring(temp.lastIndexOf('.') + 1);
    }

    public static Integer fixPlayerPassenger(Player p) {
        if (p.getPassenger() != null) {

            try {
                Object entityPlayer = craftPlayerGetHandler.invoke(craftPlayerClass.cast(p));

                Object worldServer = craftWorldGetHandler.invoke(craftWorldClass.cast(p.getLocation().getWorld()));
                Object entity = entityRabbitConstructor.newInstance(worldServer);

                Location location = p.getLocation();
                entitySetLocation.invoke(entity, location.getX() + 2, location.getY(), location.getZ() + 2, location.getYaw(), location.getPitch());
                entitySetInvisible.invoke(entity, true);

                Object packet1 = packetPlayOutSpawnEntityLivingConstructor.newInstance(entity);
                Object packet2 = packetPlayOutMountClass.getConstructor().newInstance();
                Object packet3 = packetPlayOutMountClass.getConstructor().newInstance();

                Field entityIdField = packetPlayOutMountClass.getDeclaredField("a");
                Field passengerIdsField = packetPlayOutMountClass.getDeclaredField("b");

                entityIdField.setAccessible(true);
                passengerIdsField.setAccessible(true);

                int id = (int) entityGetId.invoke(entity);

                entityIdField.setInt(packet2, (int) entityGetId.invoke(entityPlayer));
                passengerIdsField.set(packet2, new int[] { id });

                entityIdField.setInt(packet3, (int) entityGetId.invoke(entity));
                passengerIdsField.set(packet3, new int[] { p.getPassenger().getEntityId() });

                sendPacket.invoke(entityPlayerClass.getDeclaredField("playerConnection").get(entityPlayer), packet1);
                sendPacket.invoke(entityPlayerClass.getDeclaredField("playerConnection").get(entityPlayer), packet2);
                sendPacket.invoke(entityPlayerClass.getDeclaredField("playerConnection").get(entityPlayer), packet3);

                return id;
            } catch (InvocationTargetException | IllegalAccessException | InstantiationException | NoSuchFieldException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static void sendUnBindEntity(Integer i) {
        if (i == null) {
            return;
        }
        for(Player player : Bukkit.getServer().getOnlinePlayers()) {
            try {
                Object entityPlayer = craftPlayerGetHandler.invoke(craftPlayerClass.cast(player));

                Field entityIdField = packetPlayOutMountClass.getDeclaredField("a");
                Field passengerIdsField = packetPlayOutMountClass.getDeclaredField("b");

                entityIdField.setAccessible(true);
                passengerIdsField.setAccessible(true);
                Object packet1 = packetPlayOutMountClass.getConstructor().newInstance();
                entityIdField.setInt(packet1, i);
                passengerIdsField.set(packet1, new int[0]);
                sendPacket.invoke(entityPlayerClass.getDeclaredField("playerConnection").get(entityPlayer), packet1);
            } catch (InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
    }
}
