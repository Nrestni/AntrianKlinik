package com.nndndev.antrianklinik;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

/**
 * RiwayatActivity — Menampilkan riwayat antrian pasien dengan filter dan pencarian.
 */
public class RiwayatActivity extends AppCompatActivity {

    private RecyclerView rvRiwayat;
    private Spinner spinnerFilter, spinnerWaktu;
    private TextView tvTotalRiwayat, tvEmpty;
    private SearchView searchView;
    private BottomNavigationView bottomNavigationView;

    private PasienAdapter pasienAdapter;
    private List<Pasien> daftarRiwayat;
    private List<Pasien> daftarRiwayatFiltered;

    private DatabaseHelper databaseHelper;

    private static final String[] FILTER_STATUS = {
        "Semua Status", "Menunggu", "Sedang Dilayani", "Selesai", "Dibatalkan"
    };

    private static final String[] FILTER_WAKTU = {
        "Semua Waktu", "Hari Ini", "Bulan Ini", "Tahun Ini"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riwayat);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Riwayat Antrian");
        }

        databaseHelper = new DatabaseHelper(this);
        daftarRiwayatFiltered = new ArrayList<>();

        bindViews();
        setupFilterSpinner();
        setupWaktuSpinner(); // Inisialisasi spinner waktu
        setupRecyclerView();
        setupSearchView();
        setupBottomNav();
        
        loadRiwayat();
    }

    private void bindViews() {
        rvRiwayat       = findViewById(R.id.rv_riwayat);
        spinnerFilter   = findViewById(R.id.spinner_filter);
        spinnerWaktu    = findViewById(R.id.spinner_waktu); // Spinner waktu baru
        tvTotalRiwayat  = findViewById(R.id.tv_total_riwayat);
        tvEmpty         = findViewById(R.id.tv_empty);
        searchView      = findViewById(R.id.search_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setupBottomNav() {
        bottomNavigationView.setSelectedItemId(R.id.nav_history);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                finish();
                overridePendingTransition(0, 0);
                return true;
            }
            return true;
        });
    }

    private void setupFilterSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, FILTER_STATUS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(adapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadRiwayat();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // Setup adapter buat filter waktu
    private void setupWaktuSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, FILTER_WAKTU);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWaktu.setAdapter(adapter);

        spinnerWaktu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadRiwayat();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupRecyclerView() {
        pasienAdapter = new PasienAdapter(this, daftarRiwayatFiltered);
        rvRiwayat.setLayoutManager(new LinearLayoutManager(this));
        rvRiwayat.setAdapter(pasienAdapter);

        // Pake helper dialog biar cakep desainnya
        pasienAdapter.setOnItemClickListener((pasien, position) -> showDetailPasien(pasien));

        // long click untuk hapus
        pasienAdapter.setOnItemLongClickListener((pasien, position) -> {
            showDeleteConfirmDialog(pasien);
            return true;
        });
    }

    private void showDeleteConfirmDialog(Pasien p) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Hapus Riwayat")
            .setMessage("Hapus data " + p.getNama() + " dari riwayat?")
            .setPositiveButton("Hapus", (d, w) -> {
                databaseHelper.deletePasien(p.getId());
                loadRiwayat();
                android.widget.Toast.makeText(this, "Data dihapus", android.widget.Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Batal", null)
            .show();
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterByName(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterByName(newText);
                return true;
            }
        });
    }

    /**
     * Load data riwayat pake filter status dan waktu sekaligus.
     */
    private void loadRiwayat() {
        String status = spinnerFilter.getSelectedItem().toString();
        String waktu = spinnerWaktu.getSelectedItem().toString();
        
        // "Semua Waktu" dianggep null sama DatabaseHelper
        String waktuArg = "Semua Waktu".equals(waktu) ? null : waktu;
        
        daftarRiwayat = databaseHelper.getPasienFiltered(status, waktuArg);
        
        // Reset kolom pencarian tiap ganti filter biar nggak bingung
        searchView.setQuery("", false);
        filterByName("");
    }

    /**
     * Filter list berdasarkan input nama (real-time).
     */
    private void filterByName(String query) {
        daftarRiwayatFiltered.clear();
        if (query.isEmpty()) {
            daftarRiwayatFiltered.addAll(daftarRiwayat);
        } else {
            String q = query.toLowerCase();
            for (Pasien p : daftarRiwayat) {
                if (p.getNama().toLowerCase().contains(q)) {
                    daftarRiwayatFiltered.add(p);
                }
            }
        }
        
        pasienAdapter.updateData(daftarRiwayatFiltered);
        tvTotalRiwayat.setText("Total: " + daftarRiwayatFiltered.size() + " data");
        
        // Update pesan empty state biar user tau kenapa listnya kosong
        if (daftarRiwayat.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Belum ada data riwayat pasien");
        } else if (daftarRiwayatFiltered.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Pasien tidak ditemukan");
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
    }

    // Nampilin detail pasien pake desain Material 3 yang baru
    private void showDetailPasien(Pasien p) {
        PatientDialogHelper.showDetail(this, p, false, null);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_riwayat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
