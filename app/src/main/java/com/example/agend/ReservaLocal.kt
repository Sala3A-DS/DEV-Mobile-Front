package com.example.agend

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.agend.auth.ReservaResponse
import com.google.gson.Gson

class ReservaLocal(context: Context) : SQLiteOpenHelper(context, "reservas.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE consultas (conta TEXT PRIMARY KEY, reservas TEXT NOT NULL)")
    }
    override fun onUpgrade(db: SQLiteDatabase, antiga: Int, nova: Int) = Unit

    fun salvar(conta: String, reservas: List<ReservaResponse>) {
        if (conta.isBlank()) return
        val dados = ContentValues().apply {
            put("conta", conta)
            put("reservas", Gson().toJson(reservas))
        }
        writableDatabase.replaceOrThrow("consultas", null, dados)
    }
    fun ler(conta: String): List<ReservaResponse>? =
        readableDatabase.rawQuery("SELECT reservas FROM consultas WHERE conta = ?", arrayOf(conta)).use {
            if (it.moveToFirst()) Gson().fromJson(it.getString(0), Array<ReservaResponse>::class.java).toList() else null
        }
}
