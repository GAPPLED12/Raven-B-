//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.8.9"!

package me.kopamed.raven.bplus.client.feature.module;

import java.util.ArrayList;
import java.util.Iterator;

import me.kopamed.raven.bplus.client.Raven;
import me.kopamed.raven.bplus.client.feature.module.modules.player.FallSpeed;
import me.kopamed.raven.bplus.client.feature.setting.SelectorRunnable;
import me.kopamed.raven.bplus.client.feature.setting.Setting;
import me.kopamed.raven.bplus.client.feature.setting.settings.ComboSetting;
import me.kopamed.raven.bplus.client.visual.clickgui.plus.PlusGui;
import me.kopamed.raven.bplus.helper.manager.ModuleManager;
import me.kopamed.raven.bplus.client.feature.module.modules.other.DiscordRPCModule;
import me.kopamed.raven.bplus.client.feature.setting.settings.BooleanSetting;
import me.kopamed.raven.bplus.helper.utils.NotificationRenderer;
import me.kopamed.raven.bplus.helper.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.lwjgl.input.Keyboard;

public abstract class Module {
   private     final    String               moduleName;
   private     final    String               tooltip;
   private     final    ModuleCategory       moduleCategory;
   private              ArrayList<Integer>   keycodes;
   protected   static   Minecraft            mc;
   private              boolean              registeredKeyPress   =  false;
   private              boolean              shownOnHud;
   private              BindMode             bindMode;
   private              boolean              toggled;
   protected            ArrayList<Setting>   settings;
   private     final    int                  maxBinds             =  5;

   public Module(String name, String tooltip, ModuleCategory moduleCategory) {
      this(name, tooltip, moduleCategory, false, new ArrayList<>(), BindMode.TOGGLE, true);
   }

   public Module(String name, ModuleCategory moduleCategory) {
      this(name, "", moduleCategory, false, new ArrayList<>(), BindMode.TOGGLE, true);
   }

   public Module(String name, ModuleCategory moduleCategory, int bruh) {
      this(name, "", moduleCategory, false, new ArrayList<>(), BindMode.TOGGLE, true);
   }

   public Module(String name, String tooltip, ModuleCategory moduleCategory, BindMode bindMode) {
      this(name, tooltip, moduleCategory, false, new ArrayList<>(), bindMode, true);
   }

   public Module(String name, String tooltip, ModuleCategory moduleCategory, boolean toggled) {
      this(name, tooltip, moduleCategory, toggled, new ArrayList<>(), BindMode.TOGGLE, true);
   }

   public Module(String moduleName, String tooltip, ModuleCategory moduleCategory, ArrayList<Integer> keycodes) {
      this(moduleName, tooltip, moduleCategory, false, keycodes, BindMode.TOGGLE, true);
   }

   public Module(String moduleName, String tooltip, ModuleCategory moduleCategory, boolean toggled, ArrayList<Integer> keycodes, BindMode bindMode, boolean shownOnHud){
      this.moduleName = moduleName;
      this.tooltip = tooltip;
      this.moduleCategory = moduleCategory;
      this.toggled = toggled;
      this.keycodes = keycodes;
      this.bindMode = bindMode;
      this.shownOnHud = shownOnHud;
      this.settings = new ArrayList<>();
      mc = Raven.client.getMc();

      BooleanSetting showOnHud = new BooleanSetting("Shown on HUD", true);
      ComboSetting toggleType = new ComboSetting("Toggle Type", new String[] {"Toggle", "Hold"}, getBindMode() == BindMode.TOGGLE ? 0 : 1);
      toggleType.addSelector(new SelectorRunnable() {
         @Override
         public boolean showOnlyIf() {
            return hasKeybind();
         }
      });
      this.settings.add(showOnHud);
      this.settings.add(toggleType);

   }

   public static Module getModule(Class<? extends Module> a) {
      Iterator var1 = ModuleManager.modsList.iterator();

      Module module;
      do {
         if (!var1.hasNext()) {
            return null;
         }

         module = (Module)var1.next();
      } while(module.getClass() != a);

      return module;
   }

   public void keybind() {
      if (!keycodes.isEmpty()) { //todo
         this.bindMode = ((ComboSetting)this.getSettingByName("Toggle Type")).getMode().equals("Toggle")  ? BindMode.TOGGLE : BindMode.HOLD;
         if(areBindsDown()){
            if(!registeredKeyPress){
               if (bindMode == BindMode.HOLD){
                  enable();
               } else if( bindMode == BindMode.TOGGLE){
                  toggle();
               }
               this.registeredKeyPress = true;
            }
         } else{
            if(registeredKeyPress){
               if (bindMode == BindMode.HOLD){
                  disable();
               }
               this.registeredKeyPress = false;
            }
         }
      }
   }

   private boolean areBindsDown() {
      for(Integer i : keycodes){
         if(!Keyboard.isKeyDown(i))
            return false;
      }
      return true;
   }

   public String getBindAsString(){
      if(keycodes.isEmpty())
         return "None";

      StringBuilder bobTheBuilder = new StringBuilder();
      for(Integer i : keycodes){
         bobTheBuilder.append(Keyboard.getKeyName(i)).append("+");
      }

      bobTheBuilder.deleteCharAt(bobTheBuilder.lastIndexOf("+"));
      return bobTheBuilder.toString();
   }

   public void enable() {
      this.toggled = true;
      MinecraftForge.EVENT_BUS.register(this);
      //FMLCommonHandler.instance().bus().register(this);
      this.onEnable();

      NotificationRenderer.moduleStateChanged(this); //todo
      Raven.client.getDiscordRPCManager().updateRPC();
   }

   public void disable() {
      this.toggled = false;
      MinecraftForge.EVENT_BUS.unregister(this);
      //FMLCommonHandler.instance().bus().unregister(this);
      this.onDisable();

      NotificationRenderer.moduleStateChanged(this);
      Raven.client.getDiscordRPCManager().updateRPC();
   }


   public String getName() {
      return this.moduleName;
   }

   public ArrayList<Setting> getSettings() {
      return this.settings;
   }

   public Setting getSettingByName(String name) {
      for (Setting setting : this.settings) {
         if (setting.getName().equalsIgnoreCase(name))
            return setting;
      }
      return null;
   }

   public void registerSetting(Setting setting) {
      this.settings.add(settings.size() - 2, setting);
   }

   public ModuleCategory moduleCategory() {
      return this.moduleCategory;
   }

   public boolean isToggled() {
      return this.toggled;
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void toggle() {
      if (toggled){
         this.disable();
      } else {
         this.enable();
      }
   }

   public void update() {
   }

   public void guiUpdate() {
   }

   public ArrayList<Integer> getKeycodes() {
      return this.keycodes;
   }

   public void setBinds(ArrayList<Integer> binds) {
      this.keycodes = binds;
   }

   public void clearBinds(){
      keycodes.clear();
   }

   public BindMode getBindMode() {
      return bindMode;
   }

   public void setBindMode(BindMode bindMode) {
      this.bindMode = bindMode;
   }

   public boolean hasKeybind() {
      return keycodes.isEmpty();
   }

   public boolean canToggle(){
      return Utils.Player.isPlayerInGame() && !(mc.currentScreen instanceof PlusGui);
   }

   public String getTooltip() {
      return tooltip;
   }

   public void addKeyCode(int keyCode) {
      if(canAddMoreBinds() && !keycodes.contains(keyCode))
         this.keycodes.add(keyCode);
   }

   public boolean canAddMoreBinds(){
      return keycodes.size() < maxBinds;
   }

   public void setToggled(boolean b){
      this.toggled = b;
   }

   public boolean isShownOnHud() {
      return shownOnHud;
   }

   public void setShownOnHud(boolean shownOnHud) {
      this.shownOnHud = shownOnHud;
   }

   public void onGuiClose() {
      this.bindMode = ((ComboSetting)this.getSettingByName("Toggle Type")).getMode().equals("Toggle") ? BindMode.TOGGLE : BindMode.HOLD;
      if(bindMode == BindMode.HOLD)
         this.disable();
   }
}