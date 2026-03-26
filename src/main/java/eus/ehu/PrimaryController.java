package eus.ehu;

import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class PrimaryController {

    private static final String POKE_API_URL = "https://pokeapi.co/api/v2/pokemon/";
    private static final int MIN_POKEMON_ID = 1;
    private static final int MAX_POKEMON_ID = 1025;

    private int currentPokemonId = 25;

    @FXML
    private TextField idField;

    @FXML
    private TextField nameField;

    @FXML
    private TextArea typesArea;

    @FXML
    private TextField heightField;

    @FXML
    private ImageView pokemonImage;

    @FXML
    private Button previousButton;

    @FXML
    private Button nextButton;

    @FXML
    private void initialize() {
        loadPokemon(currentPokemonId);
    }

    @FXML
    private void handlePrevious() {
        if (currentPokemonId > MIN_POKEMON_ID) {
            loadPokemon(currentPokemonId - 1);
        }
    }

    @FXML
    private void handleNext() {
        if (currentPokemonId < MAX_POKEMON_ID) {
            loadPokemon(currentPokemonId + 1);
        }
    }

    private void loadPokemon(int pokemonId) {
        setLoadingState(true);

        Thread requestThread = new Thread(() -> {
            try {
                String responseBody = request(String.valueOf(pokemonId));
                Pokemon pokemon = new Gson().fromJson(responseBody, Pokemon.class);

                int id = pokemon != null ? pokemon.getId() : 0;
                String name = pokemon != null ? pokemon.getName() : "";
                int height = pokemon != null ? pokemon.getHeight() : 0;

                List<Pokemon.TypeSlot> typeSlots = pokemon != null ? pokemon.getTypes() : null;

                StringBuilder typesText = new StringBuilder();
                if (typeSlots != null) {
                    for (int i = 0; i < typeSlots.size(); i++) {
                        Pokemon.TypeSlot slot = typeSlots.get(i);
                        String typeName = slot != null && slot.getType() != null ? slot.getType().getName() : "";
                        typesText.append(capitalize(typeName));
                        if (i < typeSlots.size() - 1) {
                            typesText.append("\n");
                        }
                    }
                }

                final String spriteUrl = (pokemon != null
                        && pokemon.getSprites() != null
                        && pokemon.getSprites().getFrontDefault() != null)
                                ? pokemon.getSprites().getFrontDefault()
                                : "";

                javafx.application.Platform.runLater(() -> {
                    currentPokemonId = id;
                    idField.setText(String.valueOf(id));
                    nameField.setText(capitalize(name));
                    typesArea.setText(typesText.toString());
                    heightField.setText(height + " dm");
                    if (spriteUrl.isEmpty() || "null".equals(spriteUrl)) {
                        pokemonImage.setImage(null);
                    } else {
                        pokemonImage.setImage(new Image(spriteUrl, true));
                    }
                    setLoadingState(false);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                javafx.application.Platform.runLater(() -> setLoadingState(false));
            }
        });

        requestThread.setDaemon(true);
        requestThread.start();
    }

    public static String request(String id) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(POKE_API_URL + id)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new RuntimeException("Request failed with HTTP code " + response.code());
            }
            return response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setLoadingState(boolean loading) {
        previousButton.setDisable(loading || currentPokemonId <= MIN_POKEMON_ID);
        nextButton.setDisable(loading || currentPokemonId >= MAX_POKEMON_ID);
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }
}
