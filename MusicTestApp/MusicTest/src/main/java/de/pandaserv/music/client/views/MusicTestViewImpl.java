package de.pandaserv.music.client.views;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.media.client.Audio;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 2/10/13
 * Time: 11:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class MusicTestViewImpl extends Composite implements MusicTestView {
    @UiTemplate("MusicTestView.ui.xml")
    interface MusicTestViewUiBinder extends UiBinder<HTMLPanel, MusicTestViewImpl> {

    }
    private static MusicTestViewUiBinder ourUiBinder = GWT.create(MusicTestViewUiBinder.class);

    public MusicTestViewImpl() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public Audio getAudio() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}