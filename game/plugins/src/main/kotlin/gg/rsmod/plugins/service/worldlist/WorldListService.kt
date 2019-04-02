package gg.rsmod.plugins.service.worldlist

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import gg.rsmod.game.Server
import gg.rsmod.game.model.World
import gg.rsmod.game.plugin.Plugin
import gg.rsmod.game.service.Service
import gg.rsmod.plugins.service.worldlist.io.WorldListChannelHandler
import gg.rsmod.plugins.service.worldlist.io.WorldListChannelInitializer
import gg.rsmod.plugins.service.worldlist.model.WorldEntry
import gg.rsmod.util.ServerProperties
import mu.KLogging
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * @author Triston Plummer ("Dread")
 *
 * Handles the initialisation of the network pipeline used for providing the client's world list. Perhaps in the future
 * if we decide to actually handle multiple worlds, this logic should be moved to a login service or some intermediate
 * service that can retrieve data from multiple worlds.
 */
class WorldListService : Service {

    /**
     * The port for this [WorldListService] to listen on
     */
    private var port : Int = 80

    /**
     * The interval between updating players
     */
    private var playerUpdateInterval : Int = 100

    /**
     * The [Path] to the configuration file holding the [WorldEntry] data.
     */
    private lateinit var path : Path

    /**
     * The list of [WorldEntry]s.
     */
    @Volatile private lateinit var list : List<WorldEntry>

    /**
     * Initialises the [WorldListService] by checking that the world list configuration file exists,
     * and attempts to parse it.
     *
     * @param server            The server instance
     * @param world             The game world instance
     * @param serviceProperties The properties for this server
     */
    override fun init(server: Server, world: World, serviceProperties: ServerProperties) {
        port = serviceProperties.getOrDefault("port", 80)
        path = Paths.get(serviceProperties.getOrDefault("config-path", "./data/cfg/worlds.json"))

        // If the world configuration file doesn't exist, spit out a warning and do nothing
        if (!Files.exists(path)) {
            logger.warn("World list configuration file does not exist: $path")
            return
        }

        parse()
    }

    /**
     * Parses the world list
     */
    private fun parse() {
        Files.newBufferedReader(path).use { reader ->
            list = Gson().fromJson<List<WorldEntry>>(reader, object : TypeToken<List<WorldEntry>>() {}.type)
        }
    }

    /**
     * Binds the network used for serving the world list
     *
     * @param server    The [Server] context
     * @param world     The [World] instance
     */
    override fun bindNet(server: Server, world: World) {

        // The inbound channel handler for the world list protocol
        val handler = WorldListChannelHandler(list)

        // Bind the world list network pipeline
        val bootstrap = server.bootstrap.clone()
        bootstrap.childHandler(WorldListChannelInitializer(handler))
        bootstrap.bind(port).syncUninterruptibly()
    }

    /**
     * Binds the plugin logic for incrementing and decrementing player count
     *
     * @param server    The [Server] instance
     * @param world     The [World] instance
     */
    override fun postLoad(server: Server, world: World) {
        val plugins = world.plugins
        plugins.bindLogin(incrementPlayerCount)
        plugins.bindLogout(decrementPlayerCount)
    }

    /**
     * Increments the player count for every entry in the world list
     */
    private val incrementPlayerCount : Plugin.() -> Unit = {
        list.forEach { it.players++ }
    }

    /**
     * Decrements the player count for every entry in the world list
     */
    private val decrementPlayerCount : Plugin.() -> Unit = {
        list.forEach { it.players-- }
    }

    companion object : KLogging()
}