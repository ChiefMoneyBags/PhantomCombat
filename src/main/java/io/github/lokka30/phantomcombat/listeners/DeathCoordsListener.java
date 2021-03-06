package io.github.lokka30.phantomcombat.listeners;

import io.github.lokka30.phantomcombat.PhantomCombat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class DeathCoordsListener implements Listener {
    //TODO remove poor LightningStorage usage below and in the next classes.
    private PhantomCombat instance;

    public DeathCoordsListener(final PhantomCombat instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onDeath(final PlayerDeathEvent e) {
        if (instance.settings.getBoolean("death-coords.enable")) {
            final Player p = e.getEntity();
            if (p.hasPermission("phantomcombat.death-coords")) {
                final String uuid = p.getUniqueId().toString();
                final String path = "death-coords." + uuid + ".";
                instance.data.set(path + "x", String.valueOf(p.getLocation().getBlockX()));
                instance.data.set(path + "y", String.valueOf(p.getLocation().getBlockY()));
                instance.data.set(path + "z", String.valueOf(p.getLocation().getBlockZ()));
                instance.data.set(path + "world", p.getWorld().getName());

                //In a runnable so it will send the message after all the other death messages.
                new BukkitRunnable() {
                    public void run() {
                        p.sendMessage(instance.colorize(instance.messages.getString("death-coords.on-death")));
                    }
                }.runTaskLater(instance, 5L);
            }
        }
    }
}
