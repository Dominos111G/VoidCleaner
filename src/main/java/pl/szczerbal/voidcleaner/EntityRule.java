package pl.szczerbal.voidcleaner;

import org.bukkit.entity.*;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Reprezentuje jeden wpis z listy default-clean / high-clean / blacklist.
 *
 * Format wpisu w config.yml:
 *   "minecraft:oak_boat +empty"          -> puste lodki
 *   "minecraft:wolf !tamed +named"       -> dziki wilk z nametag
 *   "minecraft:minecart"                 -> wszystkie minecarty (bez warunkow)
 *   "minecraft:item"                     -> wszystkie lezace przedmioty
 *
 * Obslugiwane warunki (mozna laczyc wiele na jednym wpisie):
 *   +empty     / !empty     - encja nie ma pasazerow (lodki, minecarty)
 *   +tamed     / !tamed     - encja jest oswojona (Tameable: wilk, kot, kon...)
 *   +named     / !named     - encja ma wlasna nazwe (nametag)
 *   +adult     / !adult     - encja jest dorosla (Ageable)
 *   +baby      / !baby      - encja jest mloda (Ageable)
 *   +hostile   / !hostile   - encja jest wroga (Monster)
 *   +passive   / !passive   - encja jest pasywna (Animals)
 *   +on_ground / !on_ground - encja stoi na ziemi
 *
 * Przyklady:
 *   "minecraft:horse +empty +tamed +adult"
 *   "minecraft:zombie !named"
 */
public class EntityRule {

    private final String entityType;    // np. "minecraft:oak_boat"
    private final Set<String> required; // warunki ktore MUSZA byc spelnione  (+)
    private final Set<String> forbidden;// warunki ktore NIE MOGA byc spelnione (!)

    private EntityRule(String entityType, Set<String> required, Set<String> forbidden) {
        this.entityType = entityType;
        this.required   = required;
        this.forbidden  = forbidden;
    }

    /** Parsuje surowy string z configu, np. "minecraft:oak_boat +empty !named" */
    public static EntityRule parse(String raw) {
        String[] parts = raw.trim().split("\\s+");
        String type = parts[0].toLowerCase();
        Set<String> req  = new LinkedHashSet<>();
        Set<String> forb = new LinkedHashSet<>();
        for (int i = 1; i < parts.length; i++) {
            String token = parts[i].toLowerCase();
            if      (token.startsWith("+")) req.add(token.substring(1));
            else if (token.startsWith("!")) forb.add(token.substring(1));
        }
        return new EntityRule(type, req, forb);
    }

    /** Zwraca true jesli dana encja pasuje do tej reguly (typ + wszystkie warunki). */
    public boolean matches(Entity entity) {
        if (!matchesType(entity))                          return false;
        for (String c : required)  if (!eval(entity, c))  return false;
        for (String c : forbidden) if ( eval(entity, c))  return false;
        return true;
    }

    // -----------------------------------------------------------------------

    private boolean matchesType(Entity entity) {
        // Specjalny wildcard: "minecraft:item" = kazdy lezacy przedmiot (Item entity)
        if (entityType.equals("minecraft:item")) return entity instanceof Item;

        String mcId = entityType.replace("minecraft:", "");

        // FIX #1: Bukkit EntityType czesto rozni sie od Minecraft ID.
        // Pelna lista wyjatkow – domyslny mcId.toUpperCase() daje zly wynik dla:
        //   - minecartow (Bukkit odwraca kolejnosc czlonow: chest_minecart -> MINECART_CHEST)
        //   - rzuconych mikstur (potion -> SPLASH_POTION)
        //   - kilku innych encji
        String bukkitName = switch (mcId) {
            // Minecarty – Bukkit uzywa prefiksu MINECART_
            case "chest_minecart"   -> "MINECART_CHEST";
            case "hopper_minecart"  -> "MINECART_HOPPER";
            case "tnt_minecart"     -> "MINECART_TNT";
            case "furnace_minecart" -> "MINECART_FURNACE";
            case "command_block_minecart" -> "MINECART_COMMAND";
            // Pociski / rzucane przedmioty
            case "potion"            -> "SPLASH_POTION";
            case "experience_bottle" -> "THROWN_EXP_BOTTLE";
            // Pasywne moby z nieoczywistymi nazwami
            case "mooshroom"         -> "MUSHROOM_COW";
            case "snow_golem"        -> "SNOWMAN";
            // Dla wszystkich pozostalych: prosta zamiana _ na _ i upper case
            // np. oak_boat -> OAK_BOAT, zombified_piglin -> ZOMBIFIED_PIGLIN
            default                  -> mcId.toUpperCase();
        };
        return entity.getType().name().equals(bukkitName);
    }

    private boolean eval(Entity entity, String condition) {
        return switch (condition) {
            case "empty"     -> entity.getPassengers().isEmpty();
            case "tamed"     -> entity instanceof Tameable t && t.isTamed();
            case "named"     -> entity.getCustomName() != null;
            case "adult"     -> !(entity instanceof Ageable a) || a.isAdult();
            case "baby"      -> entity instanceof Ageable a && !a.isAdult();
            case "hostile"   -> entity instanceof Monster;
            case "passive"   -> entity instanceof Animals;
            case "on_ground" -> entity.isOnGround();
            // Nieznany warunek -> false, nie blokuje ani nie wymusza usuniecia
            default          -> false;
        };
    }

    public String getEntityType() { return entityType; }
}
