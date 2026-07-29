package com.nndndev.antrianklinik;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * MainActivity — Dashboard utama aplikasi antrian klinik.
 */
public class MainActivity extends AppCompatActivity {

    private RecyclerView rvAntrian;
    private FloatingActionButton fabTambah;
    private Button btnPanggil;
    private TextView tvNomorAntrian, tvJumlahMenunggu, tvNamaUser, tvTanggal, tvEmpty;
    private BottomNavigationView bottomNavigationView;

    private PasienAdapter pasienAdapter;
    private List<Pasien> daftarPasien;

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private SharedPreferences themePrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // apply tema sebelum view dibuat biar nggak flicker
        themePrefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        boolean isDark = themePrefs.getBoolean("isDark", false);
        AppCompatDelegate.setDefaultNightMode(
            isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Dashboard Antrian");
        }

        databaseHelper  = new DatabaseHelper(this);
        sessionManager  = new SessionManager(this);

        rvAntrian        = findViewById(R.id.rv_antrian);
        fabTambah        = findViewById(R.id.fab_tambah);
        tvNomorAntrian   = findViewById(R.id.tv_nomor_antrian);
        tvJumlahMenunggu = findViewById(R.id.tv_jumlah_menunggu);
        tvNamaUser       = findViewById(R.id.tv_nama_user);
        tvTanggal        = findViewById(R.id.tv_tanggal);
        tvEmpty          = findViewById(R.id.tv_empty);
        btnPanggil       = findViewById(R.id.btn_panggil_berikutnya);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // setup bottom navigation
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_history) {
                startActivity(new Intent(this, RiwayatActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return true;
        });

        // set nama user dan tanggal hari ini
        tvNamaUser.setText("Halo, " + sessionManager.getUsername());
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM yyyy", new Locale("id", "ID"));
        tvTanggal.setText(sdf.format(new Date()));

        setupRecyclerView();
        loadDataAntrian();

        fabTambah.setOnClickListener(v ->
            startActivity(new Intent(this, TambahPasienActivity.class))
        );

        btnPanggil.setOnClickListener(v -> panggilPasienBerikutnya());
    }

    /**
     * Cari pasien "Menunggu" paling awal, ubah status ke "Dilayani".
     */
    private void panggilPasienBerikutnya() {
        Pasien target = null;
        for (Pasien p : daftarPasien) {
            if (DatabaseHelper.STATUS_MENUNGGU.equals(p.getStatus())) {
                target = p;
                break;
            }
        }

        if (target != null) {
            databaseHelper.updateStatusPasien(target.getId(), DatabaseHelper.STATUS_DILAYANI);
            loadDataAntrian(); // refresh list
            
            new AlertDialog.Builder(this)
                .setTitle("Panggilan Pasien")
                .setMessage("Panggil Antrian: " + target.getNomorAntrianFormatted() + "\nNama: " + target.getNama())
                .setPositiveButton("OK", null)
                .show();
        } else {
            Toast.makeText(this, "Tidak ada pasien menunggu", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView() {
        daftarPasien = new ArrayList<>();
        pasienAdapter = new PasienAdapter(this, daftarPasien);
        rvAntrian.setLayoutManager(new LinearLayoutManager(this));
        rvAntrian.setAdapter(pasienAdapter);

        // klik item untuk detail dan ubah status
        pasienAdapter.setOnItemClickListener((pasien, position) -> showDetailPasien(pasien));
        
        // long click untuk hapus data
        pasienAdapter.setOnItemLongClickListener((pasien, position) -> {
            showDeleteConfirmDialog(pasien);
            return true;
        });
    }

    private void showDeleteConfirmDialog(Pasien p) {
        new AlertDialog.Builder(this)
            .setTitle("Hapus Data")
            .setMessage("Hapus antrian " + p.getNama() + "?")
            .setPositiveButton("Hapus", (d, w) -> {
                databaseHelper.deletePasien(p.getId());
                loadDataAntrian();
                Toast.makeText(this, "Data terhapus", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Batal", null)
            .show();
    }

    private void loadDataAntrian() {
        daftarPasien = databaseHelper.getAllPasienHariIni();
        pasienAdapter.updateData(daftarPasien);

        // update info nomor antrian & counter
        int nextNo = databaseHelper.getNomorAntrianBerikutnya();
        tvNomorAntrian.setText(nextNo == 0 ? "-" : String.format("%03d", nextNo));
        tvJumlahMenunggu.setText(databaseHelper.getJumlahMenunggu() + " pasien menunggu");

        tvEmpty.setVisibility(daftarPasien.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /**
     * Dialog detail pasien versi modern.
     */
    private void showDetailPasien(Pasien p) {
        PatientDialogHelper.showDetail(this, p, true, this::showUpdateStatusDialog);
    }

    /**
     * Dialog pilihan status antrian baru.
     */
    private void showUpdateStatusDialog(Pasien p) {
        String[] options = {
            DatabaseHelper.STATUS_MENUNGGU,
            DatabaseHelper.STATUS_DILAYANI,
            DatabaseHelper.STATUS_SELESAI,
            DatabaseHelper.STATUS_DIBATALKAN
        };

        new AlertDialog.Builder(this)
            .setTitle("Ubah Status")
            .setItems(options, (dialog, which) -> {
                databaseHelper.updateStatusPasien(p.getId(), options[which]);
                loadDataAntrian();
                Toast.makeText(this, "Status diperbarui", Toast.LENGTH_SHORT).show();
            })
            .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDataAntrian();
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_dark_mode) {
            toggleDarkMode();
            return true;
        }
        if (id == R.id.action_reset) {
            showResetConfirmDialog();
            return true;
        }
        if (id == R.id.action_logout) {
            showLogoutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showResetConfirmDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Reset Antrian")
            .setMessage("Selesaikan semua antrian aktif hari ini? Data tidak akan dihapus.")
            .setPositiveButton("Reset", (d, w) -> {
                databaseHelper.resetAntrianHariIni();
                loadDataAntrian();
                Toast.makeText(this, "Antrian hari ini di-reset", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Batal", null)
            .show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Apakah Anda yakin ingin keluar?")
            .setPositiveButton("Ya", (dialog, which) -> {
                sessionManager.logout();
                startActivity(new Intent(this, LoginActivity.class));
                finishAffinity();
            })
            .setNegativeButton("Batal", null)
            .show();
    }

    private void toggleDarkMode() {
        boolean isDark = themePrefs.getBoolean("isDark", false);
        SharedPreferences.Editor editor = themePrefs.edit();
        editor.putBoolean("isDark", !isDark);
        editor.apply();
        
        // panggil ulang biar temanya ganti
        AppCompatDelegate.setDefaultNightMode(
            !isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}
