package com.escravosdev.api.entities.discord;

import java.util.List;
import java.util.Map;

public class DiscordProfileRoles {

    // Gênero
    private static final String HOMEM  = "1457044028003455121";
    private static final String MULHER = "1457044165472026848";

    // Religião — ordem de prioridade (índice 0 = maior prioridade)
    private static final List<String> RELIGION_PRIORITY = List.of(
            "1457031371804381247", // Católico
            "1457033311418454136", // Recém-Convertido
            "1457031784167374972", // Protestante
            "1457031659319988296", // Ortodoxo
            "1463638998235742351", // Sem Denominação
            "1457031879365759323", // Pagão
            "1457032315355267174", // Judeu
            "1457032302222639149", // Muçulmano
            "1457032320035848233"  // Ateu/Agnóstico
    );

    private static final Map<String, String> RELIGION_NAMES = Map.of(
            "1457031371804381247", "Católico",
            "1457033311418454136", "Recém-Convertido",
            "1457031784167374972", "Protestante",
            "1457031659319988296", "Ortodoxo",
            "1463638998235742351", "Sem Denominação",
            "1457031879365759323", "Pagão",
            "1457032315355267174", "Judeu",
            "1457032302222639149", "Muçulmano",
            "1457032320035848233", "Ateu/Agnóstico"
    );

    private DiscordProfileRoles() {}

    public static String extractGender(List<String> roles) {
        if (roles.contains(HOMEM))  return "Homem";
        if (roles.contains(MULHER)) return "Mulher";
        return null;
    }

    public static String extractReligion(List<String> roles) {
        return RELIGION_PRIORITY.stream()
                .filter(roles::contains)
                .map(RELIGION_NAMES::get)
                .findFirst()
                .orElse(null);
    }
}