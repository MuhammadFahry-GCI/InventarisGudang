package com.inventaris.view;

import com.inventaris.db.DatabaseConnection;
import com.inventaris.util.Session;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DashboardView {
    private VBox root;

    public DashboardView() { buildUI(); }

    private void buildUI() {
        root = new VBox(0);
        root.setStyle("-fx-background-color: #F4F6FA;");

        // Topbar
        HBox topbar = new HBox();
        topbar.getStyleClass().add("topbar");
        topbar.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Dashboard");
        title.getStyleClass().add("page-title");
        Label sub = new Label("   ·  Selamat datang, " + Session.getUser().getNama());
        sub.setStyle("-fx-text-fill: #64748B; -fx-font-size: 14px;");
        topbar.getChildren().addAll(title, sub);

        VBox content = new VBox(24);
        content.setPadding(new Insets(28));
        content.setStyle("-fx-background-color: #F4F6FA;");

        // Stat cards row
        HBox statsRow = new HBox(16);
        statsRow.setFillHeight(true);

        int[] stats = fetchStats();
        statsRow.getChildren().addAll(
            statCard("📦", "Total Barang",    String.valueOf(stats[0]), "#4F46E5", "#EEF2FF"),
            statCard("🏭", "Gudang Aktif",    String.valueOf(stats[1]), "#10B981", "#D1FAE5"),
            statCard("⚠️", "Stok Menipis",   String.valueOf(stats[2]), "#F59E0B", "#FEF3C7"),
            statCard("🔄", "Total Transaksi", String.valueOf(stats[3]), "#3B82F6", "#DBEAFE")
        );

        for (var node : statsRow.getChildren()) {
            HBox.setHgrow((Region) node, Priority.ALWAYS);
        }

        // Recent transactions section
        Label sectionTitle = new Label("Transaksi Terbaru");
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

        VBox txList = buildRecentTx();

        content.getChildren().addAll(statsRow, sectionTitle, txList);
        VBox.setVgrow(content, Priority.ALWAYS);
        root.getChildren().addAll(topbar, content);
    }

    private VBox statCard(String icon, String label, String value, String accentColor, String bgColor) {
        VBox card = new VBox(8);
        card.getStyleClass().add("stat-card");
        card.setPadding(new Insets(20));

        HBox iconRow = new HBox(10);
        iconRow.setAlignment(Pos.CENTER_LEFT);
        Rectangle iconBg = new Rectangle(38, 38);
        iconBg.setArcWidth(10); iconBg.setArcHeight(10);
        iconBg.setFill(Color.web(bgColor));
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 18px;");
        StackPane iconStack = new StackPane(iconBg, iconLabel);

        Label numLabel = new Label(value);
        numLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: " + accentColor + ";");
        Label descLabel = new Label(label);
        descLabel.getStyleClass().add("stat-label");

        card.getChildren().addAll(iconRow, numLabel, descLabel);
        iconRow.getChildren().add(iconStack);
        return card;
    }

    private VBox buildRecentTx() {
        VBox box = new VBox(0);
        box.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #E2E8F0; -fx-border-radius: 10;");

        // Header
        HBox header = new HBox();
        header.setPadding(new Insets(14, 20, 14, 20));
        header.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 10 10 0 0; -fx-border-color: transparent transparent #E2E8F0 transparent;");
        String[] cols = {"#", "Barang", "Gudang", "Jenis", "Jumlah", "Tanggal"};
        double[] widths = {40, 200, 150, 80, 70, 100};
        for (int i = 0; i < cols.length; i++) {
            Label lbl = new Label(cols[i]);
            lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #374151;");
            lbl.setPrefWidth(widths[i]);
            header.getChildren().add(lbl);
        }
        box.getChildren().add(header);

        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(
                "SELECT t.id_transaksi, b.nama_barang, g.nama_gudang, t.jenis_transaksi, t.jumlah, t.tanggal " +
                "FROM transaksi t " +
                "JOIN barang b ON t.id_barang = b.id_barang " +
                "JOIN gudang g ON t.id_gudang = g.id_gudang " +
                "ORDER BY t.id_transaksi DESC LIMIT 8"
            );
            boolean odd = false;
            while (rs.next()) {
                odd = !odd;
                HBox row = new HBox();
                row.setPadding(new Insets(12, 20, 12, 20));
                row.setStyle("-fx-background-color: " + (odd ? "#FAFAFA" : "white") + ";");

                String jenis = rs.getString("jenis_transaksi");
                String badgeStyle = switch (jenis) {
                    case "masuk"  -> "-fx-background-color:#D1FAE5;-fx-text-fill:#065F46;-fx-background-radius:20;-fx-padding:2 10;-fx-font-size:11px;-fx-font-weight:bold;";
                    case "keluar" -> "-fx-background-color:#FEE2E2;-fx-text-fill:#991B1B;-fx-background-radius:20;-fx-padding:2 10;-fx-font-size:11px;-fx-font-weight:bold;";
                    default       -> "-fx-background-color:#FEF3C7;-fx-text-fill:#92400E;-fx-background-radius:20;-fx-padding:2 10;-fx-font-size:11px;-fx-font-weight:bold;";
                };

                String[] vals = {
                    String.valueOf(rs.getInt("id_transaksi")),
                    rs.getString("nama_barang"),
                    rs.getString("nama_gudang"),
                    jenis,
                    String.valueOf(rs.getInt("jumlah")),
                    rs.getDate("tanggal").toString()
                };

                for (int i = 0; i < vals.length; i++) {
                    Label cell;
                    if (i == 3) {
                        cell = new Label(vals[i]);
                        cell.setStyle(badgeStyle);
                        HBox wrap = new HBox(cell);
                        wrap.setPrefWidth(widths[i]);
                        row.getChildren().add(wrap);
                    } else {
                        cell = new Label(vals[i]);
                        cell.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");
                        cell.setPrefWidth(widths[i]);
                        row.getChildren().add(cell);
                    }
                }
                box.getChildren().add(row);
            }
        } catch (Exception ex) {
            Label err = new Label("Gagal memuat data: " + ex.getMessage());
            err.setStyle("-fx-text-fill: #EF4444; -fx-padding: 16;");
            box.getChildren().add(err);
        }
        return box;
    }

    private int[] fetchStats() {
        int[] result = {0, 0, 0, 0};
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs;
            rs = st.executeQuery("SELECT COUNT(*) FROM barang"); if (rs.next()) result[0] = rs.getInt(1);
            rs = st.executeQuery("SELECT COUNT(*) FROM gudang"); if (rs.next()) result[1] = rs.getInt(1);
            rs = st.executeQuery("SELECT COUNT(DISTINCT b.id_barang) FROM barang b JOIN stok s ON b.id_barang=s.id_barang WHERE s.jumlah_stok<=b.stok_minimum");
            if (rs.next()) result[2] = rs.getInt(1);
            rs = st.executeQuery("SELECT COUNT(*) FROM transaksi"); if (rs.next()) result[3] = rs.getInt(1);
        } catch (Exception ignored) {}
        return result;
    }

    public Parent getView() { return root; }
}
