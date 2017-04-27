
package downloadaccelerator;

import java.awt.Component;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Saba
 */
public class Progress extends JProgressBar implements TableCellRenderer{
    
    public Progress(int min, int max) {
        super(min, max);
    }
    
    public Component getTableCellRendererComponent
            (JTable table , Object value , boolean isSelected ,
             boolean hasFocus , int row , int column)
            {
                
             setValue((int) ((Float) value).floatValue());
                return this;
            }    
}
