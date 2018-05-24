package fsa;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class FSAHelper {

	public static SortedSet<Entry<Integer,String>> getAuthorities() throws IOException { //Opted for sorted set on assumption that Authority list would be preferred in alphabetical order
		BufferedReader rd = null;
		JsonReader jsonReader = null;
		JsonObject result = null;
		try {
			
			InputStream is = getConnection(new URL("http://api.ratings.food.gov.uk/Authorities?pageSize=0"));
			if(is !=null) {
				rd = new BufferedReader(new InputStreamReader(is));
				jsonReader = Json.createReader(rd);
				result =  jsonReader.readObject();
			}
			
			if(result !=null) {
				JsonArray authorities = result.getJsonArray("authorities");
				if(authorities != null) {
					HashMap<Integer, String> authoritymap = mapAuthorities(authorities);
					return sortMap(authoritymap);
				}  
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(rd !=null) {
				rd.close();
			}
		}
		return null;
	}
	
	public static InputStream getConnection(URL url) {
		try {
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("x-api-version", "2");
			return conn.getInputStream();
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return null;
	}
	
	//JSonArrays are immutable so to merge them without throwing an exception this method gets the elements of both arrays and puts them into a builder that creates a fresh jsonArray.
	public static JsonArray mergeJSONArrays(JsonArray array1, JsonArray array2) { 
		JsonArrayBuilder builder = Json.createArrayBuilder();	
		for(int i = 0; i < array1.size(); i++) {
			builder.add(array1.get(i));
		}
		for(int i = 0; i < array2.size(); i++) {
			builder.add(array2.get(i));
		}
		return builder.build(); 
	}
	
	private static	HashMap<Integer, String>  mapAuthorities(JsonArray authorities) {
		HashMap<Integer, String> authoritymap = new HashMap<Integer, String>();   
		for(int i = 0; i < authorities.size(); i++ ) {
			JsonObject authority = authorities.getJsonObject(i);
			if(authority !=null) {
				String name = authority.getString("Name");
				Integer id = authority.getInt("LocalAuthorityId");
				
				if(id != null && name !=null) {
					authoritymap.put(id, name);
				}
			}
		}
		return authoritymap;
	}
	
	//Sorts data from hashmap into an alphabetically sorted data structure (SortedSet)
	private static SortedSet<Entry<Integer,String>> sortMap( HashMap<Integer, String> map){
		
		   SortedSet<Map.Entry<Integer, String>> sortedSet = new TreeSet<>(new Comparator<Map.Entry<Integer,String>>() {
		        @Override
		        public int compare(Entry<Integer, String> o1, Entry<Integer, String> o2) {
		            int result = o2.getValue().compareTo(o1.getValue());
		            result*=-1;
		            if(result==0)
		                result = o2.getKey().compareTo(o1.getKey());

		            return result;
		        }
		    });
		   
		   sortedSet.addAll(map.entrySet());
		   return sortedSet;
		   
	}
}
