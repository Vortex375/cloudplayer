package de.pandaserv.music.client.widgets;

import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.web.bindery.event.shared.HandlerRegistration;
import de.pandaserv.music.client.console.Console;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 4/10/13
 * Time: 2:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class HtmlConsole extends FlowPanel implements Console {
    private boolean promptShowing;

    private Label promptLabel;

    private Label prompt;
    private Icon loadingIcon;

    private HandlerRegistration inputHandler;

    private Runnable mainCommand;

    public HtmlConsole() {
        addStyleName("console");
        promptShowing = false;
        promptLabel = new Label();
        promptLabel.addStyleName("inline");
        promptLabel.addStyleName("consolePromptLabel");

        prompt = new Label();
        prompt.getElement().setId("console-prompt");
        prompt.addStyleName("inline");
        prompt.addStyleName("consolePrompt");
        prompt.getElement().setAttribute("contenteditable", "true");
        prompt.setText("");
        prompt.sinkEvents(Event.KEYEVENTS); // needed?

        loadingIcon = new Icon(IconType.SPINNER);
        loadingIcon.addStyleName("icon-spin");
    }

    public void setMainCommand(Runnable main) {
        this.mainCommand = main;
    }

    public void runMainCommand() {
        mainCommand.run();
    }

    @Override
    public void clear() {
        boolean promptWasShowing = false;
        if (promptShowing) {
            promptWasShowing = true;
            hidePrompt();
        }

        super.clear();

        if (promptWasShowing) {
            showPrompt();
        }
    }

    public void print(String message) {
        boolean promptWasShowing = false;
        if (promptShowing) {
            promptWasShowing = true;
            hidePrompt();
        }

        // using DOM calls is faster ?
        Element msg = DOM.createDiv();
        msg.addClassName("consoleMessage");
        msg.setInnerText(message);
        getElement().appendChild(msg);

        // delay re-showing of prompt to work around DOM issues
        if (promptWasShowing) {
            new Timer() {
                @Override
                public void run() {
                    showPrompt();
                }
            }.schedule(10);
        }
    }

    public void input(String message, final InputCallback callback) {
        promptLabel.setText(message);
        prompt.setText(""); // clear prompt
        inputHandler = prompt.addHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    event.stopPropagation();
                    event.preventDefault();
                    removeInputHandler();
                    hidePrompt();
                    callback.onInput(prompt.getText());
                }
            }
        }, KeyDownEvent.getType());
        showPrompt();
    }

     //TODO: inputHidden() for passwords etc.

    private void showPrompt() {
        add(promptLabel);
        add(prompt);
        prompt.getElement().focus();
        promptShowing = true;
    }

    private void hidePrompt() {
        remove(prompt);
        remove(promptLabel);
        promptShowing = false;
    }

    private void removeInputHandler() {
        if (inputHandler != null) {
            inputHandler.removeHandler();
            inputHandler = null;
        }
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        if (promptShowing) {
            prompt.getElement().focus();
        }
    }
}
