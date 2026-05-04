package cn.fancraft.fantpa.config;

public class ConfigData {

    public int teleportTimeout = 30;
    public int teleportCooldown = 5;
    public int teleportDelay = 3;

    public String defaultLanguage = "en_us";

    public boolean tpaEnabled = true;
    public boolean tpahereEnabled = true;
    public boolean backEnabled = true;
    public boolean homeEnabled = true;
    public boolean sethomeEnabled = true;
    public boolean delhomeEnabled = true;

    public String tpaPermission = "fantpa.command.tpa";
    public String tpaherePermission = "fantpa.command.tpahere";
    public String backPermission = "fantpa.command.back";
    public String homePermission = "fantpa.command.home";
    public String sethomePermission = "fantpa.command.sethome";
    public String delhomePermission = "fantpa.command.delhome";
    public String tpallPermission = "fantpa.admin.tpall";
    public String adminPermission = "fantpa.admin";
}
