package com.inventaris.view;

import com.inventaris.db.DatabaseConnection;
import com.inventaris.model.Barang;
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
import java.util.ArrayList;
import java.util.List;

public class BarangView {
    private VBox root;
    private TableView<Barang> table;
    private TextField searchField;
    private ObservableList<Barang> data = FXCollections.observableArrayList();

    public BarangView() {
        buildUI();
        loadData("");
    }

    private void buildUI() {
        root = new VBox(0);
        root.setStyle("-fx-background-color: #F4F6FA;");

        // Topbar
        HBox topbar = new HBox(16);
        topbar.getStyleClass().add("topbar");
        topbar.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Data Barang");
        title.getStyleClass().add("page-title");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        searchField = new TextField();
        searchField.setPromptText("🔍  Cari barang...");
        searchField.setPrefWidth(220);
        searchField.textProperty().addListener((o, oldV, newV) -> loadData(newV));
        Button addBtn = new Button("+ Tambah Barang");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> showForm(null));
        topbar.getChildren().addAll(title, sp, searchField, addBtn);

        // Table
        table = new TableView<>(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("table-view");

        TableColumn<Barang, String> colKode = new TableColumn<>("Kode");
        colKode.setCellValueFactory(new PropertyValueFactory<>("kodeBarang"));
        colKode.setMaxWidth(100);

        TableColumn<Barang, String> colNama = new TableColumn<>("Nama Barang");
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));

        TableColumn<Barang, String> colKat = new TableColumn<>("Kategori");
        colKat.setCellValueFactory(new PropertyValueFactory<>("namaKategori"));
        colKat.setMaxWidth(130);

        TableColumn<Barang, String> colSat = new TableColumn<>("Satuan");
        colSat.setCellValueFactory(new PropertyValueFactory<>("satuan"));
        colSat.setMaxWidth(80);

        TableColumn<Barang, Integer> colMin = new TableColumn<>("Stok Min");
        colMin.setCellValueFactory(new PropertyValueFactory<>("stokMinimum"));
        colMin.setMaxWidth(80);

        TableColumn<Barang, Integer> colStok = new TableColumn<>("Total Stok");
        colStok.setCellValueFactory(new PropertyValueFactory<>("totalStok"));
        colStok.setMaxWidth(90);
        colStok.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                Barang b = getTableView().getItems().get(getIndex());
                setText(String.valueOf(item));
                setStyle(b.isStokMenipis() ? "-fx-text-fill: #EF4444; -fx-font-weight: bold;"
                        : "-fx-text-fill: #10B981; -fx-font-weight: bold;");
            }
        });

        TableColumn<Barang, Void> colStatus = new TableColumn<>("Status");
        colStatus.setMaxWidth(90);
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Barang b = getTableView().getItems().get(getIndex());
                Label badge = new Label(b.isStokMenipis() ? "Menipis" : "Aman");
                badge.setStyle(b.isStokMenipis()
                        ? "-fx-background-color:#FEF3C7;-fx-text-fill:#92400E;-fx-background-radius:5;-fx-padding:2 8;-fx-font-size:11px;"
                        : "-fx-background-color:#D1FAE5;-fx-text-fill:#065F46;-fx-background-radius:5;-fx-padding:2 8;-fx-font-size:11px;");
                setGraphic(badge);
            }
        });

        TableColumn<Barang, Void> colAksi = new TableColumn<>("Aksi");
        colAksi.setMaxWidth(120);
        colAksi.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("✏ Edit");
            private final Button delBtn = new Button("🗑");
            {
                editBtn.getStyleClass().add("btn-secondary");
                delBtn.getStyleClass().add("btn-danger");
                delBtn.setStyle("-fx-padding: 5 10;");
                editBtn.setStyle("-fx-padding: 5 10; -fx-font-size: 12px;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Barang b = getTableView().getItems().get(getIndex());
                editBtn.setOnAction(e -> showForm(b));
                delBtn.setOnAction(e -> deleteBarang(b));
                HBox box = new HBox(6, editBtn, delBtn);
                if (!Session.isAdmin()) {
                    editBtn.setDisable(true);
                    delBtn.setDisable(true);
                }
                setGraphic(box);
            }
        });

        table.getColumns().addAll(colKode, colNama, colKat, colSat, colMin, colStok, colStatus, colAksi);

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
            String sql = "SELECT b.*, k.nama_kategori, COALESCE(SUM(s.jumlah_stok),0) as total_stok " +
                    "FROM barang b JOIN kategori k ON b.id_kategori=k.id_kategori " +
                    "LEFT JOIN stok s ON b.id_barang=s.id_barang " +
                    "WHERE b.nama_barang LIKE ? OR b.kode_barang LIKE ? GROUP BY b.id_barang";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + search + "%");
            ps.setString(2, "%" + search + "%");
            ResultSet rs = ps.executeQuery();

            List<String> habis = new ArrayList<>();
            while (rs.next()) {
                Barang b = new Barang(rs.getInt("id_barang"), rs.getInt("id_kategori"),
                        rs.getString("kode_barang"), rs.getString("nama_barang"),
                        rs.getString("satuan"), rs.getInt("stok_minimum"),
                        rs.getString("nama_kategori"), rs.getInt("total_stok"));
                data.add(b);
                if (rs.getInt("total_stok") == 0) {
                    habis.add("• " + b.getNamaBarang() + " (" + b.getKodeBarang() + ")");
                }
            }

            // Alert WARNING jika ada barang stok habis
            if (!habis.isEmpty()) {
                String pesanHabis = "Barang berikut stoknya telah habis (0):\n" + String.join("\n", habis);
                Alert warning = new Alert(Alert.AlertType.WARNING);
                warning.setTitle("Peringatan Stok Habis");
                warning.setHeaderText("Stok Habis Terdeteksi!");
                warning.setContentText(pesanHabis);
                warning.show();
            }

        } catch (Exception ex) {
            showAlert("Error", ex.getMessage());
        }
    }

    private void showForm(Barang barang) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(barang == null ? "Tambah Barang" : "Edit Barang");

        VBox form = new VBox(14);
        form.setPadding(new Insets(20));
        form.setPrefWidth(380);

        List<String[]> kategoriList = loadKategori();
        ComboBox<String> cbKat = new ComboBox<>();
        cbKat.setPrefWidth(Double.MAX_VALUE);
        for (String[] k : kategoriList)
            cbKat.getItems().add(k[1]);

        TextField tfKode = new TextField(barang != null ? barang.getKodeBarang() : "");
        TextField tfNama = new TextField(barang != null ? barang.getNamaBarang() : "");
        TextField tfSat = new TextField(barang != null ? barang.getSatuan() : "pcs");
        TextField tfMin = new TextField(barang != null ? String.valueOf(barang.getStokMinimum()) : "0");

        if (barang != null) {
            for (String[] k : kategoriList)
                if (k[1].equals(barang.getNamaKategori()))
                    cbKat.setValue(k[1]);
        }

        form.getChildren().addAll(
                fieldGroup("Kategori", cbKat),
                fieldGroup("Kode Barang", tfKode),
                fieldGroup("Nama Barang", tfNama),
                fieldGroup("Satuan", tfSat),
                fieldGroup("Stok Minimum", tfMin));

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        dialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {

                // Validasi field kosong
                if (cbKat.getValue() == null) {
                    showAlert("Validasi", "Kategori harus dipilih!");
                    return;
                }
                if (tfKode.getText().trim().isEmpty()) {
                    showAlert("Validasi", "Kode barang tidak boleh kosong!");
                    return;
                }
                if (tfNama.getText().trim().isEmpty()) {
                    showAlert("Validasi", "Nama barang tidak boleh kosong!");
                    return;
                }
                if (tfSat.getText().trim().isEmpty()) {
                    showAlert("Validasi", "Satuan tidak boleh kosong!");
                    return;
                }

                // Validasi format angka stok minimum
                int stokMin;
                try {
                    stokMin = Integer.parseInt(tfMin.getText().trim());
                    if (stokMin < 0) {
                        showAlert("Validasi", "Stok minimum tidak boleh negatif!");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    showAlert("Validasi", "Stok minimum harus berupa angka!");
                    return;
                }

                // Validasi nama barang tidak boleh duplikat
                if (isNamaBarangSudahAda(tfNama.getText().trim(), barang != null ? barang.getIdBarang() : -1)) {
                    showAlert("Validasi", "Nama barang \"" + tfNama.getText().trim()
                            + "\" sudah ada di tabel barang!\nGunakan nama yang berbeda.");
                    return;
                }

                // Validasi kode barang tidak boleh duplikat
                if (isKodeBarangSudahAda(tfKode.getText().trim(), barang != null ? barang.getIdBarang() : -1)) {
                    showAlert("Validasi", "Kode barang \"" + tfKode.getText().trim()
                            + "\" sudah digunakan!\nGunakan kode yang berbeda.");
                    return;
                }

                try {
                    int idKatTemp = 1;
                    for (String[] k : kategoriList)
                        if (k[1].equals(cbKat.getValue()))
                            idKatTemp = Integer.parseInt(k[0]);
                    final int idKat = idKatTemp;
                    Connection conn = DatabaseConnection.getConnection();
                    if (barang == null) {
                        PreparedStatement ps = conn.prepareStatement(
                                "INSERT INTO barang(id_kategori,kode_barang,nama_barang,satuan,stok_minimum) VALUES(?,?,?,?,?)");
                        ps.setInt(1, idKat);
                        ps.setString(2, tfKode.getText().trim());
                        ps.setString(3, tfNama.getText().trim());
                        ps.setString(4, tfSat.getText().trim());
                        ps.setInt(5, stokMin);
                        ps.executeUpdate();
                        new Alert(Alert.AlertType.INFORMATION, "Barang berhasil ditambahkan!").show();
                    } else {
                        // Konfirmasi sebelum edit
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                                "Simpan perubahan data barang ini?", ButtonType.YES, ButtonType.NO);
                        confirm.setTitle("Konfirmasi Edit");
                        confirm.showAndWait().ifPresent(c -> {
                            if (c == ButtonType.YES) {
                                try {
                                    PreparedStatement ps = conn.prepareStatement(
                                            "UPDATE barang SET id_kategori=?,kode_barang=?,nama_barang=?,satuan=?,stok_minimum=? WHERE id_barang=?");
                                    ps.setInt(1, idKat);
                                    ps.setString(2, tfKode.getText().trim());
                                    ps.setString(3, tfNama.getText().trim());
                                    ps.setString(4, tfSat.getText().trim());
                                    ps.setInt(5, stokMin);
                                    ps.setInt(6, barang.getIdBarang());
                                    ps.executeUpdate();
                                    new Alert(Alert.AlertType.INFORMATION, "Data barang berhasil diperbarui!").show();
                                } catch (Exception ex) {
                                    showAlert("Error", ex.getMessage());
                                }
                            }
                        });
                    }
                    loadData(searchField.getText());
                } catch (Exception ex) {
                    showAlert("Error", ex.getMessage());
                }
            }
        });
    }

    private boolean isNamaBarangSudahAda(String nama, int excludeId) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM barang WHERE LOWER(nama_barang) = LOWER(?) AND id_barang != ?");
            ps.setString(1, nama);
            ps.setInt(2, excludeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1) > 0;
        } catch (Exception ignored) {
        }
        return false;
    }

    private boolean isKodeBarangSudahAda(String kode, int excludeId) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM barang WHERE LOWER(kode_barang) = LOWER(?) AND id_barang != ?");
            ps.setString(1, kode);
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

    private void deleteBarang(Barang b) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Hapus " + b.getNamaBarang() + "?", ButtonType.YES,
                ButtonType.NO);
        confirm.setTitle("Konfirmasi Hapus");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    Connection conn = DatabaseConnection.getConnection();
                    conn.prepareStatement("DELETE FROM barang WHERE id_barang=" + b.getIdBarang()).executeUpdate();
                    loadData(searchField.getText());
                    new Alert(Alert.AlertType.INFORMATION, "Barang berhasil dihapus!").show();
                } catch (Exception ex) {
                    showAlert("Error", ex.getMessage());
                }
            }
        });
    }

    private List<String[]> loadKategori() {
        List<String[]> list = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery("SELECT id_kategori, nama_kategori FROM kategori");
            while (rs.next())
                list.add(new String[] { rs.getString(1), rs.getString(2) });
        } catch (Exception ignored) {
        }
        return list;
    }

    private void showAlert(String title, String msg) {
        new Alert(Alert.AlertType.ERROR, msg).show();
    }

    public Parent getView() {
        return root;
    }
}