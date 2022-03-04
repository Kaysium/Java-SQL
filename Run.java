import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

class Run {
    private JFrame frame = new JFrame("Application");
    private JButton button = new JButton("Run");
    private JButton exit = new JButton("Exit");
    private JButton help = new JButton("Reset");
    private JPanel panel = new JPanel(new BorderLayout());
    private JTextPane textPane;
    private String[] lines;
    private String[] lineCommand;
    private String[] lineFunction;
    private String instructionCount = "";

    // DATABASE CONNECTION
    private Connection connection;
    private String database = "";
    private String URL = "jdbc:mysql://localhost:3306/" + database;
    private String username = "root";
    private String password = "";
    
    private String numReg = ""; // USED IN INITIALIZATION
    private int num = 0; // USED IN INITIALIZATION
    private boolean flag = true; // AS WELL TO KNOW IF A CLEAR REGISTER NEEDS TO BE DONE OR NOT
    private int status = 0;
    private int numI = 0;
    //

    // FOR LOADING THE DATABASE
    private int z = 0;
    //

    // FOR ADDITION/SUBTRACTION
    private boolean add = true;
    //

    // FOR TEXTPANE
    private int findLastNonWordChar(String text, int index) {
        while (--index >= 0) {
            if (String.valueOf(text.charAt(index)).matches("\\W")) {
                break;
            }
        }
        return index;
    }

    private int findFirstNonWordChar(String text, int index) {
        while (index < text.length()) {
            if (String.valueOf(text.charAt(index)).matches("\\W")) {
                break;
            }
            index++;
        }
        return index;
    }
    //

    public Run() throws Exception {

        Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
        connection = DriverManager.getConnection(URL, username, password);

        // FRAME
        frame.setBackground(Color.BLUE.darker().darker().darker());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setVisible(true);
        frame.setIconImage(new ImageIcon("./Icon.png").getImage());
        frame.setResizable(false);
        //

        // PANEL
        panel.setLayout(null);
        panel.setBackground(Color.BLUE.darker().darker().darker().darker());
        panel.setSize(400, 400);
        panel.setBorder(new LineBorder(Color.BLACK, 3));
        //

        // BUTTON
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(Color.GREEN.darker().darker());
        button.setForeground(Color.WHITE);
        button.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(Color.GREEN.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(Color.GREEN.darker().darker());
            }
        });
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setFocusable(false);
        button.setBounds(new Rectangle(3, 3, 72, 30));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Initialize();
                    num = (numReg.isEmpty()) ? 0 : Integer.parseInt(numReg); // SAVES THE NUMBER OF REGISTERS FOR ONE
                                                                             // RUN
                    numReg = ""; // RESETS THE COUNTER TO BE ABLE TO RE INITIALIZE LATER ON

                    lines = textPane.getText().split("\n");
                    lineCommand = new String[lines.length];
                    lineFunction = new String[lines.length];

                    for (int i = 1; i < lines.length; i++) { // TO PLACE THE INSTRUCTIONS IN THE INPUTFROMUSER TABLE
                        lineFunction[i - 1] = lines[i].split(" ")[0].trim();
                        lineCommand[i - 1] = lines[i].split(" ")[1].trim();

                        String inputCommand = lineCommand[i - 1];
                        String inputFunction = lineFunction[i - 1];
                        instructionCount = String.valueOf(i - 1);

                        PreparedStatement ps = connection.prepareStatement(
                                "INSERT INTO `javaapplication`.`inputfromuser` (`InstructionCount`,`Command`, `Function`) VALUES (?, ?, ?);");
                        ps.setString(1, instructionCount);
                        ps.setString(2, inputCommand);
                        ps.setString(3, inputFunction);

                        status = ps.executeUpdate();
                    }

                    for (int i = 0; i < lines.length - 1; i++) {
                        String Query = "SELECT `Function` FROM `javaapplication`.`inputfromuser` WHERE InstructionCount = "
                                + i + ";";
                        Statement st = connection.createStatement();
                        ResultSet rs = st.executeQuery(Query);

                        numI = i;
                        while (rs.next()) {
                            String function = rs.getString("Function");

                            switch (function) {
                                case "lwi":
                                    LoadI();
                                    break;
                                case "add":
                                    add();
                                    break;
                                case "sub":
                                    add = false;
                                    add();
                                    break;
                                default:
                                    JOptionPane.showMessageDialog(frame, "Unknown Function!", "Command",
                                            JOptionPane.ERROR_MESSAGE);
                                    break;
                            }
                        }
                    }

                    System.out.println("Run Successful.");
                } catch (Exception e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error: " + status, "RunError",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        //

        // TEXT PANE
        final StyleContext cont = StyleContext.getDefaultStyleContext();
        final AttributeSet attr = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground,
                Color.RED);
        final AttributeSet attrBlue = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground,
                Color.BLUE.darker());
        final AttributeSet attrBlack = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, Color.BLACK);
        DefaultStyledDocument doc = new DefaultStyledDocument() {
            public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
                super.insertString(offset, str, a);

                String text = getText(0, getLength());
                int before = findLastNonWordChar(text, offset);
                if (before < 0)
                    before = 0;
                int after = findFirstNonWordChar(text, offset + str.length());
                int wordL = before;
                int wordR = before;

                while (wordR <= after) {
                    if (wordR == after || String.valueOf(text.charAt(wordR)).matches("\\W")) {
                        if (text.substring(wordL, wordR).matches("(\\W)*(add|sub|beq|bne|lwi|sw|lw)")) {
                            setCharacterAttributes(wordL, wordR - wordL, attr, false);

                        } else if (text.substring(wordL, wordR).matches("(\\W)*(Initialize|end)")) {
                            setCharacterAttributes(wordL, wordR - wordL, attrBlue, false);

                        } else {
                            setCharacterAttributes(wordL, wordR - wordL, attrBlack, false);
                        }
                        wordL = wordR;
                    }
                    wordR++;
                }
            }

            public void remove(int offs, int len) throws BadLocationException {
                super.remove(offs, len);

                String text = getText(0, getLength());
                int before = findLastNonWordChar(text, offs);
                if (before < 0)
                    before = 0;
                int after = findFirstNonWordChar(text, offs);

                if (text.substring(before, after).matches("(\\W)*(add|sub|beq|bne|lwi|sw|lw)")) {
                    setCharacterAttributes(before, after - before, attr, false);

                } else if (text.substring(before, after).matches("(\\W)*(Initialize|end)")) {
                    setCharacterAttributes(before, after - before, attrBlue, false);

                } else {
                    setCharacterAttributes(before, after - before, attrBlack, false);
                }
            }
        };
        textPane = new JTextPane(doc);
        textPane.getDocument().putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");
        textPane.setBorder(new LineBorder(Color.BLACK, 3));
        textPane.setFont(new Font("Arial", Font.BOLD, 16));
        textPane.setBounds(new Rectangle(75, 0, 310, 360));
        //

        // Exit button
        exit.setBackground(Color.RED.darker().darker());
        exit.setForeground(Color.WHITE);
        exit.setBorderPainted(false);
        exit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exit.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                exit.setBackground(Color.RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                exit.setBackground(Color.RED.darker().darker());
            }
        });
        exit.setFont(new Font("Arial", Font.BOLD, 18));
        exit.setFocusable(false);
        exit.setBounds(new Rectangle(3, 328, 72, 30));
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (flag == true) {
                        clearRegisters();
                    } // RESETING TO FUTUREPROOF THE RE RUN
                } catch (Exception e1) {
                    e1.printStackTrace();
                } finally {
                    System.out.println("Exit Succesful.");
                    System.exit(1);
                }
            }
        });
        //

        // HELP BUTTON OR RESET BUTTON
        help.setBackground(Color.ORANGE.darker());
        help.setForeground(Color.WHITE);
        help.setBorderPainted(false);
        help.setCursor(new Cursor(Cursor.HAND_CURSOR));
        help.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                help.setBackground(Color.ORANGE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                help.setBackground(Color.ORANGE.darker());
            }
        });
        help.setFont(new Font("Arial", Font.BOLD, 13));
        help.setFocusable(false);
        help.setBounds(new Rectangle(3, 165, 72, 30));
        help.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    textPane.setText("");
                    PreparedStatement ps = connection.prepareStatement(
                            "DELETE FROM `javaapplication`.`inputfromuser`;");
                    status = ps.executeUpdate();

                    PreparedStatement ps2 = connection.prepareStatement(
                            "DELETE FROM `javaapplication`.`registers`;");
                    status = ps2.executeUpdate();

                    clearRegisters(); // TO RESET

                    flag = false;
                    z = 0;
                    System.out.println("Deletion Successful.");
                } catch (Exception e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error: " + status, "DeletionError",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        //

        // MISC
        panel.add(button);
        panel.add(help);
        panel.add(exit);
        panel.add(textPane);
        frame.add(panel);
        //
    }

    public static void main(String[] args) throws Exception {
        new Run();
    }

    private void Initialize() throws Exception {
        String[] innit = textPane.getText().split("\n");

        if (innit[0].startsWith("Initialize")) {
            this.numReg = innit[0].split(" ")[1]; // Getting the number of Regs that needs to be used

            for (int i = 0; i < Integer.parseInt(this.numReg); i++) {
                try {
                    PreparedStatement ps = connection.prepareStatement(
                            "ALTER TABLE `javaapplication`.`registers` ADD `R" + (i + 1) + "` VARCHAR(45) DEFAULT 0;");
                    status = ps.executeUpdate();

                } catch (Exception e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Cannot Initialize " + numReg, "InitializationError",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
            flag = true;
        } else {
            JOptionPane.showMessageDialog(frame, "Never Initialized!", "InitializationError",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearRegisters() throws Exception {
        for (int i = 0; i < this.num; i++) {
            try {
                PreparedStatement ps3 = connection.prepareStatement(
                        "ALTER TABLE `javaapplication`.`registers` DROP `R" + (i + 1) + "`;");
                status = ps3.executeUpdate();
            } catch (Exception e3) {
                e3.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error in resetting Registers!", "DeletionError",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void LoadI() throws Exception {
        try {
            String Query = "SELECT `Command` FROM `javaapplication`.`inputfromuser` WHERE InstructionCount = "
                    + numI + ";";
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(Query);

            while (rs.next()) {
                String command = rs.getString("Command");

                String reg = command.split(",")[0].trim();
                String regValue = command.split(",")[1].trim();

                if (Integer.parseInt(reg.substring(1, reg.length())) > this.num) {
                    throw new Exception("Register Number is out of range!");
                }

                if (z < 1) {
                    PreparedStatement ps = connection.prepareStatement(
                            "INSERT INTO `javaapplication`.`registers`  (`InstructionCount`, `" + reg
                                    + "`) VALUES (?,?);");
                    ps.setString(1, String.valueOf(numI));
                    ps.setString(2, regValue);
                    status = ps.executeUpdate();
                } else {
                    PreparedStatement ps = connection.prepareStatement(
                            "UPDATE `javaapplication`.`registers` SET `InstructionCount` = " + numI + ", `" + reg
                                    + "` = " + regValue);
                    status = ps.executeUpdate();
                }
                z++;
                System.out.println("Instruction Count Updated.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "SyntaxError!", "LoadImmediateError", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void add() {
        try {
            String Query = "SELECT `Command` FROM `javaapplication`.`inputfromuser` WHERE InstructionCount = "
                    + numI + ";";
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(Query);

            String regD = "";
            String regS = "";
            String regT = "";

            while (rs.next()) {
                regD = rs.getString("Command").split(",")[0].trim();
                regS = rs.getString("Command").split(",")[1].trim();
                regT = rs.getString("Command").split(",")[2].trim();

                if (Integer.parseInt(regD.substring(1, regD.length())) > this.num
                        || Integer.parseInt(regS.substring(1, regS.length())) > this.num
                        || Integer.parseInt(regT.substring(1, regT.length())) > this.num) {
                    throw new Exception("Register Number is out of range!");
                }
            }

            String query1 = "SELECT `" + regS + "`,`" + regT
                    + "` FROM `javaapplication`.`registers`;";
            Statement st1 = connection.createStatement();
            ResultSet rs1 = st1.executeQuery(query1);

            String regValueS = "";
            String regValueT = "";

            while (rs1.next()) {
                regValueS = rs1.getString(regS);
                regValueT = rs1.getString(regT);
            }

            int sum = (add == true) ? Integer.parseInt(regValueS) + Integer.parseInt(regValueT)
                    : Integer.parseInt(regValueS) - Integer.parseInt(regValueT);

            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE `javaapplication`.`registers` SET `InstructionCount` = " + numI + ", `" + regD
                            + "` = " + sum +
                            ";");
            status = ps.executeUpdate();

            add = true;
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "SyntaxError!", "AdditionError", JOptionPane.ERROR_MESSAGE);
        }
    }
}