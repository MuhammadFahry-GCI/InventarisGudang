<?php
require_once 'config.php';
$pdo = getDB();

$tgl_dari  = $_GET['dari']  ?? date('Y-01-01');
$tgl_sampai = $_GET['sampai'] ?? date('Y-m-d');
$filter_jenis = $_GET['jenis'] ?? '';

$params = [$tgl_dari, $tgl_sampai];
$whereJenis = '';
if ($filter_jenis !== '') {
    $whereJenis = " AND t.jenis_transaksi = ?";
    $params[] = $filter_jenis;
}

$sql = "SELECT t.id_transaksi, b.kode_barang, b.nama_barang, g.nama_gudang,
        COALESCE(s.nama_supplier, '-') AS nama_supplier,
        u.nama AS nama_user, t.jenis_transaksi, t.jumlah, t.tanggal, t.keterangan
        FROM transaksi t
        JOIN barang b ON t.id_barang = b.id_barang
        JOIN gudang g ON t.id_gudang = g.id_gudang
        LEFT JOIN supplier s ON t.id_supplier = s.id_supplier
        JOIN users u ON t.id_user = u.id_user
        WHERE t.tanggal BETWEEN ? AND ? $whereJenis
        ORDER BY t.tanggal DESC, t.id_transaksi DESC";

$rows = $pdo->prepare($sql);
$rows->execute($params);
$items = $rows->fetchAll();

// Ringkasan
$total_masuk  = 0; $jml_masuk  = 0;
$total_keluar = 0; $jml_keluar = 0;
$total_retur  = 0; $jml_retur  = 0;
foreach ($items as $r) {
    if ($r['jenis_transaksi'] === 'masuk')  { $total_masuk++;  $jml_masuk  += $r['jumlah']; }
    if ($r['jenis_transaksi'] === 'keluar') { $total_keluar++; $jml_keluar += $r['jumlah']; }
    if ($r['jenis_transaksi'] === 'retur')  { $total_retur++;  $jml_retur  += $r['jumlah']; }
}
$tanggal = date('d F Y H:i');
?>
<!DOCTYPE html>
<html lang="id">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Laporan Transaksi — Inventaris Gudang</title>
<style>
  @import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');
  * { margin:0; padding:0; box-sizing:border-box; }
  body { font-family:'Inter',Arial,sans-serif; background:#F4F6FA; color:#1E293B; }
  .page { max-width:1150px; margin:0 auto; padding:32px 24px; }

  .report-header {
    background: linear-gradient(135deg, #059669 0%, #0D9488 100%);
    border-radius:16px; padding:32px 36px; color:white; margin-bottom:28px;
    display:flex; justify-content:space-between; align-items:center;
  }
  .report-header h1 { font-size:26px; font-weight:700; }
  .report-header p  { font-size:13px; opacity:.85; margin-top:4px; }
  .report-meta { text-align:right; font-size:13px; opacity:.9; }
  .report-meta strong { display:block; font-size:15px; }

  /* Filter bar */
  .filter-bar {
    background:white; border-radius:12px; border:1px solid #E2E8F0;
    padding:16px 24px; margin-bottom:24px; display:flex; gap:16px; align-items:flex-end; flex-wrap:wrap;
  }
  .filter-bar label { font-size:12px; font-weight:600; color:#374151; display:block; margin-bottom:5px; }
  .filter-bar input, .filter-bar select {
    border:1.5px solid #E2E8F0; border-radius:8px; padding:8px 12px; font-size:13px; color:#374151; background:#F8FAFC;
  }
  .filter-bar input:focus, .filter-bar select:focus { outline:none; border-color:#059669; }
  .btn-filter { background:#059669; color:white; border:none; border-radius:8px; padding:9px 20px; font-size:13px; font-weight:600; cursor:pointer; }
  .btn-filter:hover { background:#047857; }

  .stats-row { display:grid; grid-template-columns:repeat(4,1fr); gap:16px; margin-bottom:28px; }
  .stat-card { background:white; border-radius:12px; padding:18px 22px; border:1px solid #E2E8F0; }
  .stat-card .num { font-size:28px; font-weight:700; }
  .stat-card .lbl { font-size:12px; color:#64748B; margin-top:2px; }
  .stat-masuk  .num { color:#059669; }
  .stat-keluar .num { color:#DC2626; }
  .stat-retur  .num { color:#D97706; }
  .stat-total  .num { color:#4F46E5; }

  .table-card { background:white; border-radius:14px; border:1px solid #E2E8F0; overflow:hidden; margin-bottom:28px; }
  .table-card-header { padding:18px 24px; border-bottom:1px solid #E2E8F0; display:flex; justify-content:space-between; align-items:center; }
  .table-card-header h2 { font-size:16px; font-weight:600; }
  table { width:100%; border-collapse:collapse; }
  thead { background:#F8FAFC; }
  th { padding:11px 14px; text-align:left; font-size:11.5px; font-weight:600; color:#64748B; text-transform:uppercase; letter-spacing:.5px; border-bottom:1px solid #E2E8F0; }
  td { padding:11px 14px; font-size:13px; border-bottom:1px solid #F1F5F9; }
  tr:last-child td { border-bottom:none; }
  tr:hover td { background:#F8FAFC; }

  .badge { display:inline-block; padding:3px 12px; border-radius:20px; font-size:11px; font-weight:600; }
  .b-masuk  { background:#D1FAE5; color:#065F46; }
  .b-keluar { background:#FEE2E2; color:#991B1B; }
  .b-retur  { background:#FEF3C7; color:#92400E; }

  .report-footer { text-align:center; color:#94A3B8; font-size:12px; padding:16px 0; border-top:1px solid #E2E8F0; margin-top:8px; }

  .no-print { display:flex; gap:12px; margin-bottom:24px; }
  .btn { padding:10px 20px; border:none; border-radius:8px; font-size:14px; font-weight:600; cursor:pointer; }
  .btn-print { background:#059669; color:white; }
  .btn-back  { background:#E2E8F0; color:#374151; }

  @media print {
    body { background:white; }
    .page { padding:16px; }
    .no-print, .filter-bar { display:none !important; }
    .report-header { -webkit-print-color-adjust:exact; print-color-adjust:exact; }
    .badge { -webkit-print-color-adjust:exact; print-color-adjust:exact; }
  }
</style>
</head>
<body>
<div class="page">

  <div class="no-print">
    <button class="btn btn-print" onclick="window.print()">🖨 Cetak Laporan</button>
    <button class="btn btn-back"  onclick="history.back()">← Kembali</button>
  </div>

  <div class="report-header">
    <div>
      <div style="font-size:13px;opacity:.8;margin-bottom:4px;">PT. Inventaris Gudang — Pekanbaru</div>
      <h1>🔄 Laporan Transaksi Barang</h1>
      <p>Periode: <?= date('d M Y', strtotime($tgl_dari)) ?> s/d <?= date('d M Y', strtotime($tgl_sampai)) ?></p>
    </div>
    <div class="report-meta">
      <strong>Dicetak</strong>
      <?= $tanggal ?>
    </div>
  </div>

  <!-- Filter -->
  <form class="filter-bar no-print" method="GET">
    <div>
      <label>Dari Tanggal</label>
      <input type="date" name="dari" value="<?= $tgl_dari ?>">
    </div>
    <div>
      <label>Sampai Tanggal</label>
      <input type="date" name="sampai" value="<?= $tgl_sampai ?>">
    </div>
    <div>
      <label>Jenis Transaksi</label>
      <select name="jenis">
        <option value="">Semua</option>
        <option value="masuk"  <?= $filter_jenis==='masuk'  ? 'selected':'' ?>>Masuk</option>
        <option value="keluar" <?= $filter_jenis==='keluar' ? 'selected':'' ?>>Keluar</option>
        <option value="retur"  <?= $filter_jenis==='retur'  ? 'selected':'' ?>>Retur</option>
      </select>
    </div>
    <button type="submit" class="btn-filter">Filter</button>
  </form>

  <!-- Stats -->
  <div class="stats-row">
    <div class="stat-card stat-total">
      <div class="num"><?= count($items) ?></div>
      <div class="lbl">Total Transaksi</div>
    </div>
    <div class="stat-card stat-masuk">
      <div class="num"><?= number_format($jml_masuk) ?></div>
      <div class="lbl">Unit Masuk (<?= $total_masuk ?> trx)</div>
    </div>
    <div class="stat-card stat-keluar">
      <div class="num"><?= number_format($jml_keluar) ?></div>
      <div class="lbl">Unit Keluar (<?= $total_keluar ?> trx)</div>
    </div>
    <div class="stat-card stat-retur">
      <div class="num"><?= number_format($jml_retur) ?></div>
      <div class="lbl">Unit Retur (<?= $total_retur ?> trx)</div>
    </div>
  </div>

  <!-- Table -->
  <div class="table-card">
    <div class="table-card-header">
      <h2>Detail Transaksi</h2>
      <span style="font-size:13px;color:#64748B;"><?= count($items) ?> transaksi</span>
    </div>
    <table>
      <thead>
        <tr>
          <th>#</th>
          <th>Tanggal</th>
          <th>Kode</th>
          <th>Nama Barang</th>
          <th>Gudang</th>
          <th>Jenis</th>
          <th style="text-align:center">Jumlah</th>
          <th>Supplier</th>
          <th>Petugas</th>
          <th>Keterangan</th>
        </tr>
      </thead>
      <tbody>
      <?php if (empty($items)): ?>
        <tr><td colspan="10" style="text-align:center;padding:32px;color:#94A3B8;">Tidak ada transaksi ditemukan</td></tr>
      <?php else: ?>
      <?php foreach ($items as $i => $r): ?>
        <tr>
          <td style="color:#94A3B8"><?= $i+1 ?></td>
          <td style="white-space:nowrap"><?= date('d/m/Y', strtotime($r['tanggal'])) ?></td>
          <td><code style="background:#F1F5F9;padding:2px 6px;border-radius:4px;font-size:11px"><?= htmlspecialchars($r['kode_barang']) ?></code></td>
          <td style="font-weight:500"><?= htmlspecialchars($r['nama_barang']) ?></td>
          <td style="color:#64748B"><?= htmlspecialchars($r['nama_gudang']) ?></td>
          <td>
            <?php $j = $r['jenis_transaksi']; ?>
            <span class="badge b-<?= $j ?>"><?= ucfirst($j) ?></span>
          </td>
          <td style="text-align:center;font-weight:600;
            color:<?= $j==='masuk' ? '#059669' : ($j==='keluar' ? '#DC2626' : '#D97706') ?>">
            <?= $j==='masuk' ? '+' : ($j==='keluar' ? '-' : '±') ?><?= number_format($r['jumlah']) ?>
          </td>
          <td style="color:#64748B"><?= htmlspecialchars($r['nama_supplier']) ?></td>
          <td><?= htmlspecialchars($r['nama_user']) ?></td>
          <td style="color:#64748B;font-size:12px"><?= htmlspecialchars($r['keterangan'] ?? '') ?></td>
        </tr>
      <?php endforeach; ?>
      <?php endif; ?>
      </tbody>
    </table>
  </div>

  <div class="report-footer">
    Dicetak pada <?= $tanggal ?> &mdash; Sistem Inventaris Gudang &copy; <?= date('Y') ?>
  </div>
</div>
</body>
</html>
