package eus.ehu;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Pokemon {
    private String name;
    private int id;
    private int height;
    private Sprites sprites;
    private List<TypeSlot> types;

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Sprites getSprites() {
        return sprites;
    }

    public void setSprites(Sprites sprites) {
        this.sprites = sprites;
    }

    public List<TypeSlot> getTypes() {
        return types;
    }

    public void setTypes(List<TypeSlot> types) {
        this.types = types;
    }

    //Nested Sprites class
    public static class Sprites {
        @SerializedName("front_default")
        private String frontDefault;

        public String getFrontDefault() {
            return frontDefault;
        }

        public void setFrontDefault(String frontDefault) {
            this.frontDefault = frontDefault;
        }
    }

    public static class TypeSlot {
        private TypeInfo type;

        public TypeInfo getType() {
            return type;
        }

        public void setType(TypeInfo type) {
            this.type = type;
        }
    }

    public static class TypeInfo {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}
