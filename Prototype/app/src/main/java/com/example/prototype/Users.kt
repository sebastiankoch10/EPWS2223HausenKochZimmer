import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream


@Serializable
data class User(
    val email: String,
    val password: String,
    val username: String,
    var notifications: MutableList<String>
)


fun main() {
    //only absolute filepath for the json file seems to work, no idea why
    val fileName = "C:\\Users\\Frederik\\IdeaProjects\\LoginPoC2\\src\\main\\kotlin\\Users.json"

    //reading the file and converting into JSON string
    val inputStream: InputStream = File(fileName).inputStream()
    val jsonString = inputStream.bufferedReader().use { it.readText() }

    //creating objects from JSON string
    val userlist: List<User> = Json.decodeFromString(jsonString)

    //Query
    println("Bitte Username eingeben")
    val username = readLine()
    println("Bitte Passwort eingeben")
    val passwort = readLine()

    //check if username+password are registered
    var loggedIn = false
    for (User in userlist) {
        if (username == User.username && passwort == User.password)
            loggedIn = true
    }

    //print login status
    println(loggedIn)
}