package com.whitefang.stepsofbabylon.data.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Manages the Room database encryption passphrase.
 * Generates a random passphrase on first run, encrypts it with an
 * Android Keystore key, and stores the encrypted blob in SharedPreferences.
 */
object DatabaseKeyManager {

    private const val KEYSTORE_ALIAS = "steps_of_babylon_db_key"
    private const val PREFS_NAME = "db_key_prefs"
    private const val PREF_ENCRYPTED = "encrypted_passphrase"
    private const val PREF_IV = "passphrase_iv"

    fun getPassphrase(context: Context): ByteArray {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getString(PREF_ENCRYPTED, null)
        return if (existing != null) {
            decrypt(
                android.util.Base64.decode(existing, android.util.Base64.NO_WRAP),
                android.util.Base64.decode(prefs.getString(PREF_IV, "")!!, android.util.Base64.NO_WRAP),
            )
        } else {
            val passphrase = generateRandomPassphrase()
            val (encrypted, iv) = encrypt(passphrase)
            prefs.edit()
                .putString(PREF_ENCRYPTED, android.util.Base64.encodeToString(encrypted, android.util.Base64.NO_WRAP))
                .putString(PREF_IV, android.util.Base64.encodeToString(iv, android.util.Base64.NO_WRAP))
                .apply()
            passphrase
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
