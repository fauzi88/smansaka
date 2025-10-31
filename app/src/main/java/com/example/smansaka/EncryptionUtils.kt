package com.example.smansaka

import android.util.Base64
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object EncryptionUtils {

    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES"
    // IMPORTANT: This is not a secure way to store a key.
    // For a production app, use the Android Keystore system.
    private const val SECRET_KEY = "SmansakaJaya2024" // Must be 16 bytes for AES-128

    private fun getKey(): Key {
        // Ensure the key is exactly 16 bytes
        val keyBytes = SECRET_KEY.toByteArray().copyOf(16)
        return SecretKeySpec(keyBytes, ALGORITHM)
    }

    fun encrypt(data: String): String? {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getKey())
            val encryptedBytes = cipher.doFinal(data.toByteArray())
            Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun decrypt(encryptedData: String): String? {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getKey())
            val decodedBytes = Base64.decode(encryptedData, Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            String(decryptedBytes)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
