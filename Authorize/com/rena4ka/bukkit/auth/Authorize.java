/*     */
package com.rena4ka.bukkit.auth;
/*     */
/*     */

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */

/*     */
/*     */ public class Authorize extends JavaPlugin
/*     */ {
    /*  42 */   private final AuthorizePlayerListener playerListener = new AuthorizePlayerListener(this);
    /*     */
/*  44 */   private final AuthorizeBlockListener blockListener = new AuthorizeBlockListener(this);
    /*  45 */   private final AuthorizeEntityListener entityListener = new AuthorizeEntityListener(this);
    /*  46 */   private List<Integer> authorizedIds = new ArrayList();
    /*  47 */   public boolean allowRegister = true;
    /*  48 */   public boolean allowPassChange = true;
    /*  49 */   public boolean forceRegister = true;
    /*  50 */   public boolean kickOnBadPassword = true;
    /*  51 */   public boolean allowUnregister = false;
    /*  52 */   public boolean requireEmail = false;
    /*  53 */   public boolean allowEmailChange = false;
    /*  54 */   private int sourcetype = 0;
    /*     */
/*  60 */   public String loginMessage = ChatColor.RED + "Please login with command /login <password>";
    /*  61 */   public String registerMessage = ChatColor.RED + "Please register with command /register <password>";
    /*  62 */   public String authorizedMessage = ChatColor.DARK_RED + "You are already authorized!";
    /*  63 */   public String loginUsageMessage = ChatColor.DARK_RED + "Correct usage is: /login <password>";
    /*  64 */   public String passwordAcceptedMessage = ChatColor.GREEN + "Password accepted. Welcome!";
    /*  65 */   public String badPasswordMessage = "Bad password!";
    /*  66 */   public String registerUsageMessage = ChatColor.DARK_RED + "Correct usage is: /register <password>";
    /*  67 */   public String alreadyRegisteredMessage = ChatColor.DARK_RED + "You are already registered!";
    /*  68 */   public String registrationNotAllowedMessage = ChatColor.DARK_RED + "Registration not allowed!";
    /*  69 */   public String registeredMessage = ChatColor.GREEN + "You have been registered!";
    /*  70 */   public String registerErrorMessage = ChatColor.RED + "Error while registering you!";
    /*  71 */   public String passwordUsageMessage = ChatColor.DARK_RED + "Correct usage is: /password <oldpassword> <password>";
    /*  72 */   public String passwordNotRegisteredMessage = ChatColor.DARK_RED + "Register first!";
    /*  73 */   public String passNotAllowedMessage = ChatColor.DARK_RED + "Password changing not allowed!";
    /*  74 */   public String badOldPasswordMessage = ChatColor.DARK_RED + "Bad old password!";
    /*  75 */   public String passwordChangedMessage = ChatColor.GREEN + "You'r password has been changed!";
    /*  76 */   public String passwordChangeErrorMessage = ChatColor.RED + "Error while changing your password!";
    /*  77 */   public String emailRequiredMessage = ChatColor.RED + "Email required for registration!";
    /*  78 */   public String emailUsageMessage = ChatColor.RED + "Correct usage is: /email <oldpasword> <email>";
    /*  79 */   public String emailChangeNotAllowedMessage = ChatColor.RED + "Email changing not allowed!";
    /*  80 */   public String emailUnexpectedMessage = ChatColor.RED + "Email contains unexpected letters!";
    /*  81 */   public String emailChangedMessage = ChatColor.RED + "Email successfully changed!";
    /*  82 */   public String emailChangeErrorMessage = ChatColor.RED + "Error while changing email!";
    /*  83 */   public String notAdminMessage = ChatColor.RED + "You're not admin!";
    /*  84 */   public String resetUsageMessage = ChatColor.RED + "Correct usage is: /loginreset <login>";
    /*  85 */   public String userResettedMessage = "resetted";
    /*  86 */   public String reloadedMessage = ChatColor.GREEN + "Authorize is reloaded!";
    /*     */   public String unregisterUsageMessage;
    /*     */   public String unregisterNotRegisteredMessage;
    /*     */   public String unregNotAllowedMessage;
    /*     */   public String unregisteredMessage;
    /*     */   public String unregisterErrorMessage;
    /*  92 */   private HashMap<String, String> db = new HashMap();
    /*  93 */   private String dbFileName = "auths.db";
    /*  94 */   public Logger log = Logger.getLogger("Minecraft");
    /*  95 */   private HashMap<String, ItemStack[]> inventories = new HashMap();
    /*  96 */   private Connection mysql = null;
    /*  97 */   private List<String> admins = new ArrayList();

    /*     */
/*     */
    public Authorize(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
/* 100 */
        super(pluginLoader, instance, desc, folder, plugin, cLoader);
/*     */
    }

    /*     */
/*     */
    public void onDisable() {
/* 104 */
        this.authorizedIds.clear();
/* 105 */
        this.db.clear();
/* 106 */
        Set pl = this.inventories.keySet();
/* 107 */
        Iterator i = pl.iterator();
/* 108 */
        while (i.hasNext()) {
/* 109 */
            String player = (String) i.next();
/* 110 */
            Player pla = getServer().getPlayer(player);
/* 111 */
            if (pl != null)
/* 112 */ pla.getInventory().setContents((ItemStack[]) this.inventories.get(player));
/*     */
        }
/* 114 */
        this.inventories.clear();
/* 115 */
        this.admins.clear();
/* 116 */
        if (this.mysql != null)
/*     */ try {
/* 118 */
            this.mysql.close();
        } catch (SQLException localSQLException) {
/*     */
        }
/*     */
    }

    /*     */
/*     */
    public void onEnable() {
/* 123 */
        this.allowRegister = getConfiguration().getBoolean("allow-register", this.allowRegister);
/* 124 */
        this.allowPassChange = getConfiguration().getBoolean("allow-pass-change", this.allowPassChange);
/* 125 */
        this.forceRegister = getConfiguration().getBoolean("force-register", this.forceRegister);
/* 126 */
        this.kickOnBadPassword = getConfiguration().getBoolean("kick-on-bad-password", this.kickOnBadPassword);
/* 127 */
        this.allowUnregister = getConfiguration().getBoolean("allow-unregister", this.allowUnregister);
/* 128 */
        this.requireEmail = getConfiguration().getBoolean("require-email", this.requireEmail);
/* 129 */
        this.allowEmailChange = getConfiguration().getBoolean("allow-email-change", this.allowEmailChange);
/* 130 */
        String stype = getConfiguration().getString("source-type", "flatfile");
/* 131 */
        if (stype.equalsIgnoreCase("flatfile"))
/* 132 */ this.sourcetype = 0;
/*     */
        else
/* 134 */       this.sourcetype = 1;
/* 135 */
        String dbDriver = getConfiguration().getString("db-driver", "com.mysql.jdbc.Driver");
/* 136 */
        String dbUsername = getConfiguration().getString("db-username", "root");
/* 137 */
        String dbPassword = getConfiguration().getString("db-password", "");
/* 138 */
        String dbDb = getConfiguration().getString("db-db", "jdbc:mysql://localhost:3306/minecraft");
/* 139 */
        Boolean convertOnStart = Boolean.valueOf(getConfiguration().getBoolean("db-convert", false));
/* 140 */
        String[] adm = getConfiguration().getString("admins", "").split(",");
/* 141 */
        for (short i = 0; i < adm.length; i = (short) (i + 1))
/* 142 */
            this.admins.add(adm[i].toLowerCase());
/* 143 */
        this.loginMessage = getConfiguration().getString("login-message", this.loginMessage);
/* 144 */
        this.registerMessage = getConfiguration().getString("register-message", this.registerMessage);
/* 145 */
        this.authorizedMessage = getConfiguration().getString("authorized-message", this.authorizedMessage);
/* 146 */
        this.loginUsageMessage = getConfiguration().getString("login-usage-message", this.loginUsageMessage);
/* 147 */
        this.passwordAcceptedMessage = getConfiguration().getString("password-accepted-message", this.passwordAcceptedMessage);
/* 148 */
        this.badPasswordMessage = getConfiguration().getString("bad-password-message", this.badPasswordMessage);
/* 149 */
        this.registerUsageMessage = getConfiguration().getString("register-usage-message", this.registerUsageMessage);
/* 150 */
        this.alreadyRegisteredMessage = getConfiguration().getString("already-registered-message", this.alreadyRegisteredMessage);
/* 151 */
        this.registrationNotAllowedMessage = getConfiguration().getString("registration-not-allowed-message", this.registrationNotAllowedMessage);
/* 152 */
        this.registeredMessage = getConfiguration().getString("registered-message", this.registeredMessage);
/* 153 */
        this.registerErrorMessage = getConfiguration().getString("register-error-message", this.registerErrorMessage);
/* 154 */
        this.passwordUsageMessage = getConfiguration().getString("password-usage-message", this.passwordUsageMessage);
/* 155 */
        this.passwordNotRegisteredMessage = getConfiguration().getString("password-not-registered-message", this.passwordNotRegisteredMessage);
/* 156 */
        this.passNotAllowedMessage = getConfiguration().getString("pass-not-allowed-message", this.passNotAllowedMessage);
/* 157 */
        this.badOldPasswordMessage = getConfiguration().getString("bad-old-password-message", this.badOldPasswordMessage);
/* 158 */
        this.passwordChangedMessage = getConfiguration().getString("password-сhanged-message", this.passwordChangedMessage);
/* 159 */
        this.passwordChangeErrorMessage = getConfiguration().getString("password-сhange-error-message", this.passwordChangeErrorMessage);
/* 160 */
        this.unregisterUsageMessage = getConfiguration().getString("unregister-usage-message", this.unregisterUsageMessage);
/* 161 */
        this.unregisterNotRegisteredMessage = getConfiguration().getString("unregister-not-registered-message", this.unregisterNotRegisteredMessage);
/* 162 */
        this.unregNotAllowedMessage = getConfiguration().getString("unreg-not-allowed-message", this.unregNotAllowedMessage);
/* 163 */
        this.unregisteredMessage = getConfiguration().getString("unregistered-message", this.unregisteredMessage);
/* 164 */
        this.unregisterErrorMessage = getConfiguration().getString("unregister-error-message", this.unregisterErrorMessage);
/* 165 */
        this.emailRequiredMessage = getConfiguration().getString("email-required-message", this.emailRequiredMessage);
/* 166 */
        this.emailUsageMessage = getConfiguration().getString("email-usage-message", this.emailUsageMessage);
/* 167 */
        this.emailChangeNotAllowedMessage = getConfiguration().getString("email-change-not-allowed-message", this.emailChangeNotAllowedMessage);
/* 168 */
        this.emailUnexpectedMessage = getConfiguration().getString("email-unexpected-message", this.emailUnexpectedMessage);
/* 169 */
        this.emailChangedMessage = getConfiguration().getString("email-changed-message", this.emailChangedMessage);
/* 170 */
        this.emailChangeErrorMessage = getConfiguration().getString("email-change-error-message", this.emailChangeErrorMessage);
/* 171 */
        PluginDescriptionFile pdfFile = getDescription();
/* 172 */
        PreparedStatement ps = null;
/* 173 */
        ResultSet rs = null;
/*     */
        try {
/* 175 */
            if (this.sourcetype == 0) {
/* 176 */
                if (!getDataFolder().exists())
/* 177 */ getDataFolder().mkdir();
/* 178 */
                File dbFile = new File(getDataFolder(), this.dbFileName);
/* 179 */
                if (!dbFile.exists()) {
/* 180 */
                    dbFile.createNewFile();
/*     */
                }
/* 182 */
                Scanner scanner = new Scanner(dbFile);
/* 183 */
                while (scanner.hasNext()) {
/* 184 */
                    String str = scanner.next();
/* 185 */
                    String[] split = str.split(":");
/* 186 */
                    if (split.length >= 2)
/* 187 */ this.db.put(split[0], split[1]);
/*     */
                }
/* 189 */
                scanner.close();
/* 190 */
                this.log.info("[" + pdfFile.getName() + "] " + this.db.size() + " user registrations loaded");
/*     */
            } else {
/* 192 */
                Class.forName(dbDriver);
/* 193 */
                this.mysql = DriverManager.getConnection(dbDb + "?autoReconnect=true&user=" + dbUsername + "&password=" + dbPassword);
/* 194 */
                if (convertOnStart.booleanValue()) {
/* 195 */
                    File dbFile = new File(getDataFolder(), this.dbFileName);
/* 196 */
                    if (dbFile.exists()) {
/* 197 */
                        int i = 0;
/* 198 */
                        Scanner scanner = new Scanner(dbFile);
/* 199 */
                        while (scanner.hasNext()) {
/* 200 */
                            String str = scanner.next();
/* 201 */
                            String[] split = str.split(":");
/* 202 */
                            if (split.length >= 2) {
/* 203 */
                                ps = this.mysql.prepareStatement("INSERT INTO `authorize_users` (`name`,`password`) VALUES (?,?)", 1);
/* 204 */
                                ps.setString(1, split[0]);
/* 205 */
                                ps.setString(2, split[1]);
/* 206 */
                                ps.executeUpdate();
/* 207 */
                                ps.close();
/* 208 */
                                i++;
/*     */
                            }
/*     */
                        }
/* 211 */
                        scanner.close();
/* 212 */
                        this.log.info("[" + pdfFile.getName() + "] " + i + " users converted from flatfile to mysql");
/*     */
                    }
/*     */
                }
/* 215 */
                ps = this.mysql.prepareStatement("SELECT COUNT(*) as `cnt` FROM `authorize_users`");
/* 216 */
                rs = ps.executeQuery();
/* 217 */
                if (rs.next()) {
/* 218 */
                    this.log.info("[" + pdfFile.getName() + "] " + rs.getInt("cnt") + " user registrations in database");
/*     */
                }
/*     */
            }
/* 221 */
            PluginManager pm = getServer().getPluginManager();
/* 222 */
            pm.registerEvent(Event.Type.PLAYER_JOIN, this.playerListener, Event.Priority.Lowest, this);
/* 223 */
            pm.registerEvent(Event.Type.PLAYER_QUIT, this.playerListener, Event.Priority.Lowest, this);
/* 224 */
            pm.registerEvent(Event.Type.PLAYER_COMMAND, this.playerListener, Event.Priority.Lowest, this);
/* 225 */
            pm.registerEvent(Event.Type.PLAYER_MOVE, this.playerListener, Event.Priority.Lowest, this);
/* 226 */
            pm.registerEvent(Event.Type.PLAYER_ITEM, this.playerListener, Event.Priority.Lowest, this);
/* 227 */
            pm.registerEvent(Event.Type.PLAYER_CHAT, this.playerListener, Event.Priority.Lowest, this);
/* 228 */
            pm.registerEvent(Event.Type.BLOCK_PLACED, this.blockListener, Event.Priority.Lowest, this);
/* 229 */
            pm.registerEvent(Event.Type.BLOCK_DAMAGED, this.blockListener, Event.Priority.Lowest, this);
/* 230 */
            pm.registerEvent(Event.Type.ENTITY_DAMAGED, this.entityListener, Event.Priority.Lowest, this);
/* 231 */
            this.log.info("[" + pdfFile.getName() + "] " + pdfFile.getName() + " plugin build " + pdfFile.getVersion() + " is enabled");
/*     */
        } catch (IOException e) {
/* 233 */
            this.log.warning("[" + pdfFile.getName() + "] Exception while parsing user db file:");
/* 234 */
            e.printStackTrace();
/* 235 */
            this.log.warning("[" + pdfFile.getName() + "] " + pdfFile.getName() + " plugin won't work!");
/*     */
            try
/*     */ {
/* 246 */
                if (ps != null)
/* 247 */ ps.close();
/* 248 */
                if (rs != null)
/* 249 */ rs.close();
/*     */
            }
/*     */ catch (SQLException localSQLException1)
/*     */ {
/*     */
            }
/*     */
        }
/*     */ catch (SQLException e)
/*     */ {
/* 237 */
            this.log.warning("[" + pdfFile.getName() + "] SQL Exception happend:");
/* 238 */
            e.printStackTrace();
/* 239 */
            this.log.warning("[" + pdfFile.getName() + "] " + pdfFile.getName() + " plugin won't work!");
/*     */
            try
/*     */ {
/* 246 */
                if (ps != null)
/* 247 */ ps.close();
/* 248 */
                if (rs != null)
/* 249 */ rs.close();
/*     */
            }
/*     */ catch (SQLException localSQLException2)
/*     */ {
/*     */
            }
/*     */
        }
/*     */ catch (ClassNotFoundException e)
/*     */ {
/* 241 */
            this.log.warning("[" + pdfFile.getName() + "] unable to found MySQL driver:");
/* 242 */
            e.printStackTrace();
/* 243 */
            this.log.warning("[" + pdfFile.getName() + "] " + pdfFile.getName() + " plugin won't work!");
/*     */
            try
/*     */ {
/* 246 */
                if (ps != null)
/* 247 */ ps.close();
/* 248 */
                if (rs != null)
/* 249 */ rs.close();
/*     */
            }
/*     */ catch (SQLException localSQLException3)
/*     */ {
/*     */
            }
/*     */
        }
/*     */ finally
/*     */ {
/*     */
            try
/*     */ {
/* 246 */
                if (ps != null)
/* 247 */ ps.close();
/* 248 */
                if (rs != null)
/* 249 */ rs.close();
/*     */
            } catch (SQLException localSQLException4) {
/*     */
            }
/*     */
        }
/*     */
    }

    /*     */
/*     */
    public boolean isAuthorized(int id) {
/* 255 */
        return this.authorizedIds.contains(Integer.valueOf(id));
/*     */
    }

    /*     */
/*     */
    public void unauthorize(int id) {
/* 259 */
        this.authorizedIds.remove(Integer.valueOf(id));
/*     */
    }

    /*     */
/*     */
    public void authorize(int id) {
/* 263 */
        this.authorizedIds.add(Integer.valueOf(id));
/*     */
    }

    /*     */
/*     */
    private String md5(String string) {
/*     */
        try {
/* 268 */
            MessageDigest digest = MessageDigest.getInstance("MD5");
/* 269 */
            digest.update(string.getBytes());
/* 270 */
            byte[] hash = digest.digest();
/* 271 */
            String result = "";
/* 272 */
            for (int i = 0; i < hash.length; i++) {
/* 273 */
                result = result + Integer.toString((hash[i] & 0xFF) + 256, 16).substring(1);
/*     */
            }
/* 275 */
            return result;
/*     */
        } catch (NoSuchAlgorithmException e) {
/* 277 */
            e.printStackTrace();
/*     */
        }
/* 279 */
        return null;
/*     */
    }

    /*     */
/*     */
    public boolean checkPassword(String player, String password) {
/* 283 */
        if (this.sourcetype == 0) {
/* 284 */
            String rmd5 = (String) this.db.get(player.toLowerCase());
/* 285 */
            if ((rmd5 != null) &&
/* 286 */         (rmd5.equals(md5(password))))
/* 287 */ return true;
/*     */
        } else {
/*     */
            try {
/* 290 */
                PreparedStatement ps = this.mysql.prepareStatement("SELECT `name` FROM `authorize_users` WHERE lower(`name`) = '" + player.toLowerCase() + "' and `password` = '" + md5(password) + "'");
/* 291 */
                ResultSet rs = ps.executeQuery();
/* 292 */
                if (rs.next())
/* 293 */ return true;
/*     */
            } catch (SQLException e) {
/* 295 */
                this.log.warning("[Authorize] SQL Exception:");
/* 296 */
                e.printStackTrace();
/*     */
            }
/*     */
        }
/* 299 */
        return false;
/*     */
    }

    /*     */
/*     */
    public void register(String player, String password) throws IOException, SQLException {
/* 303 */
        register(player, password, "");
/*     */
    }

    /*     */
/*     */
    public void register(String player, String password, String email) throws IOException, SQLException {
/* 307 */
        String md5 = md5(password);
/* 308 */
        if (this.sourcetype == 0) {
/* 309 */
            this.db.put(player.toLowerCase(), md5);
/* 310 */
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(getDataFolder(), this.dbFileName), true));
/* 311 */
            bw.append(player.toLowerCase() + ":" + md5);
/* 312 */
            bw.newLine();
/* 313 */
            bw.close();
/*     */
        } else {
/* 315 */
            PreparedStatement ps = this.mysql.prepareStatement("INSERT INTO `authorize_users` (`name`,`password`,`email`) VALUES (?,?,?)", 1);
/* 316 */
            ps.setString(1, player);
/* 317 */
            ps.setString(2, md5);
/* 318 */
            ps.setString(3, email);
/* 319 */
            ps.executeUpdate();
/*     */
        }
/*     */
    }

    /*     */
/*     */
    public void unregister(String player) throws IOException, SQLException {
/* 324 */
        if (this.sourcetype == 0) {
/* 325 */
            this.db.remove(player);
/* 326 */
            updateDb();
/*     */
        } else {
/* 328 */
            PreparedStatement ps = this.mysql.prepareStatement("DELETE FROM `authorize_users` WHERE `name` = '" + player + "' or `name` = '" + player.toLowerCase() + "'");
/* 329 */
            ps.executeUpdate();
/*     */
        }
/*     */
    }

    /*     */
/*     */
    public void changePassword(String player, String password) throws IOException, SQLException {
/* 334 */
        String md5 = md5(password);
/* 335 */
        if (this.sourcetype == 0) {
/* 336 */
            this.db.put(player.toLowerCase(), md5);
/* 337 */
            updateDb();
/*     */
        } else {
/* 339 */
            PreparedStatement ps = this.mysql.prepareStatement("UPDATE `authorize_users` SET `password` = '" + md5 + "' WHERE `name` = '" + player + "' or `name` = '" + player.toLowerCase() + "'");
/* 340 */
            ps.executeUpdate();
/*     */
        }
/*     */
    }

    /*     */
/*     */
    public void updateDb() throws IOException {
/* 345 */
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(getDataFolder(), this.dbFileName)));
/* 346 */
        Set keys = this.db.keySet();
/* 347 */
        Iterator i = keys.iterator();
/* 348 */
        while (i.hasNext()) {
/* 349 */
            String key = (String) i.next();
/* 350 */
            bw.append(key + ":" + (String) this.db.get(key));
/* 351 */
            bw.newLine();
/*     */
        }
/* 353 */
        bw.close();
/*     */
    }

    /*     */
/*     */
    public boolean isRegistered(String player) {
/* 357 */
        if ((this.sourcetype == 0) && (this.db.containsKey(player.toLowerCase())))
/* 358 */ return true;
/* 359 */
        if (this.sourcetype == 1) {
/*     */
            try {
/* 361 */
                PreparedStatement ps = this.mysql.prepareStatement("SELECT * FROM `authorize_users` WHERE lower(`name`) = '" + player.toLowerCase() + "'");
/* 362 */
                ResultSet rs = ps.executeQuery();
/* 363 */
                if (rs.next()) {
/* 364 */
                    this.db.put(rs.getString("name").toLowerCase(), rs.getString("password"));
/* 365 */
                    return true;
/*     */
                }
/*     */
            } catch (SQLException e) {
/* 368 */
                this.log.warning("[Authorize] SQL Exception:");
/* 369 */
                e.printStackTrace();
/*     */
            }
/*     */
        }
/* 372 */
        return false;
/*     */
    }

    /*     */
/*     */
    public void storeInventory(String player, ItemStack[] inventory) throws IOException {
/* 376 */
        File inv = new File(getDataFolder(), player + "_inv");
/* 377 */
        if (inv.exists())
/* 378 */ return;
/* 379 */
        inv.createNewFile();
/* 380 */
        BufferedWriter bw = new BufferedWriter(new FileWriter(inv));
/* 381 */
        for (short i = 0; i < inventory.length; i = (short) (i + 1)) {
/* 382 */
            bw.write(inventory[i].getTypeId() + ":" + inventory[i].getAmount() + ":" + (inventory[i].getData() == null ? "" : Byte.valueOf(inventory[i].getData().getData())) + ":" + inventory[i].getDurability());
/* 383 */
            bw.newLine();
/*     */
        }
/* 385 */
        bw.close();
/* 386 */
        this.inventories.put(player.toLowerCase(), inventory);
/*     */
    }

    /*     */
/*     */
    public ItemStack[] getInventory(String player) {
/* 390 */
        File f = new File(getDataFolder(), player + "_inv");
/* 391 */
        if (this.inventories.containsKey(player.toLowerCase())) {
/* 392 */
            if ((f.exists()) && (!f.delete()))
/* 393 */ this.log.warning("[Authorize] Unable to delete user inventory file: " + player + "_inv");
/* 394 */
            return (ItemStack[]) this.inventories.remove(player.toLowerCase());
/*     */
        }
/* 396 */
        if (f.exists()) {
/* 397 */
            ItemStack[] inv = new ItemStack[36];
/*     */
            try {
/* 399 */
                Scanner s = new Scanner(f);
/* 400 */
                short i = 0;
/* 401 */
                while (s.hasNextLine()) {
/* 402 */
                    String line = s.nextLine();
/* 403 */
                    String[] split = line.split(":");
/* 404 */
                    if (split.length == 4) {
/* 405 */
                        inv[i] =
/* 406 */               new ItemStack(Integer.valueOf(split[0]).intValue(), Integer.valueOf(split[1]).intValue(),
/* 406 */               Short.valueOf(split[3]).shortValue(), split[2].length() == 0 ? null : Byte.valueOf(split[2]));
/* 407 */
                        i = (short) (i + 1);
/*     */
                    }
/*     */
                }
/* 410 */
                s.close();
/* 411 */
                if (!f.delete())
/* 412 */ this.log.warning("[Authorize] Unable to delete user inventory file: " + player + "_inv");
/*     */
            } catch (IOException e) {
/* 414 */
                this.log.severe("[Authorize] Inventory file read error:");
/* 415 */
                e.printStackTrace();
/*     */
            }
/* 417 */
            return inv;
/*     */
        }
/*     */
/* 420 */
        return null;
/*     */
    }

    /*     */
/*     */
    public void changeEmail(String player, String email) throws SQLException {
/* 424 */
        if (this.sourcetype != 1)
/* 425 */ return;
/* 426 */
        PreparedStatement ps = this.mysql.prepareStatement("UPDATE `authorize_users` SET `email` = '" + email + "' WHERE `name` = '" + player + "' or `name` = '" + player.toLowerCase() + "'");
/* 427 */
        ps.executeUpdate();
/*     */
    }

    /*     */
/*     */
    public boolean checkEmail(String email)
/*     */ {
/* 432 */
        return !email.contains("'");
/*     */
    }

    /*     */
/*     */
    public boolean isAdmin(String player)
/*     */ {
/* 437 */
        return this.admins.contains(player.toLowerCase());
/*     */
    }
/*     */
}

/* Location:           D:\Misc\Games\Minecraft\Beta\Development\Server Bukkit\Authorize.jar
 * Qualified Name:     com.rena4ka.bukkit.auth.Authorize
 * JD-Core Version:    0.6.0
 */