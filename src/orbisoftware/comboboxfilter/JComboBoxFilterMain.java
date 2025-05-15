package orbisoftware.comboboxfilter;

import javax.swing.*;
import javax.swing.border.BevelBorder;

import java.awt.*;
import java.util.List;

public class JComboBoxFilterMain {
	
    public static void main(String[] args) {
    	
        List<TextEntry> countries = CountriesDataAccess.getCountries();
        JComboBox<TextEntry> comboBoxCountries = new JComboBox<>(
        		countries.toArray(new TextEntry[countries.size()]));

        ComboBoxFilterDecorator<TextEntry> decorateComboBoxCountries = ComboBoxFilterDecorator.decorate(comboBoxCountries,
                CustomComboRenderer::getDisplayText,
                JComboBoxFilterMain::comboBoxFilter);

        comboBoxCountries.setRenderer(new CustomComboRenderer(decorateComboBoxCountries.getFilterTextSupplier()));
        comboBoxCountries.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        
        JPanel panel = new JPanel();
        panel.add(comboBoxCountries);
        
        List<TextEntry> stateNames = StateNamesDataAccess.getStateNames();
        JComboBox<TextEntry> comboBoxStateNames = new JComboBox<>(
                stateNames.toArray(new TextEntry[stateNames.size()]));

        ComboBoxFilterDecorator<TextEntry> decorateStateNames = ComboBoxFilterDecorator.decorate(comboBoxStateNames,
                CustomComboRenderer::getDisplayText,
                JComboBoxFilterMain::comboBoxFilter);

        comboBoxStateNames.setRenderer(new CustomComboRenderer(decorateStateNames.getFilterTextSupplier()));
        comboBoxStateNames.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        
        panel.add(comboBoxStateNames);
        
        JFrame frame = createFrame();
        frame.add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static boolean comboBoxFilter(TextEntry emp, String textToFilter) {
    	
        if (textToFilter.isEmpty()) {
            return true;
        }
        return CustomComboRenderer.getDisplayText(emp).toLowerCase()
                                  .contains(textToFilter.toLowerCase());
    }

    private static JFrame createFrame() {
        JFrame frame = new JFrame("Java ComboBox Filter Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(600, 300));
        return frame;
    }
}