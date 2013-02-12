package de.pandaserv.music.client.widgets;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 2/12/13
 * Time: 1:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class Slider extends FlowPanel {
    public static interface ValueChangeHandler {
        public void onValueChanged(double value);
    }
    private List<ValueChangeHandler> handlers;

    private boolean down;

    public Slider() {
        handlers = new ArrayList<ValueChangeHandler>();

    }

    @Override
    protected void onAttach() {
        super.onAttach();
        create(getElement());
    }

    @Override
    protected void onDetach() {
        destroy(getElement());
        super.onDetach();
    }

    void onStartSlide() {
        down = true;
    }

    void onStopSlide() {
        down = false;
        double value = getValue();
        for (ValueChangeHandler handler: handlers) {
            handler.onValueChanged(value);
        }
    }

    public double getValue() {
        return doGetValue(getElement());
    }

    public void setValue(double value) {
        if (!down) {
            doSetValue(getElement(), value);
        }
    }

    public void setMin(double value) {
        doSetMin(getElement(), value);
    }

    public void setMax(double value) {
        doSetMax(getElement(), value);
    }

    public HandlerRegistration addValueChangeHandler(final ValueChangeHandler handler) {
        handlers.add(handler);
        return new HandlerRegistration() {
            @Override
            public void removeHandler() {
                handlers.remove(handler);
            }
        };
    }

    private native void create(Element element) /*-{
        var that = this;
        $wnd.$(element).slider({
            start: function (event, ui) {
                that.@de.pandaserv.music.client.widgets.Slider::onStartSlide()();
            },
            stop: function (event, ui) {
                that.@de.pandaserv.music.client.widgets.Slider::onStopSlide()();
            }
        });
    }-*/;

    private native void destroy(Element element) /*-{
        $wnd.$(element).slider("destroy");
    }-*/;

    private native void doSetMin(Element element, double value) /*-{
        $wnd.$(element).slider("option", "min", value);
    }-*/;

    private native void doSetMax(Element element, double value) /*-{
        $wnd.$(element).slider("option", "max", value);
    }-*/;

    private native void doSetValue(Element element, double value) /*-{
        $wnd.$(element).slider("option", "value", value);
    }-*/;

    private native double doGetValue(Element element) /*-{
        return $wnd.$(element).slider("option", "value");
    }-*/;
}
