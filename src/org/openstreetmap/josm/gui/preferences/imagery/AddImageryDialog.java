// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.gui.preferences.imagery;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.Dimension;

import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.preferences.imagery.AddImageryPanel.ContentValidationListener;

/**
 * Dialog shown to add a new imagery (WMS/TMS) source from imagery preferences.
 * @since 5731
 */
public class AddImageryDialog extends ExtendedDialog implements ContentValidationListener {

    /**
     * Constructs a new AddImageryDialog.
     * @param parent The parent element that will be used for position and maximum size
     * @param panel  The content that will be displayed in the message dialog
     */
    public AddImageryDialog(Component parent, AddImageryPanel panel) {
        super(parent, tr("Add Imagery URL"), new String[] {tr("OK"), tr("Cancel")});
        setButtonIcons(new String[] {"ok", "cancel"});
        setCancelButton(2);
        configureContextsensitiveHelp("/Dialog/AddImagery", true /* show help button */);
        setContent(panel, false);
        setMinimumSize(new Dimension(300, 400));
        panel.addContentValidationListener(this);
    }

    @Override
    public void setupDialog() {
        super.setupDialog();
        contentChanged(false);
    }

    @Override
    public void contentChanged(boolean isValid) {
        buttons.get(0).setEnabled(isValid);
    }
}
