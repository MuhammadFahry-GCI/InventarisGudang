package com.inventaris.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.awt.Desktop;
import java.net.URI;

public class LaporanView {
    private VBox root;

    // Sesuaikan dengan URL server PHP kamu
    private static final String PHP_BASE = "http://localhost/php_laporan/";

    public LaporanView() { buildUI(); }

    private void buildUI() {
        root = new VBox(0);
        root.setStyle("-fx-background-color: #F4F6FA;");

        HBox topbar = new HBox();
        topbar.getStyleClass().add("topbar");
        topbar.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Laporan");
        title.getStyleClass().add("page-title");
        topbar.getChildren().add(title);

        VBox content = new VBox(24);
        content.setPadding(new Insets(28));

        Label subtitle = new Label("Pilih jenis laporan yang ingin dicetak atau diekspor:");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748B;");

        HBox cards = new HBox(16);
        cards.getChildren().addAll(
            reportCard("📊", "Laporan Stok", "Lihat stok semua barang per gudang",
                "#4F46E5", "#EEF2FF", "laporan_stok.php"),
            reportCard("🔄", "Laporan Transaksi", "Riwayat semua transaksi masuk/keluar",
                "#10B981", "#D1FAE5", "laporan_transaksi.php"),
            reportCard("⚠️", "Barang Stok Minim", "Daftar barang yang perlu restock segera",
                "#F59E0B", "#FEF3C7", "laporan_stok_minim.php")
        );

        content.getChildren().addAll(subtitle, cards);
        root.getChildren().addAll(topbar, content);
    }

    private VBox reportCard(String icon, String title, String desc, String color, String bgColor, String phpFile) {
        VBox card = new VBox(14);
        card.getStyleClass().add("stat-card");
        card.setPadding(new Insets(24));
        card.setPrefWidth(220);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #E2E8F0; -fx-border-radius: 12; -fx-cursor: hand;");

        Rectangle iconBg = new Rectangle(50, 50);
        iconBg.setArcWidth(14); iconBg.setArcHeight(14);
        iconBg.setFill(Color.web(bgColor));
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px;");
        StackPane iconStack = new StackPane(iconBg, iconLabel);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        titleLabel.setWrapText(true);

        Label descLabel = new Label(desc);
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");
        descLabel.setWrapText(true);

        Button openBtn = new Button("Buka Laporan →");
        openBtn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        openBtn.setMaxWidth(Double.MAX_VALUE);
        openBtn.setOnAction(e -> openBrowser(PHP_BASE + phpFile));

        card.getChildren().addAll(iconStack, titleLabel, descLabel, openBtn);
        return card;
    }

    private void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                // Fallback untuk Linux
                Runtime.getRuntime().exec(new String[]{"xdg-open", url});
            }
        } catch (Exception ex) {
            System.err.println("Gagal buka browser: " + ex.getMessage());
        }
    }

    public Parent getView() { return root; }
}
