package com.inventaris.view;

import com.inventaris.util.Session;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class MainView {
    private final Stage stage;
    private BorderPane root;
    private StackPane contentArea;
    private Button activeBtn;

    public MainView(Stage stage) {
        this.stage = stage;
        buildUI();
    }

    private void buildUI() {
        root = new BorderPane();

        // === SIDEBAR ===
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(220);

        // Header sidebar
        VBox sideHeader = new VBox(4);
        sideHeader.getStyleClass().add("sidebar-header");
        sideHeader.setPadding(new Insets(22, 20, 18, 20));
        HBox brandRow = new HBox(10);
        brandRow.setAlignment(Pos.CENTER_LEFT);
        Rectangle iconBox = new Rectangle(36, 36);
        iconBox.setArcWidth(10);
        iconBox.setArcHeight(10);
        iconBox.setFill(Color.web("#4F46E5"));
        Label iconLabel = new Label("📦");
        StackPane iconStack = new StackPane(iconBox, iconLabel);
        VBox brandText = new VBox(2);
        Label brandName = new Label("Inventaris");
        brandName.getStyleClass().add("sidebar-title");
        Label brandSub = new Label("Gudang — Pekanbaru");
        brandSub.getStyleClass().add("sidebar-sub");
        brandText.getChildren().addAll(brandName, brandSub);
        brandRow.getChildren().addAll(iconStack, brandText);
        sideHeader.getChildren().add(brandRow);

        // User info
        VBox userInfo = new VBox(2);
        userInfo.setPadding(new Insets(12, 20, 12, 20));
        Label userLabel = new Label("👤  " + Session.getUser().getNama());
        userLabel.setStyle("-fx-text-fill: #CBD5E1; -fx-font-size: 13px;");
        Label roleLabel = new Label(Session.getUser().isAdmin() ? "🔑  Administrator" : "🛡  Petugas");
        roleLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px;");
        userInfo.getChildren().addAll(userLabel, roleLabel);

        Separator sideSep = new Separator();
        sideSep.setStyle("-fx-background-color: #334155; -fx-padding: 0;");

        // Nav buttons
        Button btnDashboard = navButton("🏠  Dashboard");
        Button btnBarang = navButton("📦  Data Barang");
        Button btnKategori = navButton("🗂  Kategori");
        Button btnStok = navButton("📊  Stok Gudang");
        Button btnTransaksi = navButton("🔄  Transaksi");
        Button btnSupplier = navButton("🏭  Supplier");
        Button btnGudang = navButton("🏬  Gudang");
        Button btnLaporan = navButton("📄  Laporan");

        VBox navGroup = new VBox(2);
        navGroup.setPadding(new Insets(10, 0, 0, 0));
        navGroup.getChildren().addAll(btnDashboard, btnBarang, btnKategori, btnStok, btnTransaksi, btnSupplier,
                btnGudang);
        if (Session.isAdmin())
            navGroup.getChildren().add(btnLaporan);

        // Logout
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        Button btnLogout = new Button("🚪  Logout");
        btnLogout.setStyle(
                "-fx-background-color: #334155; -fx-text-fill: #CBD5E1; -fx-font-size: 13px; -fx-padding: 10 20; -fx-pref-width: 220; -fx-alignment: CENTER_LEFT; -fx-cursor: hand;");
        btnLogout.setOnAction(e -> doLogout());

        sidebar.getChildren().addAll(sideHeader, userInfo, sideSep, navGroup, spacer, btnLogout);

        // === CONTENT AREA ===
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: #F4F6FA;");

        root.setLeft(sidebar);
        root.setCenter(contentArea);

        // Default page
        setActivePage(new DashboardView().getView());
        activeBtn = btnDashboard;
        btnDashboard.getStyleClass().add("nav-btn-active");

        // Nav actions
        btnDashboard.setOnAction(e -> {
            setActivePage(new DashboardView().getView());
            setActive(btnDashboard);
        });
        btnBarang.setOnAction(e -> {
            setActivePage(new BarangView().getView());
            setActive(btnBarang);
        });
        btnKategori.setOnAction(e -> {
            setActivePage(new KategoriView().getView());
            setActive(btnKategori);
        });
        btnStok.setOnAction(e -> {
            setActivePage(new StokView().getView());
            setActive(btnStok);
        });
        btnTransaksi.setOnAction(e -> {
            setActivePage(new TransaksiView().getView());
            setActive(btnTransaksi);
        });
        btnSupplier.setOnAction(e -> {
            setActivePage(new SupplierView().getView());
            setActive(btnSupplier);
        });
        btnGudang.setOnAction(e -> {
            setActivePage(new Gudangview().getView());
            setActive(btnGudang);
        });
        if (Session.isAdmin())
            btnLaporan.setOnAction(e -> {
                setActivePage(new LaporanView().getView());
                setActive(btnLaporan);
            });
    }

    private Button navButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        return btn;
    }

    private void setActive(Button btn) {
        if (activeBtn != null)
            activeBtn.getStyleClass().remove("nav-btn-active");
        btn.getStyleClass().add("nav-btn-active");
        activeBtn = btn;
    }

    private void setActivePage(Parent view) {
        contentArea.getChildren().setAll(view);
    }

    private void doLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Yakin ingin keluar dari aplikasi?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Konfirmasi Logout");
        confirm.setHeaderText("Logout");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                Session.clear();
                stage.setWidth(420);
                stage.setHeight(520);
                stage.setResizable(false);
                stage.centerOnScreen();
                LoginView loginView = new LoginView(stage);
                stage.getScene().setRoot(loginView.getView());
            }
        });
    }

    public Parent getView() {
        return root;
    }
}