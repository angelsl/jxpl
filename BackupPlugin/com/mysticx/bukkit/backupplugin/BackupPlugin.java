package com.mysticx.bukkit.backupplugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Server;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * BackupPlugin for Bukkit
 * 2011-01-18
 * <p/>
 * BackupPlugin by MysticX is licensed under a Creative Commons
 * Attribution-NonCommercial-ShareAlike 3.0 Unported License.
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 * <p/>
 * Permissions beyond the scope of this license may be available
 * at http://forum.hey0.net/showthread.php?tid=179
 *
 * @author MysticX
 */
public class BackupPlugin extends JavaPlugin implements Observer {

    // listeners
    private final BackupPluginPlayerListener playerListener = new BackupPluginPlayerListener(this);
    //    private final BackupPluginBlockListener blockListener = new BackupPluginBlockListener(this);
    private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();

    // config
//    private Configuration config;
    private TimeUnit timeunit = TimeUnit.MINUTES;

    // cache control
    private CacheControl cc;

    // Units
    private BackupUnit bu;
    private MapperUnit mu;

    private static PermissionHandler permissions = null;

    public static PermissionHandler getPermissions()
    {
        return permissions;
    }



    /*
     * (non-Javadoc)
     * @see org.bukkit.plugin.Plugin#onEnable()
     */
    public void onEnable() {

        MessageHandler.setServer(getServer());
        try {
            //
            Field cField = CraftServer.class.getDeclaredField("console");
            cField.setAccessible(true);
            ((MinecraftServer) (cField.get(this.getServer()))).console.isPlayer();

        } catch (Throwable e) {
            e.printStackTrace();
            MessageHandler.log(Level.SEVERE, "Not running on CraftBukkit/Notchian Server! Unloading self.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!setupPermissions()) {
            MessageHandler.log(Level.SEVERE, "Permissions plugin not loaded! No one can use commands.");
        }
        // Register our events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, this.playerListener, Event.Priority.Normal, this);

        PluginDescriptionFile pdfFile = this.getDescription();
        MessageHandler.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
        load();
    }

    private boolean setupPermissions() {
        Plugin p = this.getServer().getPluginManager().getPlugin("Permissions");
        if (permissions == null && p != null) {
            permissions = ((Permissions) p).getHandler();
            return true;
        }
        return false;
    }

    /**
     * Loads property files and initializes some other stuff
     *
     * @return true if successful
     */
    protected boolean load() {
//		Configuration config = this.getConfiguration();

        //TODO: use Bukkit config when its finally working!
        com.mysticx.bukkit.backupplugin.Configuration config = new com.mysticx.bukkit.backupplugin.Configuration("BackupPlugin.properties");
        config.load();

        String separator = System.getProperty("file.separator");

        // some important values
        String world = config.getString("level-name", "world");
        String backup_folder = config.getString("backup-path", "world-backups");
        String mapper_path = config.getString("mapper-executable", "mcmap" + separator + "mcmap.exe");
        String map_folder = config.getString("map-path", "world-maps");
        String map_options = config.getString("map-options", "-png -file $o $w;-night -png -file $o $w");
        Integer autobackup_period = config.getInt("autobackup-period", 0);
        Integer automap_period = config.getInt("automap-period", 0);
        Integer cache_lifetime = config.getInt("cache-lifetime", 30);
        String tempdir = backup_folder + "/temp";
        String loglevel = config.getString("log-level", "INFO");
        String time_unit = config.getString("time-unit", "MINUTES");
        Integer num_backups = config.getInt("backup-history", 5);
        Boolean useLatest = config.getBoolean("use-latest", false);
        String firstRun = config.getString("first-run", "1200");
        String admins = config.getString("authorized-users", "");

        MessageHandler.setLogLevel(loglevel);

        // authorized users
        authorizedUsers = new ArrayList<String>();

        String[] access = admins.split(";");

        String logInfo = "";

        for (String name : access) {
            if (!name.isEmpty()) {
                authorizedUsers.add(name);
                logInfo += name + ", ";
            }
        }


        MessageHandler.log(Level.FINE, String.format("There are %d user(s) in the authorized-users list: %s", authorizedUsers.size(), logInfo));

        // timeUnit
        try {
            TimeUnit tu = TimeUnit.valueOf(time_unit);
            this.timeunit = tu;
        } catch (Exception e) {
            MessageHandler.warning("Failed to parse time-unit, using default.");
        }

        // init cache
        this.cc = CacheControl.getInstance();
        this.cc.setWorld(world);
        this.cc.setTimeUnit(timeunit);
        this.cc.setCacheLifetime(cache_lifetime);
        this.cc.setTempDir(new File(tempdir));
        this.cc.setCacheHistory(num_backups);

        // init BackupUnit
        this.bu = new BackupUnit(this.getServer(), new File(backup_folder), true);
        this.bu.addObserver(this);

        // init MapperUnit
        this.mu = new MapperUnit(this.getServer(), new File(map_folder), false);
        this.mu.setMapperPath(new File(mapper_path));
        this.mu.setUseLatest(useLatest);
        this.mu.addObserver(this);

        String[] parameters = map_options.split(";");
        this.mu.setMapOptions(parameters);

        // init scheduler
        scheduler.shutdownNow();
        scheduler = Executors.newScheduledThreadPool(2);

        // schedule timer
        long backup_delay = -1;
        long map_delay = -1;

        try {
            long timeToExecuteB = calcNextPointOfTime(firstRun, "HHmm", TimeUnit.MILLISECONDS.convert(autobackup_period, timeunit));
            backup_delay = timeToExecuteB - System.currentTimeMillis();

            long timeToExecuteM = calcNextPointOfTime(firstRun, "HHmm", TimeUnit.MILLISECONDS.convert(automap_period, timeunit));
            map_delay = timeToExecuteM - System.currentTimeMillis();
        } catch (ParseException pe) {
            MessageHandler.log(Level.WARNING, "Failed to parse firstRun, disabled automatic execution", pe);
        }

        if (autobackup_period != null && backup_delay >= 0 && autobackup_period > 0) {
            setupTimer(bu, backup_delay, autobackup_period, this.timeunit);
        }

        if (automap_period != null && map_delay >= 0 && automap_period > 0) {
            setupTimer(mu, map_delay, automap_period, this.timeunit);
        }

        return true;
    }

    /*
      * (non-Javadoc)
      * @see org.bukkit.plugin.Plugin#onDisable()
      */
    public void onDisable() {
        System.out.println("BackupPlugin disabled!");
    }

    /**
     * Checks whether player is in debug mode or not
     *
     * @param player
     * @return
     */
    public boolean isDebugging(final Player player) {
        if (debugees.containsKey(player)) {
            return debugees.get(player);
        } else {
            return false;
        }
    }

    /**
     * Sets debug status of player
     *
     * @param player
     * @param value
     */
    public void setDebugging(final Player player, final boolean value) {
        debugees.put(player, value);
    }

    /**
     * @param time
     * @param pattern
     * @param period
     * @return next scheduled point in time, 0 if there is none
     * @throws ParseException
     */
    private long calcNextPointOfTime(String time, String pattern, long period) throws ParseException {
        if (period <= 0)
            return 0;

        Date d = new Date();

        DateFormat df = new SimpleDateFormat(pattern);
        df.setLenient(true);
        Date date = df.parse(time);

        //TODO rewrite
        d.setHours(date.getHours());
        d.setMinutes(date.getMinutes());
        d.setSeconds(0);

        MessageHandler.log(Level.FINEST, "firstRun: " + d.toString());

        long nextRun = d.getTime();

        while (nextRun < System.currentTimeMillis()) {
            MessageHandler.log(Level.FINEST, "Date is in the past, adding some  minutes: " + period / 1000 / 60);
            nextRun += period;
        }

        return nextRun;
    }

    /**
     * Scheduled Executor for plugin units
     */
    private ScheduledExecutorService scheduler = Executors
            .newScheduledThreadPool(2);

    /**
     * Starts a new timer with given Runnable and times
     *
     * @param r        the Runnable object
     * @param delay    in milliseconds
     * @param period   period
     * @param TimeUnit TimeUnit
     * @return
     */
    private boolean setupTimer(Runnable r, long delay, Integer period, TimeUnit tu) {
        ScheduledFuture<?> sf = scheduler.scheduleAtFixedRate(r, tu.convert(delay, TimeUnit.MILLISECONDS), period, tu);

        MessageHandler.info("Finished setting up a thread: " + r.getClass() + " Next run in: " + TimeUnit.MINUTES.convert(delay, TimeUnit.MILLISECONDS) + " minutes.");
        return true;
    }

    /**
     * update only happens after a manual unit run, reset force for scheduler afterwards
     */
    @Override
    public void update(Observable arg0, Object arg1) {
        if (arg0 instanceof PluginUnit) {
            PluginUnit pu = (PluginUnit) arg0;
            pu.resetForce();
        }
    }

    /**
     * Backups current world
     *
     * @param force true disables cache usage
     * @return true if successful
     */
    protected void performBackup(boolean force) {
        bu.setForce(force);
        scheduler.execute(bu);
    }


    /**
     * Creates map of current world
     *
     * @param force true disables cache usage
     * @return true if successful
     */
    protected void performMapping(boolean force) {
        mu.setForce(force);
        scheduler.execute(mu);
    }

    // authorized users go in here
    List<String> authorizedUsers = new ArrayList<String>();

    /**
     * checks if an user is authorized to use ingame commands
     *
     * @param userName
     * @return
     */
    protected boolean isAuthorized(String userName) {
        for (String user : authorizedUsers) {
            if (userName.compareToIgnoreCase(user) == 0) {
                return true;
            }
        }
        return false;
    }
}

