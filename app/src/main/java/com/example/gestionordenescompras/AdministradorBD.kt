package com.example.gestionordenescompras

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AdministradorBD(context: Context) : SQLiteOpenHelper (
    context,
    "gestionOrdenesCompras.db",
    null,
    2
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
                CREATE TABLE clientes(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                correo TEXT NOT NULL,
                telefono TEXT NOT NULL,
                direccion TEXT NOT NULL
                )
            """.trimIndent()
        )
        db.execSQL(
            """
                CREATE TABLE productos(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombreProducto TEXT NOT NULL,
                descripcion TEXT NOT NULL,
                precio REAL NOT NULL
                )
            """.trimIndent()
        )
        db.execSQL(
            """
                CREATE TABLE ordenes(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                idCliente INTEGER,
                fecha TEXT NOT NULL,
                estado TEXT NOT NULL,
                total REAL NOT NULL,
                FOREIGN KEY(idCliente) REFERENCES clientes(id)
                )
            """.trimIndent()
        )
        db.execSQL(
            """
                CREATE TABLE detalleOrden(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                idOrden INTEGER,
                idProducto INTEGER,
                cantidad INTEGER NOT NULL,
                FOREIGN KEY(idOrden) REFERENCES ordenes(id),
                FOREIGN KEY(idProducto) REFERENCES productos(id)
                )
            """.trimIndent()
        )
    }
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        // Esto activa las llaves foraneas que defini
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        db.execSQL("DROP TABLE IF EXISTS clientes")
        db.execSQL("DROP TABLE IF EXISTS productos")
        db.execSQL("DROP TABLE IF EXISTS ordenes")
        db.execSQL("DROP TABLE IF EXISTS detalleOrden")
        onCreate(db)
    }
}