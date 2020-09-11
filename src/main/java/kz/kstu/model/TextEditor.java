package kz.kstu.model;

import kz.kstu.service.XmlParser;
import org.drjekyll.fontchooser.FontDialog;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;

public class TextEditor extends JFrame implements ActionListener, WindowListener {
    private static volatile TextEditor instance;
    private final JTextArea jTextArea = new JTextArea();
    private File file;
    private Container container = getContentPane();
    private JMenuBar jMenuBar = new JMenuBar();
    private JMenu jMenuFile = new JMenu("Файл");
    private JMenu jMenuEdit = new JMenu("Редатировать");
    private JMenu jMenuHelp = new JMenu("Помощь");
    private Font font = new Font("Arial", Font.PLAIN, 15);
    private JFileChooser jfc = new JFileChooser();
    private JScrollPane sbrText = new JScrollPane(jTextArea);

    private TextEditor() {
        container.setLayout(new BorderLayout());
        sbrText.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        sbrText.setVisible(true);

        jTextArea.setFont(font);
        jTextArea.setLineWrap(true);
        jTextArea.setWrapStyleWord(true);


        container.add(sbrText);

        createMenuItem(jMenuFile, "Новый");
        createMenuItem(jMenuFile, "Открыть");
        createMenuItem(jMenuFile, "Сохранить");
        createMenuItem(jMenuFile, "Выгрузить метаданные");

        jMenuFile.addSeparator();

        createMenuItem(jMenuEdit, "Выбрать шрифт");
        createMenuItem(jMenuFile, "Выйти");
        createMenuItem(jMenuEdit, "Вырезать");
        createMenuItem(jMenuEdit, "Копировать");
        createMenuItem(jMenuEdit, "Вставить");
        createMenuItem(jMenuHelp, "О редакторе");

        jMenuBar.add(jMenuFile);
        jMenuBar.add(jMenuEdit);
        jMenuBar.add(jMenuHelp);

        setJMenuBar(jMenuBar);
        setIconImage(Toolkit.getDefaultToolkit().getImage("textEditor.gif"));
        addWindowListener(this);
        setSize(500, 500);
        setTitle("Нет имени.txt - Текстовый редактор");
        setVisible(true);
    }

    public static TextEditor getInstance() {
        TextEditor localInstance = instance;
        if (localInstance == null) {
            synchronized (TextEditor.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new TextEditor();
                }
            }
        }
        return localInstance;
    }

    public void createMenuItem(JMenu jm, String txt) {
        JMenuItem jMenuItem = new JMenuItem(txt);
        jMenuItem.addActionListener(this);
        jm.add(jMenuItem);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Новый")) {
            this.setTitle("Нет имени.txt - TextEditor");
            jTextArea.setText("");
            file = null;
        } else if (e.getActionCommand().equals("Открыть")) {

            int ret = jfc.showDialog(null, "Открыть");

            if (ret == JFileChooser.APPROVE_OPTION) {
                try {
                    File fyl = jfc.getSelectedFile();
                    openFile(fyl.getAbsolutePath());
                    this.setTitle(fyl.getName() + " - Текстовый редактор");
                    file = fyl;
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }

        } else if (e.getActionCommand().equals("Сохранить")) {
            if (file != null) {
                jfc.setCurrentDirectory(file);
                jfc.setSelectedFile(file);
            } else {
                jfc.setSelectedFile(new File("Нет имени.txt"));
            }

            int ret = jfc.showSaveDialog(null);

            if (ret == JFileChooser.APPROVE_OPTION) {
                try {
                    File newFile = jfc.getSelectedFile();
                    saveFile(newFile.getAbsolutePath());
                    this.setTitle(newFile.getName() + " - Текстовый редактор");
                    file = newFile;
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }

        } else if (e.getActionCommand().equals("Выгрузить метаданные")) {
            if (file != null) {
                jfc.setCurrentDirectory(file);
                jfc.setSelectedFile(file);
            } else {
                jfc.setSelectedFile(new File("Нет имени.xml"));
            }

            int ret = jfc.showSaveDialog(null);

            if (ret == JFileChooser.APPROVE_OPTION) {
                try {
                    File newFile = jfc.getSelectedFile();
                    Metadata metadata = new Metadata();
                    metadata.setFilename(getTitle());
                    metadata.setTextSize(jTextArea.getText().length());
                    metadata.setFontName(jTextArea.getFont().getFontName());
                    metadata.setFontSize(jTextArea.getFont().getSize());
                    metadata.setFontFamily(jTextArea.getFont().getFamily());
                    loadMetadata(metadata, newFile.getAbsolutePath());
                    this.setTitle(newFile.getName() + " - Текстовый редактор");
                    file = newFile;
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        } else if (e.getActionCommand().equals("Выйти")) {
            exiting();
        } else if (e.getActionCommand().equals("Копировать")) {
            jTextArea.copy();
        } else if (e.getActionCommand().equals("Вставить")) {
            jTextArea.paste();
        } else if (e.getActionCommand().equals("О редакторе")) {
            JOptionPane.showMessageDialog(this, "Текстовый редактор был создан в ходе обучения. Руководитель Исин Б.Д", "Текстовый редактор", JOptionPane.INFORMATION_MESSAGE);
        } else if (e.getActionCommand().equals("Вырезать")) {
            jTextArea.cut();
        } else if (e.getActionCommand().equals("Выбрать шрифт")) {
            FontDialog.showDialog(jTextArea);
        }
    }

    public void openFile(String fname) throws IOException {
        BufferedReader d = new BufferedReader(new InputStreamReader(new FileInputStream(fname)));
        String l;
        jTextArea.setText("");
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        while ((l = d.readLine()) != null) {
            jTextArea.setText(jTextArea.getText() + l + "\r\n");
        }
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        d.close();
    }

    public void saveFile(String fname) throws IOException {
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        DataOutputStream o = new DataOutputStream(new FileOutputStream(fname));
        o.writeBytes(jTextArea.getText());
        o.close();
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    public void loadMetadata(Metadata metadata, String filename) throws IOException {
        DataOutputStream o = new DataOutputStream((new FileOutputStream(filename)));
        o.writeUTF(XmlParser.jaxbObjectToXML(metadata));
        o.close();
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        exiting();
    }

    public void windowOpened(WindowEvent e) {
    }

    public void exiting() {
        System.exit(0);
    }

}