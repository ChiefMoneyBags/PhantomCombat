package io.github.lokka30.phantomcombat.listeners;

import io.github.lokka30.phantomcombat.PhantomCombat;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Collections;

public class BloodListener implements Listener {

    private PhantomCombat instance;

    public BloodListener(final PhantomCombat instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onEntityDamage(final EntityDamageEvent e) {
        if (instance.settings.get("blood.enable", true) && !e.isCancelled() && e.getDamage() != 0.00 && e.getEntity() instanceof LivingEntity) {
            final LivingEntity livingEntity = (LivingEntity) e.getEntity();
            if (instance.settings.get("blood.enabled-worlds", Collections.singletonList("world")).contains(livingEntity.getWorld().getName())) {
                if (livingEntity instanceof Player) {
                    if (instance.settings.get("blood.players-bleed", true)) {
                        bloodParticles(livingEntity, e.getCause());
                    }
                } else {
                    if (instance.settings.get("blood.entities-bleed", true)) {
                        for (String blockedEntityType : instance.settings.get("blood.blacklisted-entities", Collections.singletonList("ARMOR_STAND"))) {
                            if (livingEntity.getType().name().equalsIgnoreCase(blockedEntityType)) {
                                return;
                            }
                        }
                        bloodParticles(livingEntity, e.getCause());
                    }
                }
            }
        }
    }

    public void bloodParticles(final LivingEntity livingEntity, final EntityDamageEvent.DamageCause cause) {
        switch (cause) {
            case FALL:
                if (!instance.settings.get("blood.cause.fall", true)) {
                    return;
                }
                break;
            case DROWNING:
                if (!instance.settings.get("blood.cause.drowning", false)) {
                    return;
                }
                break;
            case STARVATION:
                if (!instance.settings.get("blood.cause.starvation", false)) {
                    return;
                }
                break;
            case SUFFOCATION:
                if (!instance.settings.get("blood.cause.suffocation", false)) {
                    return;
                }
                break;
            case CONTACT:
                if (!instance.settings.get("blood.cause.contact", true)) {
                    return;
                }
                break;
            case FIRE:
                if (!instance.settings.get("blood.cause.fire", false)) {
                    return;
                }
            default:
                break;
        }

        if (!(instance.hasWorldGuard && instance.worldGuardUtil.isPVPDenied(livingEntity))) {
            livingEntity.getWorld().playEffect(livingEntity.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
        }
    }
}
