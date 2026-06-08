package com.inventaris.view;

import com.inventaris.db.DatabaseConnection;
import com.inventaris.model.Supplier;
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

public class SupplierView {
    private VBox root;
    private TableView<Supplier> table;
    private TextField searchField;
    private ObservableList<Supplier> data = FXCollections.observableArrayList();

    public SupplierView() {
        buildUI();
        loadData("");
    }

    private void buildUI() {
        root = new VBox(0);
        root.setStyle("-fx-background-color: #F4F6FA;");

        HBox topbar = new HBox(16);
        topbar.getStyleClass().add("topbar");
        topbar.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Data Supplier");
        title.getStyleClass().add("page-title");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        searchField = new TextField();
        searchField.setPromptText("🔍  Cari supplier...");
        searchField.setPrefWidth(220);
        searchField.textProperty().addListener((o, oldV, newV) -> loadData(newV));
        Button addBtn = new Button("+ Tambah Supplier");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> showForm(null));
        topbar.getChildren().addAll(title, sp, searchField, addBtn);

        table = new TableView<>(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("table-view");

        TableColumn<Supplier, Void> colId = new TableColumn<>("#");
        colId.setMaxWidth(50);
        colId.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
            }
        });

        TableColumn<Supplier, String> colNama = new TableColumn<>("Nama Supplier");
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaSupplier"));

        TableColumn<Supplier, String> colKontak = new TableColumn<>("Kontak");
        colKontak.setCellValueFactory(new PropertyValueFactory<>("kontak"));
        colKontak.setMaxWidth(160);

        TableColumn<Supplier, String> colAlamat = new TableColumn<>("Alamat");
        colAlamat.setCellValueFactory(new PropertyValueFactory<>("alamat"));

        TableColumn<Supplier, Void> colAksi = new TableColumn<>("Aksi");
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
                Supplier s = getTableView().getItems().get(getIndex());
                editBtn.setOnAction(e -> showForm(s));
                delBtn.setOnAction(e -> deleteSupplier(s));
                HBox box = new HBox(6, editBtn, delBtn);
                if (!Session.isAdmin()) {
                    editBtn.setDisable(true);
                    delBtn.setDisable(true);
                }
                setGraphic(box);
            }
        });

        table.getColumns().addAll(colId, colNama, colKontak, colAlamat, colAksi);

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
                    "SELECT id_supplier, nama_supplier, kontak, alamat " +
                            "FROM supplier WHERE nama_supplier LIKE ? OR kontak LIKE ? ORDER BY nama_supplier");
            ps.setString(1, "%" + search + "%");
            ps.setString(2, "%" + search + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                data.add(new Supplier(
                        rs.getInt("id_supplier"),
                        rs.getString("nama_supplier"),
                        rs.getString("kontak"),
                        rs.getString("alamat")));
            }
        } catch (Exception ex) {
            showAlert("Error", ex.getMessage());
        }
    }

    private void showForm(Supplier supplier) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(supplier == null ? "Tambah Supplier" : "Edit Supplier");

        VBox form = new VBox(14);
        form.setPadding(new Insets(20));
        form.setPrefWidth(400);

        TextField tfNama = new TextField(supplier != null ? supplier.getNamaSupplier() : "");
        TextField tfKontak = new TextField(
                supplier != null && !supplier.getKontak().equals("-") ? supplier.getKontak() : "");
        TextField tfAlamat = new TextField(
                supplier != null && !supplier.getAlamat().equals("-") ? supplier.getAlamat() : "");

        tfNama.setPromptText("Contoh: PT. Maju Jaya");
        tfKontak.setPromptText("Contoh: 0812-1234-5678");
        tfAlamat.setPromptText("Contoh: Jl. Industri No. 1, Jakarta");

        form.getChildren().addAll(
                fieldGroup("Nama Supplier *", tfNama),
                fieldGroup("Kontak", tfKontak),
                fieldGroup("Alamat", tfAlamat));

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        dialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                if (tfNama.getText().trim().isEmpty()) {
                    showAlert("Validasi", "Nama supplier tidak boleh kosong!");
                    return;
                }
                if (isNamaSudahAda(tfNama.getText().trim(), supplier != null ? supplier.getIdSupplier() : -1)) {
                    showAlert("Validasi", "Nama supplier \"" + tfNama.getText().trim() + "\" sudah terdaftar!");
                    return;
                }
                if (supplier != null) {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Simpan perubahan data supplier ini?", ButtonType.YES, ButtonType.NO);
                    confirm.setTitle("Konfirmasi Edit");
                    confirm.showAndWait().ifPresent(c -> {
                        if (c == ButtonType.YES)
                            simpanSupplier(supplier, tfNama, tfKontak, tfAlamat);
                    });
                } else {
                    simpanSupplier(null, tfNama, tfKontak, tfAlamat);
                }
            }
        });
    }

    private void simpanSupplier(Supplier supplier, TextField tfNama, TextField tfKontak, TextField tfAlamat) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (supplier == null) {
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO supplier(nama_supplier, kontak, alamat) VALUES(?,?,?)");
                ps.setString(1, tfNama.getText().trim());
                ps.setString(2, tfKontak.getText().trim().isEmpty() ? null : tfKontak.getText().trim());
                ps.setString(3, tfAlamat.getText().trim().isEmpty() ? null : tfAlamat.getText().trim());
                ps.executeUpdate();
                new Alert(Alert.AlertType.INFORMATION, "Supplier berhasil ditambahkan!").show();
            } else {
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE supplier SET nama_supplier=?, kontak=?, alamat=? WHERE id_supplier=?");
                ps.setString(1, tfNama.getText().trim());
                ps.setString(2, tfKontak.getText().trim().isEmpty() ? null : tfKontak.getText().trim());
                ps.setString(3, tfAlamat.getText().trim().isEmpty() ? null : tfAlamat.getText().trim());
                ps.setInt(4, supplier.getIdSupplier());
                ps.executeUpdate();
                new Alert(Alert.AlertType.INFORMATION, "Data supplier berhasil diperbarui!").show();
            }
            loadData(searchField.getText());
        } catch (Exception ex) {
            showAlert("Error", ex.getMessage());
        }
    }

    private void deleteSupplier(Supplier s) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Hapus supplier \"" + s.getNamaSupplier() + "\"?\nData transaksi terkait tidak akan terhapus.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Konfirmasi Hapus");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    DatabaseConnection.getConnection()
                            .prepareStatement("DELETE FROM supplier WHERE id_supplier=" + s.getIdSupplier())
                            .executeUpdate();
                    loadData(searchField.getText());
                    new Alert(Alert.AlertType.INFORMATION, "Supplier berhasil dihapus!").show();
                } catch (Exception ex) {
                    showAlert("Error", ex.getMessage());
                }
            }
        });
    }

    private boolean isNamaSudahAda(String nama, int excludeId) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(
                    "SELECT COUNT(*) FROM supplier WHERE LOWER(nama_supplier) = LOWER(?) AND id_supplier != ?");
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