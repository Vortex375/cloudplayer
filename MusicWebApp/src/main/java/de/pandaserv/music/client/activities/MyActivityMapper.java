package de.pandaserv.music.client.activities;

import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import de.pandaserv.music.client.places.AdminPlace;
import de.pandaserv.music.client.places.SearchPlace;
import de.pandaserv.music.client.places.WelcomePlace;

import java.util.HashMap;
import java.util.Map;

public class MyActivityMapper implements ActivityMapper {

    private Map<Place, MyActivity> cache;

    public MyActivityMapper() {
        cache = new HashMap<Place, MyActivity>();
    }

    @Override
    public MyActivity getActivity(Place place) {
        try {
            MyActivity ret = null;

            /*
             * check cache first
             */
            if (cache.containsKey(place)) {
                // get cached activity for place
                return cache.get(place);
            }

            if (place instanceof WelcomePlace) {
                ret = new WelcomeActivity();
            } else if (place instanceof SearchPlace) {
                ret = new SearchActivity();
            } else if (place instanceof AdminPlace) {
                ret = new AdminActivity();
            }

            if (ret != null && ret instanceof CacheableActivity) {
                cache.put(place, ret);
            }

            return ret;
        } catch (RuntimeException e) {
            // something went wrong while initializing the activity e.g. missing parameter
            return null;
        }
    }

}
