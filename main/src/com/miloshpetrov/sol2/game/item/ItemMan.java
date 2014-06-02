package com.miloshpetrov.sol2.game.item;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.miloshpetrov.sol2.*;
import com.miloshpetrov.sol2.common.SolMath;
import com.miloshpetrov.sol2.game.GameCols;
import com.miloshpetrov.sol2.game.gun.GunConfig;
import com.miloshpetrov.sol2.game.gun.GunItem;
import com.miloshpetrov.sol2.game.particle.EffectTypes;
import com.miloshpetrov.sol2.game.projectile.ProjectileConfigs;
import com.miloshpetrov.sol2.game.ship.AbilityCharge;
import com.miloshpetrov.sol2.game.sound.SoundMan;
import com.miloshpetrov.sol2.ui.DebugCollector;

import java.util.*;

public class ItemMan {
  public static final String ITEM_CONFIGS_DIR = Const.CONFIGS_DIR + "items/";
  private final HashMap<String,SolItem> myM;
  private final ArrayList<SolItem> myL;
  public final ProjectileConfigs projConfigs;
  public final TextureAtlas.AtlasRegion moneyIcon;
  public final TextureAtlas.AtlasRegion bigMoneyIcon;
  public final TextureAtlas.AtlasRegion repairIcon;
  private final EngineItem.Configs myEngineConfigs;
  private final SolItemTypes myTypes;
  private final RepairItem myRepairExample;

  public ItemMan(TexMan texMan, SoundMan soundMan, EffectTypes effectTypes, GameCols cols) {
    moneyIcon = texMan.getTex(TexMan.ICONS_DIR + "money", null);
    bigMoneyIcon = texMan.getTex(TexMan.ICONS_DIR + "bigMoney", null);
    repairIcon = texMan.getTex(TexMan.ICONS_DIR + "repairItem", null);
    myM = new HashMap<String, SolItem>();

    myTypes = new SolItemTypes(soundMan, cols);
    projConfigs = new ProjectileConfigs(texMan, soundMan, effectTypes, cols);
    myEngineConfigs = EngineItem.Configs.load(soundMan, texMan, effectTypes, cols);

    Shield.Config.loadConfigs(this, soundMan, texMan, myTypes);
    Armor.Config.loadConfigs(this, soundMan, texMan, myTypes);
    AbilityCharge.Config.load(this, texMan, myTypes);

    ClipConfig.load(this, texMan, myTypes);
    GunConfig.load(texMan, this, soundMan, myTypes);

    myRepairExample = new RepairItem(myTypes.repair);
    myM.put("rep", myRepairExample);

    myL = new ArrayList<SolItem>(myM.values());
  }

  public void fillContainer(ItemContainer c, String items) {
    List<ItemConfig> list = parseItems(items);
    for (ItemConfig ic : list) {
      for (int i = 0; i < ic.amt; i++) {
        if (SolMath.test(ic.chance)) {
          SolItem item = ic.exmaple.copy();
          c.add(item);
        }
      }
    }
  }

  public List<ItemConfig> parseItems(String items) {
    ArrayList<ItemConfig> res = new ArrayList<ItemConfig>();
    if (items.isEmpty()) return res;
    for (String rec : items.split(" ")) {
      String[] parts = rec.split(":");
      if (parts.length == 0) continue;
      String[] names = parts[0].split("\\|");
      String name = names[SolMath.intRnd(names.length)].trim();
      SolItem example = getExample(name);
      if (example == null) {
        throw new AssertionError("unknown item " + name + "@" + parts[0] + "@" + rec + "@" + items);
      }

      float chance = 1;
      if (parts.length > 1) {
        chance = Float.parseFloat(parts[1]);
        if (chance <= 0 || 1 < chance) throw new AssertionError(chance);
      }

      int amt = 1;
      if (parts.length > 2) {
        amt = Integer.parseInt(parts[2]);
      }
      ItemConfig ic = new ItemConfig(example, amt, chance);
      res.add(ic);
    }
    return res;
  }

  public SolItem getExample(String name) {
    return myM.get(name);
  }

  public SolItem random() {
    return myL.get(SolMath.intRnd(myM.size())).copy();
  }

  public void registerItem(String itemCode, SolItem example) {
    SolItem existing = getExample(itemCode);
    if (existing != null) {
      throw new AssertionError("2 item types registered for item code " + itemCode + ":\n" + existing + " and " + example);
    }
    myM.put(itemCode, example);
  }

  public EngineItem.Configs getEngineConfigs() {
    return myEngineConfigs;
  }

  public void printGuns() {
    ArrayList<GunConfig> l = new ArrayList<GunConfig>();
    for (SolItem i : myM.values()) {
      if (!(i instanceof GunItem)) continue;
      GunItem g = (GunItem) i;
      l.add(g.config);
    }
    Comparator<GunConfig> comp = new Comparator<GunConfig>() {
      public int compare(GunConfig o1, GunConfig o2) {
        return Float.compare(o1.meanDps, o2.meanDps);
      }
    };
    Collections.sort(l, comp);
    StringBuilder sb = new StringBuilder();
    for (GunConfig c : l) {
      sb.append(c.tex.name).append(": ").append(c.meanDps).append("\n");
    }
    String msg = sb.toString();
    System.out.println(msg);
    DebugCollector.warn(msg);
  }

  public MoneyItem moneyItem(boolean big) {
    return new MoneyItem(big, big ? myTypes.bigMoney : myTypes.money);
  }

  public RepairItem getRepairExample() {
    return myRepairExample;
  }

  public static class ItemConfig {
    public final SolItem exmaple;
    public final int amt;
    public final float chance;

    public ItemConfig(SolItem exmaple, int amt, float chance) {
      this.exmaple = exmaple;
      this.amt = amt;
      this.chance = chance;
    }
  }
}
