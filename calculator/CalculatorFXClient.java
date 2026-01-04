import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.rmi.Naming;

public class CalculatorFXClient extends Application {
    
    // Changed variable names
    private TextField resultScreen = new TextField();
    private double operand1 = 0;
    private String operationSymbol = "";
    private Calculator remoteCalculator;
    
    @Override
    public void init() {
        initializeRemoteConnection();
    }
    
    @Override
    public void start(Stage primaryWindow) {
        // Configure display field
        resultScreen.setFont(Font.font("Arial", 26));
        resultScreen.setEditable(false);
        resultScreen.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-alignment: CENTER-RIGHT;");
        resultScreen.setPrefHeight(70);
        
        // Create button panel
        GridPane buttonPanel = createButtonPanel();
        
        // Main container
        BorderPane mainContainer = new BorderPane();
        mainContainer.setTop(resultScreen);
        mainContainer.setCenter(buttonPanel);
        mainContainer.setStyle("-fx-padding: 20; -fx-background-color: #e8e8e8;");
        
        // Scene setup
        Scene mainScene = new Scene(mainContainer, 350, 450);
        setupKeyboardControls(mainScene);
        
        // Window configuration
        primaryWindow.setTitle("Distributed Calculator Application");
        primaryWindow.setScene(mainScene);
        primaryWindow.setResizable(false);
        primaryWindow.show();
    }
    
    private GridPane createButtonPanel() {
        GridPane panel = new GridPane();
        panel.setHgap(8);
        panel.setVgap(8);
        panel.setStyle("-fx-padding: 10;");
        
        // Button definitions with different organization
        String[][] buttonConfig = {
                {"C", "⌫", "÷", "×"},
                {"7", "8", "9", "-"},
                {"4", "5", "6", "+"},
                {"1", "2", "3", "="},
                {"0", ".", "=", "="}
        };
        
        // Create and position buttons
        for (int rowIndex = 0; rowIndex < buttonConfig.length; rowIndex++) {
            for (int colIndex = 0; colIndex < buttonConfig[rowIndex].length; colIndex++) {
                String buttonLabel = buttonConfig[rowIndex][colIndex];
                Button btn = createStyledButton(buttonLabel);
                
                // Span last button across two columns
                if (rowIndex == 4 && colIndex == 2) {
                    GridPane.setColumnSpan(btn, 2);
                }
                
                panel.add(btn, colIndex, rowIndex);
            }
        }
        
        return panel;
    }
    
    private Button createStyledButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", 20));
        btn.setPrefSize(75, 60);
        
        // Different styling for different button types
        if (text.matches("[+\\-×÷=]")) {
            btn.setStyle("-fx-background-color: #ff9500; -fx-text-fill: white; -fx-font-weight: bold;");
        } else if (text.matches("[C⌫]")) {
            btn.setStyle("-fx-background-color: #a6a6a6; -fx-text-fill: black;");
        } else {
            btn.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc;");
        }
        
        btn.setOnAction(event -> processUserInput(text));
        return btn;
    }
    
    private void initializeRemoteConnection() {
        try {
            remoteCalculator = (Calculator) Naming.lookup("rmi://localhost/CALCULATOR");
        } catch (Exception connectionError) {
            displayErrorMessage("Server Connection Failed");
            System.exit(1);
        }
    }
    
    private void processUserInput(String input) {
        try {
            if (input.matches("[0-9.]")) {
                resultScreen.appendText(input);
            } else if (input.matches("[+\\-×÷]")) {
                operand1 = Double.parseDouble(resultScreen.getText());
                operationSymbol = convertSymbol(input);
                resultScreen.clear();
            } else if (input.equals("=")) {
                performCalculation();
            } else if (input.equals("C")) {
                resultScreen.clear();
                operationSymbol = "";
            } else if (input.equals("⌫")) {
                removeLastCharacter();
            }
        } catch (Exception calcError) {
            resultScreen.setText("Invalid Operation");
        }
    }
    
    private String convertSymbol(String symbol) {
        // Convert display symbols to actual operators
        return switch (symbol) {
            case "×" -> "*";
            case "÷" -> "/";
            default -> symbol;
        };
    }
    
    private void performCalculation() {
        double operand2 = Double.parseDouble(resultScreen.getText());
        double calculationResult = 0;
        
        try {
            calculationResult = switch (operationSymbol) {
                case "+" -> remoteCalculator.add(operand1, operand2);
                case "-" -> remoteCalculator.subtract(operand1, operand2);
                case "*" -> remoteCalculator.multiply(operand1, operand2);
                case "/" -> remoteCalculator.divide(operand1, operand2);
                default -> operand2;
            };
            
            resultScreen.setText(String.format("%.6f", calculationResult));
            operationSymbol = "";
            
        } catch (Exception remoteError) {
            resultScreen.setText("Remote Error");
        }
    }
    
    private void removeLastCharacter() {
        String currentText = resultScreen.getText();
        if (!currentText.isEmpty()) {
            resultScreen.setText(currentText.substring(0, currentText.length() - 1));
        }
    }
    
    private void setupKeyboardControls(Scene scene) {
        scene.setOnKeyPressed(keyEvent -> {
            switch (keyEvent.getCode()) {
                case DIGIT0, NUMPAD0 -> processUserInput("0");
                case DIGIT1, NUMPAD1 -> processUserInput("1");
                case DIGIT2, NUMPAD2 -> processUserInput("2");
                case DIGIT3, NUMPAD3 -> processUserInput("3");
                case DIGIT4, NUMPAD4 -> processUserInput("4");
                case DIGIT5, NUMPAD5 -> processUserInput("5");
                case DIGIT6, NUMPAD6 -> processUserInput("6");
                case DIGIT7, NUMPAD7 -> processUserInput("7");
                case DIGIT8, NUMPAD8 -> processUserInput("8");
                case DIGIT9, NUMPAD9 -> processUserInput("9");
                case ADD, PLUS -> processUserInput("+");
                case SUBTRACT, MINUS -> processUserInput("-");
                case MULTIPLY -> processUserInput("×");
                case DIVIDE, SLASH -> processUserInput("÷");
                case ENTER, EQUALS -> processUserInput("=");
                case BACK_SPACE -> processUserInput("⌫");
                case DELETE, ESCAPE -> processUserInput("C");
                case PERIOD, DECIMAL -> processUserInput(".");
            }
        });
    }
    
    private void displayErrorMessage(String message) {
        Alert errorDialog = new Alert(Alert.AlertType.ERROR, message);
        errorDialog.setHeaderText("Connection Issue");
        errorDialog.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}