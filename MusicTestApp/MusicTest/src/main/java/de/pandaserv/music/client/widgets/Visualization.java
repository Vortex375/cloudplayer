package de.pandaserv.music.client.widgets;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.user.client.ui.Composite;
import de.pandaserv.music.shared.NotSupportedException;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 2/16/13
 * Time: 12:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class Visualization extends Composite {
    private Canvas canvas;
    private Animation animation;

    public Visualization() throws NotSupportedException {
        canvas = Canvas.createIfSupported();
        if (canvas == null) {
            throw new NotSupportedException();
        }
        initWidget(canvas);
        addStyleName("visCanvas");
    }

    public void update(int[] data) {
        Context2d cx = canvas.getContext2d();
        cx.scale(1.0, 1.0);
        double width = getOffsetWidth();
        double height = getOffsetHeight();
        canvas.setCoordinateSpaceWidth((int) width);
        canvas.setCoordinateSpaceHeight((int) height);

        double barWidth = (width / data.length) - 4; // 4px spacing between bars

        // clear background
        cx.setFillStyle("#f5f5f5");
        cx.fillRect(0, 0, width, height);

        cx.setFillStyle("#000000");
        // test: mark corners
        //cx.fillRect(0, 0, 1, 1);
        //cx.fillRect(0, height - 1, 1, 1);
        //cx.fillRect(width - 1, 0, 1, 1);
        //cx.fillRect(width - 1, height - 1, 1, 1);
        for (int i = 0; i < data.length; i++) {
            int grey = 20 + (int) (120 * (i / (double) data.length));
            cx.setFillStyle("rgb(" + grey + "," + grey + "," + grey + ")");
            // draw bars
            int barHeight = (int) (height * (data[i] / 255.0));
            int x =(int) (barWidth + 4) * i;
            int y = (int) height - barHeight;

            cx.fillRect(x, y, barWidth, barHeight);
            GWT.log("drawing bar: x=" + x+ " y=" + y + " width=" + barWidth + " height=" + barHeight);
        }
        GWT.log("drawed vis: " + Arrays.toString(data));
    }
}
