package de.pandaserv.music.client.views;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.media.client.Audio;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import de.pandaserv.music.client.misc.TimeUtil;
import de.pandaserv.music.client.widgets.Slider;
import de.pandaserv.music.client.widgets.Visualization;
import de.pandaserv.music.client.misc.NotSupportedException;
import de.pandaserv.music.client.misc.PlaybackStatus;
import de.pandaserv.music.shared.Track;
import de.pandaserv.music.shared.TrackDetail;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 2/10/13
 * Time: 11:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class MusicTestViewImpl extends Composite implements MusicTestView {
    private Presenter presenter;
    private Visualization vis;

    private Timer searchTimer;

    @UiTemplate("MusicTestView.ui.xml")
    interface MusicTestViewUiBinder extends UiBinder<HTMLPanel, MusicTestViewImpl> {

    }

    @UiField Widget header;
    @UiField Widget mainContent;
    @UiField
    Widget footer;

    @UiField Label titleLabel;
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
    TextBox searchBox;
    @UiField Button searchButton;
    @UiField
    FlexTable searchResults;

    private static MusicTestViewUiBinder ourUiBinder = GWT.create(MusicTestViewUiBinder.class);
    public MusicTestViewImpl() {
        initWidget(ourUiBinder.createAndBindUi(this));

        searchTimer = new Timer() {
            @Override
            public void run() {
                presenter.newSearchQuery();
            }
        };

        timeSlider.addValueChangeHandler(new Slider.ValueChangeHandler() {
            @Override
            public void onValueChanged(double value) {
                presenter.seekTo(value);
            }
        });

        try {
            vis = new Visualization();
            visHolder.add(vis);
        } catch (NotSupportedException e) {
            GWT.log("Canvas element not supported");
            vis = null;
        }

        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent resizeEvent) {
                resizeMainContent();
            }
        });

        new Timer() {
            @Override
            public void run() {
                resizeMainContent();
            }
        }.schedule(1);
    }

    private void resizeMainContent() {
        int height = Window.getClientHeight();
        height -= header.getOffsetHeight();
        height -= footer.getOffsetHeight();
        height -= 4; // :-/
        if (height < 10) {
            height = 10;
        }
        mainContent.setHeight(height + "px");
    }

    @Override
    public void showError(boolean show) {
        //errorLabel.setVisible(show);
    }

    @Override
    public void setErrorMessage(String message) {
        //errorLabel.setText(message);
    }

    @Override
    public void setSearchResults(TrackDetail[] results) {
        searchResults.clear();
        for (int i = 0; i < results.length; i++) {
            final int index = i;
            ClickHandler handler = new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    presenter.onSearchResultClicked(index);
                }
            };
            Label titleLabel = new Label(results[i].getTitle());
            Label artistLabel = new Label(results[i].getArtist());
            Label albumLabel = new Label(results[i].getAlbum());
            titleLabel.addClickHandler(handler);
            artistLabel.addClickHandler(handler);
            albumLabel.addClickHandler(handler);
            searchResults.setWidget(i, 0, titleLabel);
            searchResults.setWidget(i, 1, artistLabel);
            searchResults.setWidget(i, 2, albumLabel);
        }
    }

    @Override
    public void setCurrentTrackInfo(Track track) {
        titleLabel.setText(track.getTitle());
        artistLabel.setText(track.getArtist());
        albumLabel.setText(track.getAlbum());
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
    public void setDebugString(String debug) {
        //debugLabel.setText(debug);
    }

    @Override
    public String getSearchQuery() {
        return searchBox.getText();
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @UiHandler("playButton")
    void onPlayButtonClicked(ClickEvent e) {
        presenter.playToggle();
    }

    @UiHandler("searchBox")
    void onSearchBoxChanged(KeyUpEvent e) {
        searchTimer.cancel();
        searchTimer.schedule(300);
    }
    @UiHandler("searchButton")
    void onSearchButtonClicked(ClickEvent e) {
        searchTimer.cancel();
        presenter.newSearchQuery();
    }
}