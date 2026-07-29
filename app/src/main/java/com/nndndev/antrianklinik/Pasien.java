package com.nndndev.antrianklinik;

/**
 * Pasien — Model data untuk satu entri antrian pasien.
 */
public class Pasien {

    // ─── Fields ─────────────────────────────────────────────────────────────
    private int    id;
    private int    nomorAntrian;
    private String nama;
    private int    umur;
    private String jenisKelamin;   // "Laki-laki" atau "Perempuan"
    private String poli;           // Nama poli tujuan
    private String keluhan;
    private String status;         // Menunggu / Sedang Dilayani / Selesai / Dibatalkan
    private String tanggalDaftar;  // Format: yyyy-MM-dd HH:mm:ss

    // ─── Constructors ────────────────────────────────────────────────────────

    /** Constructor kosong */
    public Pasien() {}

    /** Constructor lengkap */
    public Pasien(int id, int nomorAntrian, String nama, int umur,
                  String jenisKelamin, String poli,
                  String keluhan, String status, String tanggalDaftar) {
        this.id            = id;
        this.nomorAntrian  = nomorAntrian;
        this.nama          = nama;
        this.umur          = umur;
        this.jenisKelamin  = jenisKelamin;
        this.poli          = poli;
        this.keluhan       = keluhan;
        this.status        = status;
        this.tanggalDaftar = tanggalDaftar;
    }

    // ─── Getters & Setters ───────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getNomorAntrian() { return nomorAntrian; }
    public void setNomorAntrian(int nomorAntrian) { this.nomorAntrian = nomorAntrian; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public int getUmur() { return umur; }
    public void setUmur(int umur) { this.umur = umur; }

    public String getJenisKelamin() { return jenisKelamin; }
    public void setJenisKelamin(String jenisKelamin) { this.jenisKelamin = jenisKelamin; }

    public String getPoli() { return poli; }
    public void setPoli(String poli) { this.poli = poli; }

    public String getKeluhan() { return keluhan; }
    public void setKeluhan(String keluhan) { this.keluhan = keluhan; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTanggalDaftar() { return tanggalDaftar; }
    public void setTanggalDaftar(String tanggalDaftar) { this.tanggalDaftar = tanggalDaftar; }

    // ─── Helper Methods ──────────────────────────────────────────────────────

    public String getNomorAntrianFormatted() {
        return String.format("%03d", nomorAntrian);
    }

    /**
     * Cek apakah pasien masih aktif.
     */
    public boolean isAktif() {
        return status != null &&
            (status.equals(DatabaseHelper.STATUS_MENUNGGU) ||
             status.equals(DatabaseHelper.STATUS_DILAYANI));
    }

    @Override
    public String toString() {
        return "Pasien{" +
            "id=" + id +
            ", nomorAntrian=" + nomorAntrian +
            ", nama='" + nama + '\'' +
            ", poli='" + poli + '\'' +
            ", status='" + status + '\'' +
            '}';
    }
}
