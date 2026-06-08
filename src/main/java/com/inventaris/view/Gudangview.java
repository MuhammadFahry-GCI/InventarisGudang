package com.inventaris.view;

import com.inventaris.db.DatabaseConnection;
import com.inventaris.model.Gudang;
import com.inventaris.util.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.sql.*;

public class Gudangview {
    private VBox root;
    private TableView<Gudang> table;
    private TextField searchField;
    private ObservableList<Gudang> data = FXCollections.observableArrayList();

    public Gudangview() {
        buildUI();
        loadData("");
    }

    private void buildUI() {
        root = new VBox(0);
        root.setStyle("-fx-background-color: #F4F6FA;");

        HBox topbar = new HBox(16);
        topbar.getStyleClass().add("topbar");
        topbar.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Data Gudang");
        title.getStyleClass().add("page-title");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        searchField = new TextField();
        searchField.setPromptText("🔍  Cari gudang...");
        searchField.setPrefWidth(220);
        searchField.textProperty().addListener((o, oldV, newV) -> loadData(newV));
        Button addBtn = new Button("+ Tambah Gudang");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> showForm(null));
        topbar.getChildren().addAll(title, sp, searchField, addBtn);

        table = new TableView<>(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("table-view");

        TableColumn<Gudang, Void> colId = new TableColumn<>("#");
        colId.setMaxWidth(50);
        colId.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
            }
        });

        TableColumn<Gudang, String> colNama = new TableColumn<>("Nama Gudang");
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaGudang"));

        TableColumn<Gudang, String> colLokasi = new TableColumn<>("Lokasi");
        colLokasi.setCellValueFactory(new PropertyValueFactory<>("lokasi"));

        TableColumn<Gudang, String> colPJ = new TableColumn<>("Penanggung Jawab");
        colPJ.setCellValueFactory(new PropertyValueFactory<>("penanggungJawab"));
        colPJ.setMaxWidth(160);

        TableColumn<Gudang, Void> colAksi = new TableColumn<>("Aksi");
        colAksi.setMaxWidth(120);
        colAksi.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("✏ Edit");
            private final Button delBtn = new Button("🗑");
            {
                editBtn.getStyleClass().add("btn-secondary");
                delBtn.getStyleClass().add("btn-danger");
                editBtn.setStyle("-fx-padding: 5 10; -fx-font-size: 12px;");
                delBtn.setStyle("-fx-padding: 5 10;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Gudang g = getTableView().getItems().get(getIndex());
                editBtn.setOnAction(e -> showForm(g));
                delBtn.setOnAction(e -> deleteGudang(g));
                HBox box = new HBox(6, editBtn, delBtn);
                if (!Session.isAdmin()) {
                    editBtn.setDisable(true);
                    delBtn.setDisable(true);
                }
                setGraphic(box);
            }
        });

        table.getColumns().addAll(colId, colNama, colLokasi, colPJ, colAksi);

        VBox content = new VBox(16);
        content.setPadding(new Insets(24));
        VBox.setVgrow(table, Priority.ALWAYS);
        content.getChildren().add(table);
        VBox.setVgrow(content, Priority.ALWAYS);
        root.getChildren().addAll(topbar, content);
    }

    private void loadData(String search) {
        data.clear();
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT id_gudang, nama_gudang, lokasi, penanggung_jawab " +
                            "FROM gudang WHERE nama_gudang LIKE ? OR lokasi LIKE ? ORDER BY nama_gudang");
            ps.setString(1, "%" + search + "%");
            ps.setString(2, "%" + search + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                data.add(new Gudang(
                        rs.getInt("id_gudang"),
                        rs.getString("nama_gudang"),
                        rs.getString("lokasi"),
                        rs.getString("penanggung_jawab")));
            }
        } catch (Exception ex) {
            showAlert("Error", ex.getMessage());
        }
    }

    private void showForm(Gudang gudang) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(gudang == null ? "Tambah Gudang" : "Edit Gudang");

        VBox form = new VBox(14);
        form.setPadding(new Insets(20));
        form.setPrefWidth(400);

        TextField tfNama = new TextField(gudang != null ? gudang.getNamaGudang() : "");
        TextField tfLokasi = new TextField(gudang != null && !gudang.getLokasi().equals("-") ? gudang.getLokasi() : "");
        TextField tfPJ = new TextField(
                gudang != null && !gudang.getPenanggungJawab().equals("-") ? gudang.getPenanggungJawab() : "");

        tfNama.setPromptText("Contoh: Gudang Utama");
        tfLokasi.setPromptText("Contoh: Jl. Industri No. 5, Pekanbaru");
        tfPJ.setPromptText("Contoh: Budi Santoso");

        form.getChildren().addAll(
                fieldGroup("Nama Gudang *", tfNama),
                fieldGroup("Lokasi", tfLokasi),
                fieldGroup("Penanggung Jawab", tfPJ));

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        dialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                if (tfNama.getText().trim().isEmpty()) {
                    showAlert("Validasi", "Nama gudang tidak boleh kosong!");
                    return;
                }
                if (isNamaSudahAda(tfNama.getText().trim(), gudang != null ? gudang.getIdGudang() : -1)) {
                    showAlert("Validasi", "Nama gudang \"" + tfNama.getText().trim() + "\" sudah terdaftar!");
                    return;
                }
                if (gudang != null) {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Simpan perubahan data gudang ini?", ButtonType.YES, ButtonType.NO);
                    confirm.setTitle("Konfirmasi Edit");
                    confirm.showAndWait().ifPresent(c -> {
                        if (c == ButtonType.YES)
                            simpanGudang(gudang, tfNama, tfLokasi, tfPJ);
                    });
                } else {
                    simpanGudang(null, tfNama, tfLokasi, tfPJ);
                }
            }
        });
    }

    private void simpanGudang(Gudang gudang, TextField tfNama, TextField tfLokasi, TextField tfPJ) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (gudang == null) {
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO gudang(nama_gudang, lokasi, penanggung_jawab) VALUES(?,?,?)");
                ps.setString(1, tfNama.getText().trim());
                ps.setString(2, tfLokasi.getText().trim().isEmpty() ? null : tfLokasi.getText().trim());
                ps.setString(3, tfPJ.getText().trim().isEmpty() ? null : tfPJ.getText().trim());
                ps.executeUpdate();
                new Alert(Alert.AlertType.INFORMATION, "Gudang berhasil ditambahkan!").show();
            } else {
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE gudang SET nama_gudang=?, lokasi=?, penanggung_jawab=? WHERE id_gudang=?");
                ps.setString(1, tfNama.getText().trim());
                ps.setString(2, tfLokasi.getText().trim().isEmpty() ? null : tfLokasi.getText().trim());
                ps.setString(3, tfPJ.getText().trim().isEmpty() ? null : tfPJ.getText().trim());
                ps.setInt(4, gudang.getIdGudang());
                ps.executeUpdate();
                new Alert(Alert.AlertType.INFORMATION, "Data gudang berhasil diperbarui!").show();
            }
            loadData(searchField.getText());
        } catch (Exception ex) {
            showAlert("Error", ex.getMessage());
        }
    }

    private void deleteGudang(Gudang g) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Hapus gudang \"" + g.getNamaGudang() + "\"?\nPastikan tidak ada stok/transaksi terkait gudang ini.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Konfirmasi Hapus");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    DatabaseConnection.getConnection()
                            .prepareStatement("DELETE FROM gudang WHERE id_gudang=" + g.getIdGudang())
                            .executeUpdate();
                    loadData(searchField.getText());
                    new Alert(Alert.AlertType.INFORMATION, "Gudang berhasil dihapus!").show();
                } catch (Exception ex) {
                    showAlert("Error", "Gagal menghapus. Mungkin masih ada stok/transaksi terkait gudang ini.");
                }
            }
        });
    }

    private boolean isNamaSudahAda(String nama, int excludeId) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                    "SELECT COUNT(*) FROM gudang WHERE LOWER(nama_gudang) = LOWER(?) AND id_gudang != ?");
            ps.setString(1, nama);
            ps.setInt(2, excludeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1) > 0;
        } catch (Exception ignored) {
        }
        return false;
    }

    private VBox fieldGroup(String label, javafx.scene.Node field) {
        VBox group = new VBox(6);
        Label lbl = new Label(label);
        lbl.getStyleClass().add("section-label");
        group.getChildren().addAll(lbl, field);
        return group;
    }

    private void showAlert(String title, String msg) {
        new Alert(Alert.AlertType.ERROR, msg).show();
    }

    public Parent getView() {
        return root;
    }
}