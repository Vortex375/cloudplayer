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

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 4/10/13
 * Time: 2:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class HtmlConsole extends FlowPanel implements Console {
    private boolean promptShowing;
    private boolean waitShowing;

    private Label promptLabel;

    private Label prompt;
    private Icon loadingIcon;

    private List<String> history;
    private int historyPos;

    private HandlerRegistration inputHandler;

    private Runnable mainCommand;

    public HtmlConsole() {
        history = new ArrayList<String>();
        historyPos = 0;

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
        showWait(false); // hide loading icon on clear

        if (promptWasShowing) {
            showPrompt();
        }
    }

    public void print(String message) {
        boolean waitWasShowing = waitShowing;
        showWait(false);

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

        showWait(waitWasShowing);

        if (promptWasShowing) {
            showPrompt();
        }
    }

    public void input(String message, final InputCallback callback) {
        promptLabel.setText(message);
        prompt.setText(""); // clear prompt
        prompt.getElement().setInnerHTML("&#8203;"); // insert unicode zero width space to work around firefox focus bug
        inputHandler = prompt.addHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                /*
                 * submit input when pressing the enter key
                 */
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    event.stopPropagation();
                    event.preventDefault();
                    removeInputHandler();
                    hidePrompt();

                    /*
                     * add input to history
                     */
                    history.add(prompt.getText());
                    historyPos = history.size();

                    /*
                     * output input on the console
                     */
                    print(promptLabel.getText() + " " + prompt.getText());

                    callback.onInput(prompt.getText());
                } else if (event.getNativeKeyCode() == KeyCodes.KEY_UP) {
                    /*
                     * browse through history with up and down keys
                     */
                    historyPos--;
                    if (historyPos < 0) {
                        // reached the end
                        historyPos++;
                        return;
                    }

                    prompt.setText(history.get(historyPos));
                } else if (event.getNativeKeyCode() == KeyCodes.KEY_DOWN) {
                    historyPos++;
                    if (historyPos >= history.size()) {
                        // reached the end
                        historyPos--;
                        return;
                    }

                    prompt.setText(history.get(historyPos));
                }
            }
        }, KeyDownEvent.getType());
        showPrompt();
    }

    @Override
    public void showWait(boolean show) {
        if (show && !waitShowing) {
            add(loadingIcon);
        } else if (!show && waitShowing) {
            remove(loadingIcon);
        }

        waitShowing = show;
    }

    //TODO: inputHidden() for passwords etc.

    private void showPrompt() {
        // always hide wait icon when showing prompt
        // even when the current command did not hide it
        showWait(false);

        add(promptLabel);
        add(prompt);
        new Timer() {
            @Override
            public void run() {
                prompt.getElement().focus();
            }
        }.schedule(1);
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
