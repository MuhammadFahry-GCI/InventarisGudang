<?php
require_once 'config.php';
$pdo = getDB();

$sql = "SELECT b.kode_barang, b.nama_barang, k.nama_kategori, b.satuan, b.stok_minimum,
        COALESCE(SUM(CASE WHEN s.id_gudang=1 THEN s.jumlah_stok END),0) AS gudang_utama,
        COALESCE(SUM(CASE WHEN s.id_gudang=2 THEN s.jumlah_stok END),0) AS gudang_cabang,
        COALESCE(SUM(s.jumlah_stok),0) AS total_stok
        FROM barang b
        JOIN kategori k ON b.id_kategori = k.id_kategori
        LEFT JOIN stok s ON b.id_barang = s.id_barang
        GROUP BY b.id_barang ORDER BY k.nama_kategori, b.nama_barang";

$items = $pdo->query($sql)->fetchAll();
$total_barang = count($items);
$stok_minim   = array_filter($items, fn($r) => $r['total_stok'] <= $r['stok_minimum']);
$tanggal      = date('d F Y H:i');
?>
<!DOCTYPE html>
<html lang="id">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Laporan Stok Barang — Inventaris Gudang</title>
<style>
  @import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');
  * { margin: 0; padding: 0; box-sizing: border-box; }
  body { font-family: 'Inter', Arial, sans-serif; background: #F4F6FA; color: #1E293B; }

  .page { max-width: 1100px; margin: 0 auto; padding: 32px 24px; }

  /* HEADER */
  .report-header {
    background: linear-gradient(135deg, #4F46E5 0%, #7C3AED 100%);
    border-radius: 16px; padding: 32px 36px; color: white; margin-bottom: 28px;
    display: flex; justify-content: space-between; align-items: center;
  }
  .report-header h1 { font-size: 26px; font-weight: 700; }
  .report-header p  { font-size: 13px; opacity: .85; margin-top: 4px; }
  .report-logo { font-size: 48px; }
  .report-meta { text-align: right; font-size: 13px; opacity: .9; }
  .report-meta strong { display: block; font-size: 15px; }

  /* STAT CARDS */
  .stats-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 28px; }
  .stat-card { background: white; border-radius: 12px; padding: 20px 24px; border: 1px solid #E2E8F0; }
  .stat-card .num  { font-size: 32px; font-weight: 700; color: #4F46E5; }
  .stat-card .lbl  { font-size: 13px; color: #64748B; margin-top: 2px; }
  .stat-card.warn .num { color: #F59E0B; }
  .stat-card.ok   .num { color: #10B981; }

  /* TABLE */
  .table-card { background: white; border-radius: 14px; border: 1px solid #E2E8F0; overflow: hidden; margin-bottom: 28px; }
  .table-card-header { padding: 18px 24px; border-bottom: 1px solid #E2E8F0; display: flex; justify-content: space-between; align-items: center; }
  .table-card-header h2 { font-size: 16px; font-weight: 600; }
  table { width: 100%; border-collapse: collapse; }
  thead { background: #F8FAFC; }
  th { padding: 12px 16px; text-align: left; font-size: 12px; font-weight: 600; color: #64748B;
       text-transform: uppercase; letter-spacing: .5px; border-bottom: 1px solid #E2E8F0; }
  td { padding: 12px 16px; font-size: 13.5px; border-bottom: 1px solid #F1F5F9; }
  tr:last-child td { border-bottom: none; }
  tr:hover td { background: #F8FAFC; }
  tr.minim td { background: #FFFBEB; }

  .badge { display: inline-block; padding: 3px 12px; border-radius: 20px; font-size: 11px; font-weight: 600; }
  .badge-ok      { background: #D1FAE5; color: #065F46; }
  .badge-warn    { background: #FEF3C7; color: #92400E; }
  .badge-minim   { background: #FEE2E2; color: #991B1B; }
  .stok-ok   { color: #059669; font-weight: 600; }
  .stok-warn { color: #D97706; font-weight: 600; }
  .stok-zero { color: #DC2626; font-weight: 700; }

  /* FOOTER */
  .report-footer { text-align: center; color: #94A3B8; font-size: 12px; padding: 16px 0; border-top: 1px solid #E2E8F0; margin-top: 8px; }

  /* PRINT */
  .no-print { display: flex; gap: 12px; margin-bottom: 24px; }
  .btn { padding: 10px 20px; border: none; border-radius: 8px; font-size: 14px; font-weight: 600; cursor: pointer; }
  .btn-primary { background: #4F46E5; color: white; }
  .btn-secondary { background: #E2E8F0; color: #374151; }

  @media print {
    body { background: white; }
    .page { padding: 16px; }
    .no-print { display: none !important; }
    .report-header { -webkit-print-color-adjust: exact; print-color-adjust: exact; }
    .badge { -webkit-print-color-adjust: exact; print-color-adjust: exact; }
    tr.minim td { -webkit-print-color-adjust: exact; print-color-adjust: exact; }
  }
</style>
</head>
<body>
<div class="page">
  <!-- Print button -->
  <div class="no-print">
    <button class="btn btn-primary" onclick="window.print()">🖨 Cetak Laporan</button>
    <button class="btn btn-secondary" onclick="history.back()">← Kembali</button>
  </div>

  <!-- Header -->
  <div class="report-header">
    <div>
      <div style="font-size:13px;opacity:.8;margin-bottom:4px;">PT. Inventaris Gudang — Pekanbaru</div>
      <h1>📦 Laporan Stok Barang</h1>
      <p>Rekap ketersediaan stok seluruh gudang</p>
    </div>
    <div class="report-meta">
      <strong>Tanggal Cetak</strong>
      <?= $tanggal ?>
    </div>
  </div>

  <!-- Stats -->
  <div class="stats-row">
    <div class="stat-card">
      <div class="num"><?= $total_barang ?></div>
      <div class="lbl">Total Jenis Barang</div>
    </div>
    <div class="stat-card warn">
      <div class="num"><?= count($stok_minim) ?></div>
      <div class="lbl">Stok Menipis / Habis</div>
    </div>
    <div class="stat-card ok">
      <div class="num"><?= $total_barang - count($stok_minim) ?></div>
      <div class="lbl">Stok Aman</div>
    </div>
  </div>

  <!-- Table -->
  <div class="table-card">
    <div class="table-card-header">
      <h2>Detail Stok Per Barang</h2>
      <span style="font-size:13px;color:#64748B;"><?= $total_barang ?> item</span>
    </div>
    <table>
      <thead>
        <tr>
          <th>#</th>
          <th>Kode</th>
          <th>Nama Barang</th>
          <th>Kategori</th>
          <th>Satuan</th>
          <th style="text-align:center">Gudang Utama</th>
          <th style="text-align:center">Gudang Cabang A</th>
          <th style="text-align:center">Total Stok</th>
          <th style="text-align:center">Min</th>
          <th style="text-align:center">Status</th>
        </tr>
      </thead>
      <tbody>
      <?php foreach ($items as $i => $row):
        $isMinim  = $row['total_stok'] <= $row['stok_minimum'];
        $isZero   = $row['total_stok'] == 0;
        $rowClass = $isMinim ? 'minim' : '';
        $stokClass= $isZero ? 'stok-zero' : ($isMinim ? 'stok-warn' : 'stok-ok');
        $badge    = $isZero ? '<span class="badge badge-minim">Habis</span>'
                  : ($isMinim ? '<span class="badge badge-warn">Menipis</span>'
                  : '<span class="badge badge-ok">Aman</span>');
      ?>
        <tr class="<?= $rowClass ?>">
          <td style="color:#94A3B8"><?= $i+1 ?></td>
          <td><code style="background:#F1F5F9;padding:2px 6px;border-radius:4px;font-size:12px"><?= htmlspecialchars($row['kode_barang']) ?></code></td>
          <td style="font-weight:500"><?= htmlspecialchars($row['nama_barang']) ?></td>
          <td style="color:#64748B"><?= htmlspecialchars($row['nama_kategori']) ?></td>
          <td style="color:#64748B"><?= htmlspecialchars($row['satuan']) ?></td>
          <td style="text-align:center"><?= number_format($row['gudang_utama']) ?></td>
          <td style="text-align:center"><?= number_format($row['gudang_cabang']) ?></td>
          <td style="text-align:center" class="<?= $stokClass ?>"><?= number_format($row['total_stok']) ?></td>
          <td style="text-align:center;color:#94A3B8"><?= $row['stok_minimum'] ?></td>
          <td style="text-align:center"><?= $badge ?></td>
        </tr>
      <?php endforeach; ?>
      </tbody>
    </table>
  </div>

  <div class="report-footer">
    Dicetak pada <?= $tanggal ?> &mdash; Sistem Inventaris Gudang &copy; <?= date('Y') ?>
  </div>
</div>
</body>
</html>
