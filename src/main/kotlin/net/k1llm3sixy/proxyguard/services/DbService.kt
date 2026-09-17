package net.k1llm3sixy.proxyguard.services

import net.k1llm3sixy.proxyguard.error.GuardError
import net.k1llm3sixy.proxyguard.error.safeCall
import net.k1llm3sixy.proxyguard.io.Storage
import net.k1llm3sixy.proxyguard.provider.Detection
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

private enum class Statement(val sql: String)
{
    ADD_USER("INSERT OR REPLACE INTO bad_users (uuid, nick, ip, detection) VALUES (?, ?, ?, ?)"),
    GET_USER("SELECT detection FROM bad_users WHERE uuid = ?"),
    GET_USERS("SELECT uuid, nick, ip, detection, banned_at FROM bad_users"),
    GET_USERS_UUID("SELECT uuid FROM bad_users"),
    GET_USERS_NICK("SELECT nick FROM bad_users"),
    GET_USERS_IP("SELECT ip FROM bad_users"),
    DELETE_USER_BY_UUID("DELETE FROM bad_users WHERE uuid = ?"),
    DELETE_USER_BY_NICK("DELETE FROM bad_users WHERE nick = ?"),
    DELETE_USER_BY_IP("DELETE FROM bad_users WHERE ip = ?"),

    ADD_WHITELIST("INSERT OR IGNORE INTO ip_whitelist (ip) VALUES (?)"),
    DELETE_WHITELIST("DELETE FROM ip_whitelist WHERE ip = ?"),
    GET_WHITELIST("SELECT EXISTS(SELECT 1 FROM ip_whitelist WHERE ip = ?)"),
    GET_WHITELISTED("SELECT ip FROM ip_whitelist"),

    ADD_VALID_USER("INSERT OR REPLACE INTO valid_users (uuid, nick, ip) VALUES (?, ?, ?)"),
    GET_VALID_USER("SELECT EXISTS(SELECT 1 FROM valid_users WHERE uuid = ?)"),

    GET_STATS(
        """
    SELECT 
        (SELECT COUNT(*) FROM bad_users WHERE detection = 'VPN') AS count_vpn,
        (SELECT COUNT(*) FROM bad_users WHERE detection = 'PROXY') AS count_proxy,
        (SELECT COUNT(*) FROM ip_whitelist) AS count_whitelisted,
        (SELECT COUNT(*) FROM valid_users) AS count_validated
"""
    )
}

enum class Type
{
    UUID,
    NICK,
    IP
}

data class UserRecord(
    val uuid: String,
    val nick: String,
    val ip: String,
    val detection: String,
    val time: String,
)

data class StatsRecord(
    val vpn: Int = 0,
    val proxy: Int = 0,
    val whitelisted: Int = 0,
    val validated: Int = 0,
)

object DbService
{
    private lateinit var conn: Connection

    fun init()
    {
        conn = safeCall(GuardError.DB_INIT) {
            Class.forName("org.sqlite.JDBC")
            DriverManager.getConnection("jdbc:sqlite:${Storage.DB_PATH}")
        }.getOrThrow()

        conn.createStatement().use {
            it.execute("PRAGMA journal_mode = WAL;")
            it.execute("PRAGMA busy_timeout = 7000;")
        }

        runMigrations()
    }

    fun addUser(uuid: UUID, nick: String, ip: String, detection: Detection)
    {
        safeCall(GuardError.DB_QUERY) {
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
                it.setString(
                    4,
                    detection.name
                )

                it.executeUpdate()
            }
        }
    }

    fun removeUser(type: Type, data: String) = safeCall(GuardError.DB_QUERY) {
        val stmt = when (type)
        {
            Type.UUID -> Statement.DELETE_USER_BY_UUID
            Type.NICK -> Statement.DELETE_USER_BY_NICK
            Type.IP   -> Statement.DELETE_USER_BY_IP
        }

        remove(
            stmt,
            data
        )
    }.getOrDefault(false)

    private fun remove(stmt: Statement, data: String): Boolean
    {
        conn.prepareStatement(stmt.sql).use {
            it.setString(
                1,
                data
            )
            val affected = it.executeUpdate()

            return affected > 0
        }
    }

    fun getUserDetection(uuid: UUID) = safeCall(GuardError.DB_QUERY) {
        conn.prepareStatement(Statement.GET_USER.sql).use {
            it.setString(
                1,
                uuid.toString()
            )
            it.executeQuery().use { rs ->
                if (rs.next())
                {
                    Detection.valueOf(rs.getString(1))
                }
                else Detection.CLEAN
            }
        }
    }.getOrDefault(Detection.CLEAN)

    fun getUsers() = safeCall(GuardError.DB_QUERY) {
        val users = mutableListOf<UserRecord>()

        conn.prepareStatement(Statement.GET_USERS.sql).use {
            it.executeQuery().use { rs ->
                while (rs.next())
                {
                    val uuid = rs.getString("uuid")
                    val nick = rs.getString("nick")
                    val ip = rs.getString("ip")
                    val detection = rs.getString("detection")
                    val time = rs.getString("banned_at")

                    val record = UserRecord(
                        uuid,
                        nick,
                        ip,
                        detection,
                        time
                    )
                    users.add(record)
                }
            }
        }

        users
    }.getOrDefault(emptyList())

    fun getUsersData(type: Type) = safeCall(GuardError.DB_QUERY) {
        val stmt = when (type)
        {
            Type.IP   -> Statement.GET_USERS_IP
            Type.NICK -> Statement.GET_USERS_NICK
            Type.UUID -> Statement.GET_USERS_UUID
        }
        val data = mutableListOf<String>()

        conn.prepareStatement(stmt.sql).use {
            it.executeQuery().use { rs ->
                while (rs.next())
                {
                    data.add(rs.getString(1))
                }
            }
        }

        data
    }.getOrDefault(emptyList())

    fun addWhitelist(ip: String)
    {
        safeCall(GuardError.DB_QUERY) {
            conn.prepareStatement(Statement.ADD_WHITELIST.sql).use {
                it.setString(
                    1,
                    ip
                )

                it.executeUpdate()
            }
        }
    }

    fun removeWhitelist(ip: String) = safeCall(GuardError.DB_QUERY) {
        conn.prepareStatement(Statement.DELETE_WHITELIST.sql).use {
            it.setString(
                1,
                ip
            )

            it.executeUpdate() > 0
        }
    }.getOrDefault(false)

    fun getWhitelist(ip: String) = safeCall(GuardError.DB_QUERY) {
        conn.prepareStatement(Statement.GET_WHITELIST.sql).use {
            it.setString(
                1,
                ip
            )
            it.executeQuery().use { rs ->
                rs.next() && rs.getBoolean(1)
            }
        }
    }.getOrDefault(false)

    fun getWhitelisted() = safeCall(GuardError.DB_QUERY) {
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

        whitelisted
    }.getOrDefault(emptyList())

    fun addValidUser(uuid: UUID, nick: String, ip: String)
    {
        safeCall(GuardError.DB_QUERY) {
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
    }

    fun getValidUser(uuid: UUID) = safeCall(GuardError.DB_QUERY) {
        conn.prepareStatement(Statement.GET_VALID_USER.sql).use {
            it.setString(
                1,
                uuid.toString()
            )
            it.executeQuery().use { rs ->
                rs.next() && rs.getBoolean(1)
            }
        }
    }.getOrDefault(false)

    fun getStats() = safeCall(GuardError.DB_QUERY) {
        conn.prepareStatement(Statement.GET_STATS.sql).use { stmt ->
            stmt.executeQuery().use { rs ->
                if (rs.next())
                {
                    StatsRecord(
                        rs.getInt("count_vpn"),
                        rs.getInt("count_proxy"),
                        rs.getInt("count_whitelisted"),
                        rs.getInt("count_validated")
                    )
                }
                else StatsRecord()
            }
        }
    }.getOrDefault(StatsRecord())

    private fun runMigrations()
    {
        safeCall(GuardError.DB_QUERY) {
            conn.createStatement().use {
                it.execute(
                    """
                    CREATE TABLE IF NOT EXISTS bad_users (
                uuid TEXT PRIMARY KEY,
                nick TEXT NOT NULL,
                ip TEXT NOT NULL,
                detection TEXT NOT NULL,
                banned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
                """.trimIndent()
                )

                it.execute(
                    """
                    CREATE TABLE IF NOT EXISTS ip_whitelist (
                    ip TEXT PRIMARY KEY,
                    whitelisted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    );
                """.trimIndent()
                )

                it.execute(
                    """
                    CREATE TABLE IF NOT EXISTS valid_users (
                    uuid TEXT PRIMARY KEY,
                    nick TEXT NOT NULL,
                    ip TEXT NOT NULL,
                    validated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    );
                """.trimIndent()
                )
            }
        }.getOrDefault(Unit)
    }

    fun close()
    {
        safeCall(GuardError.DB_QUERY) {
            if (::conn.isInitialized && !conn.isClosed)
            {
                conn.close()
            }
        }
    }
}