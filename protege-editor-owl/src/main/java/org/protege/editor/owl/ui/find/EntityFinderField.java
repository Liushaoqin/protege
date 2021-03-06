package org.protege.editor.owl.ui.find;

import com.google.common.base.Optional;
import org.protege.editor.core.ui.util.AugmentedJTextField;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.find.OWLEntityFinderPreferences;
import org.protege.editor.owl.ui.search.SearchPanel;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 16-May-2006<br><br>

 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class EntityFinderField extends AugmentedJTextField {

    public static final int WINDOW_WIDTH = 800;

    private JWindow window;

    private JComponent parent;

    private SearchPanel searchPanel;

    private OWLEditorKit editorKit;

    private EntityFoundHandler entityFoundHandler = this::invokeDefaultEntityChosenHander;

    private SearchStartedHandler searchStartedHandler = () -> {};

    private boolean settingText = false;

    private final AbstractAction selectEntityAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            selectEntity();
        }
    };

    public EntityFinderField(JComponent parent, OWLEditorKit editorKit) {
        super(20, "Search for entity");
        this.editorKit = editorKit;
        putClientProperty("JTextField.variant", "search");
        this.parent = parent;
        searchPanel = new SearchPanel(editorKit);
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESC");
        getActionMap().put("ESC", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeResults();
            }
        });
        selectEntityAction.setEnabled(false);
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "ENTER");
        getActionMap().put("ENTER", selectEntityAction);
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    decrementListSelection();
                }
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    incrementListSelection();
                }
                if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    handleBackSpaceDown();
                }
            }
        });
        getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
            }


            public void insertUpdate(DocumentEvent e) {
                performFind();
            }


            public void removeUpdate(DocumentEvent e) {
                performFind();
            }
        });
        searchPanel.setSearchResultClickedListener((searchResult, e) -> {
            if(e.getClickCount() == 2) {
                selectEntity();
            }
        });
    }

    @Override
    public void setText(String t) {
        this.settingText = true;
        try {
            super.setText(t);
        } finally {
            this.settingText = false;
        }

    }

    public void setEntityFoundHandler(@Nonnull EntityFoundHandler handler) {
        this.entityFoundHandler = checkNotNull(handler);
    }

    public void setSearchStartedHandler(@Nonnull SearchStartedHandler searchStartedHandler) {
        this.searchStartedHandler = checkNotNull(searchStartedHandler);
    }

    private void selectEntity() {
        Optional<OWLEntity> selectedEntity = searchPanel.getSelectedEntity();
        if (selectedEntity.isPresent()) {
            entityFoundHandler.handleChosenEntity(selectedEntity.get());
        }
        closeResults();
    }

    private void invokeDefaultEntityChosenHander(@Nonnull OWLEntity entity) {
        Optional<OWLEntity> selectedEntity = searchPanel.getSelectedEntity();
        if (selectedEntity.isPresent()) {
            editorKit.getWorkspace().getOWLSelectionModel().setSelectedEntity(selectedEntity.get());
            editorKit.getWorkspace().displayOWLEntity(selectedEntity.get());
        }
        closeResults();
    }

    private void handleBackSpaceDown() {
        if(getText().isEmpty()) {
            closeResults();
        }
    }


    private void incrementListSelection() {
        searchPanel.moveSelectionDown();
    }


    private void decrementListSelection() {
        searchPanel.moveSelectionUp();
    }


    private void closeResults() {
        getWindow().setVisible(false);
        selectEntityAction.setEnabled(false);
    }


    private Timer timer = new Timer(400, new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            executeFind();
            timer.stop();
        }
    });


    private void executeFind() {
        showResults();
    }


    private void performFind() {
        if(this.settingText) {
            return;
        }
        searchStartedHandler.handleSearchStarted();
        timer.setDelay((int) OWLEntityFinderPreferences.getInstance().getSearchDelay());
        timer.restart();
    }


    private JWindow getWindow() {
        if (window == null) {
            Window w = (Window) SwingUtilities.getAncestorOfClass(Window.class, parent);
            window = new JWindow(w);
            window.setFocusableWindowState(false);
            JPanel popupContent = new JPanel(new BorderLayout(3, 3));
            popupContent.add(searchPanel);
            window.setContentPane(popupContent);
            addFocusListener(new FocusAdapter() {
                public void focusLost(FocusEvent e) {
                    window.setVisible(false);
                }
            });
            SwingUtilities.getRoot(this).addComponentListener(new ComponentAdapter() {
                public void componentMoved(ComponentEvent e) {
                    closeResults();
                }
            });
        }
        return window;
    }


    private void showResults() {
        selectEntityAction.setEnabled(true);
        JWindow window = getWindow();
        Point pt = new Point(0, 0);
        SwingUtilities.convertPointToScreen(pt, this);
        window.setLocation(pt.x + (getWidth() - WINDOW_WIDTH) / 2, pt.y + getHeight() + 2);

        Container parent = window.getParent();
        int height = 400;
        if (parent != null) {
            height = (parent.getHeight() * 3) / 4;
        }
        window.setSize(WINDOW_WIDTH, height);
        searchPanel.setSearchString(getText().trim());

        window.setVisible(true);
        window.validate();
    }

}
