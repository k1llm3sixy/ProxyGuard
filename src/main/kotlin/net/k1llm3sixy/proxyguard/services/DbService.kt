package net.k1llm3sixy.proxyguard.services

import net.k1llm3sixy.proxyguard.error.GuardError
import net.k1llm3sixy.proxyguard.error.safeCall
import net.k1llm3sixy.proxyguard.io.Storage
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

private enum class Statement(val sql: String)
{
    INSERT_USER("INSERT INTO users (uuid, nick, ip) VALUES (?, ?, ?)"),
    SELECT_USER("SELECT EXISTS(SELECT 1 FROM users WHERE uuid = ?)"),
    SELECT_USERS("SELECT uuid, nick, ip FROM users"),
    DELETE_USER("DELETE FROM users WHERE uuid = ?"),
    INSERT_IP("INSERT INTO ip_whitelist (ip) VALUES (?)"),
    DELETE_IP("DELETE FROM ip_whitelist WHERE ip = ?"),
    SELECT_IP("SELECT EXISTS(SELECT 1 FROM ip_whitelist WHERE ip = ?)")
}

data class UserRecord(
    val uuid: UUID,
    val nick: String,
    val ip: String,
)

object DbService
{
    private lateinit var conn: Connection

    fun init()
    {
        val db = Storage.getDbPath()

        conn = safeCall(GuardError.DB_INIT) {
            Class.forName("org.sqlite.JDBC")
            DriverManager.getConnection("jdbc:sqlite:$db")
        }.getOrThrow()

        conn.createStatement().use {
            it.execute("PRAGMA journal_mode = WAL;")
            it.execute("PRAGMA busy_timeout = 7000;")
        }

        runMigrations()
    }

    fun addUser(uuid: UUID, nick: String, ip: String)
    {
        conn.prepareStatement(Statement.INSERT_USER.sql).use {
            it.setString(
                1,
                uuid.toString()
            )
            it.setString(
                2,
                nick
            )
            it.setString(
                3,
                ip
            )

            it.executeUpdate()
        }
    }

    fun removeUser(uuid: String): Boolean
    {
        conn.prepareStatement(Statement.DELETE_USER.sql).use {
            it.setString(
                1,
                uuid
            )

            val affected = it.executeUpdate()

            return affected > 0
        }
    }

    fun getUser(uuid: UUID): Boolean
    {
        conn.prepareStatement(Statement.SELECT_USER.sql).use {
            it.setString(
                1,
                uuid.toString()
            )
            it.executeQuery().use { rs ->
                if (rs.next()) return rs.getBoolean(1)
            }
        }

        return false
    }

    fun getUsers(): List<UserRecord>
    {
        val users = mutableListOf<UserRecord>()

        conn.prepareStatement(Statement.SELECT_USERS.sql).use {
            it.executeQuery().use { rs ->
                while (rs.next())
                {
                    val uuid = rs.getString("uuid")
                    val nick = rs.getString("nick")
                    val ip = rs.getString("ip")

                    try
                    {
                        val record = UserRecord(
                            UUID.fromString(uuid),
                            nick,
                            ip
                        )
                        users.add(record)
                    } catch (_: Exception)
                    {
                        continue
                    }
                }
            }
        }

        return users
    }

    fun addWhitelist(ip: String)
    {
        conn.prepareStatement(Statement.INSERT_IP.sql).use {
            it.setString(
                1,
                ip
            )

            it.executeUpdate()
        }
    }

    fun removeWhitelist(ip: String)
    {
        conn.prepareStatement(Statement.DELETE_IP.sql).use {
            it.setString(
                1,
                ip
            )

            it.executeUpdate()
        }
    }

    fun getWhitelist(ip: String): Boolean
    {
        conn.prepareStatement(Statement.SELECT_IP.sql).use {
            it.setString(
                1,
                ip
            )
            it.executeQuery().use { rs ->
                if (rs.next()) return rs.getBoolean(1)
            }
        }

        return false
    }

    private fun runMigrations()
    {
        conn.createStatement().use {
            it.execute(
                """
                    CREATE TABLE IF NOT EXISTS users (
                uuid TEXT PRIMARY KEY,
                nick TEXT NOT NULL,
                ip TEXT NOT NULL
            );
                """.trimIndent()
            )

            it.execute(
                """
                    CREATE TABLE IF NOT EXISTS ip_whitelist (
                    ip TEXT PRIMARY KEY
                    );
                """.trimIndent()
            )
        }
    }
}