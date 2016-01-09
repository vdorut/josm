// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.gui.widgets;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public abstract class SearchTextResultListPanel<T> extends JPanel {

    protected final JosmTextField edSearchText;
    protected final JList<T> lsResult;
    protected final ResultListModel<T> lsResultModel = new ResultListModel<>();

    protected final transient List<ListSelectionListener> listSelectionListeners = new ArrayList<>();

    private transient ActionListener dblClickListener;
    private transient ActionListener clickListener;

    protected abstract void filterItems();

    public SearchTextResultListPanel() {
        super(new BorderLayout());

        edSearchText = new JosmTextField();
        edSearchText.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                filterItems();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                filterItems();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterItems();
            }
        });
        edSearchText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_DOWN:
                        selectItem(lsResult.getSelectedIndex() + 1);
                        break;
                    case KeyEvent.VK_UP:
                        selectItem(lsResult.getSelectedIndex() - 1);
                        break;
                    case KeyEvent.VK_PAGE_DOWN:
                        selectItem(lsResult.getSelectedIndex() + 10);
                        break;
                    case KeyEvent.VK_PAGE_UP:
                        selectItem(lsResult.getSelectedIndex() - 10);
                        break;
                    case KeyEvent.VK_HOME:
                        selectItem(0);
                        break;
                    case KeyEvent.VK_END:
                        selectItem(lsResultModel.getSize());
                        break;
                }
            }
        });
        add(edSearchText, BorderLayout.NORTH);

        lsResult = new JList<>(lsResultModel);
        lsResult.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    if (dblClickListener != null)
                        dblClickListener.actionPerformed(null);
                } else {
                    if (clickListener != null)
                        clickListener.actionPerformed(null);
                }
            }
        });
        add(new JScrollPane(lsResult), BorderLayout.CENTER);
    }

    protected static class ResultListModel<T> extends AbstractListModel<T> {

        private transient List<T> items = new ArrayList<>();

        public synchronized void setItems(List<T> items) {
            this.items = items;
            fireContentsChanged(this, 0, Integer.MAX_VALUE);
        }

        @Override
        public synchronized T getElementAt(int index) {
            return items.get(index);
        }

        @Override
        public synchronized int getSize() {
            return items.size();
        }

        public synchronized boolean isEmpty() {
            return items.isEmpty();
        }
    }

    public synchronized void init() {
        listSelectionListeners.clear();
        edSearchText.setText("");
        filterItems();
    }

    private synchronized void selectItem(int newIndex) {
        if (newIndex < 0) {
            newIndex = 0;
        }
        if (newIndex > lsResultModel.getSize() - 1) {
            newIndex = lsResultModel.getSize() - 1;
        }
        lsResult.setSelectedIndex(newIndex);
        lsResult.ensureIndexIsVisible(newIndex);
    }

    public synchronized void clearSelection() {
        lsResult.clearSelection();
    }

    public synchronized int getItemCount() {
        return lsResultModel.getSize();
    }

    public void setDblClickListener(ActionListener dblClickListener) {
        this.dblClickListener = dblClickListener;
    }

    public void setClickListener(ActionListener clickListener) {
        this.clickListener = clickListener;
    }

    /**
     * Adds a selection listener to the presets list.
     *
     * @param selectListener The list selection listener
     * @since 7412
     */
    public synchronized void addSelectionListener(ListSelectionListener selectListener) {
        lsResult.getSelectionModel().addListSelectionListener(selectListener);
        listSelectionListeners.add(selectListener);
    }

    /**
     * Removes a selection listener from the presets list.
     *
     * @param selectListener The list selection listener
     * @since 7412
     */
    public synchronized void removeSelectionListener(ListSelectionListener selectListener) {
        listSelectionListeners.remove(selectListener);
        lsResult.getSelectionModel().removeListSelectionListener(selectListener);
    }
}