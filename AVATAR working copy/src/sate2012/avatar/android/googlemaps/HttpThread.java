package sate2012.avatar.android.googlemaps;

import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import sate2012.avatar.android.Constants;
import sate2012.avatar.android.augmentedrealityview.CameraView;
import android.os.AsyncTask;
import android.util.JsonReader;

/**
 * 
 * @author ? + Tweaking by Garrett Emrick emrickgarrett@gmail.com
 * 
 * Reworked this thread to be asynchronous and to continue establishing connections.
 * Will now use methods in both camera view and the Google Maps Viewer to update
 * the points, as to not make those applications wait on them.
 *
 */
public class HttpThread extends AsyncTask<String, Void, ArrayList<MarkerPlus>>{

		private GoogleMapsViewer maps;
		private CameraView cameraView;
		private boolean success;
		
		/**
		 * Constructor. Use this for the GoogleMapsViewer
		 * @param maps : The GoogleMapsViewer
		 */
		public HttpThread(GoogleMapsViewer maps){
			this.maps = maps;
		}
		
		/**
		 * Constructor. Use this for the CameraView
		 * @param view : The CameraView
		 */
		public HttpThread(CameraView view){
			this.cameraView = view;
		}
		
		
		/**
		 * Asynchronous task, runs in background. Gets the connection for the application to the server
		 */
		@Override
		protected ArrayList<MarkerPlus> doInBackground(String...args) {

			ArrayList<MarkerPlus> markerArray = new ArrayList<MarkerPlus>();
			int tries = 0;
			
			connect:
			while(tries < 3){
				try {
					System.out.println("TRYING TO CONNECT");
					HttpClient client = new DefaultHttpClient();
					HttpGet get = new HttpGet(new URI("http://" + Constants.SERVER_ADDRESS + "/jsonPoints.php"));
					HttpResponse response = client.execute(get);
					JsonReader reader = new JsonReader(new InputStreamReader(response.getEntity().getContent()));
					
					reader.beginArray();
					while(reader.hasNext()){
						reader.beginObject();
						String data = "";
						MarkerPlus marker = new MarkerPlus();
						while(reader.hasNext()){
							try{
							String name = reader.nextName();
							if(name.equals("Name")){
								String pointName = reader.nextString();
								//System.out.println("NAME: " + pointName);
								marker.setName(pointName);
							}else if(name.equals("Lat")){
								double latitude = reader.nextDouble();
								//System.out.println("LATITUDE: " + latitude);
								marker.setLatitude(latitude);
							}else if(name.equals("Long")){
								double longitude = reader.nextDouble();
								//System.out.println("LONGITUDE: " + longitude);
								marker.setLongitude(longitude);
							}else if(name.equals("Alt (ft)")){
								double altitude = reader.nextDouble();
								//System.out.println("ALTITUDE: " + altitude);
								marker.setAltitude(altitude);
							}else if(name.equals("Date")){
								String date = reader.nextString();
								data += "Upload Date: " + date + "\r\n";
								//System.out.println("DATE: " + date);
							}else if(name.equals("Link")){
								String link = reader.nextString();
								data += "Data: " + link;
								//System.out.println("LINK: " + link);
								//marker.setImage(Drawable.createFromStream(((InputStream)new java.net.URL(link).getContent()), "BLAH"));
							}
							}catch(IllegalStateException e){
								System.out.println(reader.nextString());
							}
						}
						marker.setData(data);
						markerArray.add(marker);
						reader.endObject();	
					}
					//HELP!!!
					reader.endArray();
					reader.close();
					break connect;
				} catch (Exception e) {
					e.printStackTrace();
					tries++;
				}
			}
			return markerArray;
		}
		
		/**
		 * Called after the doInBackground is complete.
		 * Puts the markers where they need to go.
		 */
		@Override
		public void onPostExecute(ArrayList<MarkerPlus> array){
			
			if(array!= null){
				if(maps != null){
					maps.setMarkerArray(array);
					maps.drawMarkers(true);
					maps = null;
				}else{
					cameraView.setMarkerArray(array);
					cameraView = null;
					
				}
			}
		}
}
