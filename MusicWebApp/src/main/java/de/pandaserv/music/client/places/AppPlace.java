package de.pandaserv.music.client.places;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import de.pandaserv.music.client.misc.JSUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AppPlace extends Place {

    private static List<AppPlace> allPlaces;
    static {
        /*
         * Register places
         */
        allPlaces = new ArrayList<AppPlace>();
        allPlaces.add(new WelcomePlace());
        allPlaces.add(new SearchPlace());
        // fail place is not registered as it has no token
    }

    protected Map<String, String> parameters = new HashMap<String, String>();

	public abstract String getId();
    public abstract <T extends AppPlace> T copy();

	@Override
	public boolean equals(Object obj) {
        if (obj instanceof AppPlace) {
            AppPlace other = (AppPlace) obj;
            boolean equal = true;
            equal &= this.getId().equals(other.getId());
            equal &= this.parameters.size() == other.parameters.size();

            for (String key: parameters.keySet()) {
                equal &= other.parameters.containsKey(key);
                if (!equal)
                    break;
                equal &= other.parameters.get(key).equals(this.parameters.get(key));
            }

            return equal;
        }

        return false;
	}

    public static AppPlace fromToken(String token) {
        // TODO: debug only
        GWT.log("AppPlace.fromToken(): known places:");
        for (AppPlace p: allPlaces) {
            GWT.log(p.getId());
        }
        for (AppPlace p: allPlaces) {
            if (token.startsWith(p.getId())) {
                AppPlace ret = p.copy();
                int start = token.indexOf("?");
                if (start > 0) {
                    Map<String, String> parameters = new HashMap<String, String>();
                    // parse parameters
                    String parameterString = token.substring(start + 1);
                    String[] parameterSplit = parameterString.split(",");
                    for (String s : parameterSplit) {
                        String[] split = s.split("=");
                        if (split.length != 2) {
                            GWT.log("Unable to parse parameters in history token " + token);
                            break;
                        }
                        parameters.put(split[0], split[1]);
                    }
                    ret.setParameters(parameters);
                }
                GWT.log("AppPlace.fromToken(): created " + ret + " for token " + token);
                return ret;
            }
        }

        JSUtil.log("Warning: no mapping found for history token: " + token);
        return new WelcomePlace();
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public String toToken() {
        if (parameters.size() > 0) {
            StringBuilder sb = new StringBuilder(getId());
            sb.append("?");
            for (String key : parameters.keySet()) {
                sb.append(key);
                sb.append("=");
                sb.append(parameters.get(key));
                sb.append(",");
            }
            String ret = sb.substring(0, sb.length() - 1); // skip trailing comma
            GWT.log("AppPlace.toToken(): created token " + ret + " for " + this);
            return ret;
        } else {
            GWT.log("AppPlace.toToken(): created token " + getId() + " for " + this);
            return getId();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AppPlace: ");
        sb.append(getId());
        sb.append(" ( ");
        for (String key: parameters.keySet()) {
            sb.append(key);
            sb.append("=");
            sb.append(parameters.get(key));
            sb.append(" ");
        }
        sb.append(");");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return toToken().hashCode();
    }
}
