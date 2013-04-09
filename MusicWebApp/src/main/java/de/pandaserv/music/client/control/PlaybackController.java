package de.pandaserv.music.client.control;

import com.google.web.bindery.event.shared.HandlerRegistration;
import de.pandaserv.music.client.audio.AudioSystem;
import de.pandaserv.music.client.misc.PlaybackStatus;
import de.pandaserv.music.shared.QueueMode;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 3/28/13
 * Time: 11:36 PM
 * To change this template use File | Settings | File Templates.
 */
public interface PlaybackController {
    public void play();
    public void playToggle();
    public void pause();
    public void seekTo(double seconds);

    public PlaybackStatus getPlaybackStatus();
    public long getCurrentId();

    public QueueMode getQueueMode();
    public long[] getQueue(); //TODO: List<Long> ?
    public int getQueuePosition();

    public void setQueue(QueueMode queueMode, long[] queue, int pos);
    public void setQueuePosition(int position);

    public HandlerRegistration addVisDataHandler(AudioSystem.VisDataHandler handler);
}
