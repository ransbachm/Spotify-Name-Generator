package namehit;


import static spark.Spark.get;
import static spark.Spark.port;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hc.core5.http.ParseException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.enums.ModelObjectType;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.PlaylistSimplified;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import com.wrapper.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest;

public class Namehit {
	
	static String lastCode = null;
	static String lastState = null;
	
	public static void main(String [] args) throws ParseException, SpotifyWebApiException, IOException  {
		// calm down logger
		Logger.getRootLogger().setLevel(Level.ERROR);
		BasicConfigurator.configure();
		
		// API credentials
		String client_id = TokenManager.getID();
		String client_secret = TokenManager.getSecret();
		URI redirect_url = SpotifyHttpManager.makeUri("http://localhost:8888");
		
		String token = "";
		
		SpotifyApi authApi = new SpotifyApi.Builder()
				.setClientId(client_id)
				.setClientSecret(client_secret)
				.setRedirectUri(redirect_url)
				.build();
		
		String localState = getSafeState();
		AuthorizationCodeUriRequest req = authApi.authorizationCodeUri()
				.state(localState)
				.scope("playlist-read-collaborative,playlist-read-private,user-library-read")
				.show_dialog(true)
				.build();
		
		port(8888);
		get("/", (e, f) -> {
			lastCode = e.queryParams("code");
			lastState = e.queryParamsSafe("state");
			return "all done";
		});
		Desktop.getDesktop().browse(req.execute());
		
		while(!(lastCode != null && lastState != null) ) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		if(!localState.equals(lastState)) {
			System.out.println(localState);
			System.out.println(lastState);
			System.out.println("code : " + lastCode);
			System.out.println("STATE DID NOT MATCH - STOPPING");
			System.exit(1);
		}
		System.out.println("state did match");
		
		AuthorizationCodeRequest tokenReq = authApi.authorizationCode(lastCode).build();
		token = tokenReq.execute().getAccessToken();
		
		
		//System.out.println(token);
		
		// do the actual data gathering
		
		SpotifyApi api = SpotifyApi.builder()
				.setAccessToken(token)
				.build();
		
		GetListOfCurrentUsersPlaylistsRequest listsReq = api.getListOfCurrentUsersPlaylists()
				.limit(50)
				.build();
		
		
		PlaylistSimplified[] lists = listsReq.execute().getItems();
		
		LinkedList<String> rawNames = new LinkedList<>();
		LinkedList<String> names = new LinkedList<>();
		
		for(PlaylistSimplified list : lists) {
			Paging<PlaylistTrack> tracks = api.getPlaylistsItems(list.getId())
					.limit(100)
					.build().execute();
			System.out.println(list.getId());
			if(tracks.getTotal() > tracks.getLimit()) {
				System.out.println("Too many songs in " + list.getName());
			}
			for(PlaylistTrack plTrack : tracks.getItems()) {
				if(plTrack.getTrack().getType() != ModelObjectType.TRACK) continue; // ignore all but songs
				Track track = ((Track) plTrack.getTrack());
				String name = track.getName();
				//System.out.println(name);
				rawNames.add(name);
			}
		}
		Pattern regex = Pattern.compile("[^A-Za-z0-9\\s]");
		
		Iterator<String> iter = rawNames.iterator();
		while(iter.hasNext()) {
			String name = iter.next();
			Matcher matcher = regex.matcher(name);
			
			if(matcher.find()) {
				name = name.substring(0, matcher.start());
				for(String s : name.split(" ")) {
					if(s.length() > 4) { // "Only noun filter"
						names.add(s);
					}
				}
			}
		}
		
		Arrays.stream(names.toArray()).forEach(System.out::println);;
		System.out.println();
		
		for(int i=0; i<1000_000; i++) {
			System.out.println(getRandomName(names, 3));
		}
		System.exit(0);
		
	}
	
	private static String getRandomName(List<String> from, int complexity) {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<complexity; i++) {
			int random = (int) Math.floor(Math.random() * from.size());
			sb.append(from.get(random));
			if(i<complexity-1) { // if not last
				sb.append('-');
			}
		}
		return sb.toString();
	}
	
	private static String getSafeState() {
		return getRandomString(256);
	}
	
	private static String getRandomString(int length) {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<length; i++) {
			sb.append(getRandomChar());
		}
		return sb.toString();
	}
	
	private static char getRandomChar() {
		try {
			int random = SecureRandom.getInstanceStrong().nextInt(25);
			return (char) (97 + random);
		} catch (NoSuchAlgorithmException e) {
			throw new Error();
		}
	}

}
