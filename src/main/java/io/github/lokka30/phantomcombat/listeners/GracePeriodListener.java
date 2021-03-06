package io.github.lokka30.phantomcombat.listeners;

import io.github.lokka30.phantomcombat.PhantomCombat;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

public class GracePeriodListener implements Listener {

    public HashMap<UUID, Instant> graceMap = new HashMap<>();
    private PhantomCombat instance;

    public GracePeriodListener(final PhantomCombat instance) {
        this.instance = instance;
    }

    public void startGracePeriod(final Player p) {
        final int time = instance.settings.getInt("grace-period.time");
        final UUID uuid = p.getUniqueId();

        graceMap.put(uuid, Instant.now());

        graceMessage(p, instance.colorize(instance.messages.getString("grace-period.started").replaceAll("%time%", String.valueOf(time))));

        //Is the scheduler enabled? Start a timer to remove their grace period.
        if (instance.settings.getBoolean("grace-period.scheduler")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    graceMap.remove(uuid);

                    //Check if they're online, in case they log off during the period.
                    if (p.isOnline()) {
                        graceMessage(p, instance.colorize(instance.messages.getString("grace-period.ended")));
                    }
                }
            }.runTaskLater(instance, 20 * time);
        }
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        if (!p.hasPlayedBefore() && instance.settings.getBoolean("grace-period.enable")) {
            startGracePeriod(p);
        }
    }

    public void graceMessage(final Player p, final String msg) {
        if (instance.settings.getBoolean("grace-period.communication.chat")) {
            p.sendMessage(instance.colorize(msg));
        }
        if (instance.settings.getBoolean("grace-period.communication.action-bar")) {
            instance.actionBar(p, instance.colorize(msg));
        }
    }

    @EventHandler
    public void onDamageByEntity(final EntityDamageByEntityEvent e) {
        if (instance.settings.getBoolean("grace-period.enable") && !e.isCancelled()) {
            if (!e.isCancelled() && e.getEntity() instanceof Player) {
                final Player defender = (Player) e.getEntity();

                if (e.getDamager() instanceof Player) {
                    final Player attacker = (Player) e.getDamager();

                    if (checkGrace(defender)) {
                        e.setCancelled(true);
                        graceMessage(attacker, instance.colorize(instance.messages.getString("grace-period.target-protected").replaceAll("%target%", defender.getName())));
                    } else {
                        if (checkGrace(attacker)) {
                            if (!(instance.hasWorldGuard && (instance.worldGuardUtil.isPVPDenied(attacker) || instance.worldGuardUtil.isPVPDenied(defender)))) {
                                graceMessage(defender, instance.colorize(instance.messages.getString("grace-period.target-broke-period").replaceAll("%target%", attacker.getName())));
                                graceMessage(attacker, instance.colorize(instance.messages.getString("grace-period.broke-period")));
                                graceMap.remove(attacker.getUniqueId());
                            }
                        }
                    }
                } else if ((e.getDamager() instanceof Projectile) && (((Projectile) e.getDamager()).getShooter() instanceof Player)) {
                    Projectile projectile = (Projectile) e.getDamager();
                    Player attacker = (Player) projectile.getShooter();

                    if (attacker != null) {
                        if (checkGrace(defender)) {
                            e.setCancelled(true);
                            graceMessage(attacker, instance.colorize(instance.messages.getString("grace-period.target-protected").replaceAll("%target%", defender.getName())));
                        } else {
                            if (checkGrace(attacker)) {
                                if (!(instance.hasWorldGuard && (instance.worldGuardUtil.isPVPDenied(attacker) || instance.worldGuardUtil.isPVPDenied(defender)))) {
                                    graceMessage(defender, instance.colorize(instance.messages.getString("grace-period.target-broke-period").replaceAll("%target%", attacker.getName())));
                                    graceMessage(attacker, instance.colorize(instance.messages.getString("grace-period.broke-period")));
                                    graceMap.remove(attacker.getUniqueId());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean checkGrace(final Player p) {
        final UUID uuid = p.getUniqueId();
        if (graceMap.containsKey(uuid)) {
            if (instance.settings.getBoolean("grace-period.scheduler")) {
                //The scheduler hasn't removed them yet. They're still in the grace period.
                return true;
            } else {
                //Check the duration of when the period started to now, if it's equal to or
                //more than the settings specifies, end it.
                final Instant start = graceMap.get(uuid);
                final Instant now = Instant.now();

                final Duration duration = Duration.between(start, now);
                if (duration.getSeconds() >= instance.settings.getInt("grace-period.time")) {
                    graceMessage(p, instance.messages.getString("grace-period.ended"));
                    return false;
                } else {
                    return true;
                }
            }
        } else {
            return false;
        }
    }
}
