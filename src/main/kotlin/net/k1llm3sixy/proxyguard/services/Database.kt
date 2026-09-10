package net.k1llm3sixy.proxyguard.services

import net.k1llm3sixy.proxyguard.ProxyGuard.Companion.LOGGER
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
}

data class UserRecord(
    val uuid: UUID,
    val nick: String,
    val ip: String,
)

object Database
{
    private var conn: Connection? = null

    fun init()
    {
        val db = Storage.getDbPath()
        Class.forName("org.sqlite.JDBC")

        conn = try
        {
            DriverManager.getConnection("jdbc:sqlite:$db")
        } catch (e: Exception)
        {
            LOGGER.error(
                "Failed to connect to database: ${e.message}",
                e
            )
            null
        }

        runMigrations()
    }

    fun insertUser(uuid: UUID, nick: String, ip: String)
    {
        conn?.prepareStatement(Statement.INSERT_USER.sql)?.use {
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
        conn?.prepareStatement(Statement.DELETE_USER.sql)?.use {
            it.setString(1, uuid)

            val affected = it.executeUpdate()

            return affected > 0
        }

        return false
    }

    fun getUser(uuid: UUID): Boolean
    {
        conn?.prepareStatement(Statement.SELECT_USER.sql)?.use {
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

        conn?.prepareStatement(Statement.SELECT_USERS.sql)?.use {
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

    private fun runMigrations()
    {
        val sql = """
            CREATE TABLE IF NOT EXISTS users (
                uuid TEXT PRIMARY KEY,
                nick TEXT NOT NULL,
                ip TEXT NOT NULL
            );
        """.trimIndent()

        conn?.createStatement()?.use {
            it.execute(sql)
        }
    }
}