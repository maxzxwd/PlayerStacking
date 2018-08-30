package com.maxzxwd;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.maxzxwd.textfilter.Dictonary;
import com.maxzxwd.textfilter.FilterResult;
import com.maxzxwd.textfilter.RuTextFilter;
import com.maxzxwd.textfilter.Text;
import com.maxzxwd.textfilter.TextFilter;
import com.maxzxwd.textfilter.dictionaries.RuDictonary;
import com.maxzxwd.textfilter.formatters.CapsFormatter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.spigotmc.event.entity.EntityDismountEvent;

public class Listeners implements Listener {
    Map<Player, Integer> clientSideEntityIds = new HashMap<>();

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Player && event.getRightClicked().getVehicle() == null &&
            event.getRightClicked().getPassenger() == null && event.getPlayer().getVehicle() == null) {
            Player entity = (Player) event.getRightClicked();

            Entity passenger = event.getPlayer();
            while (passenger.getPassenger() != null) {
                passenger = passenger.getPassenger();
            }

            passenger.setPassenger(entity);

            if (passenger instanceof Player) {
                clientSideEntityIds.put(entity, Utils.fixPlayerPassenger((Player) passenger));
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        e.getPlayer().leaveVehicle();
        Utils.sendUnBindEntity(clientSideEntityIds.remove(e.getPlayer()));
    }

    @EventHandler
    public void onEntityDismountEvent(EntityDismountEvent e) {
        if (e.getEntity() instanceof Player) {
            Utils.sendUnBindEntity(clientSideEntityIds.remove(e.getEntity()));
        }
    }
}
