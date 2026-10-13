package model.dao

import db
import model.entity.Etudiant
import org.example.jdbc.BDD
import java.sql.PreparedStatement
import java.sql.SQLException
import java.sql.Statement

class EtudiantDAO(val bdd: BDD = db) {

    fun findAll(): MutableList<Etudiant> {
        val result = mutableListOf<Etudiant>()

        val sql = "SELECT * FROM etudiant"
        val requetePreparer = bdd.connectionBDD!!.prepareStatement(sql)

        try {
            val resultatRequete =
                bdd.executePreparedStatement(requetePreparer)

            if (resultatRequete != null) {
                while (resultatRequete.next()) {
                    val id = resultatRequete.getInt("id")
                    val nom = resultatRequete.getString("nom")
                    val prenom = resultatRequete.getString("prenom")
                    val email = resultatRequete.getString("email")
                    val dateNaissance =
                        resultatRequete.getDate("date_naissance")

                    result.add(
                        Etudiant(
                            id,
                            nom,
                            prenom,
                            email,
                            dateNaissance.toLocalDate()
                        )
                    )
                }

                resultatRequete.close()
            }
        } catch (erreur: SQLException) {
            println("Erreur lors de la récupération des étudiants : ${erreur.message}")
        } finally {
            requetePreparer.close()
        }

        return result
    }

    fun findById(id: Int): Etudiant? {
        val sql = "SELECT * FROM etudiant WHERE id = ?"
        val requetePreparer = bdd.connectionBDD!!.prepareStatement(sql)

        try {
            requetePreparer.setInt(1, id)

            val resultatRequete =
                bdd.executePreparedStatement(requetePreparer)

            if (resultatRequete != null && resultatRequete.next()) {
                val etudiant = Etudiant(
                    resultatRequete.getInt("id"),
                    resultatRequete.getString("nom"),
                    resultatRequete.getString("prenom"),
                    resultatRequete.getString("email"),
                    resultatRequete.getDate("date_naissance").toLocalDate()
                )

                resultatRequete.close()
                return etudiant
            }

            resultatRequete?.close()
        } catch (erreur: SQLException) {
            println("Erreur lors de la recherche de l'étudiant : ${erreur.message}")
        } finally {
            requetePreparer.close()
        }

        return null
    }

    fun deleteById(id: Int): Boolean {
        val sql = "DELETE FROM etudiant WHERE id = ?"
        val requetePreparer = bdd.connectionBDD!!.prepareStatement(sql)

        return try {
            requetePreparer.setInt(1, id)

            val nbLigneMaj = requetePreparer.executeUpdate()

            nbLigneMaj > 0
        } catch (erreur: SQLException) {
            println("Erreur lors de la suppression de l'étudiant : ${erreur.message}")
            false
        } finally {
            requetePreparer.close()
        }
    }

    fun save(etudiant: Etudiant): Etudiant? {
        val requetePreparer: PreparedStatement

        try {
            if (etudiant.id == null) {
                // Insertion
                val sql = """
                    INSERT INTO etudiant (nom, prenom, email, date_naissance)
                    VALUES (?, ?, ?, ?)
                """.trimIndent()

                requetePreparer = bdd.connectionBDD!!.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
                )

                requetePreparer.setString(1, etudiant.nom)
                requetePreparer.setString(2, etudiant.prenom)
                requetePreparer.setString(3, etudiant.email)
                requetePreparer.setDate(
                    4,
                    java.sql.Date.valueOf(etudiant.dateNaissance)
                )

                val nbLigneMaj = requetePreparer.executeUpdate()

                if (nbLigneMaj > 0) {
                    val generatedKeys = requetePreparer.generatedKeys

                    if (generatedKeys.next()) {
                        etudiant.id = generatedKeys.getInt(1)
                    }

                    generatedKeys.close()
                    requetePreparer.close()

                    return etudiant
                }

                requetePreparer.close()
                return null

            } else {
                // Mise à jour
                val sql = """
                    UPDATE etudiant
                    SET nom = ?, prenom = ?, email = ?, date_naissance = ?
                    WHERE id = ?
                """.trimIndent()

                requetePreparer =
                    bdd.connectionBDD!!.prepareStatement(sql)

                requetePreparer.setString(1, etudiant.nom)
                requetePreparer.setString(2, etudiant.prenom)
                requetePreparer.setString(3, etudiant.email)
                requetePreparer.setDate(
                    4,
                    java.sql.Date.valueOf(etudiant.dateNaissance)
                )
                requetePreparer.setInt(5, etudiant.id!!)

                val nbLigneMaj = requetePreparer.executeUpdate()

                requetePreparer.close()

                return if (nbLigneMaj > 0) etudiant else null
            }
        } catch (erreur: SQLException) {
            println("Erreur lors de l'enregistrement de l'étudiant : ${erreur.message}")
            return null
        }
    }
}