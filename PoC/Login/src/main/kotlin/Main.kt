import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import java.io.*


@Serializable
data class user(
    val email: String,
    val password: String,
    val username: String
)


fun main() {
    //only absolute filepath for the json file seems to work, no idea why
    val fileName = "C:\\Users\\Frederik\\IdeaProjects\\LoginPoC2\\src\\main\\kotlin\\Users.json"

    //reading the file and converting into JSON string
    val inputStream: InputStream = File(fileName).inputStream()
    val jsonString = inputStream.bufferedReader().use { it.readText() }

    //creating objects from JSON string
    val userlist : List<user> = Json.decodeFromString(jsonString)

    //Query
    println("Bitte Username eingeben")
    val username = readLine()
    println("Bitte Passwort eingeben")
    val passwort = readLine()

    //check if username+password are registered
    var loggedIn = false
    for (user in userlist) {
        if (username==user.username && passwort==user.password)
        loggedIn = true
    }

    //print login status
    println(loggedIn)
}