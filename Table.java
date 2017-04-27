
package downloadaccelerator;

import java.util.*;
import javax.swing.JProgressBar;
import javax.swing.table.*;

/**
 *
 * @author Saba
 */
public class Table extends AbstractTableModel implements Observer{
    
    //list of files downloaded or in progress
    private ArrayList downloadList = new ArrayList();
    private static final String[] columns = {"URL" , "Progress" , "Size (KB)" , "Status"}; //columns of the table
    
    //--------------------------------------------------------------------------
    
    private static final Class[] columnClasses = {String.class,JProgressBar.class,String.class,String.class};
    
    public int getColumnCount() {
        return columns.length;
    }
    public int getRowCount() {
        return downloadList.size(); // number of data in our download list
    }
    public String getColumnName(int col) {
        return columns[col];
    }
    public Class getColumnClass(int col) {
        return columnClasses[col];
    }
   
    //--------------------------------------------------------------------------
    
    public Object getValueAt(int row , int col){
        
        Download download = (Download) downloadList.get(row);
        switch(col){
            case 0 : return download.getUrl(); // URL
            case 1 : return new Float(download.getProgress()); //Progress
            case 2 : {int size = download.getSize();
                        return (size == -1) ? "" : Integer.toString(size/1000); // kiloo bytes
                     }
            case 3 : return Download.Statuses[download.getStatus()];
        }
        
    return "";
    
    }
    //--------------------------------------------------------------------------
    public void update(Observable o, Object obj) {
        int index = downloadList.indexOf(o);
         
        // Fire table row update notification to table
        fireTableRowsUpdated(index, index);
    }
    
    //--------------------------------------------------------------------------
    public void addDownload(Download d){
        
        d.addObserver(this);// be notified when the download changes
        downloadList.add(d);
        // Fire table row insertion notification to table
        fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
    }
   
    public Download getDownload(int row) { // for a specific row
        return (Download) downloadList.get(row);
    }
    
    public void removeDownload(int row) {  //remove a download from list
        downloadList.remove(row);
        fireTableRowsDeleted(row, row);// Fire table row deletion notification to table.
    }   
}