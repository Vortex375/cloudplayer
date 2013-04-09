package de.pandaserv.music.client.views;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
import de.pandaserv.music.client.misc.JSUtil;
import de.pandaserv.music.client.misc.NotSupportedException;
import de.pandaserv.music.client.misc.PlaybackStatus;
import de.pandaserv.music.client.misc.TimeUtil;
import de.pandaserv.music.client.widgets.Slider;
import de.pandaserv.music.client.widgets.Visualization;
import de.pandaserv.music.shared.Track;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 4/3/13
 * Time: 4:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlaybackViewImpl extends Composite implements PlaybackView {
    @UiTemplate("PlaybackView.ui.xml")
    interface MainViewUiBinder extends UiBinder<HTMLPanel, PlaybackViewImpl> {

    }

    private static MainViewUiBinder ourUiBinder = GWT.create(MainViewUiBinder.class);

    private Presenter presenter;

    private Visualization vis;

    @UiField
    Label titleLabel;
    @UiField Label artistLabel;
    @UiField Label albumLabel;
    @UiField
    Label timeLabel;
    @UiField
    Label durationLabel;
    @UiField
    Slider timeSlider;
    @UiField
    Button playButton;
    @UiField
    FlowPanel visHolder;
    @UiField
    Image albumCover;
    public PlaybackViewImpl() {
        initWidget(ourUiBinder.createAndBindUi(this));

        /*
         * add value change handler when dragging the time slider
         */
        timeSlider.addValueChangeHandler(new Slider.ValueChangeHandler() {
            @Override
            public void onValueChanged(double value) {
                presenter.seekTo(value);
            }
        });

        /*
         * try to set up the visualization canvas
         */
        try {
            vis = new Visualization();
            visHolder.add(vis);
        } catch (NotSupportedException e) {
            GWT.log("Canvas element not supported");
            vis = null;
        }

        /*
         * add glisse (large picture popup) on album cover element
         */
        JSUtil.addGlisse(albumCover.getElement());
    }

    @Override
    public void showPlaybackWaiting(boolean show) {
        if (show) {
            playButton.setIcon(IconType.SPINNER);
            // HACK to make the waiting icon spin
            // (rather than spinning the whole button, which looks funny
            playButton.getElement().getElementsByTagName("I").getItem(0).addClassName("icon-spin");
        } else {
            playButton.setIcon(IconType.PLAY);
            playButton.getElement().getElementsByTagName("I").getItem(0).removeClassName("icon-spin");
        }
    }

    @Override
    public void setCurrentTrackInfo(Track track) {
        titleLabel.setText(track.getTitle());
        artistLabel.setText(track.getArtist());
        albumLabel.setText(track.getAlbum());
        if (track.getCover() != null && !track.getCover().equals("")) {
            albumCover.setUrl("/service/cover/" + track.getCover());
            albumCover.getElement().setAttribute("data-glisse-big", "/service/cover/" + track.getCover());
            albumCover.setVisible(true);
        } else {
            albumCover.setVisible(false);
        }
    }

    @Override
    public void setDuration(double seconds) {
        durationLabel.setText(TimeUtil.formatTime(seconds));
        timeSlider.setMin(0);
        timeSlider.setMax(seconds);
    }

    @Override
    public void setTime(double seconds) {
        timeLabel.setText(TimeUtil.formatTime(seconds));
        timeSlider.setValue(seconds);
    }

    @Override
    public void setPlaybackStatus(PlaybackStatus status) {
        if (status == PlaybackStatus.PLAY) {
            playButton.setIcon(IconType.PAUSE);
        } else {
            playButton.setIcon(IconType.PLAY);
        }
    }

    @Override
    public void setVisData(int[] bars) {
        if (vis != null) {
            vis.update(bars);
        }
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @UiHandler("playButton")
    void onPlayButtonClicked(ClickEvent e) {
        presenter.playToggle();
    }
}