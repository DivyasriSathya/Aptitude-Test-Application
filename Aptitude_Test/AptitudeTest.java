//package Swing;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class AptitudeTest extends JFrame implements ActionListener {
    private JLabel questionLabel;
    private JRadioButton option1, option2, option3, option4;
    private ButtonGroup optionsGroup;
    private JButton submitButton;
    private int score = 0;
    private JLabel timerLabel;

    private List<String[]> questions = new ArrayList<>();
    private List<String> correctAnswers = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private List<Integer> questionOrder;
    private Timer timer;
    private String playerName;

    public AptitudeTest() {
        setTitle("Aptitude Test");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set layout for the frame
        //setLayout(new BorderLayout());
        // Ask for aptitude type
        String[] aptitudeTypes = {"Mathematics", "Verbal", "Coding", "Logical Reasoning"};
        playerName = JOptionPane.showInputDialog(this, "Enter your name:");

        if (playerName == null || playerName.trim().isEmpty()) {
            // User clicked "Cancel"
            System.exit(0);
        }

        String aptitudeType = (String) JOptionPane.showInputDialog(this, "Select aptitude type:", "Aptitude Type", JOptionPane.QUESTION_MESSAGE, null, aptitudeTypes, aptitudeTypes[0]);

        if (aptitudeType == null) {
            // User clicked "Cancel"
            System.exit(0);
        }

        // Load questions from CSV file
        loadQuestionsFromCSV(  aptitudeType.toLowerCase() + ".csv", 10); 
        // Initialize question order and shuffle it
        questionOrder = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            questionOrder.add(i);
        }
        Collections.shuffle(questionOrder);

        questionLabel = new JLabel();
        questionLabel.setVerticalAlignment(JLabel.TOP);
        questionLabel.setFont(new Font("Arial", Font.PLAIN, 20));

        timerLabel = new JLabel("Time: 1:00");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 24));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setOpaque(false);

        JPanel questionPanel = new JPanel(new BorderLayout());
        questionPanel.setOpaque(false);
        questionPanel.add(questionLabel, BorderLayout.NORTH);
        questionPanel.add(timerLabel, BorderLayout.SOUTH);

        JPanel optionsPanel = new JPanel(new GridLayout(4, 1));
        optionsPanel.setOpaque(false);
        option1 = new JRadioButton();
        option2 = new JRadioButton();
        option3 = new JRadioButton();
        option4 = new JRadioButton();
        optionsGroup = new ButtonGroup();
        optionsGroup.add(option1);
        optionsGroup.add(option2);
        optionsGroup.add(option3);
        optionsGroup.add(option4);
        optionsPanel.add(option1);
        optionsPanel.add(option2);
        optionsPanel.add(option3);
        optionsPanel.add(option4);

        option1.setOpaque(false);
        option2.setOpaque(false);
        option3.setOpaque(false);
        option4.setOpaque(false);

        option1.setFont(new Font("Arial", Font.PLAIN, 16));
        option2.setFont(new Font("Arial", Font.PLAIN, 16));
        option3.setFont(new Font("Arial", Font.PLAIN, 16));
        option4.setFont(new Font("Arial", Font.PLAIN, 16));

        questionPanel.add(optionsPanel, BorderLayout.CENTER);
        mainPanel.add(questionPanel);

        submitButton = new JButton("Submit");
        submitButton.addActionListener(this);

        BackgroundPanel BackgroundPanel = new BackgroundPanel("background.png");
        BackgroundPanel.setLayout(new BorderLayout());
        BackgroundPanel.add(mainPanel, BorderLayout.CENTER);
        BackgroundPanel.add(submitButton, BorderLayout.SOUTH);

        setContentPane(BackgroundPanel);
        loadQuestion();

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == submitButton) {
            checkAnswer();
            currentQuestionIndex++;
            if (currentQuestionIndex < questions.size()) {
                loadQuestion();
            } else {
                JOptionPane.showMessageDialog(this, "Your score is: " + score + "/" + questions.size());
                writeDataToCSV(playerName, score);
                displayScoreboard();
                System.exit(0);
            }
        }
    }

    private void loadQuestion() {
        if (questions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No questions loaded!");
            System.exit(0);
        }

        int questionIndex = questionOrder.get(currentQuestionIndex);
        String[] currentQuestion = questions.get(questionIndex);
        questionLabel.setText("<html>" + currentQuestion[0] + "</html>");

        List<String> options = Arrays.asList(currentQuestion[1], currentQuestion[2], currentQuestion[3], currentQuestion[4]);
        Collections.shuffle(options);

        option1.setText(options.get(0));
        option1.setActionCommand(options.get(0));
        option2.setText(options.get(1));
        option2.setActionCommand(options.get(1));
        option3.setText(options.get(2));
        option3.setActionCommand(options.get(2));
        option4.setText(options.get(3));
        option4.setActionCommand(options.get(3));

        optionsGroup.clearSelection();

        // 1 minute
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            
            int remainingTime = 60;
            public void run() {
                remainingTime--;
                timerLabel.setText("Time: " + remainingTime);
                if (remainingTime <= 0) {
                    timer.cancel();
                    checkAnswer();
                    currentQuestionIndex++;
                    if (currentQuestionIndex < questions.size()) {
                        loadQuestion();
                    } else {
                        JOptionPane.showMessageDialog(AptitudeTest.this, "Your score is: " + score + "/" + questions.size());
                        writeDataToCSV(playerName, score);
                        displayScoreboard();
                        System.exit(0);
                    }
                }
            }
        }, 0, 1000); // Update the timer every second
    }

    private void loadQuestionsFromCSV(String fileName, int numberOfQuestions) {
        List<String[]> questionsList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] questionData = line.split(",");
                if (questionData.length >= 5) {
                    questionsList.add(questionData);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading questions: " + e.getMessage());
            System.exit(0);
        }

        Collections.shuffle(questionsList);
        for (int i = 0; i < numberOfQuestions && i < questionsList.size(); i++) {
            questions.add(questionsList.get(i));
            correctAnswers.add(questionsList.get(i)[5]); // add correct answer to correctAnswers list
        }
    }

    private void checkAnswer() {
        String selectedAnswer = null;
        if (optionsGroup.getSelection() != null) {
            selectedAnswer = optionsGroup.getSelection().getActionCommand();
        }

        if (selectedAnswer != null && selectedAnswer.equals(correctAnswers.get(questionOrder.get(currentQuestionIndex)))) {
            score++;
        }

        if (timer != null) {
            timer.cancel();
        }
    }

    private void writeDataToCSV(String playerName, int score) {
        String csvFile = "scores.csv";
        String newLine = System.getProperty("line.separator");

        try (FileWriter writer = new FileWriter(csvFile, true)) {
            writer.append(playerName);
            writer.append(',');
            writer.append(String.valueOf(score));
            writer.append(newLine);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void displayScoreboard() {
        List<String[]> scoreData = readScoreCSV("scores.csv");
        scoreData.sort((a, b) -> Integer.compare(Integer.parseInt(b[1]), Integer.parseInt(a[1])));
        String[] columnNames = {"Rank", "Name", "Score"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        for (int i = 0; i < scoreData.size(); i++) {
            String[] row = new String[scoreData.get(i).length + 1];
            row[0] = String.valueOf(i + 1); // S.NO column
            System.arraycopy(scoreData.get(i), 0, row, 1, scoreData.get(i).length);
            model.addRow(row);
        }

        JTable scoreTable = new JTable(model);
        scoreTable.setRowHeight(30);
        scoreTable.setFont(new Font("Arial", Font.PLAIN, 18));
        scoreTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 20));
        scoreTable.getTableHeader().setBackground(Color.BLUE);
        scoreTable.getTableHeader().setForeground(Color.WHITE);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < scoreTable.getColumnCount(); i++) {
            scoreTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        for (int i = 0; i < scoreData.size(); i++) {
            if (scoreData.get(i)[0].equals(playerName)) {
                scoreTable.setRowSelectionInterval(i, i);
                break;
            }
        }
        JScrollPane scrollPane = new JScrollPane(scoreTable);

        JOptionPane.showMessageDialog(this, scrollPane, "Scoreboard", JOptionPane.INFORMATION_MESSAGE);
    }
    private List<String[]> readScoreCSV(String fileName) {
        List<String[]> scoreData = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                scoreData.add(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return scoreData;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AptitudeTest::new);
        
    }
}
class BackgroundPanel extends JPanel {
    private Image backgroundImage;

    public BackgroundPanel(String imagePath) {
        try {
            backgroundImage = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage!= null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
