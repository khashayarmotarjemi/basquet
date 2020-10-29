package io.khashayar.domain.product

import com.wrapper.spotify.SpotifyApi
import com.wrapper.spotify.SpotifyHttpManager
import com.wrapper.spotify.model_objects.specification.Album
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import redis.clients.jedis.Jedis
import java.net.URI
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletionException


open class Event

class Start : Event()

class GetToken(val code: String) : Event()

class GetAlbums : Event()

open class State

class GotURL(val url: String) : State()

class GotCode(val code: String) : State()

class GotToken : State()
class GotAlbums : State()
class Failed : State()


class SpotifyBloc(private val productInteractor: ProductInteractor) {
//    enum class State {
//        STARTING, GETTING_CODE, GETTING_TOKEN, GETTING_ALBUMS, FAILED, GOT_EM
//    }


    fun initiate() {
        GlobalScope.launch { mapEventToState() }
    }

    private val stateOutput = Channel<State>()
    private val eventInput = Channel<Event>()


    /*private suspend fun currentState(): State {
        return stateOutput.receive()
    }*/

    private suspend fun sendToOutput(state: State) {
        stateOutput.send(state)
    }

    fun states(): Channel<State> {
        return stateOutput
    }

    suspend fun dispatch(event: Event) {
        GlobalScope.launch { eventInput.send(event) }
    }


    private suspend fun mapEventToState() {
        for (event in eventInput) {
            when (event) {
                is Start -> {
                    val uri = authorizationCodeUriSync()
                    sendToOutput(GotURL(uri.toString()))
                }

                is GetToken -> {
                    val res = authorizationCodeAsync(event.code)
                    if (res) {
                        sendToOutput(GotToken())
                        dispatch(GetAlbums())
                    } else sendToOutput(Failed())
                }


                is GetAlbums -> {
                    val albums = checkUsersSavedAlbumsAsync()
                    if (albums.isNotEmpty()) {
                        albums.map { album ->
                            productInteractor.add(
                                album.name,
                                album.artists[0].name,
                                album.releaseDate,
                                20,
                                album.genres.joinToString { it },
                                imageId = album.images[0].url
                            )
                        }
                        sendToOutput(GotAlbums())
                    }
                }

                else -> {
                    print("\n\nweird event\n\n")
                }
            }
        }
    }


    // values
    private val redirectUri: URI = SpotifyHttpManager.makeUri("http://localhost:8080/spotify-redirect")

    private var spotifyApi = SpotifyApi.Builder()
        .setClientId("d50a96dd84e94d2db6562af8941230d0")
        .setClientSecret("7fb7d75eeb3c4b21894531365be6fca7")
        .setRedirectUri(redirectUri)
        .build()

    private val authorizationCodeUriRequest =
        spotifyApi.authorizationCodeUri() //          .state("x4xkmn9pu3j6ukrs8n")
            .scope("user-library-read")
            .build()


    // functions
    private fun authorizationCodeUriSync(): URI {
        return authorizationCodeUriRequest.execute();
    }

    private suspend fun authorizationCodeAsync(code: String): Boolean {
        val authorizationCodeRequest = spotifyApi.authorizationCode(code)
            .build()
        try {
            val authorizationCodeCredentialsFuture = authorizationCodeRequest.executeAsync()

            // Thread free to do other tasks...

            // Example Only. Never block in production code.
            val authorizationCodeCredentials = authorizationCodeCredentialsFuture.join()

            // Set access and refresh token for further "spotifyApi" object usage
            spotifyApi.accessToken = authorizationCodeCredentials.accessToken
            spotifyApi.refreshToken = authorizationCodeCredentials.refreshToken
            println("Expires in: " + authorizationCodeCredentials.expiresIn)
            return true
        } catch (e: CompletionException) {
            println("Error: " + e.cause!!.message)
            return false
        } catch (e: CancellationException) {
            println("Async operation cancelled.")
            return false
        }
    }


    private fun checkUsersSavedAlbumsAsync(): ArrayList<Album> {
        val checkUsersSavedAlbumsRequest = spotifyApi.currentUsersSavedAlbums
            .build()
        try {
            val resultsFuture = checkUsersSavedAlbumsRequest.executeAsync()
            val results = resultsFuture.join()

            return ArrayList(results.items.map { item -> item.album })
            /*  val album = item.album

             *//* productInteractor.add(
                    album.name,
                    20,
                    album.artists.joinToString { it.toString() },
                    imageId = album.images[0].url
                )*//*
            }*/

        } catch (e: CompletionException) {
            println("Error: " + e.cause!!.message)
        } catch (e: CancellationException) {
            println("Async operation cancelled.")
        }

        return ArrayList()
    }
}

//class ExtractionInteractor(private val productInteractor: ProductInteractor) {
//
//
//    fun populateDB() {
//        Jedis().flushAll()
//
//        productInteractor.add(
//            "Tweez",
//            12,
//            imageId = "BRwqzx3GY",
//            description = "Includes FREE MP3 version of this album. "
//        )
//        productInteractor.add(
//            "London Calling",
//            17,
//            imageId = "qd9oT7EGe",
//            description = "Best Sellers Rank: 341 in CDs & Vinyl (See Top 100 in CDs & Vinyl)"
//        )
//        productInteractor.add(
//            "The Man Machine",
//            24,
//            imageId = "p30wgwqiU",
//            description = "The seventh studio album by the German electronic band. Includes the singles 'Neon Lights', 'The Robots' and 'The Model'"
//        )
//        productInteractor.add("Cunning Stunts by Cows", 33, imageId = "bYHIZeeTJ", description = "")
//        productInteractor.add("Entertainment", 15, imageId = "U79grxOUn", description = "")
//        productInteractor.add("Bookends", 14, imageId = "s8Qu0k2nI", description = "")
//        productInteractor.add("Bleach", 24, imageId = "Zil1YENhm", description = "")
//        productInteractor.add("Physical Graffiti", 27, imageId = "2brObXy9k", description = "")
//        productInteractor.add("13 Songs", 14, imageId = "HR9jN5Qxd", description = "")
//        productInteractor.add("Computer World", 14, imageId = "wWZDwRbXt", description = "")
//        productInteractor.add("Californication", 14, imageId = "cpyVcoBiB", description = "")
//    }
//
//
//    /* fun populateFromSpotify() {
//         try {
//             authorizationCodeUri_Sync()
//         } catch (e: Exception) {
//             print(e)
//             print(e.stackTrace)
//         }
//     }*/
//
//
//    /*suspend fun authorizationCodeUri_Async() {
//        try {
//            val uriFuture = authorizationCodeUriRequest.executeAsync()
//
//            val uri = uriFuture.join()
//            println("URI: $uri")
//        } catch (e: CompletionException) {
//            println("Error: " + e.cause!!.message)
//        } catch (e: CancellationException) {
//            println("Async operation cancelled.")
//        }
//    }*/
//
//    //////
//
////    private
//
//    /* fun authorizationCode_Sync() {
//
//         try {
//             val authorizationCodeCredentials = authorizationCodeRequest.execute()
//
//             // Set access and refresh token for further "spotifyApi" object usage
//             spotifyApi.accessToken = authorizationCodeCredentials.accessToken
//             spotifyApi.refreshToken = authorizationCodeCredentials.refreshToken
//             println("Expires in: " + authorizationCodeCredentials.expiresIn)
//         } catch (e: IOException) {
//             println("Error: " + e.message)
//         } catch (e: SpotifyWebApiException) {
//             println("Error: " + e.message)
//         } catch (e: ParseException) {
//             println("Error: " + e.message)
//         }
//     }*/
//
//    /*
//
//       */
//
//    /*suspend fun checkUsersSavedAlbums_Sync() {
//        try {
//            val booleans = checkUsersSavedAlbumsRequest.execute()
//            println("Length: " + booleans.size)
//        } catch (e: IOException) {
//            println("Error: " + e.message)
//        } catch (e: SpotifyWebApiException) {
//            println("Error: " + e.message)
//        } catch (e: ParseException) {
//            println("Error: " + e.message)
//        }
//    }*/
//
//
//    /* @JvmStatic
//     fun main(args: Array<String>) {
//         authorizationCode_Sync()
//         authorizationCode_Async()
//     }*/
//}