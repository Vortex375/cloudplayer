package de.pandaserv.music.server.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import de.pandaserv.music.shared.Track;
import de.pandaserv.music.shared.TrackDetail;
import de.pandaserv.music.server.database.TrackDatabase;
import de.pandaserv.music.server.misc.HttpUtil;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;

public class DatabaseService extends AbstractHandler{
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (!request.getMethod().equals(HttpMethods.GET)) {
            HttpUtil.fail(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Invalid method for stream. The only supported method is GET.",
                    baseRequest, response);
            return;
        }

        StringTokenizer tk = new StringTokenizer(target, "/");
        if (!tk.hasMoreTokens()) {
            HttpUtil.fail(HttpServletResponse.SC_NOT_FOUND, "The database object cannot be called directly.",
                    baseRequest, response);
            return;
        }

        // TODO: only "database/tracks" for now
        String type = tk.nextToken();
        switch (type) {
            case "tracks": {
                if (!tk.hasMoreTokens()) {
                    listTracks(response);
                    baseRequest.setHandled(true);
                } else {
                    try {
                        long id = Long.parseLong(tk.nextToken());
                        getTrackInfo(id, response);
                        baseRequest.setHandled(true);
                    } catch (NumberFormatException e) {
                        HttpUtil.fail(HttpServletResponse.SC_NOT_FOUND, "Invalid track id",
                                baseRequest, response);
                    }
                }
                break;
            }
            default: {
                HttpUtil.fail(HttpServletResponse.SC_NOT_FOUND, "Unknown object: " + type,
                        baseRequest, response);
                return;
            }
        }
    }

    private void listTracks(HttpServletResponse response) throws IOException {
        List<TrackDetail> tracks = TrackDatabase.getInstance().listTracks();

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        JsonFactory jsonFactory = new JsonFactory();
        JsonGenerator jg = jsonFactory.createJsonGenerator(response.getOutputStream());

        jg.writeStartArray();
        for (TrackDetail detail: tracks) {
            jg.writeStartObject();
            jg.writeFieldName("id");
            jg.writeNumber(detail.getId());
            jg.writeFieldName("title");
            jg.writeString(detail.getTitle());
            jg.writeFieldName("artist");
            jg.writeString(detail.getArtist());
            jg.writeFieldName("album");
            jg.writeString(detail.getAlbum());
            jg.writeEndObject();
        }
        jg.writeEndArray();

        jg.flush();
        jg.close();
    }

    private void getTrackInfo(long id, HttpServletResponse response) throws IOException {
        Track track = TrackDatabase.getInstance().getTrackInfo(id);

        if (track == null) {
            //TODO: message?
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        JsonFactory jsonFactory = new JsonFactory();
        JsonGenerator jg = jsonFactory.createJsonGenerator(response.getOutputStream());

        jg.writeStartObject();
        jg.writeFieldName("id");
        jg.writeNumber(track.getId());
        jg.writeFieldName("device");
        jg.writeString(track.getDevice());
        jg.writeFieldName("title");
        jg.writeString(track.getTitle());
        jg.writeFieldName("artist");
        jg.writeString(track.getArtist());
        jg.writeFieldName("album");
        jg.writeString(track.getAlbum());
        jg.writeFieldName("genre");
        jg.writeString(track.getGenre());
        jg.writeFieldName("track");
        jg.writeNumber(track.getTrack());
        jg.writeFieldName("year");
        jg.writeNumber(track.getYear());
        jg.writeFieldName("devicePath");
        jg.writeString(track.getDevicePath());
        jg.writeEndObject();

        jg.flush();
        jg.close();
    }
}
