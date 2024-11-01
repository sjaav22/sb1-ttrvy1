package com.example.tasko.launcher;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

public class TaskoLauncher extends Application {
    private TextArea consoleOutput;
    private Button startButton;
    private Button stopButton;
    private Process serverProcess;
    private Label statusLabel;
    private ProgressBar progressBar;
    private AtomicBoolean isServerRunning = new AtomicBoolean(false);

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Tasko Server Manager");

        // Main container
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #f8f9fa, #e9ecef);");

        // Header
        Label titleLabel = new Label("Tasko Server Manager");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Status section
        VBox statusBox = createStatusSection();

        // Action buttons
        HBox buttonBox = createActionButtons();

        // Console output
        VBox consoleBox = createConsoleOutput();

        // Instructions
        TitledPane instructionsPane = createInstructionsPane();

        // Add all components
        root.getChildren().addAll(
            titleLabel,
            new Separator(),
            statusBox,
            buttonBox,
            consoleBox,
            instructionsPane
        );

        Scene scene = new Scene(root, 800, 700);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Handle window closing
        primaryStage.setOnCloseRequest(e -> {
            if (isServerRunning.get()) {
                e.consume();
                showConfirmDialog(primaryStage);
            }
        });
    }

    private VBox createStatusSection() {
        VBox statusBox = new VBox(10);
        statusBox.setAlignment(Pos.CENTER);

        statusLabel = new Label("Server Status: Stopped");
        statusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #dc3545;");

        progressBar = new ProgressBar(0);
        progressBar.setVisible(false);
        progressBar.setPrefWidth(300);

        statusBox.getChildren().addAll(statusLabel, progressBar);
        return statusBox;
    }

    private HBox createActionButtons() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        startButton = new Button("Start Server");
        startButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-min-width: 120px;");
        startButton.setOnAction(e -> startServer());

        stopButton = new Button("Stop Server");
        stopButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-min-width: 120px;");
        stopButton.setDisable(true);
        stopButton.setOnAction(e -> stopServer());

        Button clearButton = new Button("Clear Console");
        clearButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-weight: bold; -fx-min-width: 120px;");
        clearButton.setOnAction(e -> consoleOutput.clear());

        buttonBox.getChildren().addAll(startButton, stopButton, clearButton);
        return buttonBox;
    }

    private VBox createConsoleOutput() {
        VBox consoleBox = new VBox(5);
        Label consoleLabel = new Label("Server Output:");
        consoleLabel.setStyle("-fx-font-weight: bold;");

        consoleOutput = new TextArea();
        consoleOutput.setEditable(false);
        consoleOutput.setWrapText(true);
        consoleOutput.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        consoleOutput.setPrefRowCount(15);

        consoleBox.getChildren().addAll(consoleLabel, consoleOutput);
        return consoleBox;
    }

    private TitledPane createInstructionsPane() {
        VBox instructionsContent = new VBox(10);
        instructionsContent.setPadding(new Insets(10));
        instructionsContent.getChildren().addAll(
            createInstructionLabel("1. Click 'Start Server' to compile and run Tasko"),
            createInstructionLabel("2. Wait for the build process to complete"),
            createInstructionLabel("3. Server will automatically start after successful build"),
            createInstructionLabel("4. Monitor server status and output in the console"),
            createInstructionLabel("5. Use 'Stop Server' to safely shut down"),
            createInstructionLabel("6. Access Tasko at http://localhost:8080 when running")
        );

        TitledPane instructionsPane = new TitledPane("Instructions", instructionsContent);
        instructionsPane.setExpanded(false);
        return instructionsPane;
    }

    private Label createInstructionLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #495057;");
        return label;
    }

    private void startServer() {
        startButton.setDisable(true);
        progressBar.setVisible(true);
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        updateStatus("Building", "#ffc107");

        new Thread(() -> {
            try {
                // Build the project
                updateConsole("Building project...\n");
                ProcessBuilder buildProcess = new ProcessBuilder(
                    System.getProperty("os.name").toLowerCase().contains("windows") 
                        ? "mvn.cmd" 
                        : "mvn",
                    "clean", "package", "-DskipTests"
                );
                buildProcess.redirectErrorStream(true);
                Process build = buildProcess.start();

                // Read build output
                BufferedReader buildReader = new BufferedReader(new InputStreamReader(build.getInputStream()));
                String line;
                while ((line = buildReader.readLine()) != null) {
                    String finalLine = line;
                    Platform.runLater(() -> updateConsole(finalLine + "\n"));
                }

                if (build.waitFor() == 0) {
                    Platform.runLater(() -> updateConsole("Build successful. Starting server...\n"));

                    // Start the server
                    ProcessBuilder serverProcess = new ProcessBuilder(
                        "java", "-jar", "target/tasko-0.0.1-SNAPSHOT.jar"
                    );
                    serverProcess.redirectErrorStream(true);
                    this.serverProcess = serverProcess.start();
                    isServerRunning.set(true);

                    // Read server output
                    BufferedReader serverReader = new BufferedReader(
                        new InputStreamReader(this.serverProcess.getInputStream())
                    );
                    String serverLine;
                    while ((serverLine = serverReader.readLine()) != null) {
                        String finalLine = serverLine;
                        Platform.runLater(() -> {
                            updateConsole(finalLine + "\n");
                            if (finalLine.contains("Started TaskoApplication")) {
                                updateStatus("Running", "#28a745");
                                progressBar.setVisible(false);
                                stopButton.setDisable(false);
                            }
                        });
                    }
                } else {
                    Platform.runLater(() -> {
                        updateConsole("Build failed. Check the output for errors.\n");
                        updateStatus("Build Failed", "#dc3545");
                        progressBar.setVisible(false);
                        startButton.setDisable(false);
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    updateConsole("Error: " + e.getMessage() + "\n");
                    updateStatus("Error", "#dc3545");
                    progressBar.setVisible(false);
                    startButton.setDisable(false);
                });
            }
        }).start();
    }

    private void stopServer() {
        if (serverProcess != null) {
            serverProcess.destroy();
            serverProcess = null;
            isServerRunning.set(false);
            startButton.setDisable(false);
            stopButton.setDisable(true);
            updateConsole("Server stopped\n");
            updateStatus("Stopped", "#dc3545");
        }
    }

    private void updateStatus(String status, String color) {
        statusLabel.setText("Server Status: " + status);
        statusLabel.setTextFill(Color.web(color));
    }

    private void updateConsole(String text) {
        consoleOutput.appendText(text);
        consoleOutput.setScrollTop(Double.MAX_VALUE);
    }

    private void showConfirmDialog(Stage primaryStage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Exit");
        alert.setHeaderText("Server is still running");
        alert.setContentText("Do you want to stop the server and exit?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                stopServer();
                primaryStage.close();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}