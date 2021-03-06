package sate2012.avatar.android.googlemaps;

import gupta.ashutosh.avatar.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import sate2012.avatar.android.AVATARMainMenuActivity;
import sate2012.avatar.android.Constants;
import sate2012.avatar.android.MapsForgeMapViewer;
import sate2012.avatar.android.PhoneCall;
import sate2012.avatar.android.UploadMedia;
import sate2012.avatar.android.VideoPlayer;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * 
<<<<<<< HEAD
 * @author Garrett + Matt + Craig
=======
 * @author Garrett + Matt emrickgarrett@gmail.com
>>>>>>> branch 'master' of https://github.com/AVATAR-PANIC/AVATAR-PANIC.git
 * 
 * Activity that handles the map and the drawing onto the map.
 * The actual Google Map is a fragment, created inside the XML
 *
 */
public class GoogleMapsViewer extends Fragment implements LocationListener,
InfoWindowAdapter, OnCameraChangeListener, OnMapClickListener, OnMarkerClickListener, 
OnInfoWindowClickListener, OnPreparedListener{

	public GoogleMap map;
	public Location myLocation = new Location(LocationManager.NETWORK_PROVIDER);
	public Location myCurrentLocation;
	private double myAltitude;
	private double myLatitude;
	private double myLongitude;
	private static float lastKnownZoomLevel;
	private static SensorManager mySensorManager;
	private boolean sensorrunning;
	private boolean hasMapCentered = false;
	public static int[] mapTypes = { GoogleMap.MAP_TYPE_NORMAL,
			GoogleMap.MAP_TYPE_SATELLITE, GoogleMap.MAP_TYPE_HYBRID,
			GoogleMap.MAP_TYPE_TERRAIN };
	public int currentMapType = GoogleMapsViewer.mapTypes[0];
	private ArrayList<MarkerPlus> markerArray = new ArrayList<MarkerPlus>();// = MarkerMaker.makeMarkers();
	private Marker activeMarker = null;
	private Bitmap currentImage = null;
	private boolean gettingURL = false;
	private boolean asyncTaskCancel = false;
	private MediaPlayer mp;
	private static View view;
	private MarkerPlus myMarkerLocation;
	private PointDeleter pointDeleter;
	public static final int PHONE_CALL = 77;

	/**
	 * When the Fragment View is created, this is called
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);

		if(view != null){
			ViewGroup parent = (ViewGroup) view.getParent();
			if(parent != null){
				parent.removeView(view);
			}
		}
		try{
			view = inflater.inflate(R.layout.googlemap_viewer, container, false);
		}catch(InflateException e){
			
		}
		return view;
	}
	
	/**
	 * When the fragment is started, this runs.
	 */
	@Override
	public void onStart() {
		super.onStart();
		MapFragment mapfrag = ((MapFragment) getFragmentManager()
				.findFragmentById(R.id.googlemap));
		map = mapfrag.getMap();
		new HttpThread(this).execute("");
		final GoogleMapsViewer activity = this;
		
		//Set the Maps listeners
		map.setOnMapLongClickListener(new Listener());
		map.setInfoWindowAdapter(this);
		map.setOnCameraChangeListener(this);
		map.setOnMapClickListener(this);
		map.setOnMarkerClickListener(this);
        map.setMyLocationEnabled(true);
        map.setOnInfoWindowClickListener(this);
        
        drawMarkers(true);
		map.setMapType(GoogleMapsViewer.mapTypes[0]);
		lastKnownZoomLevel = map.getCameraPosition().zoom;
		// TODO
		pointDeleter = new PointDeleter();
		
		//Declare the timer
		//This timer will run a thread to connect to the server every minute
		Timer httpTimer = new Timer();
		//Set the schedule function and rate
		this.placeLocation();
		httpTimer.scheduleAtFixedRate(new TimerTask() {

		    @Override
		    public void run() {
		        new HttpThread(activity).execute("");
		    }
		         
		},
		//Set how long before to start calling the TimerTask (in milliseconds)
		30000,
		//Set the amount of time between each execution (in milliseconds)
		30000);
		
		this.placeLocation();
		
	}
	
	@Override
	public void onPause(){
		super.onPause();
	}
	
	public void onResume(){
		super.onResume();
	}
	
	@Override
	public void onDetach(){
		super.onDetach();
	}
	
	/**
	 * Calls the markers to be clustered, and then drawn when complete
	 * @param shouldClear : Whether or not it should clear the markers from the map.
	 */
	public void drawMarkers(boolean shouldClear){

		
		//Below is clustering
		//Bounds are problematic
		//LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
		
		if(markerArray!= null && map != null){
			new GoogleMapsClusterMaker(markerArray, this, map.getProjection(), shouldClear).execute("");
		}
		
	}
	
	/**
	 * Draws the clusters, called from asynchronous task from above
	 * @param clusters : Clusters to draw
	 * @param shouldClear : Whether or not it should clear
	 */
	public void drawClusters(ArrayList<GoogleMapsClusterMarker> clusters, boolean shouldClear){
		
		//System.out.println("Cluster Complete");
		
		if(map != null){
			
			if(shouldClear){
					map.clear();
					System.out.println("Map Cleared");
			}
			//If the map was cleared and this wasn't here, it would stop showing the info window.
			if(activeMarker != null){
				activeMarker.showInfoWindow();
				System.out.println("Showing Window!");
			}
			
			if(markerArray != null && map != null){
				int i = 1;
				for(GoogleMapsClusterMarker marker: clusters){
					//if(bounds.contains(marker.latlng)){
						if(marker.getPoints().size() > 1){
							map.addMarker(new MarkerOptions().position(marker.latlng).title("Cluster: " + i++).snippet(marker.getPointNames() + " | " + marker.getPoints().size()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
			//				System.out.println("Added Marker! Position: " + new LatLng(marker.latlng.latitude, marker.latlng.longitude).toString());
			//				System.out.println("Marker Name!: " + marker.getPointNames());
						}else if(marker.getPoints().get(0).getName().equals("EMERGENCY")){
							map.addMarker(new MarkerOptions().position(marker.latlng).title(marker.getPoints().get(0).getName()).snippet(marker.getPoints().get(0).getData()));
						}else{
							if(marker.getPoints().size() == 1){
							map.addMarker(new MarkerOptions().position(marker.latlng).title(marker.getPoints().get(0).getName()).snippet(marker.getPoints().get(0).getData()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
							}
						}
					//}
				}
			}
		}
		
		
	}

	public void onMapClick(LatLng latlng){

	}
	
	/**
	 * Listener called when the info window of a marker is clicked
	 * @param marker : The marker whose info window was clicked.
	 */
	public void onInfoWindowClick(Marker marker){
		System.out.println("CLICKED!");
		if(marker != null){
				try{
					if(marker.getSnippet().contains(".f4v")){
						Bundle data = new Bundle();
						data.putString("video_tag", marker.getSnippet().substring(marker.getSnippet().lastIndexOf(" ") + 1));
						VideoPlayer videoPlayer = new VideoPlayer();
						videoPlayer.setArguments(data);
						
						FragmentManager fragMgr = getFragmentManager();
						
						FragmentTransaction xact = fragMgr.beginTransaction();
						
						if(fragMgr.findFragmentByTag("VIDEO_PLAYER") != null){
							xact.replace(R.id.container, fragMgr.findFragmentByTag("VIDEO_PLAYER"), "VIDEO_PLAYER");
						}else{
							xact.replace(R.id.container, videoPlayer, "VIDEO_PLAYER");
						}
							xact.addToBackStack(null);
						xact.commit();
					}
					if(marker.getSnippet().contains(".mp4")){
						playSound( marker.getSnippet().substring(marker.getSnippet().lastIndexOf(" ") + 1));
						//new SoundPlayer(this, marker.getSnippet().substring(marker.getSnippet().lastIndexOf(" ") + 1)).execute();
					}
				}catch(Exception ex){
					ex.printStackTrace();
			}
		}
	}
	
	/**
	 * Important for knowing if it should start loading another image, or continue with the image it is loading.
	 */
	public boolean onMarkerClick(Marker marker){
		
		if(activeMarker != null){
			if(!activeMarker.getSnippet().equals(marker.getSnippet())){
				currentImage = null;
				asyncTaskCancel = true;
				//System.out.println("Snippets No Match");
			}
		}
		this.activeMarker = marker;
		return false;
	}

	/**
	 * Method called whenever the user's location is changed
	 * 
	 * @param loc
	 *            : New location of the user.
	 */
	public void onLocationChanged(Location loc) {

		// Debugging Tools
		// String TAG = "Testing: ";
		//
		// Log.d(TAG, "Latitude: " + String.valueOf(myLatitude));
		// Log.d(TAG, "Longitude: " + String.valueOf(myLongitude));
		// Log.d(TAG, "Altitude: " + String.valueOf(myAltitude));
		
		// System.out.println("HEY");
		System.out.println("HEY");
		this.placeLocation();
		

		if (!hasMapCentered) {
			map.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition
					.fromLatLngZoom(new LatLng(myLocation.getLatitude(),
							myLocation.getLongitude()),
							map.getMaxZoomLevel() / 3)));
			hasMapCentered = true;
		}
	}
	
	public void placeLocation(){
		Log.i("TestLocationPoint", "CALLED");
		Location loc = map.getMyLocation();
		if(loc != null){
			myLatitude = loc.getLatitude();
			myLongitude = loc.getLongitude();
			myAltitude = loc.getAltitude();
			
			String myID = android.provider.Settings.Secure.ANDROID_ID;
			MarkerPlus tempPoint = new MarkerPlus(myLatitude, myLongitude, myAltitude);
			if(myMarkerLocation != null)
			{
				markerArray.remove(myMarkerLocation);
				//pointDeleter.execute(myMarkerLocation.getDate());
			}
			tempPoint.setName(myID + "'s location");
			tempPoint.setInfo("User Location Point");
			map.addMarker(new MarkerOptions().position(new LatLng(myLatitude,
					myLongitude)));
			markerArray.add(tempPoint);
			myMarkerLocation = tempPoint;
			drawMarkers(true); 
			UploadMedia.HttpSender httpSender = new UploadMedia.HttpSender();
			//httpSender.execute(tempPoint.getName(), tempPoint.getLatitude() + "",
			//		tempPoint.getLongitude() + "", "0", myID + "'s location");
			
		}
	}
	
	/**
	 * OnLongClickListener that this class uses as a listener
	 * @author Matthew Weber
	 *
	 */
	class Listener implements OnMapLongClickListener  {

		@Override
		public void onMapLongClick(LatLng arg0) {
			try{
				Intent senderIntent = new Intent(getActivity().getApplicationContext(),
						UploadMedia.class);
				senderIntent.putExtra("LatLng", arg0);
				startActivity(senderIntent);
			}catch(Exception ex){
				ex.printStackTrace();
			}
			MarkerPlus tempPoint = new MarkerPlus(arg0.latitude, arg0.longitude, map.getMyLocation().getAltitude());
			tempPoint.setName("User Point");
			tempPoint.setInfo("User Submitted Point");
			markerArray.add(tempPoint);
			drawMarkers(true);
			placeLocation();
			if(map != null && markerArray != null){
				MarkerPlus tempPoint1 = new MarkerPlus(arg0.latitude, arg0.longitude, map.getMyLocation().getAltitude());
				tempPoint1.setName("User Point");
				tempPoint1.setInfo("User Submitted Point");
				markerArray.add(tempPoint1);
				drawMarkers(true);
			}
		}
	}

	/**
	 * Not really used for anything just yet.
	 */
	private SensorEventListener mySensorEventListener = new SensorEventListener() {
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		public void onSensorChanged(SensorEvent event) {
		}
	};

	@Override
	public void onProviderDisabled(String arg0) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	// Info Window Adapter implemented methods

	
	/**
	 * Method that basically creates what is to be displayed in the info window of the Google maps.
	 * Remember that the map is a static image.
	 * 
	 * @param marker : Marker to get the info contents from.
	 * @return : View that will be displayed in the window
	 */
	@Override
	public View getInfoContents(Marker marker) {
		View v = null;
		try{
		v = getActivity().getLayoutInflater().inflate(R.layout.marker_contents, null);

		// Getting reference to the TextView to set title
		TextView title = (TextView) v.findViewById(R.id.marker_title);
		TextView info = (TextView) v.findViewById(R.id.marker_info);
		ImageView image = (ImageView) v.findViewById(R.id.marker_image);
		image.setPadding(0, 0, 5, 0);

		//Determining what is the snippit, what is not.
		int snippetIndex = marker.getSnippet().length();
		int clusterSize = 1;
		if(marker.getSnippet().contains("|")){
			snippetIndex = marker.getSnippet().lastIndexOf("|")-1;
			clusterSize = Integer.parseInt(marker.getSnippet().substring(snippetIndex+3));
		}
		
		title.setText(marker.getTitle());
		info.setText(marker.getSnippet().substring(0, snippetIndex));
		// image.setImageDrawable();
        
		//If the Marker is not a "Cluster" of points. IE just one point.
		if(!(new String("Cluster").regionMatches(0, marker.getTitle(), 0, 6))){
	       
			//If there is no current image, and it is not currently getting a url, and the marker has an image
			if(currentImage == null && !gettingURL && (marker.getSnippet().contains("png") || marker.getSnippet().contains("jpg") || marker.getSnippet().contains("gif"))){
				activeMarker = marker;
				gettingURL = true;
				//Start new asynchronous thread to grab the image (Class below)
		        new ImageGrabber(image, this).execute(marker.getSnippet().substring(marker.getSnippet().lastIndexOf(" ")));
		        try {
		        	//If image received was not null
					if(currentImage != null){
						image.setImageDrawable(new BitmapDrawable(null , currentImage));
					}else{
						image.setImageDrawable(new BitmapDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.loading)));
					}
				} catch (Exception e){
					e.printStackTrace();
				}
		        //If the current image is not null 
			}else if(currentImage != null){
				image.setImageDrawable(new BitmapDrawable(null, currentImage));
			}//If the marker has an image, draw the "Loading" image I have created
			else if(marker.getSnippet().contains("png") || marker.getSnippet().contains("jpg") || marker.getSnippet().contains("gif")){
				image.setImageDrawable(new BitmapDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.loading)));
			}//If the image is a video, draw the loading icon temporarily
			else if(marker.getSnippet().contains(".f4v")){
				image.setImageDrawable(new BitmapDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.videocambuttonbackground)));
			}
			else if(marker.getSnippet().contains(".mp4")){
				image.setImageDrawable(new BitmapDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.audiobuttonbackground)));
			}else if(marker.getTitle().equals("EMERGENCY")){
				image.setImageDrawable(new BitmapDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.emergency)));
				
			}
		}//If it is a cluster, draw the ic_launcher and add the number of points within the cluster to it for display
		else{
			Bitmap clusterImage = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
			Bitmap tempImage = clusterImage.copy(Bitmap.Config.ARGB_8888, true);
			Canvas canvas = new Canvas(tempImage);
			Paint paint = new Paint();
			paint.setColor(Color.WHITE);
			int xOffset = (int) (3*(clusterSize+"").length());
			canvas.drawText(clusterSize+"", tempImage.getWidth()/2-xOffset, tempImage.getHeight()/2+4, paint);

			image.setImageBitmap(tempImage);
			clusterImage.recycle();
		}
		}catch(Exception ex){}
        //Returning the view containing InfoWindow contents
        return v;

	}

	/**
	 * Not really used for anything atm.
	 */
	@Override
	public View getInfoWindow(Marker marker) {
		View v = null;// getLayoutInflater().inflate(R.layout.marker_info_window,
						// null);
		return v;
	}

	/**
	 * Listener method that is called whenever the Camera Position is changed
	 * @param arg0 : The Camera Position.
	 */
	@Override
	public void onCameraChange(CameraPosition arg0) {
		
		if(arg0.zoom != lastKnownZoomLevel){
			lastKnownZoomLevel = arg0.zoom;
			activeMarker = null;
			drawMarkers(true);
		}
		
		
		Projection projection = map.getProjection();
		LatLng xLat = projection.fromScreenLocation(new Point(0,0));
		
		map.addGroundOverlay(new GroundOverlayOptions().image(BitmapDescriptorFactory.fromResource(R.drawable.pointkey)).position(xLat, 1));
		
		//drawMarkers(true);

	}
	
	/**
	 * Setter method used to set the markerArray
	 * @param array : Array to set to the markerArray
	 */
	public void setMarkerArray(ArrayList<MarkerPlus> array){
		if(markerArray == null){
			markerArray = new ArrayList<MarkerPlus>();
		}
		this.markerArray = array;
		//System.out.println("Set the Array!");
	}
	
	/**
	 * Plays the sound from a url
	 * @param url : URL where the sound is located
	 */
	public void playSound(String url){
		try{
			if(mp != null){
				mp.release();
				mp.stop();
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		mp = new MediaPlayer();
		try {
			mp.setDataSource(url);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mp.prepareAsync();
		mp.setOnPreparedListener(this);
		
	}
	
	/**
	 * Listener to tell when the MediaPlayer has prepared the sound
	 * @param mp : The media player that prepared the sound.
	 */
	public void onPrepared(MediaPlayer mp){
		mp.start();
	}
	
	/**
	 * Getter method for the Google Map
	 * @return : The Google Map
	 */
	public GoogleMap getMap(){
		return map;
	}
	
	
	/**
	 * 
	 * @author Garrett Emrick emrickgarrett@gmail.com
	 * 
	 * Class that will run Asynchronously when executed to get the image for a file
	 * This class will try and cancel the thread if another marker is clicked
	 * If the entire thread is successful, returns the bitmap retrieved from the URL
	 * scaled to fit the dimensions needed to fit nicely within the Google Maps Info Window
	 * 
	 *
	 */
	private class ImageGrabber extends AsyncTask<String, Void,  Bitmap>{

		private ImageView imageSlot;
		private GoogleMapsViewer map;
		private String url;
		
		/**
		 * Constructor, make sure to put the ImageView where we want the image to display.
		 * @param imageSlot : The ImageView where we want the image to appear.
		 * @param map : The map that we want to have the image to appear on (Should be a reference to above class).
		 */
		public ImageGrabber(ImageView imageSlot, GoogleMapsViewer map){
			this.imageSlot = imageSlot;
			this.map = map;
		}
		
		/**
		 * Asynchronous task that gets the Image from a URL
		 * @return : The Bitmap the method got from the URL
		 */
		@Override
		protected Bitmap doInBackground(String...params) {
			// TODO Auto-generated method stub
			try {
				currentImage = null;
				url = params[0];
				if(asyncTaskCancel){
					//System.out.println("CANCEL 1");
					asyncTaskCancel = false;
					gettingURL = false;
					currentImage = null;
					this.cancel(true);
				}
				//System.out.println("Getting URL!");
				
				//Get the connection, set it to a bitmap
				HttpURLConnection connection = (HttpURLConnection) new URL(params[0]).openConnection();
			    connection.connect();
			    connection.setConnectTimeout(5000);
			    connection.setReadTimeout(5000);
			    InputStream input = connection.getInputStream();
			    Bitmap x = BitmapFactory.decodeStream(input);
			    
			    //Max Image Height and Width
			    int MAXWIDTH = 150;//= 270;
			    int MAXHEIGHT = 100;//=150;
			    
			    if(x != null){
				    int imageWidth = x.getWidth();
				    int imageHeight = x.getHeight();
				    
				    //Find out if image dimensions are too large, then sizes it appropriately.
				    if(imageWidth > MAXWIDTH || imageHeight > MAXHEIGHT){
				    	
				    	//Ternary, determines which is larger: image or height?
				    	double ratio = (imageWidth > imageHeight)? ((float) MAXWIDTH)/imageWidth: ((float) MAXHEIGHT)/imageHeight;
				    	
				    	imageWidth =(int) (imageWidth*ratio);
				    	imageHeight =(int) (imageHeight*ratio);
				    	
				    	x = Bitmap.createScaledBitmap(x, imageWidth, imageHeight, false); //Create scaled Bitmap
				    }
				    
				    if(asyncTaskCancel){
				    	//System.out.println("CANCEL 2");
				    	asyncTaskCancel = false;
				    	gettingURL = false;
				    	currentImage = null;
				    	x = null;
				    	this.cancel(true);
				    }
				    
				    //close connections, return the x value. Goes to OnPostExecute() method.
				    input.close();
				    connection.disconnect();
					return x;
				    }
			    input.close();
			    connection.disconnect();
			    return null;
			} catch (Exception e){
				gettingURL = false;
				e.printStackTrace();
				return null;
			}
		}
		
		/**
		 * Runs after the thread, sets the image and calls the re-draw method if image is correct.
		 */
		@Override
		protected void onPostExecute(Bitmap results){
			if(asyncTaskCancel && !(activeMarker.getSnippet().substring(activeMarker.getSnippet().lastIndexOf(" ")).equals(url))){
				//System.out.println("CANCEL 3");
				asyncTaskCancel = false;
				gettingURL = false;
				results = null;
				currentImage = null;
				map.drawMarkers(true);
				this.cancel(true);
			}else if(!(activeMarker.getSnippet().substring(activeMarker.getSnippet().lastIndexOf(" ")).equals(url))){
				//System.out.println("CANCEL 5");
				asyncTaskCancel = false;
				gettingURL = false;
				results = null;
				currentImage = null;
				map.drawMarkers(true);
				this.cancel(true);
			}else{
				imageSlot.setImageBitmap(results);
				currentImage = results;
				map.drawMarkers(true);
				gettingURL = false;
				imageSlot = null;
				map = null;
			}
			
		}
			
	}
	
	private class PointDeleter extends AsyncTask<String, Void, Boolean>{

		@Override
		protected Boolean doInBackground(String... params) {
			Boolean deleted = new Boolean(false);
			// TODO Auto-generated method stub
			int tries = 0;
			while(tries < 3){
				try {
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
					nameValuePairs.add(new BasicNameValuePair("date", params[0]));
					System.out.println("TRYING TO CONNECT");
					HttpClient client = new DefaultHttpClient();
					HttpPost post = new HttpPost(new URI("http://" + Constants.SERVER_ADDRESS + "/deletePoint.php"));
					post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					client.execute(post);
					//HELP!!!
					tries = 3;
				} catch (Exception e) {
					e.printStackTrace();
					tries++;
				}
			}
			return deleted;
		}
	}
	

}
