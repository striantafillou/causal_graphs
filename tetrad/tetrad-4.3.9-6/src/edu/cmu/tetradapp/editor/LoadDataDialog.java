package edu.cmu.tetradapp.editor;

import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.data.DataReader;
import edu.cmu.tetrad.data.DelimiterType;
import edu.cmu.tetrad.util.TetradLogger;
import edu.cmu.tetradapp.util.IntTextField;
import edu.cmu.tetradapp.util.StringTextField;
import edu.cmu.tetradapp.util.TextAreaOutputStream;
import edu.cmu.tetradapp.util.WatchedProcess;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.prefs.Preferences;

/**
 * Panel (to be put in a dialog) for letting the user choose how a data file
 * should be loaded.
 *
 * @author Joseph Ramsey
 */
final class LoadDataDialog extends JPanel {
    private File file;
    private JRadioButton tabularRadioButton;
    private JRadioButton covarianceRadioButton;

    private JRadioButton comment1RadioButton;
    private JRadioButton comment2RadioButton;
    private JRadioButton comment3RadioButton;
    private StringTextField commentStringField;

    private JRadioButton delimiter1RadioButton;
    private JRadioButton delimiter2RadioButton;
    private JRadioButton delimiter3RadioButton;

//    private JRadioButton delimiter4RadioButton;
//    private StringTextField delimiterStringField;
    private JRadioButton quote1RadioButton;
    private JRadioButton quote2RadioButton;

    private JCheckBox varNamesCheckBox;
    private JCheckBox idsSupplied;
    private JRadioButton id1RadioButton;
    private JRadioButton id2RadioButton;
    private StringTextField idStringField;

    private JRadioButton missing1RadioButton;
    private JRadioButton missing2RadioButton;
    private JRadioButton missing3RadioButton;

    private JCheckBox logEmptyTokens;
    private StringTextField missingStringField;
    private IntTextField maxIntegralDiscreteIntField;
    private DataModel dataModel;
    private JLabel maxIntegralLabel1;
    private JLabel maxIntegralLabel2;

    //================================CONSTRUCTOR=======================//

    public LoadDataDialog(final File file) {
        this.file = file;

        // Construct components.
        tabularRadioButton = new JRadioButton("Tabular Data");
        covarianceRadioButton = new JRadioButton("Covariance Data");

        comment1RadioButton = new JRadioButton("//");
        comment2RadioButton = new JRadioButton("#");
        comment3RadioButton = new JRadioButton("Other: ");

        String otherCommentPreference = Preferences.userRoot().get("dataLoaderOtherCommentPreference", "");
        commentStringField = new StringTextField(otherCommentPreference, 4);

        delimiter1RadioButton = new JRadioButton("Whitespace");
        delimiter2RadioButton = new JRadioButton("Tab");
        delimiter3RadioButton = new JRadioButton("Comma");
//        delimiter4RadioButton = new JRadioButton("Other: ");
//        delimiterStringField = new StringTextField("", 4);

        quote1RadioButton = new JRadioButton("\"");
        quote2RadioButton = new JRadioButton("'");

        logEmptyTokens = new JCheckBox("Log Empty Tokens");
        logEmptyTokens.setHorizontalTextPosition(SwingConstants.LEFT);

        varNamesCheckBox = new JCheckBox("Variable names in first row of data");
        varNamesCheckBox.setHorizontalTextPosition(SwingConstants.LEFT);

        idsSupplied = new JCheckBox("Case ID's provided");
        idsSupplied.setHorizontalTextPosition(SwingConstants.LEFT);

        boolean idsSuppliedPreference = Preferences.userRoot().getBoolean("dataLoaderIdsSuppliedPreference", false);
        idsSupplied.setSelected(idsSuppliedPreference);

        id1RadioButton = new JRadioButton("Unlabeled first column");
        id2RadioButton = new JRadioButton("Column labeled: ");
        idStringField = new StringTextField("", 4);

        id1RadioButton.setEnabled(false);
        id2RadioButton.setEnabled(false);
        idStringField.setEditable(false);

        idsSupplied.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JCheckBox button = (JCheckBox) e.getSource();
                boolean selected = button.isSelected();

                id1RadioButton.setEnabled(selected);
                id2RadioButton.setEnabled(selected);
                idStringField.setEditable(selected);
            }
        });

        missing1RadioButton = new JRadioButton("*");
        missing2RadioButton = new JRadioButton("?");
        missing3RadioButton = new JRadioButton("Other: ");

        String otherMissingPreference = Preferences.userRoot().get("dataLoaderOtherMissingPreference", "");
        missingStringField = new StringTextField(otherMissingPreference, 6);

        maxIntegralDiscreteIntField = new IntTextField(0, 3);

        final JTextArea fileTextArea = new JTextArea();
        final JTextArea anomaliesTextArea = new JTextArea();
        final JTabbedPane tabbedPane = new JTabbedPane();
        JScrollPane scroll1 = new JScrollPane(fileTextArea);
        scroll1.setPreferredSize(new Dimension(500, 400));
        tabbedPane.addTab("File", scroll1);
        JScrollPane scroll2 = new JScrollPane(anomaliesTextArea);
        scroll2.setPreferredSize(new Dimension(500, 400));
        tabbedPane.addTab("Loading Log", scroll2);

        JButton scanButton = new JButton("Load");

        // Construct button groups.
        ButtonGroup group1 = new ButtonGroup();
        group1.add(tabularRadioButton);
        group1.add(covarianceRadioButton);

        String tabularPreference = Preferences.userRoot().get("loadDataTabularPreference", "tabular");

        if ("tabular".equals(tabularPreference)) {
            tabularRadioButton.setSelected(true);
        } else if ("covariance".equals(tabularPreference)) {
            covarianceRadioButton.setSelected(true);
        } else {
            throw new IllegalStateException("Unexpected preference.");
        }

        ButtonGroup group2 = new ButtonGroup();
        group2.add(comment1RadioButton);
        group2.add(comment2RadioButton);
        group2.add(comment3RadioButton);

        String commentPreference = Preferences.userRoot().get("loadDataCommentPreference", "//");

        if ("//".equals(commentPreference)) {
            comment1RadioButton.setSelected(true);
        } else if ("#".equals(commentPreference)) {
            comment2RadioButton.setSelected(true);
        } else if ("Other".equals(commentPreference)) {
            comment3RadioButton.setSelected(true);
        }


        ButtonGroup group3 = new ButtonGroup();
        group3.add(delimiter1RadioButton);
        group3.add(delimiter2RadioButton);
        group3.add(delimiter3RadioButton);

        String delimiterPreference = Preferences.userRoot().get("loadDataDelimiterPreference", "Whitespace");

        if ("Whitespace".equals(delimiterPreference)) {
            delimiter1RadioButton.setSelected(true);
        } else if ("Tab".equals(delimiterPreference)) {
            delimiter2RadioButton.setSelected(true);
        } else if ("Comma".equals(delimiterPreference)) {
            delimiter3RadioButton.setSelected(true);
        } else {
            throw new IllegalStateException("Unexpected preference.");
        }

        ButtonGroup group4 = new ButtonGroup();
        group4.add(quote1RadioButton);
        group4.add(quote2RadioButton);
        quote1RadioButton.setSelected(true);

        ButtonGroup group5 = new ButtonGroup();
        group5.add(id1RadioButton);
        group5.add(id2RadioButton);
        id1RadioButton.setSelected(true);

        ButtonGroup group6 = new ButtonGroup();
        group6.add(missing1RadioButton);
        group6.add(missing2RadioButton);
        group6.add(missing3RadioButton);
        missing1RadioButton.setSelected(true);

        // Select defaults for checkboxes.
        varNamesCheckBox.setSelected(true);
        id1RadioButton.setSelected(true);
        idStringField.setText("ID");
//        delimiterStringField.setText(";");
        commentStringField.setText("@");
        missingStringField.setText("Missing");

        // Setup file text area.
//        fileTextArea.setEditable(false);
        fileTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        setText(file, fileTextArea);

        // Layout.
        Box b = Box.createVerticalBox();

        Box b1 = Box.createHorizontalBox();
        b1.add(new JLabel("File Type:"));
        b1.add(Box.createHorizontalGlue());
        b.add(b1);

        Box b2 = Box.createHorizontalBox();
        b2.add(Box.createRigidArea(new Dimension(20, 1)));
        b2.add(tabularRadioButton);
        b2.add(covarianceRadioButton);
        b2.add(Box.createHorizontalGlue());
        b.add(b2);

        Box b5 = Box.createHorizontalBox();
        b5.add(new JLabel("Delimiter"));
        b5.add(Box.createHorizontalGlue());
        b.add(b5);

        Box b6 = Box.createHorizontalBox();
        b6.add(Box.createRigidArea(new Dimension(20, 1)));
        b6.add(delimiter1RadioButton);
        b6.add(delimiter2RadioButton);
        b6.add(delimiter3RadioButton);
//        b6.add(delimiter4RadioButton);
//        b6.add(delimiterStringField);
        b6.add(Box.createHorizontalGlue());
        b.add(b6);

        Box b9 = Box.createHorizontalBox();
//        b9.add(new JLabel("Variable names in first row of data"));
        b9.add(varNamesCheckBox);
        b9.add(Box.createHorizontalGlue());
        b.add(b9);

        Box b10 = Box.createHorizontalBox();
//        b10.add(new JLabel("Case ID's provided"));
        b10.add(idsSupplied);
        b10.add(Box.createHorizontalGlue());
        b.add(b10);

        Box b11 = Box.createHorizontalBox();
        b11.add(Box.createRigidArea(new Dimension(20, 1)));
        b11.add(id1RadioButton);
        b11.add(id2RadioButton);
        b11.add(idStringField);
        b11.add(Box.createHorizontalGlue());
        b.add(b11);

        Box b3 = Box.createHorizontalBox();
        b3.add(new JLabel("Comment Marker"));
        b3.add(Box.createHorizontalGlue());
        b.add(b3);

        Box b4 = Box.createHorizontalBox();
        b4.add(Box.createRigidArea(new Dimension(20, 1)));
        b4.add(comment1RadioButton);
        b4.add(comment2RadioButton);
        b4.add(comment3RadioButton);
        b4.add(commentStringField);
        b4.add(Box.createHorizontalGlue());
        b.add(b4);

        Box b7 = Box.createHorizontalBox();
        b7.add(new JLabel("Quote Character"));
        b7.add(Box.createHorizontalGlue());
        b.add(b7);

        Box b8 = Box.createHorizontalBox();
        b8.add(Box.createRigidArea(new Dimension(20, 1)));
        b8.add(quote1RadioButton);
        b8.add(quote2RadioButton);
        b8.add(Box.createHorizontalGlue());
        b.add(b8);

        Box b12 = Box.createHorizontalBox();
        b12.add(new JLabel("Missing value marker (other than blank field):"));
        b12.add(Box.createHorizontalGlue());
        b.add(b12);

        Box b13 = Box.createHorizontalBox();
        b13.add(Box.createRigidArea(new Dimension(20, 1)));
        b13.add(missing1RadioButton);
        b13.add(missing2RadioButton);
        b13.add(missing3RadioButton);
        b13.add(missingStringField);
        b13.add(Box.createHorizontalGlue());
        b.add(b13);
        b.add(Box.createVerticalStrut(5));

        Box b14 = Box.createHorizontalBox();
        maxIntegralLabel1 = new JLabel("Integral columns with up to ");
        maxIntegralLabel2 = new JLabel(" values are discrete.");
        b14.add(maxIntegralLabel1);
        b14.add(maxIntegralDiscreteIntField);
        b14.add(maxIntegralLabel2);
        b14.add(Box.createHorizontalGlue());

        b.add(b14);
        b.add(Box.createVerticalStrut(5));
        
        Box b16 = Box.createHorizontalBox();
        b16.add(this.logEmptyTokens);
        b16.add(Box.createHorizontalGlue());


        b.add(b16);


        b.add(Box.createVerticalGlue());
//        b.setBorder(new EmptyBorder(0, 0, 0, 3));
        b.setBorder(new TitledBorder("Data Loading Parameters"));

        Box c = Box.createVerticalBox();

        Box c1 = Box.createHorizontalBox();
//        JScrollPane scrollPane = new JScrollPane(tabbedPane);
//        scrollPane.setPreferredSize(new Dimension(500, 400));
//        c1.add(scrollPane);
        c1.add(tabbedPane);
        c.add(c1);

        Box c2 = Box.createHorizontalBox();
        c2.add(Box.createHorizontalGlue());
        c2.add(scanButton);
        c2.setBorder(new EmptyBorder(4, 4, 4, 4));
        c.add(c2);
        c.setBorder(new TitledBorder("Source File and Loading Log"));

        Box a = Box.createHorizontalBox();
        a.add(b);
        a.add(c);
        setLayout(new BorderLayout());
        add(a, BorderLayout.CENTER);

        // Listeners.

        //TODO here
        scanButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Window owner = (Window) getTopLevelAncestor();

                new WatchedProcess(owner) {
                    public void watch() {
                        Window owner = (Window) getTopLevelAncestor();

                        new WatchedProcess(owner) {
                            public void watch() {
                                anomaliesTextArea.setText("");

                                TextAreaOutputStream out1
                                        = new TextAreaOutputStream(anomaliesTextArea);
                                PrintStream out = new PrintStream(out1);

                                //LogUtils.getInstance().add(out, Level.FINEST);
                                TetradLogger.getInstance().addOutputStream(out);
                                boolean logging = TetradLogger.getInstance().isLogging();
                                TetradLogger.getInstance().setLogging(true);
                                
                                try {

                                    // Select the "Anomalies" tab.
                                    tabbedPane.setSelectedIndex(1);

                                    DataReader reader = new DataReader();
                                    reader.setLogEmptyTokens(logEmptyTokens.isSelected());

                                    reader.setCommentMarker(getCommentString());
                                    reader.setDelimiter(getDelimiterType());
                                    reader.setQuoteChar(getQuoteChar());
                                    reader.setVariablesSupplied(isVarNamesFirstRow());
                                    reader.setIdsSupplied(isIdsSupplied());
                                    reader.setIdLabel(getIdLabel());
                                    reader.setMissingValueMarker(getMissingValue());
                                    reader.setMaxIntegralDiscrete(getMaxDiscrete());

                                    DataModel dataModel;

                                    if (tabularRadioButton.isSelected()) {
//                                        dataModel = parser.parseTabular(string.toCharArray());
                                        dataModel = reader.parseTabular(file);
                                    }
                                    else {
//                                        String string = fileTextArea.getText();
//                                        dataModel = reader.parseCovariance(string.toCharArray());
                                        dataModel = reader.parseCovariance(file);
                                    }

                                    setDataModel(dataModel);

                                    anomaliesTextArea.setCaretPosition(
                                            anomaliesTextArea.getText().length());
                                }
                                catch (Exception e1) {
                                    out.println(e1.getMessage());
                                    out.println("\nIf that message was unhelpful, " +
                                            "\nplease copy and paste the (Java) " +
                                            "\nerror below to Joe Ramsey, " +
                                            "\njdramsey@andrew.cmu.edu, " +
                                            "\nso a better error message " +
                                            "\ncan be put at that location." +
                                            "\nThanks!");

                                    out.println();
                                    e1.printStackTrace(out);
                                }

                                TetradLogger.getInstance().removeOutputStream(out);
                                TetradLogger.getInstance().setLogging(logging);
                            }
                        };
                    }
                };
            }
        });

        tabularRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                idsSupplied.setEnabled(true);
//                id1RadioButton.setEnabled(true);
//                id2RadioButton.setEnabled(true);
                idStringField.setEditable(true);
                maxIntegralLabel1.setEnabled(true);
                maxIntegralLabel2.setEnabled(true);
                maxIntegralDiscreteIntField.setEnabled(true);
                varNamesCheckBox.setEnabled(true);


                if (idsSupplied.isSelected()) {
                    id1RadioButton.setEnabled(true);
                    id2RadioButton.setEnabled(true);
                }
            }
        });

        covarianceRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                idsSupplied.setEnabled(false);
                id1RadioButton.setEnabled(false);
                id2RadioButton.setEnabled(false);
                idStringField.setEditable(false);
                maxIntegralLabel1.setEnabled(false);
                maxIntegralLabel2.setEnabled(false);
                maxIntegralDiscreteIntField.setEnabled(false);
                varNamesCheckBox.setEnabled(false);
            }
        });
    }

    //==============================PUBLIC METHODS=========================//

    public DataModel getDataModel() {
        return dataModel;
    }

    private static void setText(File file, JTextArea textArea) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            StringBuffer text = new StringBuffer();
            String line;

            while ((line = in.readLine()) != null) {
                text.append(line.substring(0, line.length())).append("\n");

                if (text.length() > 50000) {
                    textArea.append("(This is a large file that begins as follows...)\n");
                    textArea.setEditable(false);
                    break;
                }
            }

            textArea.append(text.toString());

            if (!textArea.isEditable()) {
                textArea.append(". . .");
            }

            textArea.setCaretPosition(0);
            in.close();
        }
        catch (IOException e) {
            textArea.append("<<<ERROR READING FILE>>>");
            textArea.setEditable(false);
        }
    }

//    private static void setText(File file, JTextArea textArea) {
//        try {
//            FileReader in = new FileReader(file);
//            CharArrayWriter out = new CharArrayWriter();
//            int c;
//
//            while ((c = in.read()) != -1) {
//                out.write(c);
//            }
//
//            textArea.setText(out.toString());
//
//            textArea.setCaretPosition(0);
//            in.close();
//        }
//        catch (IOException e) {
//            textArea.append("<<<ERROR READING FILE>>>");
//        }
//    }


    private String getCommentString() {
        if (comment1RadioButton.isSelected()) {
            return "//";
        }
        else if (comment2RadioButton.isSelected()) {
            return "#";
        }
        else {
            return commentStringField.getText();
        }
    }

    private DelimiterType getDelimiterType() {
        if (delimiter1RadioButton.isSelected()) {
            return DelimiterType.WHITESPACE;
        }
        else if (delimiter2RadioButton.isSelected()) {
            return DelimiterType.TAB;
        }
        else if (delimiter3RadioButton.isSelected()) {
            return DelimiterType.COMMA;
        }
        else {
//            return delimiterStringField.getText();
            throw new IllegalArgumentException("Unexpected delimiter selection.");
        }
    }

    private char getQuoteChar() {
        if (quote1RadioButton.isSelected()) {
            return '"';
        }
        else {
            return '\'';
        }
    }

    private boolean isVarNamesFirstRow() {
        return varNamesCheckBox.isSelected();
    }

    private boolean isIdsSupplied() {
        return idsSupplied.isSelected();
    }

    private String getIdLabel() {
        if (id1RadioButton.isSelected()) {
            return null;
        }
        else {
            return idStringField.getText();
        }
    }

    private String getMissingValue() {
        if (missing1RadioButton.isSelected()) {
            return "*";
        }
        else if (missing2RadioButton.isSelected()) {
            return "?";
        }
        else {
            return missingStringField.getText();
        }
    }

    private int getMaxDiscrete() {
        return maxIntegralDiscreteIntField.getValue();
    }

    private void setDataModel(DataModel dataModel) {
        this.dataModel = dataModel;
    }
}