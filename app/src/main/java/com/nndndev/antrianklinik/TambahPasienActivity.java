package com.nndndev.antrianklinik;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * TambahPasienActivity — Form pendaftaran pasien baru ke antrian.
 */
public class TambahPasienActivity extends AppCompatActivity {

    private EditText etNama, etUmur, etKeluhan;
    private RadioGroup rgJenisKelamin;
    private RadioButton rbLaki, rbPerempuan;
    private Spinner spinnerPoli;
    private Button btnDaftar, btnReset;

    private DatabaseHelper databaseHelper;

    private static final String[] DAFTAR_POLI = {
        "Poli Umum", "Poli Gigi", "Poli Anak", "Poli Kandungan", "Poli Mata"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_pasien);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Daftarkan Pasien");
        }

        databaseHelper = new DatabaseHelper(this);
        bindViews();
        setupSpinnerPoli();

        btnDaftar.setOnClickListener(v -> handleDaftarPasien());
        btnReset.setOnClickListener(v -> resetForm());
    }

    private void bindViews() {
        etNama          = findViewById(R.id.et_nama);
        etUmur          = findViewById(R.id.et_umur);
        etKeluhan       = findViewById(R.id.et_keluhan);
        rgJenisKelamin  = findViewById(R.id.rg_jenis_kelamin);
        rbLaki          = findViewById(R.id.rb_laki);
        rbPerempuan     = findViewById(R.id.rb_perempuan);
        spinnerPoli     = findViewById(R.id.spinner_poli);
        btnDaftar       = findViewById(R.id.btn_daftar);
        btnReset        = findViewById(R.id.btn_reset);
    }

    private void setupSpinnerPoli() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, DAFTAR_POLI);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPoli.setAdapter(adapter);
    }

    private void handleDaftarPasien() {
        String nama    = etNama.getText().toString().trim();
        String umur    = etUmur.getText().toString().trim();
        String keluhan = etKeluhan.getText().toString().trim();
        String poli    = spinnerPoli.getSelectedItem().toString();

        // validasi input
        if (nama.length() < 3) {
            etNama.setError("Nama minimal 3 karakter");
            etNama.requestFocus();
            return;
        }
        
        int umurInt;
        try {
            umurInt = Integer.parseInt(umur);
        } catch (NumberFormatException e) {
            etUmur.setError("Umur harus berupa angka");
            etUmur.requestFocus();
            return;
        }

        if (rgJenisKelamin.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Pilih jenis kelamin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (keluhan.isEmpty()) {
            etKeluhan.setError("Keluhan tidak boleh kosong");
            etKeluhan.requestFocus();
            return;
        }

        String jenisKelamin = rbLaki.isChecked() ? "Laki-laki" : "Perempuan";
        String tgl = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Pasien p = new Pasien();
        p.setNama(nama);
        p.setUmur(umurInt);
        p.setJenisKelamin(jenisKelamin);
        p.setPoli(poli);
        p.setKeluhan(keluhan);
        p.setTanggalDaftar(tgl);
        p.setStatus(DatabaseHelper.STATUS_MENUNGGU);

        long id = databaseHelper.tambahPasien(p);
        if (id > 0) {
            showSuccessDialog(p.getNomorAntrianFormatted());
        } else {
            Toast.makeText(this, "Gagal mendaftarkan pasien", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSuccessDialog(String noAntrian) {
        new AlertDialog.Builder(this)
            .setTitle("Pendaftaran Berhasil")
            .setMessage("Nomor Antrian: " + noAntrian)
            .setCancelable(false)
            .setPositiveButton("OK", (d, w) -> finish())
            .show();
    }

    private void resetForm() {
        etNama.setText("");
        etUmur.setText("");
        etKeluhan.setText("");
        rgJenisKelamin.clearCheck();
        spinnerPoli.setSelection(0);
        etNama.requestFocus();
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
