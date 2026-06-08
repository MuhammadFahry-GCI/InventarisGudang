package com.inventaris.view;

import com.inventaris.db.DatabaseConnection;
import com.inventaris.util.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.sql.*;

public class KategoriView {

    // ─────────────────────────────────────────────
    // Inner model (tidak perlu file terpisah)
    // ─────────────────────────────────────────────
    public static class Kategori {
        private final SimpleIntegerProperty idKategori;
        private final SimpleStringProperty namaKategori;
        private final SimpleStringProperty deskripsi;
        private final SimpleIntegerProperty jumlahBarang;

        public Kategori(int idKategori, String namaKategori, String deskripsi, int jumlahBarang) {
            this.idKategori = new SimpleIntegerProperty(idKategori);
            this.namaKategori = new SimpleStringProperty(namaKategori);
            this.deskripsi = new SimpleStringProperty(deskripsi != null ? deskripsi : "");
            this.jumlahBarang = new SimpleIntegerProperty(jumlahBarang);
        }

        public int getIdKategori() {
            return idKategori.get();
        }

        public String getNamaKategori() {
            return namaKategori.get();
        }

        public String getDeskripsi() {
            return deskripsi.get();
        }

        public int getJumlahBarang() {
            return jumlahBarang.get();
        }

        public SimpleIntegerProperty idKategoriProperty() {
            return idKategori;
        }

        public SimpleStringProperty namaKategoriProperty() {
            return namaKategori;
        }

        public SimpleStringProperty deskripsiProperty() {
            return deskripsi;
        }

        public SimpleIntegerProperty jumlahBarangProperty() {
            return jumlahBarang;
        }
    }

    // ─────────────────────────────────────────────
    // Field utama
    // ─────────────────────────────────────────────
    private VBox root;
    private TableView<Kategori> table;
    private TextField searchField;
    private ObservableList<Kategori> data = FXCollections.observableArrayList();

    public KategoriView() {
        buildUI();
        loadData("");
    }

    // ─────────────────────────────────────────────
    // Build UI
    // ─────────────────────────────────────────────
    private void buildUI() {
        root = new VBox(0);
        root.setStyle("-fx-background-color: #F4F6FA;");

        /* ── Topbar ── */
        HBox topbar = new HBox(16);
        topbar.getStyleClass().add("topbar");
        topbar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Manajemen Kategori");
        title.getStyleClass().add("page-title");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        searchField = new TextField();
        searchField.setPromptText("🔍  Cari kategori...");
        searchField.setPrefWidth(220);
        searchField.textProperty().addListener((o, oldV, newV) -> loadData(newV));

        Button addBtn = new Button("+ Tambah Kategori");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> showForm(null));
        if (!Session.isAdmin())
            addBtn.setDisable(true);

        topbar.getChildren().addAll(title, sp, searchField, addBtn);

        /* ── Tabel ── */
        table = new TableView<>(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("table-view");
        table.setPlaceholder(new Label("Belum ada data kategori"));

        TableColumn<Kategori, Void> colId = new TableColumn<>("NO");
        colId.setMaxWidth(55);
        colId.setStyle("-fx-alignment: CENTER;");
        colId.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    return;
                }
                setText(String.valueOf(getIndex() + 1));
                setStyle("-fx-alignment: CENTER;");
            }
        });

        TableColumn<Kategori, String> colNama = new TableColumn<>("Nama Kategori");
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaKategori"));

        TableColumn<Kategori, String> colDesk = new TableColumn<>("Deskripsi");
        colDesk.setCellValueFactory(new PropertyValueFactory<>("deskripsi"));

        TableColumn<Kategori, Integer> colJml = new TableColumn<>("Jumlah Barang");
        colJml.setCellValueFactory(new PropertyValueFactory<>("jumlahBarang"));
        colJml.setMaxWidth(120);
        colJml.setStyle("-fx-alignment: CENTER;");
        colJml.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(String.valueOf(item));
                setStyle(item == 0
                        ? "-fx-text-fill: #9CA3AF; -fx-alignment: CENTER;"
                        : "-fx-text-fill: #2563EB; -fx-font-weight: bold; -fx-alignment: CENTER;");
            }
        });

        TableColumn<Kategori, Void> colAksi = new TableColumn<>("Aksi");
        colAksi.setMaxWidth(140);
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
                Kategori k = getTableView().getItems().get(getIndex());
                editBtn.setOnAction(e -> showForm(k));
                delBtn.setOnAction(e -> deleteKategori(k));
                HBox box = new HBox(6, editBtn, delBtn);
                if (!Session.isAdmin()) {
                    editBtn.setDisable(true);
                    delBtn.setDisable(true);
                }
                setGraphic(box);
            }
        });

        table.getColumns().addAll(colId, colNama, colDesk, colJml, colAksi);

        /* ── Layout ── */
        VBox content = new VBox(16);
        content.setPadding(new Insets(24));
        VBox.setVgrow(table, Priority.ALWAYS);
        content.getChildren().add(table);
        VBox.setVgrow(content, Priority.ALWAYS);

        root.getChildren().addAll(topbar, content);
    }

    // ─────────────────────────────────────────────
    // Load data dari DB
    // ─────────────────────────────────────────────
    private void loadData(String search) {
        data.clear();
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = """
                    SELECT k.id_kategori, k.nama_kategori, k.deskripsi,
                           COUNT(b.id_barang) AS jumlah_barang
                    FROM kategori k
                    LEFT JOIN barang b ON k.id_kategori = b.id_kategori
                    WHERE k.nama_kategori LIKE ?
                    GROUP BY k.id_kategori
                    ORDER BY k.nama_kategori
                    """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + search + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                data.add(new Kategori(
                        rs.getInt("id_kategori"),
                        rs.getString("nama_kategori"),
                        rs.getString("deskripsi"),
                        rs.getInt("jumlah_barang")));
            }
        } catch (Exception ex) {
            showAlert("Error memuat data", ex.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // Form tambah / edit
    // ─────────────────────────────────────────────
    private void showForm(Kategori kategori) {
        boolean isEdit = (kategori != null);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Kategori" : "Tambah Kategori");
        dialog.setHeaderText(isEdit ? "Ubah data kategori" : "Isi data kategori baru");

        VBox form = new VBox(14);
        form.setPadding(new Insets(20));
        form.setPrefWidth(380);

        TextField tfNama = new TextField(isEdit ? kategori.getNamaKategori() : "");
        tfNama.setPromptText("Contoh: Elektronik, Alat Tulis...");

        TextArea taDeskripsi = new TextArea(isEdit ? kategori.getDeskripsi() : "");
        taDeskripsi.setPromptText("Deskripsi singkat kategori (opsional)");
        taDeskripsi.setPrefRowCount(3);
        taDeskripsi.setWrapText(true);

        form.getChildren().addAll(
                fieldGroup("Nama Kategori *", tfNama),
                fieldGroup("Deskripsi", taDeskripsi));

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets()
                .add(getClass().getResource("/styles.css").toExternalForm());

        // Fokus ke field nama saat dialog buka
        dialog.setOnShown(e -> tfNama.requestFocus());

        dialog.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK)
                return;

            String nama = tfNama.getText().trim();
            String desk = taDeskripsi.getText().trim();

            /* Validasi */
            if (nama.isEmpty()) {
                showAlert("Validasi", "Nama kategori tidak boleh kosong!");
                return;
            }
            if (nama.length() > 100) {
                showAlert("Validasi", "Nama kategori maksimal 100 karakter!");
                return;
            }
            if (isNamaKategoriSudahAda(nama, isEdit ? kategori.getIdKategori() : -1)) {
                showAlert("Validasi", "Nama kategori \"" + nama + "\" sudah ada!\nGunakan nama yang berbeda.");
                return;
            }

            try {
                Connection conn = DatabaseConnection.getConnection();
                if (!isEdit) {
                    /* INSERT */
                    PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO kategori (nama_kategori, deskripsi) VALUES (?, ?)");
                    ps.setString(1, nama);
                    ps.setString(2, desk.isEmpty() ? null : desk);
                    ps.executeUpdate();
                    new Alert(Alert.AlertType.INFORMATION, "Kategori \"" + nama + "\" berhasil ditambahkan!").show();
                } else {
                    /* UPDATE — konfirmasi dulu */
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Simpan perubahan kategori ini?", ButtonType.YES, ButtonType.NO);
                    confirm.setTitle("Konfirmasi Edit");
                    confirm.showAndWait().ifPresent(c -> {
                        if (c != ButtonType.YES)
                            return;
                        try {
                            PreparedStatement ps = conn.prepareStatement(
                                    "UPDATE kategori SET nama_kategori=?, deskripsi=? WHERE id_kategori=?");
                            ps.setString(1, nama);
                            ps.setString(2, desk.isEmpty() ? null : desk);
                            ps.setInt(3, kategori.getIdKategori());
                            ps.executeUpdate();
                            new Alert(Alert.AlertType.INFORMATION, "Kategori berhasil diperbarui!").show();
                        } catch (Exception ex) {
                            showAlert("Error", ex.getMessage());
                        }
                    });
                }
                loadData(searchField.getText());
            } catch (Exception ex) {
                showAlert("Error", ex.getMessage());
            }
        });
    }

    // ─────────────────────────────────────────────
    // Hapus kategori
    // ─────────────────────────────────────────────
    private void deleteKategori(Kategori k) {
        // Cegah hapus jika masih ada barang
        if (k.getJumlahBarang() > 0) {
            showAlert("Tidak Bisa Dihapus",
                    "Kategori \"" + k.getNamaKategori() + "\" masih memiliki "
                            + k.getJumlahBarang() + " barang.\n"
                            + "Pindahkan atau hapus barang tersebut terlebih dahulu.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Hapus kategori \"" + k.getNamaKategori() + "\"?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Konfirmasi Hapus");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.YES)
                return;
            try {
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM kategori WHERE id_kategori = ?");
                ps.setInt(1, k.getIdKategori());
                ps.executeUpdate();
                loadData(searchField.getText());
                new Alert(Alert.AlertType.INFORMATION, "Kategori berhasil dihapus!").show();
            } catch (Exception ex) {
                showAlert("Error", ex.getMessage());
            }
        });
    }

    // ─────────────────────────────────────────────
    // Helper: cek duplikat nama
    // ─────────────────────────────────────────────
    private boolean isNamaKategoriSudahAda(String nama, int excludeId) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM kategori WHERE LOWER(nama_kategori) = LOWER(?) AND id_kategori != ?");
            ps.setString(1, nama);
            ps.setInt(2, excludeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1) > 0;
        } catch (Exception ignored) {
        }
        return false;
    }

    // ─────────────────────────────────────────────
    // Helper: group field + label
    // ─────────────────────────────────────────────
    private VBox fieldGroup(String labelText, javafx.scene.Node field) {
        VBox group = new VBox(6);
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("section-label");
        group.getChildren().addAll(lbl, field);
        return group;
    }

    // ─────────────────────────────────────────────
    // Helper: alert error
    // ─────────────────────────────────────────────
    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }

    public Parent getView() {
        return root;
    }
}