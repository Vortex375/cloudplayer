package de.pandaserv.music.client.places;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceHistoryMapper;

public class AppPlaceHistoryMapper implements PlaceHistoryMapper {

	@Override
	public Place getPlace(String token) {
        return AppPlace.fromToken(token);
	}

	@Override
	public String getToken(Place place) {
        if (place instanceof AppPlace) {
			return ((AppPlace) place).toToken();
		}
		return null;
	}

}
