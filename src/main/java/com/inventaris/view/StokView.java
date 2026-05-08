package com.inventaris.view;

import com.inventaris.db.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.*;

public class StokView {
    private VBox root;
    private TableView<ObservableList<String>> table;

    public StokView() {
        buildUI();
        loadData();
    }

    private void buildUI() {
        root = new VBox(0);
        root.setStyle("-fx-background-color: #F4F6FA;");

        HBox topbar = new HBox(16);
        topbar.getStyleClass().add("topbar");
        topbar.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Stok Gudang");
        title.getStyleClass().add("page-title");
        topbar.getChildren().add(title);

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("table-view");

        String[] cols = { "Kode", "Nama Barang", "Kategori", "Satuan", "Gudang Utama", "Gudang Cabang A", "Total",
                "Min", "Status" };
        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                    idx < data.getValue().size() ? data.getValue().get(idx) : ""));

            if (i == 8) { // Status column
                col.setCellFactory(c -> new TableCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setGraphic(null);
                            return;
                        }
                        Label badge = new Label(item);
                        badge.setStyle("Menipis".equals(item)
                                ? "-fx-background-color:#FEF3C7;-fx-text-fill:#92400E;-fx-background-radius:5;-fx-padding:2 8;-fx-font-size:11px;-fx-font-weight:bold;"
                                : "-fx-background-color:#D1FAE5;-fx-text-fill:#065F46;-fx-background-radius:5;-fx-padding:2 8;-fx-font-size:11px;");
                        setGraphic(badge);
                    }
                });
            }
            table.getColumns().add(col);
        }

        VBox content = new VBox(16);
        content.setPadding(new Insets(24));
        VBox.setVgrow(table, Priority.ALWAYS);
        content.getChildren().add(table);
        VBox.setVgrow(content, Priority.ALWAYS);
        root.getChildren().addAll(topbar, content);
    }

    private void loadData() {
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT b.kode_barang, b.nama_barang, k.nama_kategori, b.satuan, b.stok_minimum, " +
                    "COALESCE(SUM(CASE WHEN s.id_gudang=1 THEN s.jumlah_stok END),0) as stok1, " +
                    "COALESCE(SUM(CASE WHEN s.id_gudang=2 THEN s.jumlah_stok END),0) as stok2, " +
                    "COALESCE(SUM(s.jumlah_stok),0) as total " +
                    "FROM barang b JOIN kategori k ON b.id_kategori=k.id_kategori " +
                    "LEFT JOIN stok s ON b.id_barang=s.id_barang GROUP BY b.id_barang";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                int total = rs.getInt("total");
                int min = rs.getInt("stok_minimum");
                ObservableList<String> row = FXCollections.observableArrayList(
                        rs.getString("kode_barang"),
                        rs.getString("nama_barang"),
                        rs.getString("nama_kategori"),
                        rs.getString("satuan"),
                        String.valueOf(rs.getInt("stok1")),
                        String.valueOf(rs.getInt("stok2")),
                        String.valueOf(total),
                        String.valueOf(min),
                        total <= min ? "Menipis" : "Aman");
                data.add(row);
            }
        } catch (Exception ex) {
            System.err.println("Error stok: " + ex.getMessage());
        }
        table.setItems(data);
    }

    public Parent getView() {
        return root;
    }
}
