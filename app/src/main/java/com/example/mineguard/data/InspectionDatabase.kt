package com.example.mineguard.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.mineguard.model.Inspection

class InspectionDatabase(context: Context) :
    SQLiteOpenHelper(context, "mineguard.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE inspections (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                mineName TEXT NOT NULL,
                inspectorName TEXT NOT NULL,
                category TEXT NOT NULL,
                notes TEXT NOT NULL,
                riskScore INTEGER NOT NULL,
                riskLevel TEXT NOT NULL,
                status TEXT NOT NULL,
                createdAt TEXT NOT NULL
            )
            """.trimIndent()
        )
        insertSample(db, "Coal Mine Alpha", "Inspector A", "Ventilation",
            "Ventilation readings require a follow-up check.", 82, "High", "Open", "05 Nov 2026")
        insertSample(db, "Eastern Block Mine", "Inspector B", "PPE",
            "Two workers were recorded without complete protective equipment.", 58, "Medium", "Open", "04 Nov 2026")
        insertSample(db, "North Ridge Mine", "Inspector C", "Electrical",
            "No major issue recorded during the latest inspection.", 18, "Low", "Closed", "03 Nov 2026")
    }

    private fun insertSample(
        db: SQLiteDatabase, mine: String, inspector: String, category: String,
        notes: String, score: Int, level: String, status: String, date: String
    ) {
        val values = ContentValues().apply {
            put("mineName", mine); put("inspectorName", inspector)
            put("category", category); put("notes", notes)
            put("riskScore", score); put("riskLevel", level)
            put("status", status); put("createdAt", date)
        }
        db.insert("inspections", null, values)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS inspections")
        onCreate(db)
    }

    fun addInspection(item: Inspection): Long {
        val values = ContentValues().apply {
            put("mineName", item.mineName)
            put("inspectorName", item.inspectorName)
            put("category", item.category)
            put("notes", item.notes)
            put("riskScore", item.riskScore)
            put("riskLevel", item.riskLevel)
            put("status", item.status)
            put("createdAt", item.createdAt)
        }
        return writableDatabase.insert("inspections", null, values)
    }

    fun getInspections(): List<Inspection> {
        val result = mutableListOf<Inspection>()
        readableDatabase.rawQuery(
            "SELECT * FROM inspections ORDER BY id DESC", null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result.add(
                    Inspection(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        mineName = cursor.getString(cursor.getColumnIndexOrThrow("mineName")),
                        inspectorName = cursor.getString(cursor.getColumnIndexOrThrow("inspectorName")),
                        category = cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        notes = cursor.getString(cursor.getColumnIndexOrThrow("notes")),
                        riskScore = cursor.getInt(cursor.getColumnIndexOrThrow("riskScore")),
                        riskLevel = cursor.getString(cursor.getColumnIndexOrThrow("riskLevel")),
                        status = cursor.getString(cursor.getColumnIndexOrThrow("status")),
                        createdAt = cursor.getString(cursor.getColumnIndexOrThrow("createdAt"))
                    )
                )
            }
        }
        return result
    }

    fun updateStatus(id: Long, status: String): Int {
        val values = ContentValues().apply { put("status", status) }
        return writableDatabase.update("inspections", values, "id=?", arrayOf(id.toString()))
    }
}
