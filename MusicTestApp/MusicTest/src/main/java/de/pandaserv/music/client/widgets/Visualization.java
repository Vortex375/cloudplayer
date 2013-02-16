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
        double width = getOffsetWidth();
        double height = getOffsetHeight();

        double barWidth = (width / data.length) - 4; // 4px spacing between bars

        // clear background
        cx.setFillStyle("#f5f5f5");
        cx.fillRect(0, 0, width, height);

        cx.setFillStyle("#000000");
        for (int i = 0; i < data.length; i++) {
            // draw bars
            double barHeight = height * (data[i] / 255.0);
            double x = (barWidth + 4) * i;
            double y = height - barHeight;

            cx.fillRect(x, y, barWidth, barHeight);
            //GWT.log("drawing bar: x=" + x+ " y=" + y + " width=" + barWidth + " height=" + barHeight);
        }
        GWT.log("drawed vis: " + Arrays.toString(data));
    }
}
