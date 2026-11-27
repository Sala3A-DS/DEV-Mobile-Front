package com.example.agend.gemini

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal class GeminiKeyStore(context: Context) {
    private val arquivo = AtomicFile(File(context.noBackupFilesDir, "gemini-api-key"))

    fun temChave(): Boolean = arquivo.baseFile.exists()

    suspend fun salvar(chave: String) = withContext(Dispatchers.IO) {
        val keystore = keystore()
        val secretKey = (keystore.getKey(ALIAS, null) as? SecretKey) ?: KeyGenerator
            .getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER)
            .apply {
                init(
                    KeyGenParameterSpec.Builder(
                        ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    ).setKeySize(256)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .build()
                )
            }.generateKey()

        val cipher = Cipher.getInstance(CRYPTO)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val dados = cipher.iv + cipher.doFinal(chave.toByteArray(Charsets.UTF_8))
        val saida = arquivo.startWrite()
        try {
            saida.write(dados)
            arquivo.finishWrite(saida)
        } catch (e: Exception) {
            arquivo.failWrite(saida)
            throw e
        }
    }

    suspend fun ler(): String? = withContext(Dispatchers.IO) {
        if (!temChave()) return@withContext null
        val dados = arquivo.readFully()
        check(dados.size > IV_BYTES)
        val secretKey = checkNotNull(keystore().getKey(ALIAS, null) as? SecretKey)
        val cipher = Cipher.getInstance(CRYPTO)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, dados, 0, IV_BYTES))
        cipher.doFinal(dados, IV_BYTES, dados.size - IV_BYTES).toString(Charsets.UTF_8)
    }

    suspend fun remover() = withContext(Dispatchers.IO) {
        keystore().deleteEntry(ALIAS)
        arquivo.delete()
        check(!temChave())
    }

    private fun keystore() = KeyStore.getInstance(PROVIDER).apply { load(null) }

    companion object {
        private const val ALIAS = "yarooms.gemini.api"
        private const val PROVIDER = "AndroidKeyStore"
        private const val CRYPTO = "AES/GCM/NoPadding"
        private const val IV_BYTES = 12
    }
}
