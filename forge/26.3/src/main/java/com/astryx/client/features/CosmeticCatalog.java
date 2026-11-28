package com.astryx.client.features;

import java.util.*;

/** Stable public IDs; only these authored models can be shared by the wardrobe service. */
public final class CosmeticCatalog {
    public enum Slot {
        CLOAK("Cloaks"), WINGS("Wings"), HAT("Hats"), BANDANA("Bandanas"),
        BACKPACK("Backpacks"), MINI_ME("Mini-mes"), AURA("Auras & effects"),
        NECKLACE("Necklaces"), BELT("Belts"), BOOTS("Boots"), SCARF("Scarves"), TAIL("Tails"), EARRINGS("Earrings"), WRISTBANDS("Wristbands"), EARS("Ears"), BACK_BLADES("Back blades");
        public final String label;
        Slot(String label) { this.label = label; }
    }
    public enum Theme { MINECRAFT, BEDWARS, SKYWARS, SKYBLOCK, SMP, ASTRYX }
    public static final class Item {
        public final String id, name, theme, description;
        public final Slot slot;
        public final Theme design;
        public final int primary, accent, variant;
        public final net.minecraft.resources.Identifier wingMaterial;
        public boolean expanded(){return !theme.equalsIgnoreCase(design.name());}
        Item(Theme design, String theme, Slot slot, String name, String description, int primary, int accent, int variant) {
            this.design=design;this.theme=theme;this.slot=slot;this.name=name;this.description=description;
            this.primary=primary;this.accent=accent;this.variant=variant;
            id=theme.toLowerCase(Locale.ROOT)+"_"+slot.name().toLowerCase(Locale.ROOT)+(variant==0?"":"_v"+(variant+1));
            wingMaterial=slot==Slot.WINGS?net.minecraft.resources.Identifier.fromNamespaceAndPath("astryx","textures/cosmetics/flight/"+(expanded()?theme.toLowerCase(Locale.ROOT):id)+".png"):null;
        }
    }
    public static final List<Item> ITEMS;
    private static final Map<String,Item> INDEX;
    static {
        String[] themes={"Minecraft","Bedwars","Skywars","Skyblock","SMP","Astryx"};
        int[] colors={0xFF45834A,0xFFAA354B,0xFF384D91,0xFF207B77,0xFF77543A,0xFF241445};
        int[] accents={0xFFB9DB75,0xFFFFD47A,0xFFB2E6FF,0xFF77EBC6,0xFFEBC68B,0xFFC69AFF};
        String[][] names={
            {"Creeper's Calling","Oakleaf Flight","Creeper Cap","Miner's Knot","Portable Crafting","Pocket Creeper Keeper","Redstone Sparks","Emerald Pendant","Toolsmith's Belt","Mosswalkers"},
            {"Bed Defender","Defender Wings","Bed Defender Helmet","Final Kill Knot","Bed Defender Pack","Pocket Defender","Defender Orbit","Bed Charm","Builder's Pouches","Bridge Runners"},
            {"Into the Void","Void Raven","Ender Crown","Voidrunner Wrap","Floating Island","Pocket Sky Knight","Ender Orbit","Pearl Cage","Ender Satchel","Feathersteps"},
            {"Crystal Caverns","Prismatic Flight","Miner's Lantern","Prospector's Wrap","Geode Hauler","Pocket Prospector","Crystal Halo","Faceted Heart","Prospector's Kit","Crystal Treads"},
            {"Long Way Home","Forest Moth","Trailblazer","Campfire Knot","Expedition Pack","Pocket Explorer","Falling Leaves","Compass Rose","Explorer's Belt","Trail Boots"},
            {"Astral Emblem","Astral Seraph","Stargazer Hood","Midnight Knot","Astral Vessel","Pocket Stargazer","Lunar Orbit","Astryx Pendant","Amethyst Guard","Astral Greaves"}
        };
        String[][] descriptions={
            {"Woven green panels. Familiar face. Questionable intentions.","Layered oak leaves, with actual branches holding them up.","A creeper cap. Mercifully missing the fuse.","Checkered cloth and a proper tied knot.","A crafting table, now accepting shoulders.","Your skin, tiny. Creeper cap included.","Little redstone sparks. No wiring required.","An emerald in a gold setting.","A pickaxe and pouches for pretend productivity.","Mossy cuffs and stone soles."},
            {"Quilted red cloth, gold stitching and one very important bed.","Bed shields, wool panels and gold spars. Defend something for once.","A defender's helmet with a tiny, properly made bed crest.","Red cloth, gold stitching, no statistical advantage.","A framed red bed, wool pockets and a defender's shield.","A tiny teammate who will not steal your iron.","Beds, shields and diamonds. Your bed is still your problem.","A bed charm. Please defend the real one too.","Wool bundles and a gold buckle.","Reinforced toes for questionable bridges."},
            {"A floating island above a torn void hem.","Raven feathers and an ender-pearl hinge.","Dark prongs, floating shards, big final-boss energy.","A dark wrap with ender eyes.","An island and its dangling roots.","A tiny sky knight wearing your skin.","Ender pearls following an inconvenient orbit.","An ender pearl held in a silver cage.","A pearl pouch and a miniature blade.","Feathered cuffs. Falling still hurts."},
            {"Angular crystal seams across a miner's mantle.","Faceted crystal blades in a mineral frame.","A helmet with a lantern and crystal cluster.","A stitched miner's wrap with a gem clasp.","A geode with its crystals showing.","A miner in your skin. Tiny lamp included.","Orbiting cut crystals, with all the sharp bits.","A cut crystal hanging from a metal chain.","A pickaxe, gem pouch and far too many pockets.","Mineral plates and gemstone fasteners."},
            {"A mountain trail, a little sun and a split travel hem.","Moth wings with leaf veins and ringed eyespots.","A brimmed trail hat, complete with a feather.","A travel wrap with a leaf pin.","Canvas, bedroll, side bottle and real straps.","An explorer in your skin, hat and all.","A few autumn leaves that never quite land.","A compass rose for extremely decorative navigation.","A map roll, canteen and leather pouches.","Laced leather boots for one more biome."},
            {"A faceted Astryx emblem on a midnight, silver-edged cloak.","Layered celestial feathers on an amethyst-and-silver frame.","An open-faced midnight hood with a silver Astryx crest.","Midnight cloth, silver edging and an astral knot.","A small astral vessel, complete with engine rings and fins.","Your skin with a tiny astral hood and flight pack.","Crescent moons and stars. Subtlety missed its flight.","An amethyst north star in a silver frame.","A layered amethyst guard with a silver Astryx buckle.","Dark flight greaves, silver seams and amethyst heel lights."}
        };
        String[] scarves={"Creeper Knit","Defender's Scarf","Voidrunner Scarf","Prospector's Scarf","Trail Scarf","Stargazer Scarf"};
        String[][] second={
            {"Ender Watch","Ender Dragon","Pumpkin Lantern","Ender Band","Beehive Satchel","Pocket Pumpkin","Ender Drift","Diamond Frame","Ender Utility","Ender Soles","Ender Scarf"},
            {"Golden Forge","Bridge Builder","Forge Guard","Bridge Wrap","Bridge Supplies","Pocket Bridger","Forge Sparks","Iron Defender","Bridge Kit","Wool Runners","Bridge Scarf"},
            {"Pearl Ascendant","Sky Sentinel","Pearl Halo","Sky Sentinel Wrap","Sky Chest","Pocket Sentinel","Pearl Constellation","Void Compass","Sky Scavenger","Sky Talons","Cloud Scarf"},
            {"Dragon Relic","Relic Drake","Dragon Visor","Relic Wrap","Relic Chest","Pocket Relic Hunter","Relic Embers","Dragon Sigil","Relic Harness","Relic Sabatons","Relic Scarf"},
            {"Barn Owl","Barn Owl Flight","Ranger Hood","Ranger Wrap","Camp Lantern","Pocket Ranger","Firefly Drift","Trail Map Charm","Ranger's Kit","Ranger Boots","Ranger Scarf"},
            {"Crescent Watch","Nebula Drake","Crescent Crown","Crescent Wrap","Lunar Observatory","Pocket Moonwatcher","Crescent Chorus","Moonstone Pendant","Lunar Harness","Moonwalkers","Crescent Scarf"}
        };
        String[][] third={
            {"Redstone Circuit","Redstone Glider","Engineer's Goggles","Redstone Wrap","Redstone Battery","Pocket Engineer","Honey Trail","Redstone Heart","Redstone Rig","Copper Walkers","Redstone Scarf"},
            {"Diamond Prestige","Diamond Forge","Diamond Defender","Diamond Wrap","Diamond Generator","Pocket Diamond Guard","Diamond Orbit","Diamond Bed Sigil","Diamond Utility","Diamond Steps","Diamond Scarf"},
            {"Void Drake","Void Drake Flight","Void Drake Horns","Void Drake Wrap","Ender Cage","Pocket Drake","Void Shards","Ender Eye Charm","Void Harness","Void Greaves","Void Drake Scarf"},
            {"Gemstone Array","Gemstone Array Flight","Gemstone Crown","Gemstone Wrap","Gemstone Scanner","Pocket Gemkeeper","Prismatic Shards","Gem Cluster","Gemkeeper's Belt","Prismatic Steps","Gemkeeper Scarf"},
            {"Cartographer","Canvas Glider","Canvas Flight Cap","Cartographer Wrap","Mapmaker's Pack","Pocket Mapmaker","Autumn Compass","Mapmaker's Compass","Cartographer's Kit","Canvas Boots","Cartographer Scarf"},
            {"Deep Space","Solar Sails","Nova Flight Helmet","Nova Wrap","Star Chart Pack","Pocket Nova Pilot","Nova Constellation","Nova Sigil","Nova Utility","Comet Striders","Nova Scarf"}
        };
        List<Item> all=new ArrayList<Item>();Map<String,Item> index=new LinkedHashMap<String,Item>();
        for(int t=0;t<themes.length;t++)for(Slot slot:Slot.values())if(slot.ordinal()<11)for(int variant=0;variant<3;variant++) {
            String name=variant==0?(slot==Slot.SCARF?scarves[t]:names[t][slot.ordinal()]):(variant==1?second[t][slot.ordinal()]:third[t][slot.ordinal()]);
            String description=variant==0?(slot==Slot.SCARF?"Knitted cloth, moving tails and a little "+themes[t]+" attitude.":descriptions[t][slot.ordinal()]):variantDescription(slot,variant,themes[t]);
            int primary=colors[t],accent=accents[t];
            if(variant==1){if(t==0)accent=0xFF91CDB7;else if(t==2)accent=0xFFD4EAF7;else if(t==3){primary=0xFF8A6246;accent=0xFFE2BF7B;}else if(t==5)accent=0xFFD9D6EB;}
            if(variant==2){if(t==0)accent=0xFFE3A069;else if(t==1)accent=0xFFD4F4F0;else if(t==2)accent=0xFFA79BCB;else if(t==5)accent=0xFF98B9ED;}
            Item item=new Item(Theme.values()[t],themes[t],slot,name,description,primary,accent,variant);
            all.add(item);index.put(item.id,item);
        }
        ExpandedCosmetics.catalogue(all,index);
        ITEMS=Collections.unmodifiableList(all);INDEX=Collections.unmodifiableMap(index);
    }
    public static Item find(String id) {
        if(id==null)return null;
        // Saved wardrobes and older Astryx clients used this slot name.
        if(id.endsWith("_headwear"))id=id.substring(0,id.length()-9)+"_hat";
        return INDEX.get(id);
    }
    private static String variantDescription(Slot slot,int variant,String theme){
        switch(slot){
            case CLOAK:return variant==1?"A split travel mantle with a "+theme+" centrepiece. Dramatic exits included.":"A pointed banner cloak with stitched "+theme+" details.";
            case WINGS:return "Articulated spars and layered "+theme+" panels. Your jump height is still your problem.";
            case HAT:return variant==1?"A "+theme+" crown with a proper silhouette. Authority sold separately.":"A "+theme+" flight helmet. Protects your sense of style.";
            case BANDANA:return "Tied cloth, embroidered "+theme+" detail and two tails that behave themselves.";
            case BACKPACK:return variant==1?"A "+theme+" travel rig, with room for imaginary supplies.":"A "+theme+" field pack. Your inventory remains tragically finite.";
            case MINI_ME:return "Your skin in a miniature "+theme+" outfit. Small enough to dodge responsibility.";
            case AURA:return "Orbiting "+theme+" keepsakes. The world does not actually revolve around you.";
            case NECKLACE:return "A framed "+theme+" charm. Zero enchantment slots, plenty of opinions.";
            case BELT:return "A "+theme+" utility belt with actual pockets. Still cannot find your keys.";
            case BOOTS:return "Layered "+theme+" cuffs and reinforced soles. Gravity remains enabled.";
            default:return "A "+theme+" scarf with animated tails and stitched trim. Weatherproof confidence.";
        }
    }
    private CosmeticCatalog() {}
}
