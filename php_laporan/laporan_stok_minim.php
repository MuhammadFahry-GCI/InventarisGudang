<?php
require_once 'config.php';
$pdo = getDB();

$sql = "SELECT b.kode_barang, b.nama_barang, k.nama_kategori, b.satuan, b.stok_minimum,
        COALESCE(SUM(s.jumlah_stok),0) AS total_stok,
        COALESCE(SUM(CASE WHEN s.id_gudang=1 THEN s.jumlah_stok END),0) AS gudang_utama,
        COALESCE(SUM(CASE WHEN s.id_gudang=2 THEN s.jumlah_stok END),0) AS gudang_cabang
        FROM barang b
        JOIN kategori k ON b.id_kategori = k.id_kategori
        LEFT JOIN stok s ON b.id_barang = s.id_barang
        GROUP BY b.id_barang
        HAVING total_stok <= b.stok_minimum
        ORDER BY (total_stok / NULLIF(b.stok_minimum, 0)) ASC";

$items   = $pdo->query($sql)->fetchAll();
$tanggal = date('d F Y H:i');

$habis  = array_filter($items, fn($r) => $r['total_stok'] == 0);
$kritis = array_filter($items, fn($r) => $r['total_stok'] > 0 && $r['total_stok'] <= $r['stok_minimum'] / 2);
$menipis= array_filter($items, fn($r) => $r['total_stok'] > $r['stok_minimum'] / 2 && $r['total_stok'] <= $r['stok_minimum']);
?>
<!DOCTYPE html>
<html lang="id">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1.0">
<title>Laporan Stok Minim — Inventaris Gudang</title>
<style>
  @import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');
  * { margin:0; padding:0; box-sizing:border-box; }
  body { font-family:'Inter',Arial,sans-serif; background:#F4F6FA; color:#1E293B; }
  .page { max-width:1100px; margin:0 auto; padding:32px 24px; }

  .report-header {
    background:linear-gradient(135deg, #DC2626 0%, #EA580C 100%);
    border-radius:16px; padding:32px 36px; color:white; margin-bottom:28px;
    display:flex; justify-content:space-between; align-items:center;
  }
  .report-header h1 { font-size:26px; font-weight:700; }
  .report-header p  { font-size:13px; opacity:.85; margin-top:4px; }
  .report-meta { text-align:right; font-size:13px; opacity:.9; }
  .report-meta strong { display:block; font-size:15px; }

  /* Alert banner */
  .alert-banner {
    background:#FEF3C7; border:1.5px solid #FCD34D; border-radius:10px;
    padding:14px 20px; margin-bottom:24px; display:flex; align-items:center; gap:12px;
  }
  .alert-banner .icon { font-size:22px; }
  .alert-banner .text { font-size:13.5px; color:#92400E; }
  .alert-banner strong { font-weight:600; }

  .stats-row { display:grid; grid-template-columns:repeat(4,1fr); gap:16px; margin-bottom:28px; }
  .stat-card { background:white; border-radius:12px; padding:18px 22px; border:1px solid #E2E8F0; }
  .stat-card .num { font-size:28px; font-weight:700; }
  .stat-card .lbl { font-size:12px; color:#64748B; margin-top:2px; }
  .s-habis   .num { color:#DC2626; }
  .s-kritis  .num { color:#EA580C; }
  .s-menipis .num { color:#D97706; }
  .s-total   .num { color:#7C3AED; }

  /* Progress bar */
  .progress-wrap { width:100%; background:#F1F5F9; border-radius:20px; height:8px; overflow:hidden; margin-top:4px; }
  .progress-bar  { height:8px; border-radius:20px; transition:width .3s; }

  .table-card { background:white; border-radius:14px; border:1px solid #E2E8F0; overflow:hidden; margin-bottom:28px; }
  .table-card-header { padding:18px 24px; border-bottom:1px solid #E2E8F0; display:flex; justify-content:space-between; align-items:center; }
  .table-card-header h2 { font-size:16px; font-weight:600; }
  table { width:100%; border-collapse:collapse; }
  thead { background:#F8FAFC; }
  th { padding:11px 16px; text-align:left; font-size:11.5px; font-weight:600; color:#64748B; text-transform:uppercase; letter-spacing:.5px; border-bottom:1px solid #E2E8F0; }
  td { padding:12px 16px; font-size:13px; border-bottom:1px solid #F1F5F9; vertical-align:middle; }
  tr:last-child td { border-bottom:none; }
  tr.row-habis td   { background:#FFF5F5; }
  tr.row-kritis td  { background:#FFF7ED; }
  tr.row-menipis td { background:#FFFBEB; }

  .badge { display:inline-block; padding:4px 12px; border-radius:20px; font-size:11px; font-weight:600; }
  .b-habis   { background:#FEE2E2; color:#991B1B; }
  .b-kritis  { background:#FFEDD5; color:#9A3412; }
  .b-menipis { background:#FEF3C7; color:#92400E; }

  .stok-num { font-size:18px; font-weight:700; }
  .stok-habis   { color:#DC2626; }
  .stok-kritis  { color:#EA580C; }
  .stok-menipis { color:#D97706; }

  .report-footer { text-align:center; color:#94A3B8; font-size:12px; padding:16px 0; border-top:1px solid #E2E8F0; }

  .no-print { display:flex; gap:12px; margin-bottom:24px; }
  .btn { padding:10px 20px; border:none; border-radius:8px; font-size:14px; font-weight:600; cursor:pointer; }
  .btn-print { background:#DC2626; color:white; }
  .btn-back  { background:#E2E8F0; color:#374151; }

  @media print {
    body { background:white; }
    .page { padding:16px; }
    .no-print { display:none !important; }
    .report-header, tr.row-habis td, tr.row-kritis td, tr.row-menipis td { -webkit-print-color-adjust:exact; print-color-adjust:exact; }
    .badge, .progress-bar { -webkit-print-color-adjust:exact; print-color-adjust:exact; }
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
      <h1>⚠️ Laporan Barang Stok Minim</h1>
      <p>Daftar barang yang perlu segera di-restock</p>
    </div>
    <div class="report-meta">
      <strong>Dicetak</strong>
      <?= $tanggal ?>
    </div>
  </div>

  <?php if (count($habis) > 0): ?>
  <div class="alert-banner">
    <span class="icon">🚨</span>
    <span class="text"><strong><?= count($habis) ?> barang sudah HABIS!</strong> Segera lakukan pembelian/pengadaan untuk barang-barang tersebut.</span>
  </div>
  <?php endif; ?>

  <!-- Stats -->
  <div class="stats-row">
    <div class="stat-card s-total">
      <div class="num"><?= count($items) ?></div>
      <div class="lbl">Total Perlu Restock</div>
    </div>
    <div class="stat-card s-habis">
      <div class="num"><?= count($habis) ?></div>
      <div class="lbl">Stok Habis (0)</div>
    </div>
    <div class="stat-card s-kritis">
      <div class="num"><?= count($kritis) ?></div>
      <div class="lbl">Kritis (&lt;50% min)</div>
    </div>
    <div class="stat-card s-menipis">
      <div class="num"><?= count($menipis) ?></div>
      <div class="lbl">Menipis (≤ min)</div>
    </div>
  </div>

  <!-- Table -->
  <div class="table-card">
    <div class="table-card-header">
      <h2>Detail Barang Perlu Restock</h2>
      <span style="font-size:13px;color:#64748B;"><?= count($items) ?> barang</span>
    </div>

    <?php if (empty($items)): ?>
      <div style="text-align:center;padding:48px;color:#10B981;">
        <div style="font-size:48px;margin-bottom:12px;">✅</div>
        <div style="font-size:16px;font-weight:600;">Semua stok dalam kondisi aman!</div>
        <div style="font-size:13px;color:#64748B;margin-top:4px;">Tidak ada barang yang perlu restock saat ini.</div>
      </div>
    <?php else: ?>
    <table>
      <thead>
        <tr>
          <th>#</th>
          <th>Prioritas</th>
          <th>Kode</th>
          <th>Nama Barang</th>
          <th>Kategori</th>
          <th>Satuan</th>
          <th style="text-align:center">Stok Saat Ini</th>
          <th style="text-align:center">Stok Min</th>
          <th style="text-align:center">Kekurangan</th>
          <th>Level Stok</th>
        </tr>
      </thead>
      <tbody>
      <?php foreach ($items as $i => $r):
        $pct   = $r['stok_minimum'] > 0 ? min(100, round($r['total_stok'] / $r['stok_minimum'] * 100)) : 0;
        $isHabis  = $r['total_stok'] == 0;
        $isKritis = $r['total_stok'] > 0 && $r['total_stok'] <= $r['stok_minimum'] / 2;

        $rowClass  = $isHabis ? 'row-habis' : ($isKritis ? 'row-kritis' : 'row-menipis');
        $stokClass = $isHabis ? 'stok-habis' : ($isKritis ? 'stok-kritis' : 'stok-menipis');
        $badge     = $isHabis ? '<span class="badge b-habis">HABIS</span>'
                   : ($isKritis ? '<span class="badge b-kritis">Kritis</span>'
                   : '<span class="badge b-menipis">Menipis</span>');
        $barColor  = $isHabis ? '#DC2626' : ($isKritis ? '#EA580C' : '#F59E0B');
        $kekurangan = max(0, $r['stok_minimum'] - $r['total_stok']);
      ?>
        <tr class="<?= $rowClass ?>">
          <td style="color:#94A3B8"><?= $i+1 ?></td>
          <td><?= $badge ?></td>
          <td><code style="background:#F1F5F9;padding:2px 6px;border-radius:4px;font-size:11px"><?= htmlspecialchars($r['kode_barang']) ?></code></td>
          <td style="font-weight:500"><?= htmlspecialchars($r['nama_barang']) ?></td>
          <td style="color:#64748B"><?= htmlspecialchars($r['nama_kategori']) ?></td>
          <td style="color:#64748B"><?= htmlspecialchars($r['satuan']) ?></td>
          <td style="text-align:center">
            <span class="stok-num <?= $stokClass ?>"><?= number_format($r['total_stok']) ?></span>
          </td>
          <td style="text-align:center;color:#64748B"><?= $r['stok_minimum'] ?></td>
          <td style="text-align:center;font-weight:600;color:#DC2626">+<?= number_format($kekurangan) ?></td>
          <td style="min-width:120px">
            <div style="font-size:11px;color:#64748B;margin-bottom:4px;"><?= $pct ?>%</div>
            <div class="progress-wrap">
              <div class="progress-bar" style="width:<?= $pct ?>%;background:<?= $barColor ?>;"></div>
            </div>
          </td>
        </tr>
      <?php endforeach; ?>
      </tbody>
    </table>
    <?php endif; ?>
  </div>

  <div class="report-footer">
    Dicetak pada <?= $tanggal ?> &mdash; Sistem Inventaris Gudang &copy; <?= date('Y') ?>
  </div>
</div>
</body>
</html>
