package me.minidigger.minecraftlauncher.launcher.gui;

import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import me.minidigger.minecraftlauncher.launcher.LauncherSettings;
import me.minidigger.minecraftlauncher.launcher.tasks.MinecraftStartTask;
import me.minidigger.minecraftlauncher.launcher.tasks.VersionCheckerTask;

public class MainFragmentController extends FragmentController {

    public static Stage applicationOptionStage;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private Tooltip optionsTooltip;
    @FXML
    private Tooltip playTooltip;
    @FXML
    private Tooltip usernameTooltip;
    @FXML
    private Tooltip passwordTooltip;
    @FXML
    private Tooltip versionTooltip;
    @FXML
    private Label label;
    @FXML
    private TextField username;
    @FXML
    private TextField password;
    @FXML
    private ComboBox<String> version;
    @FXML
    private Button launch;
    @FXML
    private Button options;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setToolTips();
        setTextBoxMax();

        username.setText(LauncherSettings.playerUsername);

        version.getItems().addAll(minecraftDownloader.);

        for (String ob : API.getInstalledVersionsList()) {
            if (ob.equals(LauncherSettings.playerVersion)) {
                version.setValue(LauncherSettings.playerVersion);
            }
        }
    }

    @FXML
    private void launchMineCraft(ActionEvent event) {
        if (username.getText().equals("")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(resourceBundle.getString("alert.username_missing.title"));
            alert.setHeaderText(resourceBundle.getString("alert.username_missing.header"));
            alert.setContentText(resourceBundle.getString("alert.username_missing.content"));
            alert.initStyle(StageStyle.UTILITY);

            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add("/css/purple.css");

            alert.show();
            return;
        }

        LauncherSettings.playerUsername = username.getText();
        LauncherSettings.playerVersion = version.getValue();
        LauncherSettings.userSettingsSave();

        options.setDisable(true);
        launch.setDisable(true);
        version.setDisable(true);
        username.setDisable(true);
        password.setDisable(true);

        getMainFrame().loadAvatar();

        new MinecraftStartTask(this::onGameCorrupted, this::onGameStarted).start();
    }

    @FXML
    private void launchOptions(ActionEvent event) {
        getMainFrame().load(FrameController.Screen.OPTION);
    }

    public void onGameStarted() {
        LauncherSettings.playerUsername = username.getText();
        LauncherSettings.playerVersion = version.getValue();
        LauncherSettings.userSettingsSave();

        if (!LauncherSettings.keepLauncherOpen) {
            Platform.runLater(() -> setStatusText(resourceBundle.getString("status.minecraft_started")));
            System.exit(0);
        } else {
            new VersionCheckerTask(this::setStatusText).start();
        }

        username.setDisable(false);
        password.setDisable(false);
        options.setDisable(false);
        launch.setDisable(false);
        version.setDisable(false);
    }

    public void onGameCorrupted() {
        Platform.runLater(() -> {
            setStatusText(resourceBundle.getString("status.corrupted"));

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(resourceBundle.getString("alert.corruption.title"));
            alert.setHeaderText(MessageFormat.format(resourceBundle.getString("alert.corruption.header"), version.getValue()));
            alert.setContentText(resourceBundle.getString("alert.corruption.content"));
            alert.initStyle(StageStyle.UTILITY);

            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add("/css/purple.css");
            alert.showAndWait();

            username.setDisable(false);
            password.setDisable(false);
            options.setDisable(false);
            launch.setDisable(false);
            version.setDisable(false);
        });
    }

    private void setToolTips() {
        Image infoIMG = new Image(getClass().getResourceAsStream("/images/m_info.png"));

        usernameTooltip.setText(resourceBundle.getString("mainscreen.tooltip.username"));
        usernameTooltip.setGraphic(new ImageView(infoIMG));

        passwordTooltip.setText(resourceBundle.getString("mainscreen.tooltip.password"));
        passwordTooltip.setGraphic(new ImageView(infoIMG));

        versionTooltip.setText(resourceBundle.getString("mainscreen.tooltip.version"));
        versionTooltip.setGraphic(new ImageView(infoIMG));

        optionsTooltip.setText(resourceBundle.getString("mainscreen.tooltip.options"));
        optionsTooltip.setGraphic(new ImageView(infoIMG));

        playTooltip.setText(resourceBundle.getString("mainscreen.tooltip.play"));
        playTooltip.setGraphic(new ImageView(infoIMG));
    }

    private void setTextBoxMax() {
        username.lengthProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() > oldValue.intValue()) {
                if (username.getText().length() > 16) {
                    username.setText(username.getText().substring(0, 16));
                    //Toolkit.getDefaultToolkit().beep();
                }
            }
        });
    }

    @FXML
    private void kt_username(KeyEvent event) {
        if (!event.getCharacter().matches("[A-Za-z0-9\b_]")) {
            //Toolkit.getDefaultToolkit().beep();
            event.consume();
        }
    }

    @Override
    public void onClose() {

    }
}
