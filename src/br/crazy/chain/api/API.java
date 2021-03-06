/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.crazy.chain.api;

import br.crazy.chain.Chain;
import br.crazy.chain.arena.Arena;
import br.crazy.chain.playerdata.PlayerData;
import br.crazy.chain.util.Util;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;

/**
 *
 * @author Hugo
 */
public class API {

    private final Chain main;
    private final Arena arena;

    private List<PlayerData> data;

    public API(Chain m, Arena arena) {
        this.main = m;
        this.arena = arena;
        this.data = new ArrayList<>();
        this.load();
    }

    public PlayerData get(Player p) {
        for (PlayerData pd : data) {
            if (p.getName().equals(pd.getName())) {
                return pd;
            }
        }
        return null;
    }

    public boolean has(Player p) {
        return get(p) != null;
    }

    public void join(Player p) {
        if (!has(p)) {
            PlayerData pd = new PlayerData(p.getName());
            insert(pd);
        }
        Util.pasteinventory(p);
        p.teleport(arena.getLocations().get(new Random().nextInt(arena.getPlayersSize())));
        arena.addPlayer(p);
    }

    public void quit(Player p) {
        Util.clearinventory(p);
        if (has(p)) {
            update(get(p));
        }
    }

    public void insert(PlayerData pd) {
        if (main.getMySQL().enable()) {
            return;
        }
        if (!data.contains(pd)) {
            try {
                if (!main.getMySQL().checkConnection()) {
                    main.getMySQL().openConnection();
                }
                PreparedStatement ps = main.getMySQL().getConnection().prepareStatement("INSERT INTO `Chain`(Player, Kills, Deaths, Points) VALUES (?, ?, ?, ?)");
                ps.execute();
                ps.closeOnCompletion();
                data.add(pd);
            } catch (SQLException | ClassNotFoundException ex) {
                Logger.getLogger(API.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    if (main.getMySQL().checkConnection()) {
                        main.getMySQL().closeConnection();
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(API.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void update(PlayerData pd) {
        if (main.getMySQL().enable()) {
            return;
        }
        if (data.contains(pd)) {
            try {
                if (!main.getMySQL().checkConnection()) {
                    main.getMySQL().openConnection();
                }
                PreparedStatement ps = main.getMySQL().getConnection().prepareStatement("UPDATE `Chain` SET Kills=?, Deaths=?, Points=? WHERE Player='" + pd.getName() + "'");
                ps.execute();
                ps.closeOnCompletion();
            } catch (SQLException | ClassNotFoundException ex) {
                Logger.getLogger(API.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    if (main.getMySQL().checkConnection()) {
                        main.getMySQL().closeConnection();
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(API.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void load() {
        if (main.getMySQL().enable()) {
            return;
        }
        try {
            if (!main.getMySQL().checkConnection()) {
                main.getMySQL().openConnection();
            }
            ResultSet rs = main.getMySQL().querySQL("SELECT * FROM Chain");
            while (rs.next()) {
                PlayerData pd = new PlayerData(rs.getString("Player"));
                pd.updateKills(rs.getInt("Kills"));
                pd.updateDeaths(rs.getInt("Deaths"));
                pd.updatePoints(rs.getInt("Points"));
                data.add(pd);
            }
            rs.close();
        } catch (SQLException | ClassNotFoundException ex) {
            Logger.getLogger(API.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (main.getMySQL().checkConnection()) {
                    main.getMySQL().closeConnection();
                }
            } catch (SQLException ex) {
                Logger.getLogger(API.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

}
