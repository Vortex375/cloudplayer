package de.pandaserv.music.client.views;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.media.client.Audio;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import de.pandaserv.music.client.misc.TimeUtil;
import de.pandaserv.music.client.widgets.Slider;
import de.pandaserv.music.client.widgets.Visualization;
import de.pandaserv.music.shared.NotSupportedException;
import de.pandaserv.music.shared.PlaybackStatus;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 2/10/13
 * Time: 11:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class MusicTestViewImpl extends Composite implements MusicTestView {
    private Presenter presenter;
    private Audio audio;
    private Visualization vis;

    @UiTemplate("MusicTestView.ui.xml")
    interface MusicTestViewUiBinder extends UiBinder<HTMLPanel, MusicTestViewImpl> {

    }

    @UiField
    Label errorLabel;
    @UiField
    Label timeLabel;
    @UiField
    Label durationLabel;
    @UiField
    Label debugLabel;
    @UiField
    Slider timeSlider;
    @UiField
    Button playButton;
    @UiField
    FlowPanel visHolder;

    private static MusicTestViewUiBinder ourUiBinder = GWT.create(MusicTestViewUiBinder.class);
    public MusicTestViewImpl() {
        initWidget(ourUiBinder.createAndBindUi(this));

        audio = Audio.createIfSupported();
        audio.getAudioElement().setControls(false);

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
    }

    @Override
    public void showError(boolean show) {
        errorLabel.setVisible(show);
    }

    @Override
    public void setErrorMessage(String message) {
        errorLabel.setText(message);
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
        debugLabel.setText(debug);
    }

    @Override
    public AudioElement getAudioElement() {
        if (audio == null) {
            return null;
        } else {
            return audio.getAudioElement();
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