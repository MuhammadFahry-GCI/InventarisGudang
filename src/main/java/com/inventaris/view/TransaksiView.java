package com.inventaris.view;

import com.inventaris.db.DatabaseConnection;
import com.inventaris.model.Transaksi;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransaksiView {
    private VBox root;
    private TableView<Transaksi> table;
    private ObservableList<Transaksi> data = FXCollections.observableArrayList();

    public TransaksiView() { buildUI(); loadData(); }

    private void buildUI() {
        root = new VBox(0);
        root.setStyle("-fx-background-color: #F4F6FA;");

        HBox topbar = new HBox(16);
        topbar.getStyleClass().add("topbar");
        topbar.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Transaksi Barang");
        title.getStyleClass().add("page-title");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button addBtn = new Button("+ Tambah Transaksi");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> showForm());
        topbar.getChildren().addAll(title, sp, addBtn);

        table = new TableView<>(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("table-view");

        TableColumn<Transaksi, Integer> colId = new TableColumn<>("#");
        colId.setCellValueFactory(new PropertyValueFactory<>("idTransaksi"));
        colId.setMaxWidth(50);

        TableColumn<Transaksi, String> colBarang = new TableColumn<>("Barang");
        colBarang.setCellValueFactory(new PropertyValueFactory<>("namaBarang"));

        TableColumn<Transaksi, String> colGudang = new TableColumn<>("Gudang");
        colGudang.setCellValueFactory(new PropertyValueFactory<>("namaGudang"));
        colGudang.setMaxWidth(140);

        TableColumn<Transaksi, String> colJenis = new TableColumn<>("Jenis");
        colJenis.setMaxWidth(90);
        colJenis.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Transaksi t = getTableView().getItems().get(getIndex());
                String jenis = t.getJenisTransaksi();
                Label badge = new Label(jenis);
                badge.setStyle(switch (jenis) {
                    case "masuk"  -> "-fx-background-color:#D1FAE5;-fx-text-fill:#065F46;-fx-background-radius:20;-fx-padding:3 10;-fx-font-size:11px;-fx-font-weight:bold;";
                    case "keluar" -> "-fx-background-color:#FEE2E2;-fx-text-fill:#991B1B;-fx-background-radius:20;-fx-padding:3 10;-fx-font-size:11px;-fx-font-weight:bold;";
                    default       -> "-fx-background-color:#FEF3C7;-fx-text-fill:#92400E;-fx-background-radius:20;-fx-padding:3 10;-fx-font-size:11px;-fx-font-weight:bold;";
                });
                setGraphic(badge);
            }
        });

        TableColumn<Transaksi, Integer> colJml = new TableColumn<>("Jumlah");
        colJml.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        colJml.setMaxWidth(80);

        TableColumn<Transaksi, LocalDate> colTgl = new TableColumn<>("Tanggal");
        colTgl.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        colTgl.setMaxWidth(110);

        TableColumn<Transaksi, String> colSupplier = new TableColumn<>("Supplier");
        colSupplier.setCellValueFactory(new PropertyValueFactory<>("namaSupplier"));
        colSupplier.setMaxWidth(130);

        TableColumn<Transaksi, String> colUser = new TableColumn<>("Petugas");
        colUser.setCellValueFactory(new PropertyValueFactory<>("namaUser"));
        colUser.setMaxWidth(120);

        TableColumn<Transaksi, String> colKet = new TableColumn<>("Keterangan");
        colKet.setCellValueFactory(new PropertyValueFactory<>("keterangan"));

        table.getColumns().addAll(colId, colBarang, colGudang, colJenis, colJml, colTgl, colSupplier, colUser, colKet);

        VBox content = new VBox(16);
        content.setPadding(new Insets(24));
        VBox.setVgrow(table, Priority.ALWAYS);
        content.getChildren().add(table);
        VBox.setVgrow(content, Priority.ALWAYS);
        root.getChildren().addAll(topbar, content);
    }

    private void loadData() {
        data.clear();
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT t.id_transaksi, b.nama_barang, b.kode_barang, g.nama_gudang, " +
                "COALESCE(s.nama_supplier,'-') as nama_supplier, u.nama as nama_user, " +
                "t.jenis_transaksi, t.jumlah, t.tanggal, t.keterangan " +
                "FROM transaksi t JOIN barang b ON t.id_barang=b.id_barang " +
                "JOIN gudang g ON t.id_gudang=g.id_gudang " +
                "LEFT JOIN supplier s ON t.id_supplier=s.id_supplier " +
                "JOIN users u ON t.id_user=u.id_user ORDER BY t.id_transaksi DESC";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                data.add(new Transaksi(rs.getInt("id_transaksi"), rs.getString("nama_barang"),
                    rs.getString("kode_barang"), rs.getString("nama_gudang"),
                    rs.getString("nama_supplier"), rs.getString("nama_user"),
                    rs.getString("jenis_transaksi"), rs.getInt("jumlah"),
                    rs.getDate("tanggal").toLocalDate(), rs.getString("keterangan")));
            }
        } catch (Exception ex) { System.err.println(ex.getMessage()); }
    }

    private void showForm() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Tambah Transaksi");
        VBox form = new VBox(14);
        form.setPadding(new Insets(20));
        form.setPrefWidth(400);

        List<String[]> barangList   = loadList("SELECT id_barang, nama_barang FROM barang");
        List<String[]> gudangList   = loadList("SELECT id_gudang, nama_gudang FROM gudang");
        List<String[]> supplierList = loadList("SELECT id_supplier, nama_supplier FROM supplier");

        ComboBox<String> cbBarang   = buildCombo(barangList);
        ComboBox<String> cbGudang   = buildCombo(gudangList);
        ComboBox<String> cbJenis    = new ComboBox<>(FXCollections.observableArrayList("masuk", "keluar", "retur"));
        cbJenis.setPrefWidth(Double.MAX_VALUE);
        ComboBox<String> cbSupplier = buildCombo(supplierList);

        TextField tfJumlah   = new TextField("1");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setPrefWidth(Double.MAX_VALUE);
        TextField tfKet = new TextField();

        form.getChildren().addAll(
            fg("Barang", cbBarang), fg("Gudang", cbGudang),
            fg("Jenis Transaksi", cbJenis), fg("Jumlah", tfJumlah),
            fg("Supplier (opsional)", cbSupplier), fg("Tanggal", datePicker),
            fg("Keterangan", tfKet)
        );

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        dialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    Connection conn = DatabaseConnection.getConnection();
                    int idBarang   = Integer.parseInt(barangList.get(cbBarang.getSelectionModel().getSelectedIndex())[0]);
                    int idGudang   = Integer.parseInt(gudangList.get(cbGudang.getSelectionModel().getSelectedIndex())[0]);
                    int jumlah     = Integer.parseInt(tfJumlah.getText());
                    String jenis   = cbJenis.getValue();
                    Integer idSup  = cbSupplier.getValue() != null && cbSupplier.getSelectionModel().getSelectedIndex() >= 0
                        ? Integer.parseInt(supplierList.get(cbSupplier.getSelectionModel().getSelectedIndex())[0]) : null;

                    PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO transaksi(id_barang,id_gudang,id_supplier,id_user,jenis_transaksi,jumlah,tanggal,keterangan) VALUES(?,?,?,?,?,?,?,?)");
                    ps.setInt(1, idBarang); ps.setInt(2, idGudang);
                    if (idSup != null) ps.setInt(3, idSup); else ps.setNull(3, Types.INTEGER);
                    ps.setInt(4, Session.getUser().getIdUser());
                    ps.setString(5, jenis); ps.setInt(6, jumlah);
                    ps.setDate(7, Date.valueOf(datePicker.getValue()));
                    ps.setString(8, tfKet.getText());
                    ps.executeUpdate();

                    // Update stok
                    int delta = "masuk".equals(jenis) ? jumlah : -jumlah;
                    PreparedStatement psStok = conn.prepareStatement(
                        "INSERT INTO stok(id_barang,id_gudang,jumlah_stok) VALUES(?,?,?) " +
                        "ON DUPLICATE KEY UPDATE jumlah_stok=jumlah_stok+" + delta);
                    psStok.setInt(1, idBarang); psStok.setInt(2, idGudang);
                    psStok.setInt(3, Math.max(delta, 0));
                    psStok.executeUpdate();

                    loadData();
                } catch (Exception ex) { new Alert(Alert.AlertType.ERROR, ex.getMessage()).show(); }
            }
        });
    }

    private VBox fg(String label, javafx.scene.Node field) {
        VBox g = new VBox(6);
        Label l = new Label(label); l.getStyleClass().add("section-label");
        g.getChildren().addAll(l, field);
        return g;
    }

    private ComboBox<String> buildCombo(List<String[]> list) {
        ComboBox<String> cb = new ComboBox<>();
        for (String[] item : list) cb.getItems().add(item[1]);
        cb.setPrefWidth(Double.MAX_VALUE);
        return cb;
    }

    private List<String[]> loadList(String sql) {
        List<String[]> list = new ArrayList<>();
        try {
            ResultSet rs = DatabaseConnection.getConnection().createStatement().executeQuery(sql);
            while (rs.next()) list.add(new String[]{rs.getString(1), rs.getString(2)});
        } catch (Exception ignored) {}
        return list;
    }

    public Parent getView() { return root; }
}
