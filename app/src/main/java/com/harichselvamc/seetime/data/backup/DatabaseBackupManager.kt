package com.harichselvamc.seetime.data.backup

import android.content.Context
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.security.GeneralSecurityException
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Zero-Cloud Encrypted Local Backup Manager.
 * Uses AES-GCM-256 with PBKDF2 key derivation (10,000 iterations) to securely
 * export and import SeeTime Room databases without cloud exposure.
 */
object DatabaseBackupManager {

    private val MAGIC_HEADER = byteArrayOf('S'.code.toByte(), 'T'.code.toByte(), 'B'.code.toByte(), 'K'.code.toByte(), 0x01)
    private const val SALT_LENGTH_BYTES = 16
    private const val IV_LENGTH_BYTES = 12
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val PBKDF2_ITERATIONS = 10000
    private const val KEY_LENGTH_BITS = 256

    /**
     * Encrypts raw database bytes using AES-GCM-256 with a password-derived key.
     * Output format: [5-byte MAGIC][16-byte Salt][12-byte IV][Ciphertext + 16-byte GCM Tag]
     */
    fun encryptData(plainBytes: ByteArray, password: CharArray): ByteArray {
        val secureRandom = SecureRandom()
        val salt = ByteArray(SALT_LENGTH_BYTES).also { secureRandom.nextBytes(it) }
        val iv = ByteArray(IV_LENGTH_BYTES).also { secureRandom.nextBytes(it) }

        val keySpec = PBEKeySpec(password, salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
        val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = secretKeyFactory.generateSecret(keySpec).encoded
        val secretKey = SecretKeySpec(keyBytes, "AES")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
        cipher.updateAAD(MAGIC_HEADER)
        val cipherText = cipher.doFinal(plainBytes)

        val output = ByteArrayOutputStream()
        output.write(MAGIC_HEADER)
        output.write(salt)
        output.write(iv)
        output.write(cipherText)
        return output.toByteArray()
    }

    /**
     * Decrypts AES-GCM-256 encrypted bytes.
     * Throws GeneralSecurityException or IllegalArgumentException on invalid magic, wrong password, or tampered payload.
     */
    fun decryptData(encryptedBytes: ByteArray, password: CharArray): ByteArray {
        if (encryptedBytes.size < MAGIC_HEADER.size + SALT_LENGTH_BYTES + IV_LENGTH_BYTES + (GCM_TAG_LENGTH_BITS / 8)) {
            throw IllegalArgumentException("Backup file is too small or corrupted.")
        }

        val byteBuffer = ByteBuffer.wrap(encryptedBytes)

        // 1. Verify Magic Header
        val header = ByteArray(MAGIC_HEADER.size)
        byteBuffer.get(header)
        if (!header.contentEquals(MAGIC_HEADER)) {
            throw IllegalArgumentException("Invalid backup format or unsupported version.")
        }

        // 2. Extract Salt and IV
        val salt = ByteArray(SALT_LENGTH_BYTES)
        byteBuffer.get(salt)

        val iv = ByteArray(IV_LENGTH_BYTES)
        byteBuffer.get(iv)

        // 3. Extract Ciphertext + GCM Tag
        val cipherText = ByteArray(byteBuffer.remaining())
        byteBuffer.get(cipherText)

        // 4. Derive key
        val keySpec = PBEKeySpec(password, salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
        val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = secretKeyFactory.generateSecret(keySpec).encoded
        val secretKey = SecretKeySpec(keyBytes, "AES")

        // 5. Decrypt
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
        cipher.updateAAD(MAGIC_HEADER)
        return cipher.doFinal(cipherText)
    }

    /**
     * Exports the local Room database to an encrypted output stream.
     */
    fun exportDatabase(context: Context, dbName: String = "seetime_db", password: CharArray, destination: OutputStream) {
        val dbFile = context.getDatabasePath(dbName)
        if (!dbFile.exists()) {
            throw IllegalStateException("Database file $dbName does not exist.")
        }

        val plainBytes = FileInputStream(dbFile).use { it.readBytes() }
        val encrypted = encryptData(plainBytes, password)
        destination.write(encrypted)
        destination.flush()
    }

    /**
     * Restores the local Room database from an encrypted input stream after validating password and decryption.
     */
    fun restoreDatabase(context: Context, inputStream: InputStream, password: CharArray, dbName: String = "seetime_db") {
        val encryptedBytes = inputStream.readBytes()
        val decryptedBytes = decryptData(encryptedBytes, password)

        val dbFile = context.getDatabasePath(dbName)
        dbFile.parentFile?.mkdirs()

        FileOutputStream(dbFile).use { output ->
            output.write(decryptedBytes)
            output.flush()
        }
    }
}
