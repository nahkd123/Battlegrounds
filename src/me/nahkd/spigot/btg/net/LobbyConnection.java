package me.nahkd.spigot.btg.net;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.UUID;

import me.nahkd.eroto.ErotoConnection;
import me.nahkd.spigot.btg.Battlegrounds;

public class LobbyConnection {
	
	public Socket socket;
	public ErotoConnection connection;
	Battlegrounds plugin;
	
	public LobbyConnection(int port, Battlegrounds plugin) throws UnknownHostException, IOException {
		this.plugin = plugin;
		socket = new Socket("localhost", port);
		connection = new ErotoConnection(socket) {
			@Override
			public void processString(String input) {
				final String[] args = input.split("\u0000");
				if (args[0].equals("weaponskin")) {
					UUID uuid = UUID.fromString(args[1]);
					plugin.selectedSkins.get(uuid).put(args[2], args[3]);
					System.out.println("Lobby: Skin " + args[2] + " for " + uuid + " is " + args[3]);
				}
			}
		};
		connection.start();
		connection.setup(ErotoConnection.MESSAGETYPE_STRING);
	}
	
	public void skinRequest(String weaponId, UUID uuid) {
		connection.sendString("weaponskin\u0000" + uuid.toString() + "\u0000" + weaponId);
	}
	
	public void shutdown() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
