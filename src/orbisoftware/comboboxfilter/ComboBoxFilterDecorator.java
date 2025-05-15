package orbisoftware.comboboxfilter;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.event.MouseMotionAdapter;

public class ComboBoxFilterDecorator<T> {
	
    private JComboBox<T> comboBox;
    private BiPredicate<T, String> userFilter;
    private Function<T, String> comboDisplayTextMapper;
    java.util.List<T> originalItems;
    private FilterEditor filterEditor;

    private boolean keyboardCommitIssued = false;
    private boolean mouseClickIssued = false;
    private long lastItemStateChangeMillis = 0;
    private TextEntry lastSelectedItem = null;
    private boolean ignoreComhoHoxUpdates = false;
    private int lastHoverIndex = 0;
    
    public ComboBoxFilterDecorator(JComboBox<T> comboBox,
                                   BiPredicate<T, String> userFilter,
                                   Function<T, String> comboDisplayTextMapper) {
        this.comboBox = comboBox;
        this.userFilter = userFilter;
        this.comboDisplayTextMapper = comboDisplayTextMapper;
    }

	public static <T> ComboBoxFilterDecorator<T> decorate(JComboBox<T> comboBox,
			Function<T, String> comboDisplayTextMapper, BiPredicate<T, String> userFilter) {
		
		ComboBoxFilterDecorator decorator = new ComboBoxFilterDecorator(comboBox, userFilter, comboDisplayTextMapper);
		decorator.init();

        // Set a custom UI that overrides createPopup()
        comboBox.setUI(new BasicComboBoxUI() {
            @Override
            protected ComboPopup createPopup() {
                BasicComboPopup popup = new BasicComboPopup(comboBox);
                popup.getList().addMouseMotionListener(new MouseMotionAdapter() {
                    @Override
                    public void mouseMoved(MouseEvent e) {
                        JList<?> list = popup.getList();
                        int index = list.locationToIndex(e.getPoint());
                        if (index > -1) {
                            Object item = list.getModel().getElementAt(index);
                            if (index != decorator.lastHoverIndex) {
                            	//System.out.println("Hovering over: " + item.toString());
                            	TextEntry textEntry = new TextEntry();
                            	decorator.lastHoverIndex = index;
                            	
    							textEntry.setName(item.toString());
    							
    							decorator.comboBox.setSelectedItem(textEntry);
    							//decorator.mouseClickIssued = true;
    							//decorator.lastSelectedItem = textEntry;
                            }
                        }
                    }
                });
                
                return popup;
            }
        });
                
		comboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {

				if (decorator.ignoreComhoHoxUpdates)
					return;

				if (e.getStateChange() == ItemEvent.SELECTED) {

					try {

						long stateChangeIgnore = 100;
						long currentTimeMillis = java.lang.System.currentTimeMillis();
						TextEntry textEntry;

						if ((currentTimeMillis - decorator.lastItemStateChangeMillis) > stateChangeIgnore) {

							textEntry = (TextEntry) comboBox.getSelectedItem();

							decorator.mouseClickIssued = true;
							decorator.lastSelectedItem = textEntry;
							
							//System.out.println("item listener - textEntry.getName = " + textEntry.getName());
						}

						decorator.lastItemStateChangeMillis = currentTimeMillis;
					} catch (Exception e2) {
					}
				}
			}
		});

		return decorator;
	}

    private void init() {
    	
        prepareComboFiltering();
        initComboPopupListener();
        initComboKeyListener();
    }

    private void prepareComboFiltering() {
    	
        DefaultComboBoxModel<T> model = (DefaultComboBoxModel<T>) comboBox.getModel();
        this.originalItems = new ArrayList<>();
        
        for (int i = 0; i < model.getSize(); i++) {
            this.originalItems.add(model.getElementAt(i));
        }

        filterEditor = new FilterEditor(comboDisplayTextMapper, new Consumer<Boolean>() {
        	
            //editing mode commit change listener
            @Override
            public void accept(Boolean aBoolean) {
                if (aBoolean) {//commit
                	lastSelectedItem = (TextEntry) comboBox.getSelectedItem();
                } else {//rollback to the last one
                    comboBox.setSelectedItem(lastSelectedItem);
                    filterEditor.setItem(lastSelectedItem);
                }
            }
        });

        JLabel filterLabel = filterEditor.getFilterLabel();
        filterLabel.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                filterLabel.setBorder(BorderFactory.createLoweredBevelBorder());
            }

            @Override
            public void focusLost(FocusEvent e) {
                filterLabel.setBorder(UIManager.getBorder("TextField.border"));
                resetFilterComponent();
            }
        });
        comboBox.setEditor(filterEditor);
        comboBox.setEditable(true);
    }

	private void initComboKeyListener() {
		
		filterEditor.getFilterLabel().addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyPressed(KeyEvent e) {
				char keyChar = e.getKeyChar();
				if (!Character.isDefined(keyChar)) {
					return;
				}
				int keyCode = e.getKeyCode();
				switch (keyCode) {
				case KeyEvent.VK_DELETE:
					return;
				case KeyEvent.VK_ENTER:
					lastSelectedItem = (TextEntry) comboBox.getSelectedItem();
                	keyboardCommitIssued = true;
					resetFilterComponent();
					return;
				case KeyEvent.VK_ESCAPE:
					resetFilterComponent();
					return;
				case KeyEvent.VK_BACK_SPACE:
					filterEditor.removeCharAtEnd();
					break;
				default:
					filterEditor.addChar(keyChar);
				}
				if (!comboBox.isPopupVisible()) {
					comboBox.showPopup();
				}
				if (filterEditor.isEditing() && filterEditor.getText().length() > 0) {
					applyFilter();
				} else {
					comboBox.hidePopup();
					resetFilterComponent();
				}
			}
		});
	}

    public Supplier<String> getFilterTextSupplier() {
    	
        return () -> {
            if (filterEditor.isEditing()) {
                return filterEditor.getFilterLabel().getText();
            }
            return "";
        };
    }

    private void initComboPopupListener() {
    	
        comboBox.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                resetFilterComponent();
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                resetFilterComponent();
            }
        });
    }

    private void resetFilterComponent() {
    	
        if (!filterEditor.isEditing()) {
            return;
        }
        
        if ((comboBox.getSelectedIndex() == 0) || keyboardCommitIssued) {
        	ignoreComhoHoxUpdates = true;
        	keyboardCommitIssued = false;
        } else
        	ignoreComhoHoxUpdates = false;

        //restore original order
        DefaultComboBoxModel<T> model = (DefaultComboBoxModel<T>) comboBox.getModel();
        model.removeAllElements();
        for (T item : originalItems) {
            model.addElement(item);
        }
        filterEditor.reset();
        
        if (mouseClickIssued && !ignoreComhoHoxUpdates) {
        	comboBox.setSelectedItem(lastSelectedItem);
        	mouseClickIssued = false;
        }
        
        ignoreComhoHoxUpdates = false;
    }

    private void applyFilter() {
    	
        DefaultComboBoxModel<T> model = (DefaultComboBoxModel<T>) comboBox.getModel();
        model.removeAllElements();
        java.util.List<T> filteredItems = new ArrayList<>();
        
        //add matched items at top
        for (T item : originalItems) {
            if (userFilter.test(item, filterEditor.getFilterLabel().getText())) {
                model.addElement(item);
            } else {
                filteredItems.add(item);
            }
        }

        //red color when no match
        filterEditor.getFilterLabel()
                    .setForeground(model.getSize() == 0 ?
                            Color.red : UIManager.getColor("Label.foreground"));
        //add unmatched items
        filteredItems.forEach(model::addElement);
    }
}