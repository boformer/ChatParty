package uk.co.drnaylor.chatparty.ess;

import com.earth2me.essentials.Essentials;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class EssentialsHook {
    
    private static Essentials essentials;
    
    private EssentialsHook() {}
    
    public static Essentials GetEssentials() {
        if (essentials != null) {
            return essentials;
        }
        
        Plugin pl = Bukkit.getPluginManager().getPlugin("Essentials");
        if (pl instanceof Essentials) {
            essentials = (Essentials) pl;
            return essentials;
        }
        
        return null;
    }
    
    public static void ClearEssentials() {
        essentials = null;
    }
    
    public static boolean isMuted(Player player) {
        if (GetEssentials() == null) {
            return false;
        }
        
        try {
            return essentials.getUser(player).isMuted();
        } catch (NullPointerException ex) {
            return false;
        }
    }
}
