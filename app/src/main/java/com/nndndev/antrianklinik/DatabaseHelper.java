package com.nndndev.antrianklinik;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseHelper — Mengelola database SQLite untuk aplikasi antrian klinik.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME    = "antrian_klinik.db";
    private static final int    DATABASE_VERSION = 3; // Naikkan versi untuk menghapus data lama (trigger onUpgrade)

    public static final String TABLE_PASIEN = "tb_pasien";

    public static final String COL_ID              = "id";
    public static final String COL_NOMOR_ANTRIAN   = "nomor_antrian";
    public static final String COL_NAMA            = "nama";
    public static final String COL_UMUR            = "umur";
    public static final String COL_JENIS_KELAMIN   = "jenis_kelamin";
    public static final String COL_POLI            = "poli";
    public static final String COL_KELUHAN         = "keluhan";
    public static final String COL_STATUS          = "status";
    public static final String COL_TANGGAL_DAFTAR  = "tanggal_daftar";

    public static final String STATUS_MENUNGGU        = "Menunggu";
    public static final String STATUS_DILAYANI        = "Sedang Dilayani";
    public static final String STATUS_SELESAI         = "Selesai";
    public static final String STATUS_DIBATALKAN      = "Dibatalkan";

    private static final String CREATE_TABLE_PASIEN =
        "CREATE TABLE " + TABLE_PASIEN + " (" +
            COL_ID             + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_NOMOR_ANTRIAN  + " INTEGER NOT NULL, " +
            COL_NAMA           + " TEXT NOT NULL, " +
            COL_UMUR           + " INTEGER, " +
            COL_JENIS_KELAMIN  + " TEXT, " +
            COL_POLI           + " TEXT, " +
            COL_KELUHAN        + " TEXT, " +
            COL_STATUS         + " TEXT DEFAULT '" + STATUS_MENUNGGU + "', " +
            COL_TANGGAL_DAFTAR + " TEXT" +
        ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PASIEN);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Reset total biar gampang buat demo
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PASIEN);
        onCreate(db);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  CRUD Operations — Pasien
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Tambah pasien baru dengan nomor antrian otomatis hari ini.
     */
    public long tambahPasien(Pasien pasien) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        int nextNo = 1;
        String query = "SELECT MAX(" + COL_NOMOR_ANTRIAN + ") FROM " + TABLE_PASIEN + 
                      " WHERE DATE(" + COL_TANGGAL_DAFTAR + ") = DATE('now')";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            nextNo = cursor.getInt(0) + 1;
        }
        cursor.close();
        
        pasien.setNomorAntrian(nextNo);

        ContentValues values = new ContentValues();
        values.put(COL_NOMOR_ANTRIAN,  pasien.getNomorAntrian());
        values.put(COL_NAMA,           pasien.getNama());
        values.put(COL_UMUR,           pasien.getUmur());
        values.put(COL_JENIS_KELAMIN,  pasien.getJenisKelamin());
        values.put(COL_POLI,           pasien.getPoli());
        values.put(COL_KELUHAN,        pasien.getKeluhan());
        values.put(COL_STATUS,         pasien.getStatus());
        values.put(COL_TANGGAL_DAFTAR, pasien.getTanggalDaftar());

        long id = db.insert(TABLE_PASIEN, null, values);
        db.close();
        return id;
    }

    public List<Pasien> getAllPasien() {
        List<Pasien> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PASIEN + " ORDER BY " + COL_ID + " DESC", null);

        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToPasien(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public List<Pasien> getAllPasienHariIni() {
        List<Pasien> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_PASIEN + 
                      " WHERE DATE(" + COL_TANGGAL_DAFTAR + ") = DATE('now')" +
                      " ORDER BY " + COL_NOMOR_ANTRIAN + " ASC";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToPasien(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public List<Pasien> getPasienByStatus(String status) {
        List<Pasien> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_PASIEN + " WHERE " + COL_STATUS + " = ?" +
                      " ORDER BY " + COL_NOMOR_ANTRIAN + " ASC";
        Cursor cursor = db.rawQuery(query, new String[]{status});

        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToPasien(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public List<Pasien> getPasienFiltered(String status, String waktu) {
        List<Pasien> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        StringBuilder query = new StringBuilder("SELECT * FROM " + TABLE_PASIEN + " WHERE 1=1");
        List<String> args = new ArrayList<>();

        if (status != null && !status.equals("Semua Status")) {
            query.append(" AND ").append(COL_STATUS).append(" = ?");
            args.add(status);
        }

        if (waktu != null) {
            if (waktu.equals("Hari Ini")) {
                query.append(" AND DATE(").append(COL_TANGGAL_DAFTAR).append(") = DATE('now')");
            } else if (waktu.equals("Bulan Ini")) {
                query.append(" AND strftime('%Y-%m', ").append(COL_TANGGAL_DAFTAR).append(") = strftime('%Y-%m', 'now')");
            } else if (waktu.equals("Tahun Ini")) {
                query.append(" AND strftime('%Y', ").append(COL_TANGGAL_DAFTAR).append(") = strftime('%Y', 'now')");
            }
        }

        query.append(" ORDER BY ").append(COL_ID).append(" DESC");
        
        Cursor cursor = db.rawQuery(query.toString(), args.toArray(new String[0]));

        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToPasien(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public int updateStatusPasien(int id, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STATUS, status);

        return db.update(TABLE_PASIEN, values, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void deletePasien(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PASIEN, COL_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void resetAntrianHariIni() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STATUS, STATUS_SELESAI);

        String where = "DATE(" + COL_TANGGAL_DAFTAR + ") = DATE('now') AND (" +
                      COL_STATUS + " = ? OR " + COL_STATUS + " = ?)";
        db.update(TABLE_PASIEN, values, where, new String[]{STATUS_MENUNGGU, STATUS_DILAYANI});
        db.close();
    }

    public int getNomorAntrianBerikutnya() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COL_NOMOR_ANTRIAN + " FROM " + TABLE_PASIEN + 
                      " WHERE " + COL_STATUS + " = '" + STATUS_MENUNGGU + "'" +
                      " AND DATE(" + COL_TANGGAL_DAFTAR + ") = DATE('now')" +
                      " ORDER BY " + COL_NOMOR_ANTRIAN + " ASC LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);
        int no = 0;
        if (cursor.moveToFirst()) {
            no = cursor.getInt(0);
        }
        cursor.close();
        return no;
    }

    public int getJumlahMenunggu() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_PASIEN + 
                      " WHERE " + COL_STATUS + " = '" + STATUS_MENUNGGU + "'" +
                      " AND DATE(" + COL_TANGGAL_DAFTAR + ") = DATE('now')";
        Cursor cursor = db.rawQuery(query, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    private Pasien cursorToPasien(Cursor cursor) {
        Pasien pasien = new Pasien();
        pasien.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
        pasien.setNomorAntrian(cursor.getInt(cursor.getColumnIndexOrThrow(COL_NOMOR_ANTRIAN)));
        pasien.setNama(cursor.getString(cursor.getColumnIndexOrThrow(COL_NAMA)));
        pasien.setUmur(cursor.getInt(cursor.getColumnIndexOrThrow(COL_UMUR)));
        pasien.setJenisKelamin(cursor.getString(cursor.getColumnIndexOrThrow(COL_JENIS_KELAMIN)));
        pasien.setPoli(cursor.getString(cursor.getColumnIndexOrThrow(COL_POLI)));
        pasien.setKeluhan(cursor.getString(cursor.getColumnIndexOrThrow(COL_KELUHAN)));
        pasien.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS)));
        pasien.setTanggalDaftar(cursor.getString(cursor.getColumnIndexOrThrow(COL_TANGGAL_DAFTAR)));
        return pasien;
    }
}
