package com.example.final_project.Service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class ProfileGradientService {

    private static final List<String[]> GRADIENTS = List.of(
            new String[]{"#ff5a00", "#ff0055"},
            new String[]{"#00c6ff", "#0072ff"},
            new String[]{"#f857a6", "#ff5858"},
            new String[]{"#11998e", "#38ef7d"},
            new String[]{"#fc466b", "#3f5efb"},
            new String[]{"#8360c3", "#2ebf91"},
            new String[]{"#f7971e", "#ffd200"},
            new String[]{"#00b09b", "#96c93d"},
            new String[]{"#667eea", "#764ba2"},
            new String[]{"#f093fb", "#f5576c"},
            new String[]{"#4facfe", "#00f2fe"},
            new String[]{"#43e97b", "#38f9d7"},
            new String[]{"#fa709a", "#fee140"},
            new String[]{"#30cfd0", "#330867"},
            new String[]{"#a8edea", "#fed6e3"}
    );

    private static final Random RANDOM = new Random();

    public String generateRandomGradient() {
        String[] colors = GRADIENTS.get(RANDOM.nextInt(GRADIENTS.size()));
        return colors[0] + "," + colors[1];
    }

    public String generateGradientFromSeed(String seed) {
        if (seed == null || seed.isEmpty()) {
            return generateRandomGradient();
        }
        int hash = 0;
        for (int i = 0; i < seed.length(); i++) {
            hash = ((hash << 5) - hash) + seed.charAt(i);
            hash = hash & hash;
        }
        int index = Math.abs(hash) % GRADIENTS.size();
        String[] colors = GRADIENTS.get(index);
        return colors[0] + "," + colors[1];
    }

    public String getDefaultGradient() {
        return "#ff5a00,#ff7c33";
    }

    public String toCssGradient(String stored) {
        if (stored == null || stored.isEmpty()) {
            return "linear-gradient(135deg, #ff5a00, #ff7c33)";
        }
        String[] parts = stored.split(",");
        if (parts.length != 2) {
            return "linear-gradient(135deg, #ff5a00, #ff7c33)";
        }
        return "linear-gradient(135deg, " + parts[0].trim() + ", " + parts[1].trim() + ")";
    }
}
