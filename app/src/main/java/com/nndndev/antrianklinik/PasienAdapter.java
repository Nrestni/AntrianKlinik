package com.nndndev.antrianklinik;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * PasienAdapter — RecyclerView Adapter untuk menampilkan daftar antrian pasien.
 *
 * Fitur:
 * - Menampilkan nomor antrian, nama, poli, keluhan, status
 * - Warna badge status berbeda-beda (kuning=menunggu, biru=dilayani, hijau=selesai, merah=batal)
 * - Interface klik untuk detail / ubah status
 */
public class PasienAdapter extends RecyclerView.Adapter<PasienAdapter.PasienViewHolder> {

    private final Context      context;
    private       List<Pasien> daftarPasien;

    // ─── Interface Listener ─────────────────────────────────────────────────

    /** Callback untuk klik satu item */
    public interface OnItemClickListener {
        void onItemClick(Pasien pasien, int position);
    }

    /** Callback untuk long-click satu item */
    public interface OnItemLongClickListener {
        boolean onItemLongClick(Pasien pasien, int position);
    }

    private OnItemClickListener     onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.onItemLongClickListener = listener;
    }

    // ─── Constructor ────────────────────────────────────────────────────────

    public PasienAdapter(Context context, List<Pasien> daftarPasien) {
        this.context      = context;
        this.daftarPasien = daftarPasien;
    }

    // ─── RecyclerView.Adapter Methods ───────────────────────────────────────

    @NonNull
    @Override
    public PasienViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
            .inflate(R.layout.item_pasien, parent, false);
        return new PasienViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PasienViewHolder holder, int position) {
        Pasien pasien = daftarPasien.get(position);

        holder.tvNomorAntrian.setText(pasien.getNomorAntrianFormatted());
        holder.tvNama.setText(pasien.getNama());
        holder.tvPoli.setText(pasien.getPoli());
        holder.tvStatus.setText(pasien.getStatus());

        if (pasien.getKeluhan() != null && !pasien.getKeluhan().isEmpty()) {
            holder.tvKeluhan.setVisibility(View.VISIBLE);
            holder.tvKeluhan.setText(pasien.getKeluhan());
        } else {
            holder.tvKeluhan.setVisibility(View.GONE);
        }

        // set warna badge status
        setStatusBadgeColor(holder.tvStatus, pasien.getStatus());

        // ambil jam saja dari tanggal daftar (format: yyyy-MM-dd HH:mm:ss)
        if (pasien.getTanggalDaftar() != null && pasien.getTanggalDaftar().length() >= 16) {
            String jam = pasien.getTanggalDaftar().substring(11, 16);
            holder.tvTanggal.setText(jam);
        }

        // click listener
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(pasien, holder.getAdapterPosition());
            }
        });

        // Long click listener
        holder.itemView.setOnLongClickListener(v -> {
            if (onItemLongClickListener != null) {
                return onItemLongClickListener.onItemLongClick(pasien, holder.getAdapterPosition());
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return daftarPasien != null ? daftarPasien.size() : 0;
    }

    // ─── Helper Methods ──────────────────────────────────────────────────────

    /**
     * Update seluruh daftar data dan refresh tampilan.
     */
    public void updateData(List<Pasien> newData) {
        this.daftarPasien = newData;
        notifyDataSetChanged();
    }

    /**
     * Tambahkan satu item baru di atas daftar.
     */
    public void addItem(Pasien pasien) {
        daftarPasien.add(0, pasien);
        notifyItemInserted(0);
    }

    /**
     * Hapus item berdasarkan posisi.
     */
    public void removeItem(int position) {
        daftarPasien.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * Set warna badge TextView berdasarkan status antrian.
     */
    private void setStatusBadgeColor(TextView tvStatus, String status) {
        if (status == null) return;

        switch (status) {
            case DatabaseHelper.STATUS_MENUNGGU:
                tvStatus.setBackgroundColor(Color.parseColor("#FFC107")); // Kuning
                tvStatus.setTextColor(Color.BLACK);
                break;
            case DatabaseHelper.STATUS_DILAYANI:
                tvStatus.setBackgroundColor(Color.parseColor("#2196F3")); // Biru
                tvStatus.setTextColor(Color.WHITE);
                break;
            case DatabaseHelper.STATUS_SELESAI:
                tvStatus.setBackgroundColor(Color.parseColor("#4CAF50")); // Hijau
                tvStatus.setTextColor(Color.WHITE);
                break;
            case DatabaseHelper.STATUS_DIBATALKAN:
                tvStatus.setBackgroundColor(Color.parseColor("#F44336")); // Merah
                tvStatus.setTextColor(Color.WHITE);
                break;
            default:
                tvStatus.setBackgroundColor(Color.GRAY);
                tvStatus.setTextColor(Color.WHITE);
        }
    }

    // ─── ViewHolder ─────────────────────────────────────────────────────────

    public static class PasienViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        TextView tvNomorAntrian;
        TextView tvNama;
        TextView tvPoli;
        TextView tvKeluhan;
        TextView tvStatus;
        TextView tvTanggal;

        public PasienViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView        = itemView.findViewById(R.id.card_pasien);
            tvNomorAntrian  = itemView.findViewById(R.id.tv_nomor_antrian);
            tvNama          = itemView.findViewById(R.id.tv_nama);
            tvPoli          = itemView.findViewById(R.id.tv_poli);
            tvKeluhan       = itemView.findViewById(R.id.tv_keluhan);
            tvStatus        = itemView.findViewById(R.id.tv_status);
            tvTanggal       = itemView.findViewById(R.id.tv_tanggal);
        }
    }
}
