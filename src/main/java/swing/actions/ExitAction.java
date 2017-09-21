package swing.actions;

import swing.gui.ApplicationFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Objects;

public class ExitAction extends AbstractAction {

    private ApplicationFrame frame;

    public ExitAction(String name, ApplicationFrame frame) {
        super(name);
        Objects.requireNonNull(frame);
        this.frame = frame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        frame.getActions().get(DisconnectAction.class).actionPerformed(e);
        frame.dispose();
    }
}
