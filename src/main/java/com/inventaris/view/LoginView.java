package com.inventaris.view;

import com.inventaris.db.DatabaseConnection;
import com.inventaris.model.User;
import com.inventaris.util.Session;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginView {
    private final Stage stage;
    private VBox root;

    public LoginView(Stage stage) {
        this.stage = stage;
        buildUI();
    }

    private void buildUI() {
        root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #EEF2FF, #E0E7FF);");

        VBox card = new VBox(18);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("card");
        card.setPrefWidth(360);
        card.setMaxWidth(360);

        // Logo / Icon area
        HBox logoBox = new HBox(12);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        Rectangle logoRect = new Rectangle(44, 44);
        logoRect.setArcWidth(12);
        logoRect.setArcHeight(12);
        logoRect.setFill(Color.web("#4F46E5"));
        Label logoIcon = new Label("📦");
        logoIcon.setFont(Font.font(22));
        StackPane logoStack = new StackPane(logoRect, logoIcon);
        VBox logoText = new VBox(2);
        Label appName = new Label("Inventaris Gudang");
        appName.getStyleClass().add("title-label");
        appName.setStyle("-fx-font-size: 18px;");
        Label appSub = new Label("Sistem Manajemen Gudang");
        appSub.getStyleClass().add("subtitle-label");
        logoText.getChildren().addAll(appName, appSub);
        logoBox.getChildren().addAll(logoStack, logoText);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #E2E8F0;");

        Label loginTitle = new Label("Masuk ke Akun");
        loginTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        Label loginSub = new Label("Masukkan username dan password Anda");
        loginSub.getStyleClass().add("subtitle-label");

        Label usernameLabel = new Label("Username");
        usernameLabel.getStyleClass().add("section-label");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Masukkan username...");
        usernameField.setPrefHeight(42);

        Label passwordLabel = new Label("Password");
        passwordLabel.getStyleClass().add("section-label");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Masukkan password...");
        passwordField.setPrefHeight(42);

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setVisible(false);

        Button loginBtn = new Button("Masuk");
        loginBtn.getStyleClass().add("btn-primary");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setPrefHeight(44);

        Label footer = new Label("© 2026 Sistem Inventaris Gudang — Pekanbaru");
        footer.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");
        footer.setAlignment(Pos.CENTER);

        loginBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            if (username.isEmpty() || password.isEmpty()) {
                showError(errorLabel, "Username dan password tidak boleh kosong!");
                return;
            }
            doLogin(username, password, errorLabel);
        });

        passwordField.setOnAction(e -> loginBtn.fire());

        card.getChildren().addAll(
                logoBox, sep, loginTitle, loginSub,
                usernameLabel, usernameField,
                passwordLabel, passwordField,
                errorLabel, loginBtn, footer);
        VBox.setMargin(footer, new Insets(4, 0, 0, 0));

        root.getChildren().add(card);
    }

    private void doLogin(String username, String password, Label errorLabel) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT id_user, nama, username, role FROM users WHERE username = ? AND password = ?");
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User user = new User(rs.getInt("id_user"), rs.getString("nama"),
                        rs.getString("username"), rs.getString("role"));
                Session.setUser(user);
                openMainView();
            } else {
                showError(errorLabel, "Username atau password salah!");
            }
        } catch (Exception ex) {
            showError(errorLabel, "Koneksi database gagal: " + ex.getMessage());
        }
    }

    private void showError(Label label, String msg) {
        label.setText(msg);
        label.setVisible(true);
    }

    private void openMainView() {
        MainView mainView = new MainView(stage);
        stage.getScene().setRoot(mainView.getView());
        stage.setWidth(1100);
        stage.setHeight(700);
        stage.setResizable(true);
        stage.centerOnScreen();
    }

    public Parent getView() {
        return root;
    }
}
