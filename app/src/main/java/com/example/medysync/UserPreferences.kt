import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Instancia

private val Context.dataStore by preferencesDataStore("user_preferences")

// Lee

class UserPreferences (private val context: Context) {
    companion object {
        private val NOMBRE_KEY = stringPreferencesKey("user_nombre")
        private val APELLIDO_KEY = stringPreferencesKey("user_apellido")
        private val ID_KEY = stringPreferencesKey("user_id")
    }

// Método

    suspend fun saveUserData(name: String, apellido: String, id: String) {
        context.dataStore.edit { preferences ->
            preferences[NOMBRE_KEY] = name
            preferences[APELLIDO_KEY] = apellido
            preferences[ID_KEY] = id
        }
    }

    // Leer, cambios

    val nombreUsuario: Flow<String> = context.dataStore.data.map { preferences -> preferences[NOMBRE_KEY] ?: "" }
    val apellidoUsuario: Flow<String> = context.dataStore.data.map { preferences -> preferences[APELLIDO_KEY] ?: "" }
    val idUsuario: Flow<String> = context.dataStore.data.map { preferences -> preferences[ID_KEY] ?: "" }

    suspend fun clearUserData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }

    }

}