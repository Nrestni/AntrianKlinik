package com.nndndev.antrianklinik;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Helper buat nampilin dialog detail pasien biar konsisten di semua halaman.
 */
public class PatientDialogHelper {

    public interface OnStatusUpdateListener {
        void onUpdateStatus(Pasien pasien);
    }

    public static void showDetail(Context context, Pasien p, boolean canUpdate, OnStatusUpdateListener listener) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_detail_pasien, null);
        
        // Gak usah pake tema activity biar posisinya otomatis center (tengah)
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();

        // Biar background aslinya transparan supaya radius CardView kita kelihatan
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Binding view
        TextView tvNoAntrian = view.findViewById(R.id.dialog_tv_nomor_antrian);
        TextView tvNama = view.findViewById(R.id.dialog_tv_nama);
        TextView tvStatus = view.findViewById(R.id.dialog_tv_status);
        TextView tvUmur = view.findViewById(R.id.dialog_tv_umur);
        TextView tvGender = view.findViewById(R.id.dialog_tv_gender);
        TextView tvPoli = view.findViewById(R.id.dialog_tv_poli);
        TextView tvTanggal = view.findViewById(R.id.dialog_tv_tanggal);
        TextView tvKeluhan = view.findViewById(R.id.dialog_tv_keluhan);
        Button btnTutup = view.findViewById(R.id.dialog_btn_tutup);
        Button btnUbahStatus = view.findViewById(R.id.dialog_btn_ubah_status);

        // Set data
        tvNoAntrian.setText(p.getNomorAntrianFormatted());
        tvNama.setText(p.getNama());
        tvStatus.setText(p.getStatus());
        tvUmur.setText(p.getUmur() + " Tahun");
        tvGender.setText(p.getJenisKelamin());
        tvPoli.setText(p.getPoli());
        tvTanggal.setText(p.getTanggalDaftar());
        tvKeluhan.setText(p.getKeluhan());

        // Warnain badge status otomatis
        setStatusBadgeColor(tvStatus, p.getStatus());

        // Atur tombol ubah status
        if (canUpdate && !DatabaseHelper.STATUS_SELESAI.equals(p.getStatus())) {
            btnUbahStatus.setVisibility(View.VISIBLE);
            btnUbahStatus.setOnClickListener(v -> {
                dialog.dismiss();
                if (listener != null) listener.onUpdateStatus(p);
            });
        }

        btnTutup.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private static void setStatusBadgeColor(TextView tv, String status) {
        if (status == null) return;
        switch (status) {
            case DatabaseHelper.STATUS_MENUNGGU:
                tv.setBackgroundColor(Color.parseColor("#F59E0B")); // Amber
                break;
            case DatabaseHelper.STATUS_DILAYANI:
                tv.setBackgroundColor(Color.parseColor("#2563EB")); // Blue
                break;
            case DatabaseHelper.STATUS_SELESAI:
                tv.setBackgroundColor(Color.parseColor("#22C55E")); // Green
                break;
            case DatabaseHelper.STATUS_DIBATALKAN:
                tv.setBackgroundColor(Color.parseColor("#EF4444")); // Red
                break;
        }
    }
}
