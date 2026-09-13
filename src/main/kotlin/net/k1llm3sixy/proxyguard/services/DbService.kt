package net.k1llm3sixy.proxyguard.services

import net.k1llm3sixy.proxyguard.error.GuardError
import net.k1llm3sixy.proxyguard.error.safeCall
import net.k1llm3sixy.proxyguard.io.Storage
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

private enum class Statement(val sql: String)
{
    ADD_USER("INSERT INTO bad_users (uuid, nick, ip) VALUES (?, ?, ?)"),
    GET_USER("SELECT EXISTS(SELECT 1 FROM bad_users WHERE uuid = ?)"),
    GET_USERS("SELECT uuid, nick, ip FROM bad_users"),
    DELETE_USER("DELETE FROM bad_users WHERE uuid = ?"),

    ADD_WHITELIST("INSERT INTO ip_whitelist (ip) VALUES (?)"),
    DELETE_WHITELIST("DELETE FROM ip_whitelist WHERE ip = ?"),
    GET_WHITELIST("SELECT EXISTS(SELECT 1 FROM ip_whitelist WHERE ip = ?)"),
    GET_WHITELISTED("SELECT ip FROM ip_whitelist"),

    ADD_VALID_USER("INSERT INTO valid_users (uuid, nick, ip) VALUES (?, ?, ?)"),
    GET_VALID_USER("SELECT EXISTS(SELECT 1 FROM valid_users WHERE uuid = ?)"),
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
        conn.prepareStatement(Statement.ADD_USER.sql).use {
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
        conn.prepareStatement(Statement.GET_USER.sql).use {
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

        conn.prepareStatement(Statement.GET_USERS.sql).use {
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
        conn.prepareStatement(Statement.ADD_WHITELIST.sql).use {
            it.setString(
                1,
                ip
            )

            it.executeUpdate()
        }
    }

    fun removeWhitelist(ip: String): Boolean
    {
        conn.prepareStatement(Statement.DELETE_WHITELIST.sql).use {
            it.setString(
                1,
                ip
            )

            val affected = it.executeUpdate()

            return affected > 0
        }
    }

    fun getWhitelist(ip: String): Boolean
    {
        conn.prepareStatement(Statement.GET_WHITELIST.sql).use {
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

    fun getWhitelisted(): List<String>
    {
        val whitelisted = mutableListOf<String>()

        conn.prepareStatement(Statement.GET_WHITELISTED.sql).use {
            it.executeQuery().use { rs ->
                while (rs.next())
                {
                    val ip = rs.getString("ip")
                    whitelisted.add(ip)
                }
            }
        }

        return whitelisted
    }

    fun addValidUser(uuid: UUID, nick: String, ip: String)
    {
        conn.prepareStatement(Statement.ADD_VALID_USER.sql).use {
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

    fun getValidUser(uuid: UUID): Boolean
    {
        conn.prepareStatement(Statement.GET_VALID_USER.sql).use {
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


    private fun runMigrations()
    {
        conn.createStatement().use {
            it.execute(
                """
                    CREATE TABLE IF NOT EXISTS bad_users (
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

            it.execute(
                """
                    CREATE TABLE IF NOT EXISTS valid_users (
                    uuid TEXT PRIMARY KEY,
                    nick TEXT NOT NULL,
                    ip TEXT NOT NULL
                    );
                """.trimIndent()
            )
        }
    }
}