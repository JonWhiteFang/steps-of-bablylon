package com.whitefang.stepsofbabylon.data.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Manages the Room database encryption passphrase.
 * Generates a random passphrase on first run, encrypts it with an
 * Android Keystore key, and stores the encrypted blob in SharedPreferences.
 * On decryption failure (e.g. restore to new device), wipes stale data
 * and generates a fresh passphrase.
 */
object DatabaseKeyManager {

    private const val TAG = "DatabaseKeyManager"
    private const val KEYSTORE_ALIAS = "steps_of_babylon_db_key"
    private const val PREFS_NAME = "db_key_prefs"
    private const val PREF_ENCRYPTED = "encrypted_passphrase"
    private const val PREF_IV = "passphrase_iv"
    // Must match DatabaseModule.provideDatabase() name — when the passphrase is
    // lost (e.g. backup-restore to a new device), the encrypted DB file on disk
    // cannot be opened with a freshly-generated passphrase, so we delete it
    // here alongside the stale key blob and let Room rebuild from scratch.
    private const val DB_FILENAME = "steps_of_babylon.db"

    fun getPassphrase(context: Context): ByteArray {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getString(PREF_ENCRYPTED, null)
        if (existing != null) {
            try {
                return decrypt(
                    android.util.Base64.decode(existing, android.util.Base64.NO_WRAP),
                    android.util.Base64.decode(prefs.getString(PREF_IV, "")!!, android.util.Base64.NO_WRAP),
                )
            } catch (e: Exception) {
                // Keystore key missing or mismatched (e.g. device restore) — reset.
                // The existing DB file is encrypted with the now-lost passphrase
                // and cannot be recovered; delete it so Room can rebuild cleanly
                // instead of crashing in a loop on next open.
                Log.w(TAG, "Passphrase decryption failed, wiping stale key and DB file", e)
                prefs.edit().clear().apply()
                wipeDatabaseFile(context)
            }
        }
        val passphrase = generateRandomPassphrase()
        val (encrypted, iv) = encrypt(passphrase)
        prefs.edit()
            .putString(PREF_ENCRYPTED, android.util.Base64.encodeToString(encrypted, android.util.Base64.NO_WRAP))
            .putString(PREF_IV, android.util.Base64.encodeToString(iv, android.util.Base64.NO_WRAP))
            .apply()
        return passphrase
    }

    /**
     * Deletes the SQLCipher database file and its companion -shm/-wal files.
     * Called when the passphrase blob is no longer decryptable — the on-disk DB
     * is unreadable without the original passphrase, so wiping it prevents a
     * crash-on-launch loop at the cost of local progress. Progress is already
     * unrecoverable at this point.
     *
     * Visible for testing: Robolectric cannot provide a working AndroidKeyStore
     * shadow, so the full decrypt-fail → wipe path is verified via direct test
     * on this method. Keystore integration is verified by the single call site
     * in [getPassphrase] plus on-device smoke.
     */
    internal fun wipeDatabaseFile(context: Context) {
        val dbFile = context.getDatabasePath(DB_FILENAME)
        listOf(dbFile, java.io.File(dbFile.path + "-shm"), java.io.File(dbFile.path + "-wal"))
            .filter { it.exists() }
            .forEach { file ->
                if (!file.delete()) {
                    Log.w(TAG, "Failed to delete stale DB file: ${file.path}")
                }
            }
    }

    private fun generateRandomPassphrase(): ByteArray {
        val bytes = ByteArray(32)
        java.security.SecureRandom().nextBytes(bytes)
        return bytes
    }

    private fun getOrCreateKeystoreKey(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        ks.getEntry(KEYSTORE_ALIAS, null)?.let {
            return (it as KeyStore.SecretKeyEntry).secretKey
        }
        val spec = KeyGenParameterSpec.Builder(KEYSTORE_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
            init(spec)
            generateKey()
        }
    }

    private fun encrypt(data: ByteArray): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKeystoreKey())
        return cipher.doFinal(data) to cipher.iv
    }

    private fun decrypt(data: ByteArray, iv: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKeystoreKey(), GCMParameterSpec(128, iv))
        return cipher.doFinal(data)
    }
}
