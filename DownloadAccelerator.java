
package downloadaccelerator;

import java.awt.*;
import java.awt.event.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Saba
 */

//The window that the user sees when using this programm
public class DownloadAccelerator extends JFrame implements Observer{
    
    private JTextField jTxt , jTxt2 ; // to add a text field on the panel
    private JTable table; // show the list of downloads
    private Table downloadTable;
    
        //manging different options user can choose
    private JButton pauseB , resumeB , cancelB , clearB ;
    private Download selectedDL; // current download
    
    private boolean flag;// A flag to see if table selection is being cleared
    
    Font myFont = new Font("Arial" , Font.CENTER_BASELINE , 12); 
    
    //-----------------------------------------------------------------------
    //---------------------------constructor---------------------------------
    
    public DownloadAccelerator(){
        setTitle("Download Accelerator"); // Title of the window
        setSize(700, 500); // size of the window
        setIconImage(Toolkit.getDefaultToolkit().getImage("downloadIcon.gif"));//window icon (self Designed) :D
        
        //what will happen when the window is closed
        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent we){
                winExit();
            }
        });
    
    //-------------------------------Panel----------------------------------
    JPanel panel1 = new JPanel();
    JLabel urlLabel = new JLabel("URL : ");
    urlLabel.setFont(myFont);
    panel1.add(urlLabel);
    jTxt  = new JTextField(30);
    panel1.add(jTxt);
    JLabel hintLabel = new JLabel("(URL must start with \"http://\")");
    hintLabel.setFont(myFont);
    //............................................
    JPanel panel2 = new JPanel();
    JLabel connectionLabel = new JLabel("Number of connections : ");
    connectionLabel.setFont(myFont);
    panel2.add(connectionLabel);
    jTxt2 = new JTextField(2);
    panel2.add(jTxt2);
    
    JButton dButton = new JButton("Download"); // download button
    dButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent ae){
            addAction();
        }
    });
    panel2.add(dButton);
    
    //............................................
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel , BoxLayout.Y_AXIS));
    panel.add(panel1);
    panel.add(hintLabel);
    panel.add(panel2);  
    
    //-------------------------------Menu-----------------------------------
    JMenuBar menu = new JMenuBar();
    JMenu fileMenu = new JMenu("Downloading a file : ");
    fileMenu.setFont(myFont);
    fileMenu.setMnemonic(KeyEvent.VK_F);
    JMenuItem menuItem = new JMenuItem("Exit" , KeyEvent.VK_X);
    menuItem.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent ae){
            winExit();
        }
    });
    
    fileMenu.add(menuItem);
    menu.add(fileMenu);
    setJMenuBar(menu);
    
    //-------------------------------Table-----------------------------------
    downloadTable = new Table();
    table = new JTable(downloadTable);
    table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                tableSelectionChanged();
            }
        });
    // user can Only select one row at a time
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    
    Progress renderer = new Progress(0, 100); // Set up ProgressBar
    renderer.setStringPainted(true); // show progress text
    table.setDefaultRenderer(JProgressBar.class, renderer);
    
    //table's row height large enough to fit JProgressBar
    table.setRowHeight((int) renderer.getPreferredSize().getHeight());
    
    //------------------------DownloadsPanel---------------------------------
    JPanel downloadPanel = new JPanel();
    downloadPanel.setBorder(BorderFactory.createTitledBorder("Download List :"));
    downloadPanel.setLayout(new BorderLayout());
    downloadPanel.add(new JScrollPane(table),BorderLayout.CENTER);
    
    //--------------------------ButtonsPanel---------------------------------
    
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(12, 1));
    
    pauseB = new JButton("Pause");
    pauseB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            actionPause();
        }
    });
    pauseB.setEnabled(false);
    buttonPanel.add(pauseB);
    //.................................................
    resumeB = new JButton("Resume");
    resumeB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            actionResume();
        }
    });
    resumeB.setEnabled(false);
    buttonPanel.add(resumeB);
    //.................................................
    cancelB = new JButton("Cancel");
    cancelB.addActionListener(new ActionListener() {
    public void actionPerformed(ActionEvent e) {
        actionCancel();
        }
    });
    cancelB.setEnabled(false);
    buttonPanel.add(cancelB);
    //.................................................
    clearB = new JButton("Clear");
    clearB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            actionClear();
        }
    });
    clearB.setEnabled(false);
    buttonPanel.add(clearB);
    //--------------------------Introduction-----------------------------
    JLabel developerInfo = new JLabel("Developer : Saba.F    ,  Student Number : 9313021" ,
                                        SwingConstants.CENTER);
    developerInfo.setFont(new Font("Arial" , Font.CENTER_BASELINE , 10));
    
    //-------------------------------------------------------------------
    //---------------------panels to Display-----------------------------
    
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(panel, BorderLayout.NORTH);
    getContentPane().add(downloadPanel, BorderLayout.CENTER);
    getContentPane().add(buttonPanel, BorderLayout.EAST);
    getContentPane().add(developerInfo , BorderLayout.SOUTH);
    setLocationRelativeTo(null);

}
    
//-----------------------------end_of_constructor----------------------------
//---------------------------------------------------------------------------
    
    public void addAction(){
        URL checkedUrl = checkUrl(jTxt.getText());
       
        if(checkedUrl != null ){
            if (jTxt2.getText().length() != 0){
                downloadTable.addDownload(new Download(checkedUrl ,
                        Integer.parseInt(jTxt2.getText()), chooseFile()));
                
                jTxt.setText(""); // ready to write text
                jTxt2.setText("");
            }
            else{
                JOptionPane.showMessageDialog(this , "Please enter the number of connections!" , " Error :" ,JOptionPane.ERROR_MESSAGE);
            }
        }
        else{
            JOptionPane.showMessageDialog(this , " Invalid URL !!!\n Please try agein." , " Error :" ,JOptionPane.ERROR_MESSAGE);
        }
    }
    
    //----------------------checking the url format-----------------------------
    
    private URL checkUrl(String url){
        
        if(! url.toLowerCase().startsWith("http://")) // the URL must start with "http://"
                return null;
            
        URL checkedUrl = null;
        try {
            checkedUrl = new URL(url);
        } catch (Exception e) {
            return null;
        }
        
        if (checkedUrl.getFile().length() < 2) //check if it IS a file
            return null;
        
        return checkedUrl;
    }
    //----------------------checking the url format-----------------------------

//---------------------------------------------------------------------------
//---------------------------------Buttons-----------------------------------
//-------------------------pause-----------------------------
    private void actionPause() {
        selectedDL.pause();
        updateButtons();
    }
    //-------------------------resume----------------------------
    private void actionResume() {
        selectedDL.resume();
        updateButtons();
    }
    //-------------------------cancel-----------------------------
    private void actionCancel() {
        selectedDL.cancel();
        updateButtons();
    }
    //-------------------------clear-------------------------------
    private void actionClear() {
        flag = true;
        downloadTable.removeDownload(table.getSelectedRow());
        flag = false;
        selectedDL = null;
        updateButtons();
    }
//-----------------------------update_Buttons--------------------------------
    private void updateButtons() {
        if (selectedDL != null) {
            int status = selectedDL.getStatus();
            switch (status) {
                case Download.DOWNLOADING:
                    pauseB.setEnabled(true);
                    resumeB.setEnabled(false);
                    cancelB.setEnabled(true);
                    clearB.setEnabled(false);
                    break;
                case Download.PAUSED:
                    pauseB.setEnabled(false);
                    resumeB.setEnabled(true);
                    cancelB.setEnabled(true);
                    clearB.setEnabled(false);
                    break;
                case Download.ERROR:
                    pauseB.setEnabled(false);
                    resumeB.setEnabled(true);
                    cancelB.setEnabled(false);
                    clearB.setEnabled(true);
                    break;
                default: // COMPLETED or CANCELLED
                    pauseB.setEnabled(false);
                    resumeB.setEnabled(false);
                    cancelB.setEnabled(false);
                    clearB.setEnabled(true);
            }
        } else {// No selected download in table
            pauseB.setEnabled(false);
            resumeB.setEnabled(false);
            cancelB.setEnabled(false);
            clearB.setEnabled(false);
        }
    }
    
//-----------------------------end_of_Buttons--------------------------------
    
    //it's Called when table row selection is changed
    private void tableSelectionChanged() {
        if (selectedDL != null)
            selectedDL.deleteObserver(DownloadAccelerator.this);
        if (!flag) {
            selectedDL = downloadTable.getDownload(table.getSelectedRow());
            selectedDL.addObserver(DownloadAccelerator.this);
            updateButtons();
        }
    }
    
    //Show a Dialog to choose the destination folder
    public String chooseFile() {
        String result = null;
  JFileChooser fileChooser = new JFileChooser();
  if (fileChooser.showSaveDialog(fileChooser) == JFileChooser.APPROVE_OPTION) {
    result = fileChooser.getSelectedFile().toString();
  }
  return result;
}
//---------------------------------------------------------------------------
    //it's called when a Download notifies its observer of any changes
    public void update( Observable o , Object obj ){
        
        if (selectedDL != null && selectedDL.equals(o))
            updateButtons();
    }
    
    //-----------------------------Exit-----------------------------
    private void winExit(){
        System.exit(0);
    }
//---------------------------------------------------------------------------
    public static void main(String[] args) {
        
        DownloadAccelerator DA = new DownloadAccelerator();
        DA.show();
    }
    
}