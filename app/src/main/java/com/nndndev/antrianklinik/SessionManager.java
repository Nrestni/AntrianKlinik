package com.nndndev.antrianklinik;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * SessionManager — Mengelola sesi login pengguna menggunakan SharedPreferences.
 *
 * Menyimpan:
 * - Status login (boolean)
 * - Username yang sedang aktif
 * - Role pengguna (admin / dokter / perawat)
 * - Waktu login terakhir
 *
 * TODO (implementasi nanti):
 * - Tambahkan enkripsi data sensitif (EncryptedSharedPreferences)
 * - Implementasi auto-logout setelah durasi tertentu
 * - Simpan token autentikasi jika menggunakan backend/API
 */
public class SessionManager {

    // ─── SharedPreferences Config ────────────────────────────────────────────
    private static final String PREF_NAME       = "AntrianklinikSession";
    private static final int    PREF_MODE       = Context.MODE_PRIVATE;

    // ─── Keys ────────────────────────────────────────────────────────────────
    private static final String KEY_IS_LOGGED_IN  = "isLoggedIn";
    private static final String KEY_USERNAME       = "username";
    private static final String KEY_ROLE           = "role";
    private static final String KEY_LOGIN_TIME     = "loginTime";

    // ─── Role Constants ──────────────────────────────────────────────────────
    public static final String ROLE_ADMIN   = "admin";
    public static final String ROLE_DOKTER  = "dokter";
    public static final String ROLE_PERAWAT = "perawat";

    // ─── Fields ─────────────────────────────────────────────────────────────
    private final SharedPreferences         prefs;
    private final SharedPreferences.Editor  editor;
    private final Context                   context;

    public SessionManager(Context context) {
        this.context = context;
        prefs  = context.getSharedPreferences(PREF_NAME, PREF_MODE);
        editor = prefs.edit();
    }

    // ─── Session Management ──────────────────────────────────────────────────

    /**
     * Simpan sesi login setelah autentikasi berhasil.
     *
     * @param username username yang berhasil login
     * @param role     role pengguna (admin/dokter/perawat)
     */
    public void createLoginSession(String username, String role) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_ROLE, role);
        editor.putLong(KEY_LOGIN_TIME, System.currentTimeMillis());
        editor.apply();
    }

    /**
     * Overload: login session tanpa role (default: admin).
     */
    public void createLoginSession(String username) {
        createLoginSession(username, ROLE_ADMIN);
    }

    /**
     * Hapus semua data sesi (logout).
     * Redirect ke LoginActivity jika context tersedia.
     */
    public void logout() {
        editor.clear();
        editor.apply();

        // TODO: Opsional — langsung redirect dari sini
        // Intent intent = new Intent(context, LoginActivity.class);
        // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        // context.startActivity(intent);
    }

    // ─── Getters ────────────────────────────────────────────────────────────

    /** @return true jika pengguna sudah login */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /** @return username yang sedang aktif, atau null jika belum login */
    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }

    /** @return role pengguna yang sedang aktif */
    public String getRole() {
        return prefs.getString(KEY_ROLE, ROLE_ADMIN);
    }

    /** @return timestamp login dalam milidetik, atau 0 jika belum login */
    public long getLoginTime() {
        return prefs.getLong(KEY_LOGIN_TIME, 0);
    }

    // ─── Role Helpers ────────────────────────────────────────────────────────

    public boolean isAdmin() {
        return ROLE_ADMIN.equals(getRole());
    }

    public boolean isDokter() {
        return ROLE_DOKTER.equals(getRole());
    }

    /**
     * Cek apakah sesi sudah kedaluwarsa.
     * TODO: Implementasi jika diperlukan auto-logout.
     *
     * @param maxDurationMs durasi maksimum sesi dalam milidetik
     * @return true jika sesi sudah melebihi durasi yang ditentukan
     */
    public boolean isSessionExpired(long maxDurationMs) {
        long loginTime = getLoginTime();
        if (loginTime == 0) return true;
        return (System.currentTimeMillis() - loginTime) > maxDurationMs;
    }
}
