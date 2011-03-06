package com.mysticx.bukkit.backupplugin;

import org.bukkit.Server;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Backup Plugin
 * <p/>
 * Mapper Unit
 *
 * @author MysticX
 */
public class MapperUnit extends PluginUnit {

    // values
    private File mapper_path;
    private String[] map_options;
    private boolean useLatest;

    /**
     * Default constructor
     */
    public MapperUnit(Server instance, File workdir) {
        super(instance, workdir);
        this.name = "MapperUnit";
    }

    /**
     * Default constructor
     */
    public MapperUnit(Server instance, File workdir, boolean force) {
        super(instance, workdir, force);
        this.name = "MapperUnit";
    }


    /**
     * Sets path to mapper executable
     *
     * @param mapper_path
     */
    public void setMapperPath(File mapper_tool) {
        this.mapper_path = mapper_tool;
        if (mapper_path == null || !mapper_path.exists()) {
            setEnabled(false);
            MessageHandler.warning("Disabled MapperUnit, mapper_path invalid: " + mapper_path);
        }
    }

    /**
     * Sets mapping options
     *
     * @param map_options
     */
    public void setMapOptions(String[] map_options) {
        this.map_options = map_options;
    }

    /**
     * Enables or disables usage of latest.png
     *
     * @param true, if enabled
     */
    public void setUseLatest(boolean useLatest) {
        this.useLatest = useLatest;
    }

    /**
     * Generates maps via commandline
     */
    @Override
    public void run() {

        while (!isEnabled) {
            MessageHandler.log(Level.WARNING, " is disabled. Thread goes to sleep.");
            try {
                this.wait();
            } catch (InterruptedException e) {
                MessageHandler.log(Level.WARNING, "woke up from sleep unexpectedly!", e);
            }
        }

        MessageHandler.log(Level.INFO, "Starting map generation process.. this could take a while!");

        // save world and disable saving for mapping process
        consoleCommandLog("Caching process started.");
        saveWorld();

        File inputFolder = null;

        try {
            // retrieve cache
            inputFolder = cc.getCache(this.isForce());
        } catch (Exception e) {
            MessageHandler.log(Level.SEVERE, "An error ocurred during mapping", e);
            return;
        } finally {
            // TODO: enable saving again
            consoleCommandLog("Caching process completed. Enabling level saving.");
            console.worlds.get(0).w = false;
        }


        // create folders
        if (!this.getWorkDir().exists()) {
            this.getWorkDir().mkdirs();
        }

        // lock cache while generating maps
        cc.getLock().lock();
        MessageHandler.log(Level.FINEST, "got lock, starting map generation");

        // do mappings
        for (int i = 0; i < map_options.length; i++) {
            MessageHandler.info("Mapping pass " + (i + 1) + " of " + map_options.length + "...");

            // modify parameters
            String filename = generateFilename(".png");
            String map_parameters = new String(map_options[i]);
            map_parameters = map_parameters.replace("$o", new File(this.getWorkDir(), filename).getAbsolutePath());
            map_parameters = map_parameters.replace("$w", inputFolder.getAbsolutePath());

            if (map_parameters.contains("$m"))
                map_parameters = map_parameters.replace("$m", mapper_path.getParent());

            MessageHandler.log(Level.FINE, "Mapper usage: " + mapper_path + " " + map_parameters);

            // generate maps
            executeExternal(mapper_path, map_parameters);

            // save latest.png at first run
            if (i == 0 && useLatest) {
                try {
                    iohelper.deleteFile(new File(this.getWorkDir(), "latest.png"));
                    iohelper.copyFile(new File(this.getWorkDir(), filename), new File(this.getWorkDir(), "latest.png"), false);
                } catch (IOException e) {
                    MessageHandler.log(Level.WARNING, "Creating latest.png failed: ", e);
                }

            }
        }

        MessageHandler.info("Mapping process finished.");
        cc.getLock().unlock();

        setChanged();
        notifyObservers();
    }

    /**
     * Executes external binaries
     *
     * @param program   path to executable
     * @param arguments arguments to invoke
     * @return true if successful
     */
    private void executeExternal(File program, String arguments) {
        try {
            long start = System.currentTimeMillis();
            SysCommandExecutor cmdExecutor = new SysCommandExecutor();
            int exitStatus = cmdExecutor.runCommand(program + " " + arguments);

            if (exitStatus != 0) {
                MessageHandler.warning("Mapping failed, something went wrong while executing external code! Exit Status: " + exitStatus);
            } else {
                MessageHandler.info("Mapping successful! Executing mapper took " + calculateTimeDifference(start, System.currentTimeMillis()) + " seconds.");
            }
        } catch (Exception e) {
            MessageHandler.log(Level.SEVERE,
                    "Error while executing external code!", e);
        }
    }

}
