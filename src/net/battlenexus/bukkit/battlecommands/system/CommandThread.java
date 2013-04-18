package net.battlenexus.bukkit.battlecommands.system;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import net.battlenexus.bukkit.battlecommands.db.SQL;
import net.battlenexus.bukkit.battlecommands.main.BattleCommands;

public class CommandThread extends Thread {
	boolean run;
	@Override
	public void run() {
		while (run) {
			if (interrupted())
				break;
			checkCommands();
			try {
				Thread.sleep(60000 * 5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void startRun() {
		run = true;
		start();
	}

	public void stopRun() {
		run = false;
		interrupt();
		try {
			join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void checkCommands() {
		final SQL sql = BattleCommands.getInstance().getSQL();
		ResultSet r = sql.fillData("SELECT id,command FROM " + sql.getPrefix() + "commands WHERE executed=0");
		try {
			ArrayList<PreparedStatement> temp = new ArrayList<PreparedStatement>();
			while (r.next()) {
				try {
					String command = r.getString("command");
					BattleCommands.getInstance().executeCommand(command);
					PreparedStatement p = sql.getConnection().prepareStatement("UPDATE " + sql.getPrefix() + "commands SET executed=1 WHERE id=" + r.getInt("id"));
					temp.add(p);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			for (PreparedStatement p : temp) {
				p.executeUpdate();
			}
			temp.clear();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			if (!r.isClosed())
				r.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
