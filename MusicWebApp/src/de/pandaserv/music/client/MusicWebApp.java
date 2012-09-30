package de.pandaserv.music.client;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;

import de.pandaserv.music.client.activities.MyActivityMapper;
import de.pandaserv.music.client.places.MyPlace;
import de.pandaserv.music.client.places.MyPlaceHistoryMapper;


public class MusicWebApp implements EntryPoint {

	private static EventBus eventBus = new SimpleEventBus();
	
	private PlaceController placeController;
	private PlaceHistoryHandler historyHandler;
	private ActivityManager activityManager;
	
	public void onModuleLoad() {
		// set up place infrastructure
		placeController = new PlaceController(eventBus);
		historyHandler = new PlaceHistoryHandler(new MyPlaceHistoryMapper());
		historyHandler.register(placeController, getEventBus(),
				new MyPlace());
		
		// set up activity manager for main area
		activityManager = new ActivityManager(new MyActivityMapper(),
				getEventBus()); 
		
		// switch to current place
		historyHandler.handleCurrentHistory();
	}
	
	public static EventBus getEventBus() {
		return eventBus;
	}
}
